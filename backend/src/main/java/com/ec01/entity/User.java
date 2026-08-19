package com.ec01.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private int status;   //1正常 0异常
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
