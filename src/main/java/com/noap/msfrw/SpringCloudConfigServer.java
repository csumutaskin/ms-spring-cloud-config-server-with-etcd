package com.noap.msfrw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.ComponentScan;

/**
 * Bootstrap class for the Spring Cloud Config Server application.
 * 
 * @author UMUT
 * 
 */
@SpringBootApplication
@EnableConfigServer
@ComponentScan({"com.noap.msfrw"})
public class SpringCloudConfigServer {
  public static void main(String[] args) {
    SpringApplication.run(SpringCloudConfigServer.class, args);
  }
}
