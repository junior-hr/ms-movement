FROM openjdk:11
VOLUME /tmp
EXPOSE 8092
ADD ./target/ms-movement-0.0.1-SNAPSHOT.jar ms-movement.jar
ENTRYPOINT ["java","-jar","/ms-movement.jar"]