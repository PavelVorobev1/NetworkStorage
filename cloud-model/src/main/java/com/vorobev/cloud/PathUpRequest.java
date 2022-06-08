package com.vorobev.cloud;

import lombok.Data;

@Data
public class PathUpRequest implements CloudMessage {

    private final String path;

}
