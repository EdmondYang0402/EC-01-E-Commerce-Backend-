package com.ec01.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDTO {



    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Size(min = 8,max = 32)
    @Pattern(
            regexp="^(?=.*[A-Za-z])(?=.*\\d).+$"
    )
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmNewPassword;
}
