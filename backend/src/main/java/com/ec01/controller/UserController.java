package com.ec01.controller;

import com.ec01.common.Result;
import com.ec01.dto.user.ChangePasswordDTO;
import com.ec01.dto.user.UserUpdateDTO;
import com.ec01.exception.UnauthorizedException;
import com.ec01.security.JwtInterceptor;
import com.ec01.security.UserContext;
import com.ec01.service.UserService;
import com.ec01.vo.user.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserProfileVO> getCurrentUser() {
        return Result.success(userService.getProfile());
    }

    @PutMapping("/me")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateProfile(dto);
        return Result.success(null);
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(
            @RequestAttribute(JwtInterceptor.AUTH_SESSION_ID_ATTRIBUTE) String sessionId,
            @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(currentUserId(), sessionId, dto);
        return Result.success(null);
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new UnauthorizedException();
        }
        return userId;
    }
}
