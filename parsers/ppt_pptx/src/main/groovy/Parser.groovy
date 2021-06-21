import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.logging.Logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

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
        factory.host = rabbitHost
        factory.port = rabbitPort
        def connection = retryConnection(factory)
        def channel = connection.createChannel()

        def extractor = new TextExtractor()

        extractor.acceptedExtensions.each {
            channel.queueDeclare(it, false, false, false, null)
        }
        logger.info('Waiting for messages')

        Properties props = new Properties()
        props.put("bootstrap.servers", rabbitHost+":9092")
        props.put("acks", "all")
        props.put("retries", 3)
        props.put("linger.ms", 1000)
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

        def producer = new KafkaProducer(props)


        def callback = [handle: { String consumerTag, Delivery delivery ->
            def replyTo = delivery.properties.replyTo
            def extension = delivery.envelope.routingKey
            def relativePath = new String(delivery.getBody(), StandardCharsets.UTF_8)
            def path = Paths.get(storagePath, relativePath).toString()
            send(producer,"parsers","ppt", "Started parsing file " + path)

            def result = ''
            try {
                result = extractor."$extension"(path)
                logger.info('Parsing results:\n' + result)
                send(producer,"parsers","ppt", "Finished parsing file " + path)
            } catch (Throwable t) {
                logger.warning(t.message)
                send(producer,"parsers","ppt", "Error parsing file " + path)
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
    private static void send(KafkaProducer producer, String topic, String key, String value) {
        producer.send(new ProducerRecord(topic, key, value))
    }

}
