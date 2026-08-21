package com.ec01.vo.admin.user;

import com.ec01.common.UserRole;
import com.ec01.common.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private UserStatus status;
    private UserRole role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
