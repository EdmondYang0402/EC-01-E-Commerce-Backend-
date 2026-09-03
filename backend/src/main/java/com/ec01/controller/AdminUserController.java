package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.admin.user.AdminUserQueryDTO;
import com.ec01.dto.admin.user.UpdatePasswordDTO;
import com.ec01.dto.admin.user.UserStatusUpdateDTO;
import com.ec01.exception.UnauthorizedException;
import com.ec01.security.JwtInterceptor;
import com.ec01.security.UserContext;
import com.ec01.service.AdminUserService;
import com.ec01.vo.admin.user.AdminUserListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/me/password")
    public Result<Void> updatePassword(
            @RequestAttribute(JwtInterceptor.AUTH_SESSION_ID_ATTRIBUTE) String sessionId,
            @Valid @RequestBody UpdatePasswordDTO dto
    ) {
        adminUserService.updatePassword(
                currentAdminId(),
                sessionId,
                dto
        );

        return Result.success();
    }

    private Long currentAdminId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new UnauthorizedException();
        }
        return userId;
    }
}
