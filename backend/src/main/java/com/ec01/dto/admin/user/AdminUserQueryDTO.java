package com.ec01.dto.admin.user;

import com.ec01.common.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserQueryDTO {
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 20;

    @Size(max = 100, message = "搜索关键词不能超过100个字符")
    private String keyword;

    private UserStatus status;
}
