package com.vorobev.cloud;

import lombok.Data;

import java.nio.file.Path;

@Data
public class PathUpRequest implements CloudMessage {

    private final String path;

}
