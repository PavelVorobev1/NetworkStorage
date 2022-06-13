package com.vorobev.cloud;

import lombok.Data;

@Data
public class UserInfo implements CloudMessage {
    private final String login;
    private final String password;
}
