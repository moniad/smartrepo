package pl.edu.agh.smart_repo.services.upload;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.file.AcceptableFileExtensions;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.smart_repo.services.file.FileService;

@Slf4j
@Service
public class FileUploadService {
    private final Path storagePath;
    Channel channel;

    @Autowired
    FileService fileService;

    public FileUploadService(ConfigurationFactory configurationFactory) throws Exception {
        this.storagePath = configurationFactory.getStoragePath();

        //RabbitMQ settings
        int port = 5672;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configurationFactory.getRabbitHost());
        factory.setPort(port);
        Connection connection = retryConnection(factory);
        channel = connection.createChannel();

        for (AcceptableFileExtensions extension: AcceptableFileExtensions.values())
            channel.queueDeclare(extension.toString(), false, false, false, null);
        //END: RabbitMQ setting
    }

    private String parseWithParsers(File file, String path_relative_to_storage)
    {
        try {
            String reply_to = channel.queueDeclare().getQueue();

            BasicProperties props = new BasicProperties
                    .Builder()
                    .replyTo(reply_to)
                    .build();

            String extension = fileService.getExtension(file);
            if (extension == null)
            {
                log.error("error while checking file extension");
                return null;
            }

            channel.basicPublish("", extension, props, path_relative_to_storage.getBytes(StandardCharsets.UTF_8));

            GetResponse response = null;
            while (response == null)
                response= channel.basicGet(reply_to, true);

            channel.queueDelete(reply_to);

            return new String(response.getBody(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("error while sending file to parse queues");
            return null;
        }
    }

    public Result processFile(MultipartFile file) {
        //TODO: this part should be retrieved from frontend
        String path_relative_to_storage = file.getOriginalFilename();
        log.info("Start processing file: " + path_relative_to_storage);

        Path filePath = Paths.get(storagePath.toString(), path_relative_to_storage);

        File new_file = new File(filePath.toUri());

        try (FileOutputStream fos = new FileOutputStream(new_file)){
            fos.write(file.getBytes());
        } catch (FileNotFoundException e)
        {
            log.error("file couldn`t be created");
            return new Result(ResultType.FAILURE, e);
        }
        catch (IOException e) {
            log.error("error while saving file");
            return new Result(ResultType.FAILURE, e);
        }

        String parsed = parseWithParsers(new_file, path_relative_to_storage);
        if (parsed == null)
        {
            return new Result(ResultType.FAILURE, "failed to parse file");
        }

        log.info("received parse response: '" + parsed + "'");

        //TODO send to indexing service there...
        return new Result(ResultType.SUCCESS);
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
        log.info("Unable to establish connection");
        throw new IllegalStateException("No RabbitMQConnection provided");
    }
}
