package pl.edu.agh.smart_repo.services.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.AcceptableFileExtensions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
@Service
public class FileService {

    private TikaConfig config = TikaConfig.getDefaultConfig();

    public boolean hasAcceptableExtension(File file) throws FileNotFoundException
    {
        String extension = getExtension(file);
        return EnumUtils.isValidEnum(AcceptableFileExtensions.class, extension);
    }

    public String getExtension(File file)
    {
        String extension = null;
        try {
            Metadata metadata = new Metadata();
            InputStream stream = TikaInputStream.get(file, metadata);
            MediaType mediaType = null;

            mediaType = config.getMimeRepository().detect(stream, metadata);
            MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
            extension = mimeType.getExtension().split("\\.")[1];

        } catch (FileNotFoundException e){
            log.error("Couldn`t find file: " + file.getName());
        }
        catch (Exception e) {
            log.error("Cannot get file extension. File: {}", file.getName());
            log.error(e.getMessage());
        }
        return extension;
    }
}
