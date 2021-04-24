package pl.edu.agh.smart_repo.services.parse;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.AcceptableFileExtensions;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ParserService {
    Channel channel;

    @Autowired
    FileExtensionService fileExtensionService;

    public ParserService(ConfigurationFactory configurationFactory) throws Exception {
        // RabbitMQ settings
        int port = 5672;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configurationFactory.getRabbitHost());
        factory.setPort(port);
        Connection connection = retryConnection(factory);
        channel = connection.createChannel();

        for (AcceptableFileExtensions extension : AcceptableFileExtensions.values())
            channel.queueDeclare(extension.toString(), false, false, false, null);
        // END: RabbitMQ setting
    }

    public String parse(File file, String path_relative_to_storage) {
        try {
            String reply_to = channel.queueDeclare().getQueue();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .replyTo(reply_to)
                    .build();

            String extension = fileExtensionService.getExtension(file);
            if (extension == null) {
                log.error("Error while checking file extension.");
                return null;
            }

            channel.basicPublish("", extension, props, path_relative_to_storage.getBytes(StandardCharsets.UTF_8));

            GetResponse response = null;
            while (response == null)
                response = channel.basicGet(reply_to, true);

            channel.queueDelete(reply_to);

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
                Thread.sleep(retry * waitTime);
                return factory.newConnection();
            } catch (IOException e) {
                log.info("Unable to establish connection to RabbitMQ. Trial: " + retry);
            }
        }
        log.info("Unable to establish connection.");
        throw new IllegalStateException("No RabbitMQConnection provided.");
    }

}
