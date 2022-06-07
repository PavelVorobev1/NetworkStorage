package com.vorobev.client.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    private String fileName;
    private long sizeFile;
    private Path path;

    public String getFileName() {
        return fileName;
    }

    public long getSizeFile() {
        return sizeFile;
    }

    public FileInfo(String fileName, long l) {
        this.fileName = fileName;
        this.sizeFile = sizeFile;
    }

    public FileInfo(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }

    public FileInfo(Path path) {
        try {
            this.path = path;
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.sizeFile = -1L;
            } else {
                this.sizeFile = Files.size(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось получить информацию о файле");
        }
    }

    public boolean isDir() {
        return Files.isDirectory(path);
    }
}
