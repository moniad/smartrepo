FROM maven:3.5-jdk-8 as build

COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline

COPY . .

RUN mvn package

FROM openjdk:8-jre-alpine

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

COPY --from=build ./target/doc_docx-1.0-SNAPSHOT-jar-with-dependencies.jar doc_docx-1.0-SNAPSHOT-jar-with-dependencies.jar

ENTRYPOINT java -jar doc_docx-1.0-SNAPSHOT-jar-with-dependencies.jar $HOST