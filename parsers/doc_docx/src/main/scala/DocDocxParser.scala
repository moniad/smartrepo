import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Paths
import scala.util.{Failure, Success, Try, Using}

object DocDocxParser extends App {

  //TODO: create two queues: one for doc and the second for docx
  val path = "docxex.docx"
  getFileFromStorage(path) match {
    case Success(f) =>   parseDoc(f) match {
      case Success(r) => println(r)
      case Failure(r) => println(s"Cannot parse doc file. Reason: $r")
    }
    case Failure(f) => println(s"Cannot find file with path: $path. Reason: $f")
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