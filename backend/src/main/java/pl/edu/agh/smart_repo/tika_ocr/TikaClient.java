package pl.edu.agh.smart_repo.tika_ocr;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TikaClient {
    private static final String pathToPdfs = "backend/src/main/resources/parsable-documents/pdf/";
    private static final String pathToImgs = "backend/src/main/resources/parsable-images/";
    private static final String resultsPath = "backend/src/main/resources/results/";

    public static void main(String[] args) throws Exception {

        File f = new File(pathToPdfs);
        String[] pdfFileNames = f.list();
        f = new File(pathToImgs);
        String[] imgFileNames = f.list();
        TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
        Map<String, Long> times = new HashMap<>();

        languageDetectionTest();

        assert imgFileNames != null;
        for (String fileName : imgFileNames) {
            measureTimeForFile(pathToImgs, "images/", fileName, tikaConfig, true, times);
        }

        assert pdfFileNames != null;
        for (String fileName : pdfFileNames) {
            measureTimeForFile(pathToPdfs, "pdfs/OCR-", fileName, tikaConfig, true, times);
            measureTimeForFile(pathToPdfs, "pdfs/", fileName, tikaConfig, false, times);
        }

        File file = new File(resultsPath+"times.txt");
        writeHashMapToFile(file,times);
    }

    public static String parseText(String path, String filename, TikaConfig tikaConfig,
                                      Metadata metadata, Boolean withOCR) throws Exception {
        System.out.println("Examining: [" + filename + "]");

        InputStream stream = TikaInputStream.get(Paths.get(path+filename));
        Detector detector = tikaConfig.getDetector();

        LanguageDetector langDetector = new OptimaizeLangDetector().loadModels();
        LanguageResult lang = langDetector.detect(FileUtils.readFileToString(new File(path+filename), UTF_8));

        System.out.println("The language of this content is: ["
                + lang.getLanguage() + "]");

        // Get a non-detecting parser that handles all the types it can
        Parser parser = tikaConfig.getParser();
        // Tell it what we think the content is
        MediaType type = detector.detect(stream, metadata);
        metadata.set(Metadata.CONTENT_TYPE, type.toString());

        // Have the file parsed to get the content and metadata
        ContentHandler handler = new BodyContentHandler();
        ParseContext parseContext = new ParseContext();

        if(withOCR) {
            TesseractOCRConfig config = new TesseractOCRConfig();

            PDFParserConfig pdfConfig = new PDFParserConfig();
            pdfConfig.setExtractInlineImages(true);

            parseContext.set(TesseractOCRConfig.class, config);
            parseContext.set(PDFParserConfig.class, pdfConfig);
            parseContext.set(Parser.class, parser); //need to add this to make sure recursive parsing happens!
        }

        parser.parse(stream, handler, metadata, parseContext);

        return handler.toString();
    }

    public static void writeHashMapToFile(File file, Map<String, Long> map){
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                bf.write(entry.getKey() + ":" + entry.getValue());
                bf.newLine();
            }
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void measureTimeForFile(String path, String resultsPathEnd, String fileName,
                                          TikaConfig tikaConfig, Boolean withOCR,
                                          Map<String,Long> times) throws Exception {
        long startTime = System.nanoTime();
        Metadata metadata = new Metadata();
        String text = parseText(path, fileName, tikaConfig, metadata, withOCR);

        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        times.put(fileName, timeElapsed);
        System.out.println("Execution time for file " + fileName + " in seconds: " +
                timeElapsed / 1000000000);

        Path resultFilePath = Paths.get(resultsPath + resultsPathEnd + fileName.split("\\.")[0] + ".txt");
        try {
            Files.createFile(resultFilePath);

        } catch (FileAlreadyExistsException ex) {
            // no problem here
        }
        byte[] strToBytes = text.getBytes();
        Files.write(resultFilePath, strToBytes);
    }

    public static void languageDetectionTest(){
        LanguageDetector langDetector = new OptimaizeLangDetector().loadModels();
        String pol = "Ala ma kota a Ola ma psa. Tak już w życiu bywa.";
        String eng = "Frankly, I find that outrageous.";
        String de = "Die Bücher des Frühjahrs";
        System.out.println(pol+": "+langDetector.detect(pol).getLanguage());
        System.out.println(eng+": "+langDetector.detect(eng).getLanguage());
        System.out.println(de+": "+langDetector.detect(de).getLanguage());
    }
}
