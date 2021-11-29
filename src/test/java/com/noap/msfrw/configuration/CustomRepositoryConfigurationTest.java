package com.noap.msfrw.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.bus.BusProperties;

import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;
import com.noap.msfrw.etcd.util.EtcdConfigurationProperties;
import com.noap.msfrw.etcd.util.EtcdConnector;
import com.noap.msfrw.etcd.util.watch.lock.EtcdWatchLock;
import com.noap.msfrw.redis.util.RedisConfigurationProperties;
import com.noap.msfrw.redis.util.RedisException;

@ExtendWith(MockitoExtension.class)
class CustomRepositoryConfigurationTest {
	
	@Test
	@DisplayName("When Creating Etcd Environment Repository (Without Bus) Then ETCD utiltiy connects with success")
	void givenEtcdConnector_whenCreatingEtcdEnvironmentWithoutBus_thenEtcdConnectionSuccess() {

		EtcdConnector etcdConnector = Mockito.mock(EtcdConnector.class);
		CustomRepositoryConfiguration crc = new CustomRepositoryConfiguration(null, null);
		EtcdEnvironmentRepository eer = crc.etcdEnvironmentWithoutBusProperties(null, etcdConnector);
		Mockito.verify(etcdConnector).connect(nullable(String.class), nullable(String.class), nullable(Long.class), nullable(Long.class));
		Mockito.verify(etcdConnector).startListening(any(EtcdEnvironmentRepository.class));
		assertNotNull(eer);
	}
	
	@Test
	@DisplayName("When Creating Etcd Environment Repository (With Bus) Then ETCD utiltiy connects with success")
	void givenEtcdConnector_whenCreatingEtcdEnvironmentWithBus_thenEtcdConnectionSuccess() {

		EtcdConnector etcdConnector = Mockito.mock(EtcdConnector.class);
		BusProperties busProperties = Mockito.mock(BusProperties.class);
		CustomRepositoryConfiguration crc = new CustomRepositoryConfiguration(null, null);
		EtcdEnvironmentRepository eer = crc.etcdEnvironmentWithBusProperties(busProperties, etcdConnector);
		Mockito.verify(etcdConnector).connect(nullable(String.class), nullable(String.class), nullable(Long.class), nullable(Long.class));
		Mockito.verify(etcdConnector).startListening(any(EtcdEnvironmentRepository.class));
		assertNotNull(eer);
	}
	
	@Test
	@DisplayName("Given EtcdWatchLock Create EtcdConnector with success")
	void givenEtcdWatchLock_thenCreateEtcdConnectorWithSuccess() {

		EtcdWatchLock ewl = Mockito.mock(EtcdWatchLock.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		RedisConfigurationProperties rcp = Mockito.mock(RedisConfigurationProperties.class);
		CustomRepositoryConfiguration crc = new CustomRepositoryConfiguration(ecp, rcp);
		EtcdConnector etcdConnector = crc.etcdConnector(ewl);
		Mockito.verify(rcp, Mockito.times(1)).getDistributedLockEnabled();
		assertNotNull(etcdConnector);
	}
	
	@Test
	@DisplayName("Given Redis Properties Create EtcdWatchLock with success")
	void givenRedisProperties_thenCreateEtcdWatchLockWithSuccess() {		
		RedisConfigurationProperties rcp = Mockito.mock(RedisConfigurationProperties.class);
		EtcdConfigurationProperties ecp = Mockito.mock(EtcdConfigurationProperties.class);
		CustomRepositoryConfiguration crc = new CustomRepositoryConfiguration(ecp, rcp);
		assertThrows(RedisException.class, () ->  crc.etcdWatchLock());
		Mockito.verify(rcp).getUrlsWithRedisPrefix();
	}
}