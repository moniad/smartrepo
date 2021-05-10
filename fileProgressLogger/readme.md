# file progress logger

## Introduction

FileProgressLogger is node.js Kafka client app, serving information about file processing progress by websocket.

### Prerequisites
You need to have the following software installed on your system:
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- Maven
- Java

### Running test
- Go to fileProgressLogger directory and run
```bash
docker-compose up
```

- Create kafka topic for communication

```bash
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic allFiles
```

- Run fileProgressLogger node.js app
```bash
npm install
node fileProgressLogger.js
```

- Open index.html from frontendSimulator

- Go to backendSimulator and run
```bash
mvn clean package
./target/producer
```

Backend simulator produces logs from pdf and mp3 files. Frontend asks fileProgressLogger only for specified file.

- Results can be seen in browser console (frontendSimulator). 

- clean after yourself
```bash
docker-compose down
```
