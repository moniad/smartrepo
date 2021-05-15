import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.CancelCallback

import java.io.IOException

import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Paths
import scala.util.{Failure, Success, Try, Using}

object DocDocxParser extends App {

  var host = ""
  if (args.length > 0) host = args(0)
  else host = "localhost"
  val port = 5672

  val factory = new ConnectionFactory()
  factory.setHost(host)
  factory.setPort(port)
  val connection = retryConnection(factory)
  val channel = connection.createChannel()

  val queueNameDoc = "doc"
  channel.queueDeclare(queueNameDoc, false, false, false, null)
  println("Waiting for messages - doc.")

  val queueNameDocx = "docx"
  channel.queueDeclare(queueNameDocx, false, false, false, null)
  println("Waiting for messages - docx.")

   val deliverCallbackDoc: DeliverCallback = (consumerTag, delivery) => {

    val path = new String(delivery.getBody(), "UTF-8")

    val reply_to = delivery.getProperties().getReplyTo()
    println("Parsing: '" + path + "'")

    val extension = path.split("\\.").last

    var result = ""

    getFileFromStorage(path) match {
      case Success(f) =>
          parseDoc(f) match {
            case Success(r) =>
              println(r)
              result = r

            case Failure(r) => println(s"Cannot parse doc file. Reason: $r")}
      case Failure(f) => println(s"Cannot find file with path: $path. Reason: $f")
    }

    println("Parsed doc succesfull")

    channel.basicPublish("", reply_to, null, result.getBytes("UTF-8"))
  }

  val deliverCallbackDocx: DeliverCallback = (consumerTag, delivery) => {

    val path = new String(delivery.getBody(), "UTF-8")

    val reply_to = delivery.getProperties().getReplyTo()
    println("Parsing: '" + path + "'")

    val extension = path.split("\\.").last

    var result = ""

    getFileFromStorage(path) match {
      case Success(f) =>
          parseDocx(f) match {
            case Success(r) =>
              println(r)
              result = r
            case Failure(r) => println(s"Cannot parse docx file. Reason: $r")}

      case Failure(f) => println(s"Cannot find file with path: $path. Reason: $f")
    }

    println("Parsed docx succesfull")

    channel.basicPublish("", reply_to, null, result.getBytes("UTF-8"))
  }

  val cancel: CancelCallback = consumerTag => {}
  channel.basicConsume(queueNameDoc, true, deliverCallbackDoc, cancel)
  channel.basicConsume(queueNameDocx, true, deliverCallbackDocx, cancel)

  def retryConnection(factory: ConnectionFactory): Connection = {
    val connectionTrials = 5
    val waitTime = 5000
    for (retry <- 0 to connectionTrials) {
      try {
        Thread.sleep(retry * waitTime)
        return factory.newConnection() : Connection
      } catch {
        case e: IOException => println("Unable to establish connection to RabbitMQ. Trial: " + retry)
      }
    }
    println("Unable to establish connection")
    throw new IllegalStateException("No RabbitMQConnection provided")
  }

  def parseDoc (file: File) = {
    Using(new FileInputStream(file)) { fis =>
      val doc = new HWPFDocument(fis)
      val docEx = new WordExtractor(doc)
      val docText = docEx.getText()
      docText
    }
  }

  def parseDocx (file: File) = {
    Using(new FileInputStream(file)) { fis =>
      val docx = new XWPFDocument(fis)
      val docxEx = new XWPFWordExtractor(docx)
      val docxText = docxEx.getText()
      docxText
    }
  }

  def getFileFromStorage(path: String)= Try[File] {
    val fullPath = Paths.get("/storage/", path)
    new File(fullPath.toUri)
  }
}