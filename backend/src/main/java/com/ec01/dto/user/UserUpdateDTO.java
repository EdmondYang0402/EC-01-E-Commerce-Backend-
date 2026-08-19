package com.ec01.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    @Email
    private String email;
    private String phone;
    private String avatarUrl;
}
