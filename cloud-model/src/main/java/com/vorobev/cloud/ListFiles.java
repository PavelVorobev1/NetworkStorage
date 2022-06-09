package com.vorobev.cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {

    private final List<FileInfo> files;

    public ListFiles(Path path) throws IOException {

        files = Files.list(path).map(FileInfo::new).collect(Collectors.toList());

    }

}
