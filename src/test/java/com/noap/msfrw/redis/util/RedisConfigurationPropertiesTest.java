package com.noap.msfrw.redis.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RedisConfigurationPropertiesTest {
	
	RedisConfigurationProperties rcp;
	List<String> sampleUrls;	
	
	@BeforeEach
	void init() {
		rcp = new RedisConfigurationProperties();
		sampleUrls = Stream.of("sampleUrl").collect(Collectors.toList());
	}
	
	@Test
	@DisplayName("Test setter and getter for URLs")
	void whenGetUrlsCalled_thenSuccess() {
		rcp.setUrls(sampleUrls);
		List<String> urlsSet = rcp.getUrls();
		assertEquals(sampleUrls, urlsSet);
	}
	
	@Test
	@DisplayName("Test setter and getter for distributedLockEnabled")
	void whenGetDistributedLockEnabled_thenSuccess() {
		rcp.setDistributedLockEnabled(Boolean.TRUE);
		Boolean distributedLockEnabled = rcp.getDistributedLockEnabled();
		assertEquals(distributedLockEnabled, Boolean.TRUE);
	}
	
	@Test
	@DisplayName("Test setter and getter for lockWaitTime")
	void whenGetLockWaitTime_thenSuccess() {
		rcp.setLockWaitTime(Long.valueOf(10l));
		Long lockWaitTime = rcp.getLockWaitTime();
		assertEquals(lockWaitTime, 10l);
	}
	
	@Test
	@DisplayName("Test setter and getter for lockLeaseTime")
	void whenGetLockLeaseTime_thenSuccess() {
		rcp.setLockLeaseTime(Long.valueOf(10l));
		Long locklTime = rcp.getLockLeaseTime();
		assertEquals(locklTime, 10l);
	}
	
	@Test
	@DisplayName("Test getter for URLs With Redis Prefix")
	void whenGetUrlsWithPrefixCalled_thenSuccess() {
		rcp.setUrls(sampleUrls);
		List<String> urlsSet = rcp.getUrlsWithRedisPrefix();
		assertTrue(urlsSet.stream().anyMatch(u -> u.contains("redis://")));
	}
}
