package pl.edu.agh.smart_repo.common.file;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


@Slf4j
@Service
public class FileService {

    public boolean hasAcceptableExtension(File file) throws FileNotFoundException {
        TikaConfig config = TikaConfig.getDefaultConfig();

        Metadata metadata = new Metadata();
        InputStream stream = TikaInputStream.get(file, metadata);
        MediaType mediaType = null;
        try {
            mediaType = config.getMimeRepository().detect(stream, metadata);
            MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
            String extension = mimeType.getExtension().split("\\.")[1];
            return EnumUtils.isValidEnum(AcceptableFileExtensions.class, extension);
        } catch (Exception e) {
            log.error("Cannot get file extension. File: %s", file.getName());
            log.error(e.getMessage());
            return false;
        }
    }
}
