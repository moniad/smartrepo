FROM openjdk:11-jdk-slim as build

WORKDIR /src
COPY . .
RUN chmod +x ./gradlew && apt update && apt install dos2unix && dos2unix ./gradlew && ./gradlew build --no-daemon

FROM groovy:3.0.7-jre11

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

COPY --from=build ./src/build/libs/ppt_pptx-1.0-SNAPSHOT.jar .

ENTRYPOINT java -jar ppt_pptx-1.0-SNAPSHOT.jar $HOST
