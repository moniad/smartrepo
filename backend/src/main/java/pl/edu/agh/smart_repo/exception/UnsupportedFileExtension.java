package pl.edu.agh.smart_repo.exception;

public class UnsupportedFileExtension extends RuntimeException {

    public UnsupportedFileExtension(String extension) {
        super("Text have not been handled correctly by indexer. Probably it has not been initialized properly in file system.");
    }
}
