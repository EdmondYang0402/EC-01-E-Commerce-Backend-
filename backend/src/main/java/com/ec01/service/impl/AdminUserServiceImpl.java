package com.ec01.service.impl;

import com.ec01.common.PageResult;
import com.ec01.common.UserStatus;
import com.ec01.dto.admin.user.AdminUserQueryDTO;
import com.ec01.dto.admin.user.UserStatusUpdateDTO;
import com.ec01.entity.User;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.UserMapper;
import com.ec01.security.UserContext;
import com.ec01.service.AdminUserService;
import com.ec01.vo.admin.user.AdminUserListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private final UserMapper userMapper;

    @Override
    public PageResult<AdminUserListVO> getUserPage(AdminUserQueryDTO dto) {
        validatePage(dto);
        String keyword = normalize(dto.getKeyword());
        Integer status = dto.getStatus() == null ? null : (int) dto.getStatus().getCode();
        long offset = (long) (dto.getPage() - 1) * dto.getSize();

        List<AdminUserListVO> records = userMapper.selectAdminPage(
                        keyword, status, offset, dto.getSize())
                .stream()
                .map(this::toListVO)
                .toList();
        long total = userMapper.countAdminUsers(keyword, status);
        return new PageResult<>(records, total);
    }

    @Override
    public void changeUserStatus(Long userId, UserStatusUpdateDTO dto) {
        if (userId == null || userId <= 0 || dto == null || dto.getStatus() == null) {
            throw new BusinessException(400, "用户状态参数不合法");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        Long currentUserId = UserContext.get();
        if (dto.getStatus() == UserStatus.DISABLED && userId.equals(currentUserId)) {
            throw new BusinessException(400, "管理员不能禁用自己的账号");
        }
        if (user.getStatus() == dto.getStatus().getCode()) {
            return;
        }
        if (userMapper.updateStatus(userId, dto.getStatus().getCode()) <= 0) {
            throw new BusinessException(500, "用户状态更新失败");
        }
    }

    private AdminUserListVO toListVO(User user) {
        AdminUserListVO vo = new AdminUserListVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(toUserStatus(user.getStatus()));
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    private void validatePage(AdminUserQueryDTO dto) {
        if (dto == null || dto.getPage() == null || dto.getSize() == null
                || dto.getPage() < 1 || dto.getSize() < 1 || dto.getSize() > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UserStatus toUserStatus(int code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new BusinessException(500, "用户状态数据异常");
    }
}
