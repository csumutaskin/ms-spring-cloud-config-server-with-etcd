package com.noap.msfrw.etcd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

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
		boolean connect = connectorSample.connect("", "", null, null);
		assertEquals(true, connect);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("When All Key Values are Requested then Successfully Return Key Values")
	void whenAllKeyValuesAreRequested_thenSuccessfullyRetrieve() throws ExecutionException, InterruptedException {
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
		Map<String, String> allKeyValues = connector.getAllKeyValues("application", "profile", "label");
		Mockito.verify(mockClient,times(1)).getKVClient();
		Mockito.verify(kvmock,times(1)).get(any(ByteSequence.class), any(GetOption.class));
		assertNotNull(allKeyValues);
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
