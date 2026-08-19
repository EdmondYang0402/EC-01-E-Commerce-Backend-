package com.ec01.auth;

import com.ec01.dto.user.UserLoginDTO;
import com.ec01.dto.user.UserRegisterDTO;
import com.ec01.service.UserService;
import com.ec01.vo.user.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public void register(UserRegisterDTO dto) {
        userService.register(dto);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        return userService.login(dto);
    }

    @Override
    public void logout(String sessionId) {
        userService.logout(sessionId);
    }
}
