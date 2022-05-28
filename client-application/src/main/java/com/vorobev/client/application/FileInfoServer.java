package com.vorobev.client.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfoServer {
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

    public FileInfoServer(String fileName,Long sizeFile) {
            this.fileName = fileName;
            this.sizeFile = sizeFile;
    }

}
