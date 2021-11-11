package com.noap.msfrw.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;
import com.noap.msfrw.etcd.util.EtcdConfigurationProperties;
import com.noap.msfrw.etcd.util.EtcdConnector;
import com.noap.msfrw.etcd.util.watch.lock.EtcdWatchLock;
import com.noap.msfrw.redis.util.RedisConfigurationProperties;

/**
 * Configuration class that initializes {@link EtcdEnvironmentRepository} bean.
 * 
 * @author UMUT
 *
 */
@Configuration
public class CustomRepositoryConfiguration {

  private EtcdConfigurationProperties etcdProperties;
  private RedisConfigurationProperties redisProperties;

  public CustomRepositoryConfiguration(EtcdConfigurationProperties etcdProperties,
      RedisConfigurationProperties redisProperties) {
    this.etcdProperties = etcdProperties;
    this.redisProperties = redisProperties;
  }

  @Bean
  @ConditionalOnBean(BusProperties.class)
  public EtcdEnvironmentRepository etcdEnvironmentWithBusProperties(BusProperties busProperties,
      EtcdConnector etcdConnector) {
    return new EtcdEnvironmentRepository(etcdConnector, busProperties.getId());
  }

  @Bean
  @ConditionalOnMissingBean(BusProperties.class)
  public EtcdEnvironmentRepository etcdEnvironmentWithoutBusProperties(
      @Value("${spring.cloud.bus.id:application}") String id, EtcdConnector etcdConnector) {
    return new EtcdEnvironmentRepository(etcdConnector, id);
  }

  @Bean
  public EtcdConnector etcdConnector(EtcdWatchLock etcdWatchLock) {
    return new EtcdConnector(etcdWatchLock, etcdProperties);
  }

  @Bean
  public EtcdWatchLock etcdWatchLock() {
    return new EtcdWatchLock(redisProperties);
  }
}
