package pl.edu.agh.smart_repo.common.file;

import lombok.Data;

@Data
public class FileInfo {
    private String name;
    private long creationDate;
    private String extension;
    private boolean isDirectory;
    private long size;

    public FileInfo(String name, long creationDate, boolean isDirectory, long size) {
        this.creationDate = creationDate;
        this.isDirectory = isDirectory;
        this.name = name;
        this.size = size;
    }
}
