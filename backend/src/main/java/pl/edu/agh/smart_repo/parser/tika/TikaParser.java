package pl.edu.agh.smart_repo.parser.tika;

import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TikaParser implements Parser {
    @Override
    public DocumentStructure parse(String path) {
        BodyContentHandler handler = new BodyContentHandler(-1);

        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        File initialFile = new File(path);
        try (InputStream stream = FileUtils.openInputStream(initialFile)) {
            parser.parse(stream, handler, metadata);
        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
        }

        DocumentStructure documentStructure = new DocumentStructure();
        documentStructure.setByName(DocumentFields.PATH, path);
        documentStructure.setByName(DocumentFields.CONTENTS, handler.toString());
        documentStructure.setByName(DocumentFields.KEYWORDS, null);

        return documentStructure;
    }
}
