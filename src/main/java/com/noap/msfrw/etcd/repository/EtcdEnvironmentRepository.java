package com.noap.msfrw.etcd.repository;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.Arrays;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.StringUtils;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;

//@Component
public class EtcdEnvironmentRepository implements EnvironmentRepository/*, Ordered*/, ApplicationEventPublisherAware {

	private static Log log = LogFactory.getLog(EtcdEnvironmentRepository.class);

	//@Value("${spring.cloud.config.server.etcdrepo.order:0}")
	//private int order = Ordered.LOWEST_PRECEDENCE;

	private String busId;

	//private final PropertyPathNotificationExtractor extractor;
	private ApplicationEventPublisher applicationEventPublisher;

	public EtcdEnvironmentRepository(/*PropertyPathNotificationExtractor extractor, */String busId) {
		//this.extractor = extractor;
		this.busId = busId;
	}

	@Override
	public Environment findOne(String application, String profile, String label) {
				

		
		Environment environment = new Environment(application, profile);

		Map<String, String> properties = new HashMap();
		try {
			properties = createEtcdMap();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		environment.add(new PropertySource("mapPropertySource", properties));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				addWatch();
				
			}
		}).start();
		
		return environment;
	}

//	public void setOrder(int order) {
//		this.order = order;
//	}
//
//	@Override
//	public int getOrder() {
//		return order;
//	}

	private Map<String, String> createEtcdMap() throws InterruptedException, ExecutionException {
		final Map<String, String> properties = new HashMap<String, String>();

		// Client client = Client.builder().endpoints("http://127.0.0.1:2379").build();
		Client client = Client.builder().endpoints("http://192.168.1.100:2379").build();

		ByteSequence key = ByteSequence.from("\0".getBytes());

		GetOption option = GetOption.newBuilder().withSortField(GetOption.SortTarget.KEY)
				.withSortOrder(GetOption.SortOrder.DESCEND).withRange(key).build();

		CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(key, option);

		GetResponse response = futureResponse.get();

		if (response.getKvs().isEmpty()) {
			return null;
		}

		Map<String, String> keyValueMap = new HashMap<>();
		for (KeyValue kv : response.getKvs()) {
			keyValueMap.put(kv.getKey().toString(), kv.getValue().toString());
		}

		return keyValueMap;
	}

	// below from spring cloud config monitor
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	private void addWatch() {

	    ByteSequence key = ByteSequence.from("sample.value".getBytes());
		
		CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);
		// ByteSequence key = ByteSequence.from(cmd.key, StandardCharsets.UTF_8);
		Collection<URI> endpoints = Util.toURIs(Stream.of("http://192.168.1.100:2379").collect(Collectors.toList()));

		Watch.Listener listener = Watch.listener(responsex -> {
			System.out.println("Watching for key: test_key");

			for (WatchEvent event : responsex.getEvents()) {
				/*
				 * LOGGER.info("type={}, key={}, value={}", event.getEventType(),
				 * Optional.ofNullable(event.getKeyValue().getKey()).map(bs ->
				 * bs.toString(StandardCharsets.UTF_8)).orElse(""),
				 * Optional.ofNullable(event.getKeyValue().getValue()).map(bs ->
				 * bs.toString(StandardCharsets.UTF_8)) .orElse(""));
				 */
				System.out.println(
						"Event !!!!!!: " + event.getKeyValue().getKey() + " " + event.getKeyValue().getValue());
			}
			
			
			publishEventByPath();
			latch.countDown();
		});

		try (Client clientx = Client.builder().endpoints(endpoints).build();
				Watch watch = clientx.getWatchClient();
				Watch.Watcher watcher = watch.watch(key, listener)) {

			latch.await();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}

	private Set<String> publishEventByPath() {

		String[] pathsToSendRefreshNotificaiton = new String[] { "sample" };

		if (!Arrays.isNullOrEmpty(pathsToSendRefreshNotificaiton)) {

			Set<String> services = new LinkedHashSet<>();

			for (String path : pathsToSendRefreshNotificaiton) {
				services.addAll(guessServiceName(path));
			}
			if (this.applicationEventPublisher != null) {
				for (String service : services) {
					log.info("Refresh for: " + service);
					this.applicationEventPublisher
							.publishEvent(new RefreshRemoteApplicationEvent(this, this.busId, service));
				}
				return services;
			}

		}
		return Collections.emptySet();
	}

	private Set<String> guessServiceName(String path) {
		Set<String> services = new LinkedHashSet<>();
		if (path != null) {
			String stem = StringUtils.stripFilenameExtension(StringUtils.getFilename(StringUtils.cleanPath(path)));
			// TODO: correlate with service registry
			int index = stem.indexOf("-");
			while (index >= 0) {
				String name = stem.substring(0, index);
				String profile = stem.substring(index + 1);
				if ("application".equals(name)) {
					services.add("*:" + profile);
				} else if (!name.startsWith("application")) {
					services.add(name + ":" + profile);
				}
				index = stem.indexOf("-", index + 1);
			}
			String name = stem;
			if ("application".equals(name)) {
				services.add("*");
			} else if (!name.startsWith("application")) {
				services.add(name);
			}
		}
		return services;
	}
}
