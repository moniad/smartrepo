package pl.edu.agh.smart_repo.services.directory_tree.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Service
public class FileInfoService {
    private final Path userFilesDirectoryPath;
    private final FileExtensionService fileExtensionService;

    public FileInfoService(ConfigurationFactory configurationFactory, FileExtensionService fileExtensionService) {
        this.userFilesDirectoryPath = configurationFactory.getStoragePath();
        this.fileExtensionService = fileExtensionService;
    }

    public FileInfo getFileInfo(File file) {
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            log.error("Error while getting additional file data from " + file);
        }
        var fileInfo = new FileInfo(file.getName(), attr.creationTime().toMillis(), file.isDirectory(), file.length());
        if (!file.isDirectory()) {
            var extension = fileExtensionService.getExtension(file);
            fileInfo.setExtension(extension);
        }
        return fileInfo;
    }

    public FileInfo getFileByName(String name) { //todo: change it to getFileByPath when path is indexed properly
        String resultPath = Paths.get(userFilesDirectoryPath.toString(), name).toString(); //todo: directory path
        return getFileInfo(new File(resultPath));
    }
}
