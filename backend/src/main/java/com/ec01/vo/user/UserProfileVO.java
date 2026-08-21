package com.ec01.vo.user;

import com.ec01.common.UserRole;
import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private UserRole role;
}
