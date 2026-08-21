package com.ec01.vo.user;

import com.ec01.common.UserRole;
import lombok.Data;

@Data
public class UserLoginVO {
    private Long userId;
    private String token;
    private UserRole role;
}
