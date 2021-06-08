package pl.edu.agh.smart_repo.services.directory_tree.util;

import https.agh_edu_pl.smart_repo.file_extension_service.FileExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Service
public class FileInfoService {
    private final FileExtensionService fileExtensionService;

    public FileInfoService(FileExtensionService fileExtensionService) {
        this.fileExtensionService = fileExtensionService;
    }

    public FileInfo getFileInfo(File file) {
        FileInfo fileInfo = null;
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            fileInfo = new FileInfo(file.getName(), attr.creationTime().toMillis(), attr.lastModifiedTime().toMillis(), file.isDirectory(), file.length());
            if (!file.isDirectory()) {
                setExtension(file, fileInfo);
            }
        } catch (IOException e) {
            log.error("Error while getting additional file data from " + file);
        }
        return fileInfo;
    }

    private void setExtension(File file, FileInfo fileInfo) {
        FileExtension extension = fileExtensionService.getStoredFileExtension(file.toPath());
        if (extension != null) {
            fileInfo.setExtension(extension.value());
        }
    }
}
