package pl.edu.agh.smart_repo.services.parse;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.AcceptableFileExtension;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ParserService {
    private final Channel channel;
    private final FileExtensionService fileExtensionService;

    public ParserService(ConfigurationFactory configurationFactory, FileExtensionService fileExtensionService) throws Exception {
        this.fileExtensionService = fileExtensionService;
        // RabbitMQ settings
        int port = 5672;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configurationFactory.getRabbitHost());
        factory.setPort(port);
        Connection connection = retryConnection(factory);
        channel = connection.createChannel();

        for (AcceptableFileExtension extension : AcceptableFileExtension.values())
            channel.queueDeclare(extension.toString(), false, false, false, null);
        // END: RabbitMQ setting
    }
    public String parse(File file, String pathRelativeToStorage) {
        try {
            String replyQueue = channel.queueDeclare().getQueue();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .replyTo(replyQueue)
                    .build();

            String queueName = fileExtensionService.getNewFileExtension(file);
            if (queueName == null) {
                log.error("Queue name not determined. File will not be sent to any queue.");
                return null;
            }

            channel.basicPublish("", queueName, props, pathRelativeToStorage.getBytes(StandardCharsets.UTF_8));

            GetResponse response = null;
            while (response == null)
                response = channel.basicGet(replyQueue, true);

            channel.queueDelete(replyQueue);

            return new String(response.getBody(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("Error while sending file to parse queues.");
            return null;
        }
    }

    private static Connection retryConnection(ConnectionFactory factory) throws Exception {
        int connectionTrials = 5;
        int waitTime = 5000;
        for (int retry = 0; retry <= connectionTrials; ++retry) {
            try {
                Thread.sleep((long) retry * waitTime);
                return factory.newConnection();
            } catch (IOException e) {
                log.info("Unable to establish connection to RabbitMQ. Trial: " + retry);
            }
        }
        log.info("Unable to establish connection.");
        throw new IllegalStateException("No RabbitMQConnection provided.");
    }

}
