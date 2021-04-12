import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Producer {

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

        if (args.length > 1 && args[1].equals("docker")) {
            int count = 0;
            while(true) {
                String msg = "Message " + ++count;
                channel.basicPublish("", queueName, null, msg.getBytes(StandardCharsets.UTF_8));
                Thread.sleep(10000);
            }
        } else {
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter message: ");
                String message = br.readLine();

                if ("exit".equals(message)) {
                    System.out.println("exit");
                    break;
                }

                channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sent: '" + message + "'");
            }
        }

        channel.close();
        connection.close();
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