package com.ec01.controller;

import com.ec01.auth.AuthService;
import com.ec01.common.Result;
import com.ec01.dto.user.UserLoginDTO;
import com.ec01.dto.user.UserRegisterDTO;
import com.ec01.security.JwtInterceptor;
import com.ec01.vo.user.UserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        authService.register(dto);
        return Result.success(null);
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestAttribute(JwtInterceptor.AUTH_SESSION_ID_ATTRIBUTE) String sessionId) {
        authService.logout(sessionId);
        return Result.success(null);
    }
}
