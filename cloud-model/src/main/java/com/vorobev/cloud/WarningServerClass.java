package com.vorobev.cloud;

import lombok.Data;

@Data
public class WarningServerClass implements CloudMessage {

    private final String warning;

}
