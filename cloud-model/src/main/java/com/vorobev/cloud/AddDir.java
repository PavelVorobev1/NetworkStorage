package com.vorobev.cloud;

import lombok.Data;

@Data
public class AddDir implements CloudMessage {
    private final String dirName;
}
