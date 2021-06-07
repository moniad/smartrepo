import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.logging.Logger

class Parser {

    private static def logger = Logger.getLogger(this.class.name)

    static void main(String[] args) {

        def storagePath = "/storage"

        def rabbitHost = 'localhost'
        def rabbitPort = 5672
        if (args.size() > 0) {
            rabbitHost = args[0]
        }

        def factory = new ConnectionFactory()
        factory.requestedHeartbeat = 600
        factory.connectionTimeout = 1000
        factory.host = rabbitHost
        factory.port = rabbitPort
        def connection = retryConnection(factory)
        def channel = connection.createChannel()

        def extractor = new TextExtractor()

        extractor.acceptedExtensions.each {
            channel.queueDeclare(it, false, false, false, null)
        }
        logger.info('Waiting for messages')


        def callback = [handle: { String consumerTag, Delivery delivery ->
            def replyTo = delivery.properties.replyTo
            def extension = delivery.envelope.routingKey
            def relativePath = new String(delivery.getBody(), StandardCharsets.UTF_8)
            def path = Paths.get(storagePath, relativePath).toString()

            def result = ''
            try {
                result = extractor."$extension"(path)
                logger.info('Parsing results:\n' + result)
            } catch (Throwable t) {
                logger.warning(t.message)
            }
            channel.basicPublish('', replyTo, null, result.getBytes(StandardCharsets.UTF_8))
        }] as DeliverCallback

        def cancel = [handle: { consumerTag -> }] as CancelCallback

        extractor.acceptedExtensions.each {
            channel.basicConsume(it, true, callback, cancel)
        }
    }

    private static Connection retryConnection(ConnectionFactory factory) {
        def connectionTrials = 5
        def waitTime = 5000

        for ( i in 0..connectionTrials ) {
            try {
                sleep(i * waitTime)
                return factory.newConnection()
            } catch (IOException ignored) {
                logger.info('Unable to establish connection to RabbitMQ. Trial: ' + i)
            }
        }
        logger.warning('Unable to establish connection')
        throw new IllegalStateException("No RabbitMQConnection provided")
    }

}
