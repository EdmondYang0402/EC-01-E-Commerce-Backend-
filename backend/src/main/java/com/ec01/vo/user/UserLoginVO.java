package com.ec01.vo.user;

import lombok.Data;

@Data
public class UserLoginVO {
    private Long userId;
    private String token;
}
