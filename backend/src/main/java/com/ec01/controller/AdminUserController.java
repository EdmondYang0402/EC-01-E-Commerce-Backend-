package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.admin.user.AdminUserQueryDTO;
import com.ec01.dto.admin.user.UserStatusUpdateDTO;
import com.ec01.service.AdminUserService;
import com.ec01.vo.admin.user.AdminUserListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<AdminUserListVO>> getUserPage(
            @Valid @ModelAttribute AdminUserQueryDTO dto) {
        return Result.success(adminUserService.getUserPage(dto));
    }

    @PatchMapping("/{userId}/status")
    public Result<Void> changeUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateDTO dto) {
        adminUserService.changeUserStatus(userId, dto);
        return Result.success(null);
    }
}
