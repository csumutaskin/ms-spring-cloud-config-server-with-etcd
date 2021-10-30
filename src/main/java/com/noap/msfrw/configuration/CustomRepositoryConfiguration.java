package com.noap.msfrw.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.noap.msfrw.etcd.repository.EtcdEnvironmentRepository;

@Configuration
public class CustomRepositoryConfiguration {

  @Bean
  public EtcdEnvironmentRepository etcdEnvironmentRepository() {
    return new EtcdEnvironmentRepository();
  }
}
