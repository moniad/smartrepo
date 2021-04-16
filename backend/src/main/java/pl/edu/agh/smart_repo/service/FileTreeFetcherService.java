package pl.edu.agh.smart_repo.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.ConfigurationFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileTreeFetcherService {

    private final Path userFilesDirectoryPath;

    public FileTreeFetcherService(ConfigurationFactory configurationFactory) {
        this.userFilesDirectoryPath = configurationFactory.getFileCatalogPath();
    }

    public List<File> fetchFileTree(String directoryPath, boolean recursive, String[] extensions){
        String resultPath = Paths.get(userFilesDirectoryPath.toString(), directoryPath).toString();

        List<File> files = (List<File>) FileUtils.listFiles(new File(resultPath), extensions, recursive);
        return files;
    }
}
