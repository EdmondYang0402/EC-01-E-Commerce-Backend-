package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.admin.user.AdminUserQueryDTO;
import com.ec01.dto.admin.user.UserStatusUpdateDTO;
import com.ec01.vo.admin.user.AdminUserListVO;

public interface AdminUserService {
    PageResult<AdminUserListVO> getUserPage(AdminUserQueryDTO dto);

    void changeUserStatus(Long userId, UserStatusUpdateDTO dto);
}
