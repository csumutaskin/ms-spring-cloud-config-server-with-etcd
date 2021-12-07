FROM openjdk:15
LABEL maintainer="umutaskin@gmail.com"
COPY target/com.noap.msfrw-0.0.1-SNAPSHOT.jar etcd-sccs.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java","-jar","/etcd-sccs.jar"]