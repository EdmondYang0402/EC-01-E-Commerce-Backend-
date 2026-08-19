package com.ec01.service;

import com.ec01.dto.user.ChangePasswordDTO;
import com.ec01.dto.user.UserLoginDTO;
import com.ec01.dto.user.UserRegisterDTO;
import com.ec01.dto.user.UserUpdateDTO;
import com.ec01.vo.user.UserLoginVO;
import com.ec01.vo.user.UserProfileVO;

public interface UserService {

    void register(UserRegisterDTO dto);

    UserLoginVO login(UserLoginDTO dto);

    UserProfileVO getProfile();

    void updateProfile(UserUpdateDTO dto);

    void changePassword(Long userId, String sessionId, ChangePasswordDTO dto);

    void logout(String sessionId);
}
