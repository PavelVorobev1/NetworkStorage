package com.vorobev.cloud;

import lombok.Data;


@Data
public class PathInRequest implements CloudMessage {

    String path;

    public PathInRequest(String path) {
        this.path = path;
    }
}
