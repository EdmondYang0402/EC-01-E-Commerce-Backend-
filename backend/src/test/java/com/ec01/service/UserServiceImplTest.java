package com.ec01.service;

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
import com.ec01.service.impl.UserServiceImpl;
import com.ec01.vo.user.UserLoginVO;
import com.ec01.vo.user.UserProfileVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginSessionService loginSessionService;
    @Mock
    private JwtUtil jwtUtil;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder, loginSessionService, jwtUtil);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void registerCreatesNormalUserWithEncodedPassword() {
        UserRegisterDTO dto = registerDto("alice", "Password123");
        when(userMapper.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("alice", captor.getValue().getUsername());
        assertEquals("encoded", captor.getValue().getPassword());
        assertNotEquals("Password123", captor.getValue().getPassword());
        assertEquals(1, captor.getValue().getStatus());
        assertEquals(UserRole.USER, captor.getValue().getRole());
        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void registerRejectsBlankUsername() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerDto(" ", "Password123")));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(userMapper);
    }

    @Test
    void registerRejectsBlankPassword() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerDto("alice", " ")));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(userMapper);
    }

    @Test
    void registerRejectsMismatchedConfirmation() {
        UserRegisterDTO dto = registerDto("alice", "Password123");
        dto.setConfirmPassword("Different123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(dto));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(userMapper);
    }

    @Test
    void registerRejectsDuplicateUsernameAsConflict() {
        UserRegisterDTO dto = registerDto("alice", "Password123");
        when(userMapper.existsByUsername("alice")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(dto));

        assertEquals(409, exception.getCode());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void registerRejectsInvalidPasswordFormat() {
        UserRegisterDTO dto = registerDto("alice", "onlyletters");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(dto));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(userMapper);
    }

    @Test
    void loginCreatesSessionAndJwt() {
        User user = user(7L, 1, "encoded");
        UserLoginDTO dto = loginDto("alice", "Password123");
        when(userMapper.selectByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("Password123", "encoded")).thenReturn(true);
        when(loginSessionService.createSession(7L)).thenReturn("session-1");
        when(jwtUtil.generateToken(7L, "session-1")).thenReturn("jwt-token");

        UserLoginVO result = userService.login(dto);

        assertEquals(7L, result.getUserId());
        assertEquals("jwt-token", result.getToken());
        assertEquals(UserRole.USER, result.getRole());
        verify(loginSessionService).createSession(7L);
        verify(jwtUtil).generateToken(7L, "session-1");
    }

    @Test
    void loginRejectsWrongPasswordAsUnauthorized() {
        User user = user(7L, 1, "encoded");
        when(userMapper.selectByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDto("alice", "wrong")));

        assertEquals(401, exception.getCode());
        verify(loginSessionService, never()).createSession(anyLong());
    }

    @Test
    void loginRejectsMissingUserAsUnauthorized() {
        when(userMapper.selectByUsername("missing")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDto("missing", "Password123")));

        assertEquals(401, exception.getCode());
        verifyNoInteractions(passwordEncoder, loginSessionService, jwtUtil);
    }

    @Test
    void loginRejectsDisabledUserAsForbidden() {
        User user = user(7L, 0, "encoded");
        when(userMapper.selectByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("Password123", "encoded")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDto("alice", "Password123")));

        assertEquals(403, exception.getCode());
        verify(loginSessionService, never()).createSession(anyLong());
    }

    @Test
    void getProfileUsesUserContextAndReturnsNoPassword() {
        User user = user(7L, 1, "encoded");
        user.setUsername("alice");
        user.setNickname("Alice");
        user.setEmail("alice@example.com");
        user.setPhone("13800138000");
        user.setAvatarUrl("https://example.com/avatar.png");
        UserContext.set(7L);
        when(userMapper.selectById(7L)).thenReturn(user);

        UserProfileVO profile = userService.getProfile();

        assertEquals(7L, profile.getId());
        assertEquals("alice", profile.getUsername());
        assertEquals("Alice", profile.getNickname());
        assertEquals("alice@example.com", profile.getEmail());
        assertEquals("13800138000", profile.getPhone());
        assertEquals("https://example.com/avatar.png", profile.getAvatarUrl());
        assertEquals(UserRole.USER, profile.getRole());
        assertThrows(NoSuchFieldException.class,
                () -> UserProfileVO.class.getDeclaredField("password"));
        verify(userMapper).selectById(7L);
    }

    @Test
    void updateProfileOnlyUpdatesAllowedFields() {
        User user = user(7L, 1, "encoded");
        user.setUsername("alice");
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname("New name");
        dto.setEmail("new@example.com");
        dto.setPhone("123");
        dto.setAvatarUrl("https://example.com/avatar.png");
        UserContext.set(7L);
        when(userMapper.selectById(7L)).thenReturn(user);
        when(userMapper.updateProfile(user)).thenReturn(1);

        userService.updateProfile(dto);

        assertEquals("alice", user.getUsername());
        assertEquals("encoded", user.getPassword());
        assertEquals(1, user.getStatus());
        assertEquals("New name", user.getNickname());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("123", user.getPhone());
        assertEquals("https://example.com/avatar.png", user.getAvatarUrl());
        verify(userMapper).updateProfile(user);
    }

    @Test
    void updateProfileCannotTargetAnotherUser() {
        User currentUser = user(7L, 1, "encoded");
        UserContext.set(7L);
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname("Current user only");
        when(userMapper.selectById(7L)).thenReturn(currentUser);
        when(userMapper.updateProfile(currentUser)).thenReturn(1);

        userService.updateProfile(dto);

        verify(userMapper).selectById(7L);
        verify(userMapper, never()).selectById(8L);
        verify(userMapper).updateProfile(currentUser);
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        User user = user(7L, 1, "encoded-old");
        when(userMapper.selectById(7L)).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.changePassword(7L, "session-1",
                        changePasswordDto("wrong", "NewPassword123")));

        assertEquals(400, exception.getCode());
        verify(userMapper, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void changePasswordUpdatesHashAndInvalidatesCurrentSession() {
        User user = user(7L, 1, "encoded-old");
        ChangePasswordDTO dto = changePasswordDto("OldPassword123", "NewPassword123");
        when(userMapper.selectById(7L)).thenReturn(user);
        when(passwordEncoder.matches("OldPassword123", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encoded-new");
        when(userMapper.updatePassword(7L, "encoded-new")).thenReturn(1);

        userService.changePassword(7L, "session-1", dto);

        verify(userMapper).updatePassword(7L, "encoded-new");
        verify(loginSessionService).deleteSession("session-1");
    }

    @Test
    void logoutInvalidatesOnlyCurrentSession() {
        userService.logout("session-1");

        verify(loginSessionService).deleteSession("session-1");
        verifyNoMoreInteractions(loginSessionService);
    }

    private UserRegisterDTO registerDto(String username, String password) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setConfirmPassword(password);
        return dto;
    }

    private UserLoginDTO loginDto(String username, String password) {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private ChangePasswordDTO changePasswordDto(String oldPassword, String newPassword) {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmNewPassword(newPassword);
        return dto;
    }

    private User user(Long id, int status, String password) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setPassword(password);
        user.setRole(UserRole.USER);
        return user;
    }
}
