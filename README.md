# ms-spring-cloud-config-server-with-etcd

# Introduction

Configuration Management in a project can become a huge mess if it is not well designed and implemented. Popular Java Frameworks try to adapt their approaches and solutions in order to solve the difficulties that arise during a project's configuration management. The very first problematic issues that come into mind are:

* Recreating the artifact (project) each time a change is needed for internally stored configurations.
* It is not always easy to find a user friendly and secure GUI to manipulate configuration data for externally held configurations.(e.g. configuration management stored in a database)
* Most of the frameworks that use "inversion of control" principle and inject configuration to the component structures of the project, may need a restart of the application after a configuration change, even if "restarting" is redundant for the business logic.
* For orchestration tools (e.g. kubernetes) that auto control the application availability from multiple perspectives,  it is not always easy to find a fully adaptable API that connects the configuration store with the implementation technology/language used within the project.

All these and the ones that are not mentioned above makes it critical to design and implement a proper solution for handling configuration.

Spring framework provides various ways to handle configuration management. The one which will focus on this project is the *Spring Cloud Config Server*. You can reach the official documentation of Spring Cloud Config Server here on [this link](https://cloud.spring.io/spring-cloud-config/multi/multi__spring_cloud_config_server.html)

The demo project uses ETCD as the configuration store. ETCD is an open source distributed key value store which is resided in the "Control Plane" of Kubernetes Components and which are easily accessible by any nodes in the Kubernetes cluster through the API provided. [This](https://etcd.io/docs/) link shows detailed information on ETCD.

### What this project is about

This project is a Spring Cloud Config Server implementation that uses ETCD as the configuration management store, so that high availability can be established by using its distributed nature. For many Devops teams, kubernetes has become the defacto standart when an orchestration tool is necessary, and ETCD can be considered as one of the subcomponents this technology.

Spring Cloud Config is a server-client implementation that distribute configurations to their belonging applications over network. It is a secure, distributed and highly available approach that allows the systems to hold configurations of many different applications in a microservice ecosystem. 

Below image shows how a basic communication is established for configuration management that uses Spring Cloud Config Architecture:

![SCCS Network](https://github.com/csumutaskin/project-docs/blob/main/ms-spring-cloud-config-server-with-etcd/Design/UML/NetworkDiagrams/SCCS%20Very%20Basic%20Network%20Diagram.jpg?raw=true)

As clearly seen on the above image, configuration management is simply sending configurations that are located in one (or more) type of configuration stores by the *Spring Cloud Config Server* to all of the Microservices that have a Spring Cloud Config Client nature and have a healthy connection with the Server. Above image does not support the refreshment of the application context of the clients whenever an update occurs in the configuration stores.
Although scope refreshment is among the capabilities of the Spring Cloud Config architecture, additional tools are required to use it (that I will mention later on).

### What is special about this current demo project?

Code Repositories contain a lot of examples on Spring Cloud Config Architecture, most of them also contain code about the auto refreshment of properties on config clients. But I met none, with an **"ETCD version 3+" used as configuration store and auto refreshment of config clients are still on but this time "Without the help of Web hooks"** This project aims to find a solution to this problem. And still, as the other open source examples, it does this refreshment process by the help of Spring Cloud Config Bus. 

### What is needed to run this application?

To compile and run this application you need to have:

* [JDK 15](https://jdk.java.net/java-se-ri/15) (You can of course change the compiler version to the installed version on your local environment, but slight modifications might be needed if you do so.)
* [Maven](https://maven.apache.org/download.cgi)
* A Java IDE (Preferably but not mandatory)
* [Docker](https://docs.docker.com/engine/install/ubuntu/) -> to directly see the demonstration
installed on your system.

### How to run the project

* Clone the project to your local
* Run:
	`mvn clean install`
* From the root folder of the project where docker-compose file is located run: "docker-compose up"
The below items will be explained later in detail, but to see the outputs of the demo, you can quickly continue to what is instructed below:
* Open URL http://localhost:8081/get on your browser, this is the client application end point which returns the value of the configuration key: "value"
Since initial ETCD store contains no key value pairs, it will directly return its default value: "not assigned yet"
* Add a new key value to the ETCD using the below endpoint that belongs to the spring cloud config server: 
  "http://localhost:8080/etcd/add/dev.sample.value/HelloUser"
  The compose contains an etcd3 browser that can be reachable using the URL: "http://localhost:9091", but I think it throws an unexpected exception when the end user adds a new key value (But editing existing value is always possible). So use the endpoint above (localhost:8080/etcd/add/.....) to add, but you can edit using the tool that runs on port 9091. This tool is taken from [the rustyx's open source project (Copyright (c) 2019 rustyx)](https://github.com/rustyx/etcdv3-browser) for non profitable purposes.
* Whenever value of the key "dev.sample.value" is updated in ETCD, please hit "http://localhost:8081/get" on browser and see how the configuration change affects the Spring Cloud Config Client.
* Do not forget to shut down the demo using the : docker-compose down --rmi 'all' command to clean the containers and images created by the project.
	
### Links you might need throughout the project execution:

* [The client project that displays one of its configuration with key named "value" @ http://localhost:8081/get](http://localhost:8081/get)
* [ETCD Browser to edit key values @ http://localhost:9091](http://localhost:9091)
* [To add a new Key Value Pair Use @ http://localhost:8080/etcd/add/key/value](http://localhost:8080/etcd/add/dev.sample.value/umut)

after you run the application.

## How auto refreshing is done using this utility

![Simple Flow](https://github.com/csumutaskin/project-docs/blob/main/ms-spring-cloud-config-server-with-etcd/Design/UML/NetworkDiagrams/Spring%20Cloud%20Config%20With%20Refresh.jpg?raw=true)

### About Spring Cloud Config Client Implementation (Microservices side)

* When a Spring Cloud Config Client natured microservice is started/restarted it will try to establish a connection with the Spring Cloud Config Server.   For making the microservice having a proper Spring Cloud Config Client, be sure to:
    * Enable Actuator end points for the client application, /refresh endpoint might be enough (I will check and edit later) but for demonstration all actuator end points are enabled.
    * RabbitMQ connection settings should also be included in the application properties.
    * Spring Cloud Config bus and refresh properties should be also enabled.
    * Add spring.application.name property for Spring Cloud Config Server to serve the client, its configuration set. 
    * Do not forget to add the URL of the spring cloud config server to the application properties.

A simple yaml file for a client natured microservice might be as follows: (This is a simple configuration and not suitable for production environment, because there is no security around the actuator end points and all actuator end points are enabled for simplicity below)
```yaml
server:
  port: 8081
spring:
  rabbitmq:
    host: 192.168.1.100
    port: 5672
    username: guest
    passowrd: guest    
  config:
    import: "optional:configserver:"
  cloud:
    bus:
      enabled: true
      refresh:
        enabled: true
    config:
      uri: http://localhost:8080
  application:
    name: sample      
  profiles:
    active: dev
management:
  endpoints:
    web:
      exposure:
        include: "*"    
```

* For all the configurations that should be updated immediately as the key value changes, you need to add @RefreshScope annotation to the bean (or to the method).

```java
package com.noap.restexample.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class SampleController {
  
  @Value("${value:not assigned yet}")
  private String sampleValue;

  //...
  }
}
```

### About Spring Cloud Config Server Implementation

* Spring Cloud Config Server should also be configured to access the rabbitmq and of course ETCD. I have not met an official/community library that connects Spring Cloud Config Server
to the ETCD store. Thus, I used the "Custom Repo Definition" solution for Spring Cloud Config Server to read key values from the ETCD store by providing an actual implementation to the
org.springframework.cloud.config.server.environment.EnvironmentRepository interface. Details will be given later. The application.yaml for the Spring Cloud Config Server is as follows:
```yaml
spring:
  rabbitmq:
    host: example-rabbitmq
    port: 5672
    username: guest
    password: guest
  profiles:
    active: native
management:
  security:
    enabled: false
etcd:
  keyPrefixOrder:
    - profile
    - application
    - label
  httpsEnabled: false
  urls:
    - "example-etcd:2379"    
redis:
  distributedlockEnabled: true
  lockWaitTime: 2
  lockLeaseTime: 5
  urls: 
    - "example-redis:6379"
```

* Properties within etcd... are custom properties that establishes the connection to the ETCD store. The ones that begin with "redis" are also custom properties and will be declared later
on this document, for simplicity, in order to use this utility without a redis connection please make sure that redis.distributedlockEnabled is set to false. Spring Cloud Config
Server should contain the annotation @EnableConfigServer as below:
```java
@SpringBootApplication
@EnableConfigServer

public class SpringCloudConfigServer {
  public static void main(String[] args) {
    SpringApplication.run(SpringCloudConfigServer.class, args);
  }
}
```

* For Spring Cloud Config server to feed from a custom repository (any kind of persistent store, or even cache (although strongly discouraged :) technically possible) can be used as a possible key value store) It should contain an implementation of the EnvironmentRepository interface and in findOne() method add all the possible key value pairs for the application to the Environment map. This environment map that returns will be consumed by the Spring Cloud Config Clients.

```java
import org.springframework.cloud.config.server.environment.EnvironmentRepository;

public class EtcdEnvironmentRepository implements EnvironmentRepository {
  
  @Override
  public Environment findOne(String application, String profile, String label) {
  	//Setup the environment here...
  }
}
```

### The Webhook replacement by the ETCD v3 Watch utility in Spring Cloud Config Server Implementation

* In order to trigger the automatic refreshment of the properties Spring Cloud Config Server uses Spring Cloud Bus project to trigger an event for all the subscriber config clients to listen and after that use their actuator refresh end points to re-fetch the new configuration data set from the server.

* Spring Cloud Bus can use Kafka, Redis or RabbitMQ as the delivery bus structure (or a custom implementation). But among the current community artifacts Redis connector seems to be obsolete, so, Kafka and RabbitMQ are preferred instead. pom.xml of this project contains artifacts needed for Spring Cloud Config Server to connect to the bus.

* Spring Cloud Config Architecture uses 3 different ways to refresh the configurations:
    * **/actuator/refresh:** triggered from the config client, and fetches the new configuration set only for that client.
    * **/actuator/bus-refresh:** triggered from the config client, but this time a refresh event is broadcasted using the Spring Cloud Bus, and it notifies all the subscribed clients
	to re-fetch their configurations.
    * **/monitor:** This is part of the [Spring Cloud Config Monitor](https://github.com/spring-cloud/spring-cloud-config/tree/main/spring-cloud-config-monitor) project attached to the Spring Cloud Config Server. It is triggered from the server and as the /actuator/bus-refresh end point, a broadcasted message by the Spring Cloud Bus notifies all the subscribed clients to re-fetch their configurations.

* /monitor end point is used by the webhooks mechanism provided by git repositories. When any modification is done on property files in storage, git triggers a web hook which is actually calling the monitor end point of the spring cloud monitor. This will trigger the refreshment process.

* ETCD does not provide an official webhook utility to trigger the monitor end point, but what happens when the watch API of ETCDv3 implemented in Spring Cloud Config server continuously listens to the modifications in ETCD store and internally trigger the event itself w.r.t to the application information (of the altered configuration)? The utility is implemented in 
EtcdConnector.java within startListening(EtcdEnvironmentRepository repository) method. 

* Two problems still exist:
    * How to understand which application the altered configuration belongs to: There may be multiple applications that connect to the same Spring Cloud Config Server Cluster, it will be a waste of time and resource for all applications to refresh their configuration data even if the configuration does not belong to them. This was handled easily when storage was a git repository. You might have followed a naming convention for the files of the property sets, and whenever a configuration changes in a file, it will detect its belonging application from the name, profiling would also be easier too, by the help of a well designed folder structure in which each distinguishes the environment in the git repository. 
    The problem here is solved by defining a naming rule in the application properties of Spring Cloud Config Server and of course by obeying this custom rule when defining the key naming in ETCD store. To be more precise:
    application.yaml contains the below custom property set:
    ```yaml
    etcd:
	  keyPrefixOrder:
    	- profile
	    - application
    	- label
    ```
    This set defines what will be order of naming of a key in ETCD store. If the configuration belongs to an application called "sample" (by application.name property in the microservice), and the current client environment is "dev", the value can only be consumed by that client if the key name is "dev.sample.whatevertheinjectedvalueis"
    If you change the etcd.keyPrefixOrder you have to rename your keys. (And you do not have to use all of them, choose any arbitrary subset among them)
    profile: is the environment where the client is running like dev, qa, prod, custom....
    application: is the name of the application, defined by the property application.name in Config Client side.
    label: in case you need a 3rd distinguisher, any alphanumeric string is possible.
    
    * What happens when you are running a cluster of Spring Cloud Config Servers for availability? This means each node will have its own watcher and for even a single key change,
    the bus will broadcast "spring cloud config server node" number of times the same notification. It will not result with an unexpected outcome, however this is time consuming too, this is where the distributed redis lock utility come into the scene. If only one node of Spring Cloud Config Server is running, then it is unnecessary to enable this lock utility, but to decrease the resource cost in a multi node Cloud Server cluster then you may enable it from the application.yaml of the server. More on Redis utility later.
    

## ETCD v3 API

Details about ETCD v3 API can be read from the [link](https://etcd.io/docs/v3.3/rfc/)

## Project's Docker Compose Network Diagram

When the docker-compose up is executed on the root path of the docker compose file, a private network is constructed with the applications running on ports:

Image here...

## Redis's Necessity and Usage in the Project

Redis acts as a distributed lock to prevent all nodes in the Spring Cloud Config Server to publish the same event for the same key change. It basically puts a self ending (expiring after a certain period of time) distributed lock using the name key value concatenated with the new value of that key. So that whenever the first node to reach the publish stage retrieves the lock publishes the event and the lock expires itself. (I will add an endpoint that releases the lock with the given name, when a not graceful shutdown occurs and lock is still taken by the shutdown node) 
The lock is defined by the custom property set in the application.yaml:
    ```yaml
	redis:
	  distributedlockEnabled: true
	  lockWaitTime: 2
	  lockLeaseTime: 5
	  urls: 
    	- "example-redis:6379"
	 ```   
	 
	 distributedlockEnabled: true to enable the lock utility, false otherwise
	 lockWaitTime: time in seconds for the racing thread to wait for the lock and terminate its functionality (afterwards if it can not take the lock)
	 lockLeaseTime: time in seconds for the lock winning thread to release the lock automatically.
	 urls: redis urls with port information to connect to.
	 
As stated before it is not mandatory to use the distributed lock, since configuration change should not occur frequently in an application.

to be continued:
## More on Custom Properties in This Project and What They Do:
## code cov tags
