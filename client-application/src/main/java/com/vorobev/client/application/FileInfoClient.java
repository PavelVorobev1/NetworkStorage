package com.vorobev.client.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfoClient {
    private String fileName;
    private long sizeFile;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSizeFile() {
        return sizeFile;
    }

    public void setSizeFile(long sizeFile) {
        this.sizeFile = sizeFile;
    }

    public FileInfoClient(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.sizeFile = Files.size(path);
        } catch (IOException e) {
            System.err.println("Не удалось получить информацию о файле");
            e.printStackTrace();
        }
    }
}
