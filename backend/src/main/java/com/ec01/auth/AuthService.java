package com.ec01.auth;

import com.ec01.dto.user.UserLoginDTO;
import com.ec01.dto.user.UserRegisterDTO;
import com.ec01.vo.user.UserLoginVO;

public interface AuthService {

    void register(UserRegisterDTO dto);

    UserLoginVO login(UserLoginDTO dto);

    void logout(String sessionId);
}
