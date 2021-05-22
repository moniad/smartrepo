FROM maven:3.8-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package


FROM adoptopenjdk/openjdk11:alpine-jre

ARG RABBITMQ_HOST
ENV RABBITMQ_HOST=$RABBITMQ_HOST

ARG ELASTICSEARCH_ADDRESS
ENV ELASTICSEARCH_ADDRESS=$ELASTICSEARCH_ADDRESS

COPY --from=build /usr/src/app/target/backend-0.0.1-SNAPSHOT.jar /usr/app/backend-0.0.1-SNAPSHOT.jar
EXPOSE 7777

ENTRYPOINT java -jar /usr/app/backend-0.0.1-SNAPSHOT.jar $RABBITMQ_HOST $ELASTICSEARCH_ADDRESS