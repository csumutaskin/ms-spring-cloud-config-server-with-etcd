package com.noap.msfrw.etcd.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.context.ApplicationEventPublisher;

import com.noap.msfrw.etcd.util.EtcdConnector;

@ExtendWith(MockitoExtension.class)
class EtcdEnvironmentRepositoryTest {
	
	@Test
	@DisplayName("When application event publisher is set then successfully set it")
	void whenApplicationEventPublisherSet_thenSuccess() {
		ApplicationEventPublisher aep = Mockito.mock(ApplicationEventPublisher.class);
		EtcdConnector connector = Mockito.mock(EtcdConnector.class);
		String busId = "sampleBusId";
		EtcdEnvironmentRepository eer = new EtcdEnvironmentRepository(connector, busId);
		eer.setApplicationEventPublisher(aep);
		assertEquals(eer.getApplicationEventPublisher(), aep);
	}
	
	@Test
	@DisplayName("Given Application Profile and Label When Key Values are Requested Then Return Environment With Keys and Values")
	void givenApplicationProfileAndLabel_whenKeyValuesAreRequested_thenReturnEnvironmentWithKeys() {
		
		Map<String, String> sampleMap = new HashMap<>();
		String sampleKey = "samplekey";
		String sampleValue = "samplevalue";
		sampleMap.put(sampleKey, sampleValue);
		
		EtcdConnector connector = Mockito.mock(EtcdConnector.class);
		String busId = "sampleBusId";
		EtcdEnvironmentRepository eer = new EtcdEnvironmentRepository(connector, busId);
		Mockito.when(connector.getAllKeyValues(anyString(), anyString(), anyString())).thenReturn(sampleMap);
		
		Environment findOne = eer.findOne("sample_application", "sample_profile", "sample_label");
		List<PropertySource> propertySources = findOne.getPropertySources();
		for(PropertySource propertySrc : propertySources) {
			if("mapPropertySource".equals(propertySrc.getName())) {
				assertEquals(propertySrc.getSource().get(sampleKey), sampleValue); 
			}
		}
	}
	
	@Test
	@DisplayName("When Publish Event by Path is Called, Event Publisher Publishes an Event")
	void whenEventPublishedByPath_thenApplicationEventPublisherPublishesEventWithSuccess() {
		ApplicationEventPublisher aep = Mockito.mock(ApplicationEventPublisher.class);
		EtcdConnector connector = Mockito.mock(EtcdConnector.class);
		String busId = "sampleBusId";
		EtcdEnvironmentRepository eer = new EtcdEnvironmentRepository(connector, busId);
		eer.setApplicationEventPublisher(aep);
		eer.publishEventByPath("sampleApp");
		Mockito.verify(aep, times(1)).publishEvent(any(RefreshRemoteApplicationEvent.class));
	}
}