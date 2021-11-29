package com.noap.msfrw.etcd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.noap.msfrw.etcd.util.watch.lock.EtcdWatchLock;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

@ExtendWith(MockitoExtension.class)
class EtcdConnectorTest {
	
	@Test
	@DisplayName("When Connection is Requested then Successfully Establish Connection")
	void whenConnectionRequested_thenSuccessfullyEstablishConnection() {
		String sampleIp = "192.168.1.101";
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Mockito.when(ecp.getUrlsWithHttpPrefix()).thenReturn(Stream.of(sampleIp).collect(Collectors.toList()));
		EtcdConnector connectorSample = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		boolean connect = connectorSample.connect("sampleUser", "samplePassword", 10l, 10l);
		assertEquals(true, connect);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Given KeyPrefix Order, When All Key Values are Requested then Successfully Return Key Values")
	void givenPrefixOrder_whenAllKeyValuesAreRequested_thenSuccessfullyRetrieve() throws ExecutionException, InterruptedException {
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Client mockClient = Mockito.mock(Client.class);
		CompletableFuture<GetResponse> futureResponse = Mockito.mock(CompletableFuture.class);
		EtcdConnector connector = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		connector.setEtcdClient(mockClient);
		KV kvmock = Mockito.mock(KV.class);
		Mockito.when(mockClient.getKVClient()).thenReturn(kvmock);
		Mockito.when(kvmock.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(futureResponse);
		Mockito.when(futureResponse.get()).thenReturn(null);
		Mockito.when(ecp.getKeyPrefixOrder()).thenReturn(Stream.of(KeyPrefix.APPLICATION, KeyPrefix.LABEL, KeyPrefix.PROFILE).collect(Collectors.toList()));
		Map<String, String> allKeyValues = connector.getAllKeyValues("application", "profile", "label");
		Mockito.verify(mockClient,times(1)).getKVClient();
		Mockito.verify(kvmock,times(1)).get(any(ByteSequence.class), any(GetOption.class));
		assertNotNull(allKeyValues);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Given no KeyPrefix Order, When All Key Values are Requested then Successfully Return Key Values")
	void givenNoPrefixOrder_whenAllKeyValuesAreRequested_thenSuccessfullyRetrieve() throws ExecutionException, InterruptedException {
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Client mockClient = Mockito.mock(Client.class);
		CompletableFuture<GetResponse> futureResponse = Mockito.mock(CompletableFuture.class);
		EtcdConnector connector = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		connector.setEtcdClient(mockClient);
		KV kvmock = Mockito.mock(KV.class);
		Mockito.when(mockClient.getKVClient()).thenReturn(kvmock);
		Mockito.when(kvmock.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(futureResponse);
		Mockito.when(futureResponse.get()).thenReturn(null);
		Mockito.when(ecp.getKeyPrefixOrder()).thenReturn(new ArrayList<>());
		Map<String, String> allKeyValues = connector.getAllKeyValues("application", "profile", "label");
		Mockito.verify(mockClient,times(1)).getKVClient();
		Mockito.verify(kvmock,times(1)).get(any(ByteSequence.class), any(GetOption.class));
		assertNotNull(allKeyValues);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Given no KeyPrefix Order, When All Key Values are Requested then Successfully Return Some Sample Key Values")
	void givenNoPrefixOrder_whenAllKeyValuesAreRequested_thenSuccessfullyRetrieveSomeFilledKVs() throws ExecutionException, InterruptedException {
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Client mockClient = Mockito.mock(Client.class);
		CompletableFuture<GetResponse> futureResponse = Mockito.mock(CompletableFuture.class);
		GetResponse getResponse = Mockito.mock(GetResponse.class);
		EtcdConnector connector = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		connector.setEtcdClient(mockClient);
		KV kvmock = Mockito.mock(KV.class);
		Mockito.when(mockClient.getKVClient()).thenReturn(kvmock);
		Mockito.when(kvmock.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(futureResponse);
		Mockito.when(futureResponse.get()).thenReturn(getResponse);
		KeyValue kv = Mockito.mock(KeyValue.class);	
		Mockito.when(getResponse.getKvs()).thenReturn(Stream.of(kv).collect(Collectors.toList()));
		Mockito.when(kv.getKey()).thenReturn(ByteSequence.from("SampleKey".getBytes()));
		Mockito.when(kv.getValue()).thenReturn(ByteSequence.from("SampleValue".getBytes()));
		Mockito.when(ecp.getKeyPrefixOrder()).thenReturn(new ArrayList<>());
		Map<String, String> allKeyValues = connector.getAllKeyValues("application", "profile", "label");
		Mockito.verify(mockClient,times(1)).getKVClient();
		Mockito.verify(kvmock,times(1)).get(any(ByteSequence.class), any(GetOption.class));
		assertNotNull(allKeyValues);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Given no KeyPrefix Order, When All Key Values are Requested then Throw InterruptedException")
	void givenNoPrefixOrder_whenAllKeyValuesAreRequested_thenThrowInterruptedException() throws ExecutionException, InterruptedException {
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Client mockClient = Mockito.mock(Client.class);
		CompletableFuture<GetResponse> futureResponse = Mockito.mock(CompletableFuture.class);
		EtcdConnector connector = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		connector.setEtcdClient(mockClient);
		KV kvmock = Mockito.mock(KV.class);
		Mockito.when(mockClient.getKVClient()).thenReturn(kvmock);
		Mockito.when(kvmock.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(futureResponse);
		Mockito.when(futureResponse.get()).thenThrow(InterruptedException.class);
		Mockito.when(ecp.getKeyPrefixOrder()).thenReturn(new ArrayList<>());		
		Map<String, String> allKeyValues = connector.getAllKeyValues("application", "profile", "label");
		assertNotNull(allKeyValues);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Given no KeyPrefix Order, When All Key Values are Requested then Throw ExecutionException")
	void givenNoPrefixOrder_whenAllKeyValuesAreRequested_thenThrowExecutionException() throws ExecutionException, InterruptedException {
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Client mockClient = Mockito.mock(Client.class);
		CompletableFuture<GetResponse> futureResponse = Mockito.mock(CompletableFuture.class);
		EtcdConnector connector = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		connector.setEtcdClient(mockClient);
		KV kvmock = Mockito.mock(KV.class);
		Mockito.when(mockClient.getKVClient()).thenReturn(kvmock);
		Mockito.when(kvmock.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(futureResponse);
		Mockito.when(futureResponse.get()).thenThrow(ExecutionException.class);
		Mockito.when(ecp.getKeyPrefixOrder()).thenReturn(new ArrayList<>());
		Throwable exc = null;
		try {
			connector.getAllKeyValues("application", "profile", "label");
		} catch (Exception e) {
			if(e.getCause() instanceof ExecutionException) {
				exc = e.getCause();
			}
		}
		assertNotNull(exc);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("When Get a Key Value is Requested then Successfully Return Key Value")
	void whenKeyValueIsRequested_thenSuccessfullyRetrieve() throws ExecutionException, InterruptedException {
		EtcdWatchLock etcdWatchLock = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		Client mockClient = Mockito.mock(Client.class);
		CompletableFuture<GetResponse> futureResponse = Mockito.mock(CompletableFuture.class);
		EtcdConnector connector = new EtcdConnector(etcdWatchLock, ecp, Boolean.TRUE);
		connector.setEtcdClient(mockClient);
		KV kvmock = Mockito.mock(KV.class);
		Mockito.when(mockClient.getKVClient()).thenReturn(kvmock);
		Mockito.when(kvmock.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(futureResponse);
		Mockito.when(futureResponse.get()).thenReturn(null);
		connector.getValue("AnyString");
		Mockito.verify(mockClient,times(1)).getKVClient();
		Mockito.verify(kvmock,times(1)).get(any(ByteSequence.class), any(GetOption.class));		
	}
}
