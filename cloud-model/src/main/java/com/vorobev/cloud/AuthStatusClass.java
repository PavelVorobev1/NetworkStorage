package com.vorobev.cloud;

import lombok.Data;

@Data
public class AuthStatusClass implements CloudMessage{
   private final boolean status;
}
