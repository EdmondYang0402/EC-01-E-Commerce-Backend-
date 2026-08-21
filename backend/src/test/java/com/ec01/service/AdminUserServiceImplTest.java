package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.common.UserStatus;
import com.ec01.dto.admin.user.AdminUserQueryDTO;
import com.ec01.dto.admin.user.UserStatusUpdateDTO;
import com.ec01.entity.User;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.UserMapper;
import com.ec01.security.UserContext;
import com.ec01.service.impl.AdminUserServiceImpl;
import com.ec01.vo.admin.user.AdminUserListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceImplTest {
    private UserMapper userMapper;
    private AdminUserServiceImpl service;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        service = new AdminUserServiceImpl(userMapper);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void userPageUsesFiltersAndDoesNotExposePasswordField() {
        AdminUserQueryDTO dto = new AdminUserQueryDTO();
        dto.setPage(3);
        dto.setSize(20);
        dto.setKeyword(" alice ");
        dto.setStatus(UserStatus.NORMAL);
        User user = new User();
        user.setId(5L);
        user.setStatus(1);
        user.setUsername("alice");
        user.setPassword("encoded-secret");
        user.setStatus(1);
        when(userMapper.selectAdminPage("alice", 1, 40L, 20)).thenReturn(List.of(user));
        when(userMapper.countAdminUsers("alice", 1)).thenReturn(41L);

        PageResult<AdminUserListVO> result = service.getUserPage(dto);

        assertEquals(41L, result.getTotal());
        assertEquals(UserStatus.NORMAL, result.getRecords().getFirst().getStatus());
        assertFalse(Arrays.stream(AdminUserListVO.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("password")));
    }

    @Test
    void statusUpdateOnlyCallsDedicatedMapperMethod() {
        User user = new User();
        user.setId(5L);
        user.setStatus(1);
        when(userMapper.selectById(5L)).thenReturn(user);
        when(userMapper.updateStatus(5L, 0)).thenReturn(1);
        UserStatusUpdateDTO dto = new UserStatusUpdateDTO();
        dto.setStatus(UserStatus.DISABLED);

        service.changeUserStatus(5L, dto);

        verify(userMapper).selectById(5L);
        verify(userMapper).updateStatus(5L, 0);
    }

    @Test
    void administratorCannotDisableOwnAccount() {
        User user = new User();
        user.setId(5L);
        user.setStatus(1);
        when(userMapper.selectById(5L)).thenReturn(user);
        UserStatusUpdateDTO dto = new UserStatusUpdateDTO();
        dto.setStatus(UserStatus.DISABLED);
        UserContext.set(5L);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.changeUserStatus(5L, dto));

        assertEquals(400, exception.getCode());
        verify(userMapper, org.mockito.Mockito.never()).updateStatus(5L, 0);
    }

    @Test
    void missingUserReturnsNotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);
        UserStatusUpdateDTO dto = new UserStatusUpdateDTO();
        dto.setStatus(UserStatus.DISABLED);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.changeUserStatus(99L, dto));

        assertEquals(404, exception.getCode());
    }
}
