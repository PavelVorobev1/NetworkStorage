package com.vorobev.client.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    private String fileName;
    private long sizeFile;

    public String getFileName() {
        return fileName;
    }

    public long getSizeFile() {
        return sizeFile;
    }

//    public FileInfo(String fileName, long l) {
//        this.fileName = fileName;
//        this.sizeFile = sizeFile;
//    }

    public FileInfo(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.sizeFile = Files.size(path);
        } catch (IOException e) {
            System.err.println("Не удалось получить информацию о файле");
            e.printStackTrace();
        }
    }
}
