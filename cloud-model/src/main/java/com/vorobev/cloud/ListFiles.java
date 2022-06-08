package com.vorobev.cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {

    private final List<FileInfo> files = new ArrayList<>();

    public ListFiles(Path path) throws IOException {
        List<FileInfo> info;
        info =  Files.list(path).map(FileInfo::new).collect(Collectors.toList());
        for (FileInfo fileInfo : info) {
            files.add(new FileInfo(fileInfo.getFileName(),fileInfo.getSizeFile()));
        }
    }

}
