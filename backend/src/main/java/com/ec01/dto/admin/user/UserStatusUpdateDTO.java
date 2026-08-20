package com.ec01.dto.admin.user;

import com.ec01.common.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateDTO {
    @NotNull(message = "用户状态不能为空")
    private UserStatus status;
}
