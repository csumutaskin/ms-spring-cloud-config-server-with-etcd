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

### What this project is about?

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

### How to run the project?

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
-------------------------
Do not forget to add information about:
* Created container and image files ss, ss on how to use demo application.
*code cov, test ve build tagleri
*Currently supports etcd v3 API
*how to add key value using rest end point of the demo project
*docker compose files for the demo environment.