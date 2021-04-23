import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Paths

object DocDocxParser extends App {

  //TODO: create two queues: one for doc and the second for docx

  def parseDoc (docFilePath: String) = {
    val docPath = Paths.get("/storage/", docFilePath)
    val docFin = new File(docPath.toUri)
    val docfis = new FileInputStream(docFin)
    val doc = new HWPFDocument(docfis)
    val docEx = new WordExtractor(doc)
    val docText = docEx.getText()
    println(docText)

    docfis.close()
    docText
  }

  def parseDocx (docxFilePath: String) = {
    val docxPath = Paths.get("/storage/", docxFilePath)
    val docxFin = new File(docxPath.toUri)
    val docxfis = new FileInputStream(docxFin)
    val docx = new XWPFDocument(docxfis)
    val docxEx = new XWPFWordExtractor(docx);
    val docxText = docxEx.getText()
    println(docxText)

    docxfis.close()
    docxText
  }
}