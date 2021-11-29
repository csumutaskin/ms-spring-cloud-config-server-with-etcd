package com.noap.msfrw.etcd.util;

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
class EtcdConfigurationPropertiesTest {
	
	EtcdConfigurationProperties ecp;
	List<String> sampleUrls;	
	
	@BeforeEach
	void init() {
		ecp = new EtcdConfigurationProperties();
		sampleUrls = Stream.of("sampleUrl").collect(Collectors.toList());
	}
	
	@Test
	@DisplayName("Test setter and getter for URLs")
	void whenGetUrlsCalled_thenSuccess() {
		ecp.setUrls(sampleUrls);
		List<String> urlsSet = ecp.getUrls();
		assertEquals(sampleUrls, urlsSet);
	}
	
	@Test
	@DisplayName("Test setter and getter for https enabled")
	void whenGetHttpsEnabled_thenSuccess() {
		ecp.setHttpsEnabled(Boolean.TRUE);
		Boolean https = ecp.getHttpsEnabled();
		assertEquals(https, Boolean.TRUE);
	}
	
	@Test
	@DisplayName("Test setter and getter for key prefix order")
	void whenGetLockWaitTime_thenSuccess() {
		List<KeyPrefix> order = Stream.of(KeyPrefix.APPLICATION, KeyPrefix.PROFILE, KeyPrefix.LABEL).collect(Collectors.toList());
		ecp.setKeyPrefixOrder(order);
		List<KeyPrefix> keyPrefixOrder = ecp.getKeyPrefixOrder();
		assertEquals(keyPrefixOrder, order);
	}
		
	@Test
	@DisplayName("Test getter for URLs With http Prefix")
	void whenGetUrlsWithHttpPrefixCalled_thenSuccess() {
		ecp.setUrls(sampleUrls);		
		List<String> urlsSet = ecp.getUrlsWithHttpPrefix();
		assertTrue(urlsSet.stream().anyMatch(u -> u.contains("http://")));
	}
	
	@Test
	@DisplayName("Test getter for URLs With https Prefix")
	void whenGetUrlsWithHttpsPrefixCalled_thenSuccess() {
		ecp.setUrls(sampleUrls);		
		ecp.setHttpsEnabled(Boolean.TRUE);
		List<String> urlsSet = ecp.getUrlsWithHttpPrefix();
		assertTrue(urlsSet.stream().anyMatch(u -> u.contains("https://")));
	}
}
