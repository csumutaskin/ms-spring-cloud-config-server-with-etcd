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

### What is special about this current demo project?




*Currently supports etcd v3 API

docker compose for rabbitmq:

version: '2'
services:
  rabbitmq:
    image: rabbitmq:3.5
    container_name: 'rabbitmq'
    ports:
      - 5672:5672
      - 15672:15672
    volumes:
      - /home/umut/Dev/Docker/Volume/rabbitmq/data/:/var/lib/rabbitmq/
      - /home/umut/Dev/Docker/Volume/rabbitmq/log/:/var/log/rabbitmq
