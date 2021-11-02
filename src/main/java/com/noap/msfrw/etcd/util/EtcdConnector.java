package com.noap.msfrw.etcd.util;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;

/**
 * Etcd Connector Utility Class
 * @author UMUT
 *
 */
public class EtcdConnector {
	
	Logger logger = LoggerFactory.getLogger(EtcdConnector.class);
	
	private String etcdUrl; //"http://192.168.1.100:2379"
	private Client etcdClient;
	
	public EtcdConnector(String etcdUrl) {
		this.etcdUrl = etcdUrl;		
	}
	
	public boolean connect() {
		etcdClient = Client.builder().endpoints("http://192.168.1.100:2379","http://192.168.1.100:2381","http://192.168.1.100:2383").build();
		return true;
	}
	
	public Map<String, String> getAllKeyValues() {
		
		checkConnection();
		ByteSequence key = ByteSequence.from("\0".getBytes());

		GetOption option = GetOption.newBuilder().withSortField(GetOption.SortTarget.KEY)
				.withSortOrder(GetOption.SortOrder.DESCEND).withRange(key).build();
		
		CompletableFuture<GetResponse> futureResponse = etcdClient.getKVClient().get(key, option);

		GetResponse response;
		try {
			response = futureResponse.get();
		} catch (InterruptedException | ExecutionException e) {
			//convert to EtcdConnectException
			throw new RuntimeException(String.format("An exception occurred while retrieving all the key value pairs from the etcd cluster: %s, detail is: %s", etcdUrl, ExceptionUtils.getStackTrace(e))); 
		}

		if (response.getKvs().isEmpty()) {
			logger.info("Etcd cluster contains no data yet");
			return new ConcurrentHashMap<>();
		}

		Map<String, String> keyValueMap = new ConcurrentHashMap<>();
		for (KeyValue kv : response.getKvs()) {
			keyValueMap.put(kv.getKey().toString(), kv.getValue().toString());
		}
		return keyValueMap;
	}
	
	public String getValue(String keyString) {

		checkConnection();
		
		ByteSequence key = ByteSequence.from(keyString.getBytes());
		GetOption option = GetOption.newBuilder().withRange(key).build();
		CompletableFuture<GetResponse> futureResponse = etcdClient.getKVClient().get(key, option);

		GetResponse response;
		try {
			
			response = futureResponse.get();
		} catch (InterruptedException | ExecutionException e) {
			
			//convert to EtcdConnectException
			throw new RuntimeException(String.format("An exception occurred while retrieving a key from the etcd cluster: %s, detail is: %s", etcdUrl, ExceptionUtils.getStackTrace(e))); 
		}

		if (response.getKvs().isEmpty()) {
			return null;
		}
		return response.getKvs().get(0).getValue().toString();
	}
	
	public void listen() {
		
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
				
				
//				publishEventByPath();
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
	
	private void checkConnection() {
		
		if(etcdClient == null) {
			throw new RuntimeException("Before retrieving keys of a etcd cluster, call connect() method first."); //convert to EtcdConnectException
		}
	}
}
