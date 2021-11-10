package com.noap.msfrw.etcd.util;

import java.time.Duration;
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
import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import io.etcd.jetcd.watch.WatchResponse;

/**
 * Etcd Connector Utility Class.
 * 
 * @author UMUT
 *
 */
public class EtcdConnector {

  Logger logger = LoggerFactory.getLogger(EtcdConnector.class);

  private String[] etcdUrls;
  private Client etcdClient;
  private boolean isListening = false;

  public EtcdConnector(String... etcdUrls) {
    this.etcdUrls = etcdUrls;
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
  public Map<String, String> getAllKeyValues() {

    checkConnection();

    Map<String, String> keyValueMap = new ConcurrentHashMap<>();
    ByteSequence keyAsStartStr = ByteSequence.from("\0".getBytes());
    GetOption option = GetOption.newBuilder().withSortField(GetOption.SortTarget.KEY)
        .withSortOrder(GetOption.SortOrder.DESCEND).withRange(keyAsStartStr).build();
    CompletableFuture<GetResponse> futureResponse =
        etcdClient.getKVClient().get(keyAsStartStr, option);

    GetResponse response = null;
    try {
      response = futureResponse.get();
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
        logger.info(String.format("Etcd cluster at: %s contains no key value data yet...",
            String.join(",", etcdUrls)));
      }
      return keyValueMap;
    }

    for (KeyValue kv : response.getKvs()) {
      keyValueMap.put(kv.getKey().toString(), kv.getValue().toString());
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

    System.out.println("Umut1");
    try {
      es.execute(() -> {

        CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);
        ByteSequence keyString = ByteSequence.from("\0".getBytes());
        Watcher watcher = null;
        try {
          WatchOption option = WatchOption.newBuilder().withRange(keyString).build();
          watcher = etcdClient.getWatchClient().watch(keyString, option,
              new PropertyChangedConsumer(repository, latch));
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
    System.out.println("Umut2");
  }

  private class PropertyChangedConsumer implements Consumer<WatchResponse> {

    private EtcdEnvironmentRepository repository;
    private CountDownLatch latch;

    public PropertyChangedConsumer(EtcdEnvironmentRepository repository, CountDownLatch latch) {
      this.repository = repository;
      this.latch = latch;
    }

    @Override
    public void accept(WatchResponse response) {
      runCallBackForWatchEvent(latch, response, repository);

    }

  }

  private void runCallBackForWatchEvent(CountDownLatch latch, WatchResponse response,
      EtcdEnvironmentRepository repository) {

    logger.info("******************* CALLBACK CALLED: *************************************");
    logger.info("");
    for (WatchEvent event : response.getEvents()) {
      logger.info("----------------------------------");
      logger.info("Event type: {}", event.getEventType());
      logger.info("Watching for key: {}", event.getKeyValue().getKey());
      logger.info("Value: {}", event.getKeyValue().getValue());
      logger.info("Latch values: {}", latch.getCount());

      // if not delete event, call the monitor to trigger event to bus
      if (EventType.DELETE.equals(event.getEventType())
          || EventType.UNRECOGNIZED.equals(event.getEventType())) {
        logger.error(
            "Unexpected Operation When Spring Cloud Config Server is RUNNING: Operation Type: "
                + event.getEventType() + ", Key: " + event.getKeyValue().getKey());
        throw new EtcdException(
            "Restart might be needed for the client Microservices, since a delete or an unrecognized property operation is done on the ETCD cluster");
      } else if (EventType.PUT.equals(event.getEventType())) {
        repository.publishEventByPath("sample");
      }
    }
    latch.countDown();
  }

  private void checkConnection() {
    if (etcdClient == null) {
      throw new EtcdException(
          "Connection to the ETCD cluster is null, call connect() method first.");
    }
  }
}
