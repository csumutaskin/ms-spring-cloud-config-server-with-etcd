FROM openjdk:15
LABEL maintainer="umutaskin@gmail.com"
COPY ./client-example.jar client-example.jar
EXPOSE 8081/tcp
ENTRYPOINT ["java","-jar","/client-example.jar"]