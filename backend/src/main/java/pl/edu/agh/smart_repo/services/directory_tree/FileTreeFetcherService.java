package pl.edu.agh.smart_repo.services.directory_tree;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentField;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.directory_tree.util.FileInfoService;
import pl.edu.agh.smart_repo.services.directory_tree.util.MagicObjectControllerService;
import pl.edu.agh.smart_repo.services.index.IndexerService;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileTreeFetcherService {

    private final FileInfoService fileInfoService;
    private final Path userFilesDirectoryPath;
    private final MagicObjectControllerService magicObjectController;
    private final IndexerService indexerService;

    public FileTreeFetcherService(ConfigurationFactory configurationFactory, FileInfoService fileInfoService,
                                  MagicObjectControllerService magicObjectController, IndexerService indexerService) {
        this.userFilesDirectoryPath = configurationFactory.getStoragePath();
        this.fileInfoService = fileInfoService;
        this.magicObjectController = magicObjectController;
        this.indexerService = indexerService;
    }

    public List<FileInfo> fetchFileTree(String directoryPath, boolean recursive, String[] extensions) {
        return isDirectory(directoryPath) ? fetchDirectoryTree(directoryPath, recursive, extensions) : fetchArchiveTree(directoryPath);
    }

    private boolean isDirectory(String directoryPath) {
        File directory = getFileFromPath(directoryPath);
        return directory.isDirectory();
    }

    private List<FileInfo> fetchDirectoryTree(String directoryPath, boolean recursive, String[] extensions) {
        File currentDirectory = getFileFromPath(directoryPath);

        List<File> files = (List<File>) FileUtils.listFiles(currentDirectory, extensions, recursive);
        File[] directories = Optional
                .ofNullable(currentDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter()))
                .orElse(new File[0]);
        files.addAll(Arrays.asList(directories));

        return files.stream()
                .filter(file -> magicObjectController.isNonMagicObject(file.toPath()))
                .map(fileInfoService::getFileInfo)
                .collect(Collectors.toList());
    }

    private List<FileInfo> fetchArchiveTree(String archivePath) {
        String fullArchivePath = userFilesDirectoryPath + archivePath;
        final List<FileInfo> foundFiles = indexerService.search(fullArchivePath, DocumentField.PATH).get();
        String fullPathWithDoubleSlash = userFilesDirectoryPath + "/" + archivePath; // bug with indexing files - sometimes after upload path in index gets double slash
        foundFiles.addAll(indexerService.search(fullPathWithDoubleSlash, DocumentField.PATH).get());
        final List<FileInfo> filteredFiles = foundFiles.stream()
                .filter(file -> !file.getName().equals(archivePath))
                .filter(file -> !file.getName().equals(archivePath.substring(1)))
                .collect(Collectors.toList());
        log.info("Inside archive: " + archivePath + " found indexed files: " + filteredFiles);
        return filteredFiles;
    }

    private File getFileFromPath(String directoryPath) {
        String resultPath = Paths.get(userFilesDirectoryPath.toString(), directoryPath).toString();
        return new File(resultPath);
    }
}

