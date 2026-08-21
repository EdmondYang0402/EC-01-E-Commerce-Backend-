package com.ec01.service.impl;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.common.UserRole;
import com.ec01.dto.user.ChangePasswordDTO;
import com.ec01.dto.user.UserLoginDTO;
import com.ec01.dto.user.UserRegisterDTO;
import com.ec01.dto.user.UserUpdateDTO;
import com.ec01.entity.User;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.UserMapper;
import com.ec01.security.UserContext;
import com.ec01.service.UserService;
import com.ec01.vo.user.UserLoginVO;
import com.ec01.vo.user.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionService loginSessionService;
    private final JwtUtil jwtUtil;

    @Override
    public void register(UserRegisterDTO dto) {

        if (dto == null
                || dto.getUsername() == null
                || dto.getUsername().isBlank()
                || dto.getPassword() == null
                || dto.getPassword().isBlank()) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }
        if (dto.getPassword().length() < 8 || dto.getPassword().length() > 32) {
            throw new BusinessException(400, "密码长度必须在8到32位之间");
        }

        if (!dto.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new BusinessException(400, "密码必须同时包含字母和数字");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(400, "两次密码不一致");
        }

        if (userMapper.existsByUsername(dto.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setRole(UserRole.USER);

        try {
            if (userMapper.insert(user) <= 0) {
                throw new BusinessException(500, "注册失败");
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(409, "用户名已存在");
        }
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        if (dto == null
                || dto.getUsername() == null
                || dto.getUsername().isBlank()
                || dto.getPassword() == null
                || dto.getPassword().isBlank()) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }
        User user = userMapper.selectByUsername(dto.getUsername());

        if(user==null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new BusinessException(401, "用户名或密码错误");
        }
        if(user.getStatus() != 1){
            throw new BusinessException(403, "状态异常，禁止登录");
        }

        String sessionId = loginSessionService.createSession(user.getId());
        String token = jwtUtil.generateToken(user.getId(),sessionId);

        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(user.getId());
        userLoginVO.setToken(token);
        userLoginVO.setRole(user.getRole());
        return userLoginVO;
    }

    @Override
    public UserProfileVO getProfile() {
        Long userId = currentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserProfileVO profile = new UserProfileVO();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setNickname(user.getNickname());
        profile.setEmail(user.getEmail());
        profile.setPhone(user.getPhone());
        profile.setAvatarUrl(user.getAvatarUrl());
        profile.setRole(user.getRole());
        return profile;
    }

    @Override
    public void updateProfile(UserUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "用户资料不能为空");
        }
        Long userId = currentUserId();
        User user = userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException(404, "用户不存在");
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatarUrl(dto.getAvatarUrl());
        if (userMapper.updateProfile(user) <= 0) {
            throw new BusinessException(500, "用户资料更新失败");
        }
    }

    @Override
    public void changePassword(Long userId, String sessionId, ChangePasswordDTO dto) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(401, "当前登录会话无效");
        }
        if (dto == null) {
            throw new BusinessException(400, "密码参数不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if(!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new BusinessException(400, "旧密码错误");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new BusinessException(400, "新密码不能为空");
        }
        if (dto.getNewPassword().length() < 8 || dto.getNewPassword().length() > 32) {
            throw new BusinessException(400, "密码长度必须在8到32位之间");
        }

        if (!dto.getNewPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new BusinessException(400, "密码必须同时包含字母和数字");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BusinessException(400, "两次密码不一致");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException(400, "新密码不能与旧密码相同");
        }
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        if (userMapper.updatePassword(userId, encodedPassword) <= 0) {
            throw new BusinessException(500, "密码修改失败");
        }
        loginSessionService.deleteSession(sessionId);
    }

    @Override
    public void logout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(401, "当前登录会话无效");
        }
        loginSessionService.deleteSession(sessionId);
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        return userId;
    }
}
