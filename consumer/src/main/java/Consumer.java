import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;



public class Consumer {

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

        String queueName = "queue1";
        channel.queueDeclare(queueName, false, false, false, null);
        System.out.println("Waiting for messages.");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Received: '" + message + "'");
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
