package pl.edu.agh.smart_repo.services.file_extension;

import https.agh_edu_pl.smart_repo.file_extension_service.Extension;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.AcceptableFileExtension;
import pl.edu.agh.smart_repo.services.directory_tree.util.MagicObjectControllerService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class FileExtensionService {

    private final TikaConfig config = TikaConfig.getDefaultConfig();

    public String getNewFileExtension(File file) {
        String extension = null;
        try {
            InputStream stream = TikaInputStream.get(Paths.get(file.getPath()));
            extension = getExtensionByInputStream(stream, file, new Metadata());
        } catch (IOException e) {
            log.error("Cannot check file extension - error opening file input stream: " + file);
        }
        return extension;
    }

    public Extension getStoredFileExtension(Path filePath) {
        Extension extension = null;
        try {
            Metadata metadata = new Metadata();
            InputStream stream = TikaInputStream.get(filePath, metadata);
            String fileExtension = getExtensionByInputStream(stream, filePath.toFile(), metadata);
            extension = Extension.fromValue(fileExtension);
        } catch (Exception e) {
            log.error("Cannot get stored file extension. File: {}. Message: {}", filePath, e.getMessage());
        }
        return extension;
    }

    public boolean isArchive(Path filePath) {
        Extension extension = getStoredFileExtension(filePath);
        Set<Extension> archiveExtensions = new HashSet<>(Arrays.asList(Extension.ZIP, Extension.TAR, Extension.GZ));
        return archiveExtensions.contains(extension);
    }

    private String getExtensionByInputStream(InputStream stream, File file, Metadata metadata) {
        String extension = null;
        try {
            MediaType mediaType = config.getMimeRepository().detect(stream, metadata);
            MimeType mimeType = config.getMimeRepository().forName(mediaType.toString());
            extension = getFileExtensionByFileNameOrMimeType(mimeType, file.getName());
            if (!hasAcceptableExtension(extension)) {
                if (!extension.equals(MagicObjectControllerService.nonMagicObjectMarker)) {
                    log.error("File extension '" + extension + "' is not acceptable. Available extensions are: " + Arrays.asList(AcceptableFileExtension.values()));
                }
                return null;
            }
        } catch (FileNotFoundException e) {
            log.error("Cannot find file: " + file);
        } catch (Exception e) {
            log.error("Exception while getting new file extension. Determined extension: {}. File: {}", extension, file.getName());
            log.error(e.getMessage());
        }
        return extension;
    }

    private String getFileExtensionByFileNameOrMimeType(MimeType mimeType, String fileName) {
        String[] fileNameString = fileName.split("\\.");
        String potentialExtension = fileNameString[fileNameString.length - 1];
        if (potentialExtension == null || !fileName.contains(".")) {
            String extensionFromMimeType = mimeType.getExtension();
            potentialExtension = extensionFromMimeType == null ? null : extensionFromMimeType.split("\\.")[1];
        }
        return potentialExtension;
    }

    private boolean hasAcceptableExtension(String extension) {
        return EnumUtils.isValidEnum(AcceptableFileExtension.class, extension);
    }
}
