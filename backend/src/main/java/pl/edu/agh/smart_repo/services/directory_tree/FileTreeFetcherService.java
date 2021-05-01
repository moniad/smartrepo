package pl.edu.agh.smart_repo.services.directory_tree;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileTreeFetcherService {

    @Autowired
    FileExtensionService fileExtensionService;
    private final Path userFilesDirectoryPath;

    public FileTreeFetcherService(ConfigurationFactory configurationFactory) {
        this.userFilesDirectoryPath = configurationFactory.getStoragePath();
    }

    public List<FileInfo> fetchFileTree(String directoryPath, boolean recursive, String[] extensions) {
        String resultPath = Paths.get(userFilesDirectoryPath.toString(), directoryPath).toString();
        File currentDirectory = new File(resultPath);

        List<File> files = (List<File>) FileUtils.listFiles(currentDirectory, extensions, recursive);
        File[] directories = Optional.ofNullable(currentDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter())).orElse(new File[0]);
        files.addAll(Arrays.asList(directories));

        return files.stream().map(f -> getFileInfo(f)).collect(Collectors.toList());
    }

    private FileInfo getFileInfo(File file){
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
}
