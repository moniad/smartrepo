import com.rabbitmq.client.*

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import java.util.Properties

class XlsXlsxParser {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val logger = LogManager.getLogger(javaClass)
            var host = if (args.isNotEmpty()) args[0] else "localhost"
            val port = 5672

            val factory = ConnectionFactory()
            factory.host = host
            factory.port = port
            val connection = retryConnection(factory)
            val channel = connection!!.createChannel()

            val queueNameXls = "xls"
            channel.queueDeclare(queueNameXls, false, false, false, null)
            logger.info("Waiting for messages - xls.")

            val queueNameXlsx = "xlsx"
            channel.queueDeclare(queueNameXlsx, false, false, false, null)
            logger.info("Waiting for messages - xlsx.")

            val deliverCallbackXlsx = DeliverCallback { _, delivery ->
                var path = String(delivery.body, Charsets.UTF_8)
                path = "/storage/$path"

                val replyTo = delivery.properties.replyTo
                logger.info("Parsing path: '$path'")

                val producer = createProducer(host)
                producer.produceMessage("parsers", "Started parsing file $path")

                var result = ""

                try {
                    result = parseXlsx(path)
                } catch (e: IOException) {
                    logger.error(e.getStackTrace().toString())
                    producer.produceMessage("parsers", "Error parsing file $path")
                }

                logger.info("\nParsed xlsx succesfully")
                producer.produceMessage("parsers", "Finished parsing file $path")
                channel.basicPublish("", replyTo, null, result.toByteArray(Charsets.UTF_8))
            }

            val deliverCallbackXls = DeliverCallback { _, delivery ->
                var path = String(delivery.body, Charsets.UTF_8)
                path = "/storage/$path"

                val replyTo = delivery.properties.replyTo
                logger.info("Parsing path: '$path'")

                var result = ""

                try {
                    result = parseXls(path)
                } catch (e: IOException) {
                    logger.error(e.getStackTrace().toString())
                }

                logger.info("\nParsed xls succesfully")
                channel.basicPublish("", replyTo, null, result.toByteArray(Charsets.UTF_8))
            }

            channel.basicConsume(queueNameXlsx, true, deliverCallbackXlsx) { _: String? -> }
            channel.basicConsume(queueNameXls, true, deliverCallbackXls) { _: String? -> }
        }

        fun parseXlsx(path: String): String {
            val file = FileInputStream(File(path))
            val workbook = XSSFWorkbook(file)
            val dataFormatter = DataFormatter()

            var xlsxText = ""

            for (sheet in workbook) {

                val sheetName = sheet.sheetName
                val sheet = workbook.getSheet(sheetName)

                val row = sheet.iterator()
                while (row.hasNext()) {
                    val currentRow = row.next()
                    val cellsInRow = currentRow.iterator()
                    while (cellsInRow.hasNext()) {
                        val currentCell = cellsInRow.next()
                        val cellValue = dataFormatter.formatCellValue(currentCell)
                        xlsxText += cellValue + " "
                    }
                }
            }
            workbook.close()
            file.close()
            return xlsxText
        }

        fun parseXls(path: String): String {
            val file = FileInputStream(File(path))
            val workbook = HSSFWorkbook(file)
            val dataFormatter = DataFormatter()

            var xlsText = ""

            for (sheet in workbook) {

                val sheetName = sheet.sheetName
                val sheet = workbook.getSheet(sheetName)

                val row = sheet.iterator()
                while (row.hasNext()) {
                    val currentRow = row.next()
                    val cellsInRow = currentRow.iterator()
                    while (cellsInRow.hasNext()) {
                        val currentCell = cellsInRow.next()
                        val cellValue = dataFormatter.formatCellValue(currentCell)
                        xlsText += cellValue + " "
                    }
                }
            }
            workbook.close()
            file.close()
            return xlsText
        }

        @Throws(Exception::class)
        fun retryConnection(factory: ConnectionFactory): Connection? {
            val connectionTrials = 5
            val waitTime = 5000
            val logger = LogManager.getLogger(javaClass)
            for (retry in 0..connectionTrials) {
                try {
                    Thread.sleep((retry * waitTime).toLong())
                    return factory.newConnection()
                } catch (e: IOException) {
                    logger.error("Unable to establish connection to RabbitMQ. Trial: $retry")
                }
            }
            logger.error("Unable to establish connection")
            throw IllegalStateException("No RabbitMQConnection provided")
        }

        fun createProducer(host: String): Producer<String, String> {
            val props = Properties()
            props["bootstrap.servers"] = host+":9092"
            props["acks"] = "all"
            props["retries"] = 0
            props["linger.ms"] = 1
            props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
            props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"

            return KafkaProducer(props)
        }

        fun Producer<String, String>.produceMessage(topic: String, message: String) {
            val logger = LogManager.getLogger(javaClass)
            val message = ProducerRecord(
                    topic, // topic
                    "xls", // key
                    message // value
            )
            logger.info("Producer sending message: $message")
            this@produceMessage.send(message)
        }
    }
}
