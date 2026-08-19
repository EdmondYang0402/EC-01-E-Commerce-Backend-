package com.ec01.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
}
