package com.vorobev.cloud;

import lombok.Data;

@Data
public class CreateNewDir implements CloudMessage {
    private final String dirName;
}
