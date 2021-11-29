package com.noap.msfrw.etcd.util.watch.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import com.noap.msfrw.redis.util.RedisConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class EtcdWatchLockTest {

	@Test
	@DisplayName("When Redisson Client Given Set it With Success")
	void whenRedissonClientGiven_setWithSuccess() {
		RedissonClient mockClient = Mockito.mock(RedissonClient.class);
		RedisConfigurationProperties rcp = Mockito.mock(RedisConfigurationProperties.class);
		Config config = Mockito.mock(Config.class);
		SingleServerConfig ssc = Mockito.mock(SingleServerConfig.class);
		Mockito.when(rcp.getUrlsWithRedisPrefix()).thenReturn(Stream.of("").collect(Collectors.toList()));
		Mockito.when(config.useSingleServer()).thenReturn(ssc);
		Mockito.when(ssc.setAddress(anyString())).then(invocation -> null);
		try (MockedStatic<Redisson> redissonUtil = Mockito.mockStatic(Redisson.class)) {
			redissonUtil.when(Redisson::create).then(invocation -> null);
			EtcdWatchLock ewl = new EtcdWatchLock(rcp, config);
			ewl.setRedisson(mockClient);
			assertEquals(ewl.getRedisson(), mockClient);
	    }		
	}
	
	@Test
	@DisplayName("Given Distributed Lock is Enabled When Process With Lock is requested Then Successfully Run the Method After Acquiring the Lock")
	void givenDistributedLockEnabled_whenProcessWithLockRequested_thenSuccess() throws InterruptedException {
		InsideLockRunnable runnable = Mockito.mock(InsideLockRunnable.class);
		RedissonClient mockClient = Mockito.mock(RedissonClient.class);
		RedisConfigurationProperties rcp = Mockito.mock(RedisConfigurationProperties.class);
		Config config = Mockito.mock(Config.class);
		RLock lock = Mockito.mock(RLock.class);
		SingleServerConfig ssc = Mockito.mock(SingleServerConfig.class);
		Mockito.when(rcp.getDistributedLockEnabled()).thenReturn(Boolean.TRUE);
		Mockito.when(rcp.getUrlsWithRedisPrefix()).thenReturn(Stream.of("").collect(Collectors.toList()));
		Mockito.when(config.useSingleServer()).thenReturn(ssc);
		Mockito.when(ssc.setAddress(anyString())).then(invocation -> null);
		try (MockedStatic<Redisson> redissonUtil = Mockito.mockStatic(Redisson.class)) {
			redissonUtil.when(Redisson::create).then(invocation -> null);
			EtcdWatchLock ewl = new EtcdWatchLock(rcp, config);
			ewl.setRedisson(mockClient);
			Mockito.when(mockClient.getLock(anyString())).thenReturn(lock);
			Mockito.when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
			ewl.processWithLock("key", runnable);
			Mockito.verify(runnable).runInsideLock();
	    }	
	}
	
	@Test
	@DisplayName("Given Distributed Lock is Disabled When Process With Lock is Requested Then Successfully Run the Method Without Acquiring the Lock")
	void givenDistributedLockDisabled_whenProcessWithLockRequested_thenSuccess() throws InterruptedException {
		InsideLockRunnable runnable = Mockito.mock(InsideLockRunnable.class);
		RedissonClient mockClient = Mockito.mock(RedissonClient.class);
		RedisConfigurationProperties rcp = Mockito.mock(RedisConfigurationProperties.class);
		Config config = Mockito.mock(Config.class);
		SingleServerConfig ssc = Mockito.mock(SingleServerConfig.class);
		Mockito.when(rcp.getDistributedLockEnabled()).thenReturn(Boolean.FALSE);
		Mockito.when(rcp.getUrlsWithRedisPrefix()).thenReturn(Stream.of("").collect(Collectors.toList()));
		Mockito.when(config.useSingleServer()).thenReturn(ssc);
		Mockito.when(ssc.setAddress(anyString())).then(invocation -> null);
		try (MockedStatic<Redisson> redissonUtil = Mockito.mockStatic(Redisson.class)) {
			redissonUtil.when(Redisson::create).then(invocation -> null);
			EtcdWatchLock ewl = new EtcdWatchLock(rcp, config);
			ewl.setRedisson(mockClient);
			ewl.processWithLock("key", runnable);
			Mockito.verify(runnable).runInsideLock();
	    }	
	}
}
