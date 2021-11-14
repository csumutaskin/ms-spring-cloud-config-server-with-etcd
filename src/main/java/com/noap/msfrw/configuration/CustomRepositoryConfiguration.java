package com.noap.msfrw.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;
import com.noap.msfrw.etcd.util.EtcdConfigurationProperties;
import com.noap.msfrw.etcd.util.EtcdConnector;
import com.noap.msfrw.etcd.util.watch.lock.EtcdWatchLock;
import com.noap.msfrw.redis.util.RedisConfigurationProperties;

/**
 * Configuration class that initializes {@link EtcdEnvironmentRepository} bean. Main purpose of this
 * configuration class is to initialize the main beans using the their related properties taken from
 * the .yaml file. The idea of the code for initializing the {@link EtcdEnvironmentRepository} bean
 * is taken directly from the "official spring cloud server monitor" project, so that its
 * initialization method varies related with the existence of BusProperties bean.
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
  public EtcdConnector etcdConnector(@Nullable EtcdWatchLock etcdWatchLock) {
    return new EtcdConnector(etcdWatchLock, etcdProperties,
        redisProperties.getDistributedLockEnabled());
  }

  @Bean
  @ConditionalOnProperty(prefix = "redis", name = "distributedlockEnabled", havingValue = "true")
  public EtcdWatchLock etcdWatchLock() {
    return new EtcdWatchLock(redisProperties);
  }
}
