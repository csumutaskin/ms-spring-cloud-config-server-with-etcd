package com.noap.msfrw.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;

/**
 * Configuration class that initializes {@link EtcdEnvironmentRepository} bean.
 * 
 * @author UMUT
 *
 */
@Configuration
public class CustomRepositoryConfiguration {

	@Bean
	@ConditionalOnBean(BusProperties.class)
	public EtcdEnvironmentRepository etcdEnvironmentWithBusProperties(BusProperties busProperties) {
		return new EtcdEnvironmentRepository(busProperties.getId());
	}

	@Bean
	@ConditionalOnMissingBean(BusProperties.class)
	public EtcdEnvironmentRepository etcdEnvironmentWithoutBusProperties(@Value("${spring.cloud.bus.id:application}") String id) {
		return new EtcdEnvironmentRepository(id);
	}
	
}
