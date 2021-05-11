package pl.edu.agh.smart_repo.services.directory_tree;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.directory_tree.util.FileInfoService;

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

    public FileTreeFetcherService(ConfigurationFactory configurationFactory, FileInfoService fileInfoService) {
        this.userFilesDirectoryPath = configurationFactory.getStoragePath();
        this.fileInfoService = fileInfoService;
    }

    public List<FileInfo> fetchFileTree(String directoryPath, boolean recursive, String[] extensions) {
        String resultPath = Paths.get(userFilesDirectoryPath.toString(), directoryPath).toString();
        File currentDirectory = new File(resultPath);

        List<File> files = (List<File>) FileUtils.listFiles(currentDirectory, extensions, recursive);
        File[] directories = Optional.ofNullable(currentDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter())).orElse(new File[0]);
        files.addAll(Arrays.asList(directories));

        return files.stream().map(fileInfoService::getFileInfo).collect(Collectors.toList());
    }
}
