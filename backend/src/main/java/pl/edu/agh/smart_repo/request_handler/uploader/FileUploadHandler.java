package pl.edu.agh.smart_repo.request_handler.uploader;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.GetResponse;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.file.AcceptableFileExtensions;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;
import pl.edu.agh.smart_repo.request_handler.uploader.file_saver.FileSaver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class FileUploadHandler {
    private final Path filesCatalogPath;
    Channel channel;

    public FileUploadHandler(ConfigurationFactory configurationFactory) throws Exception{
        this.filesCatalogPath = configurationFactory.getFileCatalogPath();

        int port = 5672;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(port);
        Connection connection = retryConnection(factory);
        channel = connection.createChannel();

        for (AcceptableFileExtensions extension: AcceptableFileExtensions.values())
            channel.queueDeclare(extension.toString(), false, false, false, null);
    }

    public void put_pdf()
    {
        try {
            String path = "sample.pdf";

            System.out.println("Send parse '" + path + "' request");

            String reply_to = channel.queueDeclare().getQueue();

            BasicProperties props = new BasicProperties
                    .Builder()
                    .replyTo(reply_to)
                    .build();

            channel.basicPublish("", "pdf", props, path.getBytes(StandardCharsets.UTF_8));

            GetResponse response = null;
            while (response == null)
                response= channel.basicGet(reply_to, true);

            String resp_msg = new String(response.getBody(), StandardCharsets.UTF_8);

            System.out.println("Received pdf parse resp: '" + resp_msg + "'");

            channel.queueDelete(reply_to);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put_txt()
    {
        try {
            String path = "sample.txt";

            System.out.println("Send parse '" + path + "' request");

            String reply_to = channel.queueDeclare().getQueue();

            BasicProperties props = new BasicProperties
                    .Builder()
                    .replyTo(reply_to)
                    .build();

            channel.basicPublish("", "txt", props, path.getBytes(StandardCharsets.UTF_8));

            GetResponse response = null;
            while (response == null)
                response= channel.basicGet(reply_to, true);

            String resp_msg = new String(response.getBody(), StandardCharsets.UTF_8);

            System.out.println("Received txt parse resp: '" + resp_msg + "'");

            channel.queueDelete(reply_to);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Result processFile(File file, String path) {
        Path filePath = Paths.get(filesCatalogPath.toString(), path, file.getName());
        Result result;

        try {
            // folder should already exists
            FutureTask<Result> fileSaveResultFuture = runFileSaveThread(file, filePath);

            // Do other stuff

            result = fileSaveResultFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();

            return new Result(ResultType.FAILURE, e);
        }

        return result;
    }

    private FutureTask<Result> runFileSaveThread(File file, Path path) {
        FileSaver fileSaver = new FileSaver(file, path);
        FutureTask<Result> futureTask = new FutureTask<>(fileSaver);

        Thread thread = new Thread(futureTask);
        thread.start();

        return futureTask;
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
