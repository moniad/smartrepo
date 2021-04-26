package pl.edu.agh.smart_repo.common.file;

import lombok.Data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

@Data
public class FileInfo {
    private String name;
    private long createDate;
    private String extension;
    private boolean isDirectory;
    private long size;


    public FileInfo(String name, long createDate, boolean isDirectory, long size){
        this.createDate = createDate;
        this.isDirectory = isDirectory;
        this.name = name;
        this.size = size;
    }
}
