package com.example;

import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static java.lang.String.valueOf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class Producer {

    public static void main(String[] args) throws IOException, InterruptedException {
        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("producer.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            producer = new KafkaProducer<>(properties);
        }

        try {
            for (int i = 0; i < 5; i++) {
                send(producer,"allFiles","pdf", "filename.pdf:step" + valueOf(i));
                Thread.sleep(500);
                send(producer,"allFiles","mp3", "filename.mp3:step" + valueOf(i));
                Thread.sleep(1000);
            }
            send(producer,"allFiles","pdf", "filename.pdf:indexed");
            send(producer,"allFiles","mp3", "filename.mp3:indexed");
        } catch (Throwable throwable) {
            System.out.println(throwable.getStackTrace());
        } finally {
            producer.close();
        }

    }

    private static void send(KafkaProducer<String, String> producer, String topic, String key, String value) {
        producer.send(new ProducerRecord<String, String>(topic, key, value));
    }
}
