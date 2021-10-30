# ms-spring-cloud-config-server-with-etcd
Spring Cloud Config Server with etcd as data store

still being implemented..

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
