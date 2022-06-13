package com.vorobev.cloud;

import lombok.Data;

@Data
public class RegUser implements CloudMessage {
    private final String regLogin;
    private final String regPassword;
}

