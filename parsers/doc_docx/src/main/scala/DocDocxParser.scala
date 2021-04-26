import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Paths
import scala.util.{Failure, Success, Using}

object DocDocxParser extends App {

  //TODO: create two queues: one for doc and the second for docx
  parseDoc("example_file.doc") match {
    case Success(i) => println(i)
    case Failure(s) => println(s"Failed. Reason: $s")
  }

  def parseDoc (docFilePath: String) = {
    val file = getFileFromStorage(docFilePath)

    Using(new FileInputStream(file)) { fis =>
      val doc = new HWPFDocument(fis)
      val docEx = new WordExtractor(doc)
      val docText = docEx.getText()
      docText
    }
  }

  def parseDocx (docxFilePath: String) = {
    val file = getFileFromStorage(docxFilePath)

    Using(new FileInputStream(file)) { fis =>
      val docx = new XWPFDocument(fis)
      val docxEx = new XWPFWordExtractor(docx)
      val docxText = docxEx.getText()
      docxText
    }
  }

  def getFileFromStorage(path: String)= {
    val fullPath = Paths.get("/storage/", path)
    new File(fullPath.toUri)
  }
}