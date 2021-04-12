import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class TikaClient {
    private static final String recipiesFilename = "pdf_files/przepisyChodakowska91.pdf";

    public static void main(String[] args) throws IOException, TikaException, SAXException {

        File dir = new File("pdf_files/");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {

                long startTime = System.nanoTime();

                String parsedText = parseToPlainText(child.toString());

                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;

                System.out.println("Execution time in seconds : " +
                        timeElapsed / 1000000000);
            }
        }
    }

    private static String identifyLanguage(String text) throws IOException {
        LanguageDetector languageDetector = new OptimaizeLangDetector();
        languageDetector.loadModels();
        languageDetector.addText(text);
        return languageDetector.detect().getLanguage();
    }

    private static String parseToPlainText(String filename) throws IOException, SAXException, TikaException {
        // writeLimit set to -1 disables the write limit. It can be set to 12345657 as well.
        //BodyContentHandler handler = new BodyContentHandler(-1);
        ContentHandler handler = new LinkContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        File initialFile = new File(filename);
        try (InputStream stream = FileUtils.openInputStream(initialFile)) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        }
    }
}
