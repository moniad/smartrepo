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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;

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
        System.out.println("Waiting for messages.");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String path = new String(delivery.getBody(), StandardCharsets.UTF_8);
            path = "/storage/" + path;

            String reply_to = delivery.getProperties().getReplyTo();

            System.out.println("Parsing path: '" + path + "'");

            BodyContentHandler handler = new BodyContentHandler(-1);

            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();

            File initialFile = new File(path);
            try (InputStream stream = FileUtils.openInputStream(initialFile)) {
                parser.parse(stream, handler, metadata);
            } catch (IOException | SAXException | TikaException e) {
                e.printStackTrace();
            }

            System.out.println("parsed txt succesfull");

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
                System.out.println("Unable to establish connection to RabbitMQ. Trial: " + retry);
            }
        }
        System.out.println("Unable to establish connection");
        throw new IllegalStateException("No RabbitMQConnection provided");
    }
}