package com.noap.msfrw.etcd.util;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;
import com.noap.msfrw.etcd.util.watch.lock.EtcdWatchLock;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.GetOption.Builder;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import io.etcd.jetcd.watch.WatchResponse;

/**
 * ETCD Connector Utility Class.
 * 
 * @author UMUT
 *
 */
public class EtcdConnector {

  private static final Logger logger = LoggerFactory.getLogger(EtcdConnector.class);

  private String[] etcdUrls;
  private Client etcdClient;
  private boolean isListening = false;
  private EtcdWatchLock etcdWatchLock;
  private Boolean watchLockEnabled;
  private EtcdConfigurationProperties etcdConfigurationProperties;
 
  public Client getEtcdClient() {
	return etcdClient;
  }

  public void setEtcdClient(Client etcdClient) {
	this.etcdClient = etcdClient;
  }

  public EtcdConnector(@Nullable EtcdWatchLock etcdWatchLock,
      EtcdConfigurationProperties etcdConfigurationProperties, Boolean watchLockEnabled) {
    this.etcdUrls = etcdConfigurationProperties.getUrlsWithHttpPrefix().toArray(String[]::new);
    this.etcdConfigurationProperties = etcdConfigurationProperties;
    this.etcdWatchLock = etcdWatchLock;
    this.watchLockEnabled = watchLockEnabled;
  }

  /**
   * Prepares a connection to an ETCD cluster.
   *
   * @param username user name for ETCD connection
   * @param pssword password for ETCD connection
   * @param keepAliveTimeInSeconds if null default is used : 30 seconds
   * @param keepAliveTimeoutInSeconds if null default is used: 10 seconds
   * @return true if connection builder is built.
   */
  public boolean connect(String username, String pssword, Long keepAliveTimeInSeconds,
      Long keepAliveTimeoutInSeconds) {
    ClientBuilder builder = Client.builder();
    if (StringUtils.isNotEmpty(username)) {
      ByteSequence userNameByteSeq = ByteSequence.from(username.getBytes());
      builder = builder.user(userNameByteSeq);
    }
    if (StringUtils.isNotEmpty(pssword)) {
      ByteSequence psswordByteSeq = ByteSequence.from(pssword.getBytes());
      builder = builder.password(psswordByteSeq);
    }
    if (keepAliveTimeInSeconds != null) {
      builder = builder.keepaliveTime(Duration.ofSeconds(keepAliveTimeInSeconds));
    }
    if (keepAliveTimeoutInSeconds != null) {
      builder = builder.keepaliveTimeout(Duration.ofSeconds(keepAliveTimeoutInSeconds));
    }
    etcdClient = builder.endpoints(etcdUrls).build();
    return true;
  }

  /**
   * Returns all key values stored in the ETCD cluster connected.
   *
   * @return map of key value pairs stored.
   */
  public Map<String, String> getAllKeyValues(String application, String profile, String label) {

    checkConnection();

    Map<String, String> keyValueMap = new ConcurrentHashMap<>();
    String searchKeyPrefix =
        createSearchPrefixFromApplicationParameters(application, profile, label);
    String replaceablePrefix = searchKeyPrefix;
    CompletableFuture<GetResponse> futureSearchResponse;
    Builder etcdGetBuilder = GetOption.newBuilder().withSortField(GetOption.SortTarget.KEY)
        .withSortOrder(GetOption.SortOrder.DESCEND);

    if (StringUtils.isNoneBlank(searchKeyPrefix)) {
      GetOption option = etcdGetBuilder.isPrefix(true).build();
      futureSearchResponse =
          etcdClient.getKVClient().get(ByteSequence.from(searchKeyPrefix.getBytes()), option);
    } else {
      searchKeyPrefix = "\0";
      ByteSequence searchKeyPrefixAsBytes = ByteSequence.from(searchKeyPrefix.getBytes());
      GetOption option = etcdGetBuilder.withRange(searchKeyPrefixAsBytes).build();
      futureSearchResponse = etcdClient.getKVClient().get(searchKeyPrefixAsBytes, option);
    }

    GetResponse response = null;
    try {
      response = futureSearchResponse.get();
    } catch (InterruptedException ie) {
      logger.warn("An Interruption : ", ie);
      Thread.currentThread().interrupt();
    } catch (ExecutionException ee) {
      String errorMessage = String.format(
          "An exception occurred while retrieving all the key value pairs from the etcd cluster: %s",
          String.join(",", etcdUrls));
      throw new EtcdException(errorMessage, ee);
    }

    if (response == null || response.getKvs().isEmpty()) {
      if (logger.isInfoEnabled()) {
        logger.info(
            "Etcd cluster at: {} contains no key (starting with: '{}') and value data yet...",
            String.join(",", etcdUrls), searchKeyPrefix);
      }
      return keyValueMap;
    }

    for (KeyValue kv : response.getKvs()) {
      // replace key with previously calculated prefix
      String keyToAdd = kv.getKey().toString().replaceFirst(replaceablePrefix, "");
      keyValueMap.put(keyToAdd, kv.getValue().toString());
    }
    return keyValueMap;
  }

  /**
   * Returns desired key and value stored in the ETCD cluster connected, null if no key is found.
   *
   * @return map of key value pairs stored.
   */
  public String getValue(String keyString) {

    checkConnection();

    ByteSequence key = ByteSequence.from(keyString.getBytes());
    GetOption option = GetOption.newBuilder().withRange(key).build();
    CompletableFuture<GetResponse> futureResponse = etcdClient.getKVClient().get(key, option);

    GetResponse response = null;
    try {
      response = futureResponse.get();
    } catch (InterruptedException ie) {
      logger.warn("An Interruption: ", ie);
      Thread.currentThread().interrupt();
    } catch (ExecutionException ee) {
      String errorMessage = String.format(
          "An exception occurred while retrieving key value pair with key: %s from the etcd cluster: %s",
          keyString, String.join(",", etcdUrls));
      throw new EtcdException(errorMessage, ee);
    }

    if (response == null || response.getKvs().isEmpty()) {
      return null;
    }
    return response.getKvs().get(0).getValue().toString();
  }

  /**
   * A watcher initialization for all keys in the ETCD cluster.
   */
  public synchronized void startListening(EtcdEnvironmentRepository repository) {

    if (isListening) {
      logger.info("Watcher is already listening the etcd instance");
      return;
    }
    checkConnection();
    ExecutorService es = Executors.newSingleThreadExecutor();

    try {
      es.execute(() -> {

        CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);
        ByteSequence keyString = ByteSequence.from("\0".getBytes());
        Watcher watcher = null;
        try {
          WatchOption option = WatchOption.newBuilder().withRange(keyString).build();
          watcher =
              etcdClient.getWatchClient().watch(keyString, option, new PropertyChangedConsumer(
                  repository, latch, etcdConfigurationProperties.getKeyPrefixOrder()));
          latch.await();
        } catch (InterruptedException ie) {
          logger.warn("An Interruption: ", ie);
          Thread.currentThread().interrupt();
        } catch (Exception e) {
          String errorMessage =
              String.format("An exception occurred while watching ETCD @: %s, detail is: %s",
                  String.join(",", etcdUrls), ExceptionUtils.getStackTrace(e));
          logger.error(errorMessage);
        } finally {
          if (watcher != null) {
            watcher.close();
          }
        }
      });
    } finally {
      isListening = true;
      es.shutdown();
    }
  }

  //An inner class that holds a key value change information in itself and has a callback trigger functionality after a change in key value store occurs. 
  private class PropertyChangedConsumer implements Consumer<WatchResponse> {

    private EtcdEnvironmentRepository repository;
    private CountDownLatch latch;
    private List<KeyPrefix> keyPrefixOrder;

    public PropertyChangedConsumer(EtcdEnvironmentRepository repository, CountDownLatch latch,
        List<KeyPrefix> keyPrefixOrder) {
      this.keyPrefixOrder = keyPrefixOrder;
      this.repository = repository;
      this.latch = latch;
    }

    @Override
    public void accept(WatchResponse response) {
      runCallBackForWatchEvent(keyPrefixOrder, latch, response, repository);
    }
  }

  //Callback trigger after a property change in ETCD occurs.
  private void runCallBackForWatchEvent(List<KeyPrefix> keyPrefixOrder, CountDownLatch latch,
      WatchResponse response, EtcdEnvironmentRepository repository) {

    logger.info("******************* CALLBACK CALLED: *************************************");
    for (WatchEvent event : response.getEvents()) {
      logger.info("Event type: {}", event.getEventType());
      logger.info("Watching for key: {}", event.getKeyValue().getKey());
      logger.info("Value altered: {}", event.getKeyValue().getValue());
      logger.info("Current latch value: {}", latch.getCount());

      // if not delete event, call the monitor to trigger event to bus
      if (EventType.DELETE.equals(event.getEventType())
          || EventType.UNRECOGNIZED.equals(event.getEventType())) {
        logger.error(
            "Unexpected (or Deletion) Operation When Spring Cloud Config Server is RUNNING: Operation Type: {}, Key: {}. Restart might be needed for the client Microservices, since a delete or an unrecognized property operation is done on the ETCD cluster",
            event.getEventType(), event.getKeyValue().getKey());
      } else if (EventType.PUT.equals(event.getEventType())) {
        String keyModified = event.getKeyValue().getKey().toString();
        String applicationName = extractApplicationName(keyModified, keyPrefixOrder);
        String lockString = keyModified + ":" + event.getKeyValue().getValue().toString();
        if (!Boolean.TRUE.equals(watchLockEnabled)) {
          repository.publishEventByPath(applicationName);
        } else {// distributed lock enabled
          etcdWatchLock.processWithLock(lockString, () -> {
            repository.publishEventByPath(applicationName);
          });
        }
      }
    }
    latch.countDown();
  }

  //Extracts the application name from the key name by using the key order list that is configured in application yaml/properties in this project.
  private String extractApplicationName(String modifiedKey, List<KeyPrefix> keyPrefixOrder) {

    int applicationIndexInKeyPrefix = keyPrefixOrder.indexOf(KeyPrefix.APPLICATION);
    if (applicationIndexInKeyPrefix == -1) { // No application name should exist in keys
      return "*";
    }
    String[] tokens = modifiedKey.split("\\.");
    if (tokens.length > applicationIndexInKeyPrefix + 1) { // +1 reserved for actual key itself
      return tokens[applicationIndexInKeyPrefix];
    }
    return "*";
  }

  //Checks whether an ETCD client is configured with necessary connection properties or not.
  private void checkConnection() {
    if (etcdClient == null) {
      throw new EtcdException(
          "Connection to the ETCD cluster is null, call connect() method first.");
    }
  }

  //Appends application, profile and label in given key order (as the order in application yaml) to group and detect all the key value set that belongs to an application (client) with its environment (e.g. dev, prod etc...)
  private String createSearchPrefixFromApplicationParameters(String application, String profile,
      String label) {
    List<KeyPrefix> keyPrefixOrder = etcdConfigurationProperties.getKeyPrefixOrder();
    StringBuilder keyPrefixFormation = new StringBuilder("");
    if (CollectionUtils.isEmpty(keyPrefixOrder)) {
      return keyPrefixFormation.toString();
    }
    for (KeyPrefix currentPrefix : keyPrefixOrder) {
      appendWithParameter(keyPrefixFormation, currentPrefix, application, profile, label);
    }
    return keyPrefixFormation.toString();

  }

  private void appendWithParameter(StringBuilder origin, KeyPrefix currentPrefix,
      String application, String profile, String label) {
    switch (currentPrefix) {
      case APPLICATION:
        origin.append(StringUtils.isNotBlank(application) ? (application + ".") : "");
        break;
      case LABEL:
        origin.append(StringUtils.isNotBlank(label) ? (label + ".") : "");
        break;
      case PROFILE:
        origin.append(StringUtils.isNotBlank(profile) ? (profile + ".") : "");
        break;
      default:
        throw new EtcdException(
            "Key Prefix Order retrieved from application.yaml contains wrong key, only application, label and profile words are allowed.");
    }
  }
}
