import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TxtTikaParser {

    public static void main(String[] args) throws Exception {

        String host;
        if (args.length > 0) {
            host = args[0];
        }
        else {
            host = "localhost";
        }
        int port = 5672;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        Connection connection = retryConnection(factory);
        Channel channel = connection.createChannel();


        String queueName = "txt";
        channel.queueDeclare(queueName, false, false, false, null);
        log.info("Waiting for messages.");

        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("txtTika.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            producer = new KafkaProducer<>(properties);
        }

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String fileName = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String path = "/storage/" + fileName;

            String reply_to = delivery.getProperties().getReplyTo();

            log.info("Parsing path: '" + path + "'");

            try {
                send(producer,"parsers","txt", "Parsing file " + fileName);
            } catch (Throwable throwable) {
                log.error(throwable.getStackTrace());
            }

            BodyContentHandler handler = new BodyContentHandler(-1);

            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();

            File initialFile = new File(path);
            try (InputStream stream = FileUtils.openInputStream(initialFile)) {
                parser.parse(stream, handler, metadata);
            } catch (IOException | SAXException | TikaException e) {
                e.printStackTrace();
            }

            log.info("parsed txt succesfully");
            try {
                send(producer,"parsers","txt", "Finished parsing file " + fileName);
            } catch (Throwable throwable) {
                log.error(throwable.getStackTrace());
            }finally {
                producer.close();
            }
            channel.basicPublish("", reply_to, null, handler.toString().getBytes(StandardCharsets.UTF_8));
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    private static Connection retryConnection(ConnectionFactory factory) throws Exception {
        int connectionTrials = 5;
        int waitTime = 5000;
        for (int retry = 0; retry <= connectionTrials; ++retry) {
            try {
                Thread.sleep(retry * waitTime);
                return factory.newConnection();
            } catch (IOException e) {
                log.error("Unable to establish connection to RabbitMQ. Trial: " + retry);
            }
        }
        log.error("Unable to establish connection");
        throw new IllegalStateException("No RabbitMQConnection provided");
    }

    private static void send(KafkaProducer<String, String> producer, String topic, String key, String value) {
        producer.send(new ProducerRecord<String, String>(topic, key, value));
    }
}