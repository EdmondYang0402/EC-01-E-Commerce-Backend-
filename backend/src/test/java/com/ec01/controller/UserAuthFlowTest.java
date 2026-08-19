package com.ec01.controller;

import com.ec01.auth.AuthService;
import com.ec01.auth.AuthServiceImpl;
import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.security.JwtInterceptor;
import com.ec01.security.UserContext;
import com.ec01.service.UserService;
import com.ec01.vo.user.UserProfileVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAuthFlowTest {

    private JwtUtil jwtUtil;
    private LoginSessionService loginSessionService;
    private UserService userService;
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        loginSessionService = mock(LoginSessionService.class);
        userService = mock(UserService.class);
        authService = new AuthServiceImpl(userService);
        JwtInterceptor interceptor = new JwtInterceptor(jwtUtil, loginSessionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService), new AuthController(authService))
                .addInterceptors(interceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void validTokenCanAccessCurrentUser() throws Exception {
        mockValidToken();
        UserProfileVO profile = new UserProfileVO();
        profile.setId(7L);
        profile.setUsername("alice");
        when(userService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.username").value("alice"));

        verify(userService).getProfile();
        assertNull(UserContext.get());
    }

    @Test
    void missingTokenCannotAccessCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(userService);
        assertNull(UserContext.get());
    }

    @Test
    void validTokenCanUpdateCurrentUserProfile() throws Exception {
        mockValidToken();

        mockMvc.perform(put("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"Alice",
                                  "email":"alice@example.com",
                                  "phone":"13800138000",
                                  "avatarUrl":"https://example.com/avatar.png"
                                }
                                """))
                .andExpect(status().isOk());

        verify(userService).updateProfile(argThat(dto ->
                "Alice".equals(dto.getNickname())
                        && "alice@example.com".equals(dto.getEmail())
                        && "13800138000".equals(dto.getPhone())
                        && "https://example.com/avatar.png".equals(dto.getAvatarUrl())));
        assertNull(UserContext.get());
    }

    @Test
    void logoutMakesTheSameTokenUnauthorized() throws Exception {
        AtomicBoolean active = new AtomicBoolean(true);
        when(jwtUtil.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.parseUserId("good-token")).thenReturn(7L);
        when(jwtUtil.parseSessionId("good-token")).thenReturn("session-1");
        when(loginSessionService.getUserId("session-1"))
                .thenAnswer(invocation -> active.get() ? 7L : null);
        doAnswer(invocation -> {
            active.set(false);
            return null;
        }).when(userService).logout("session-1");

        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void registerEndpointIsPublicAndValidatesRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"Password123","confirmPassword":"Password123"}
                                """))
                .andExpect(status().isOk());

        verify(userService).register(any());
    }

    private void mockValidToken() {
        when(jwtUtil.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.parseUserId("good-token")).thenReturn(7L);
        when(jwtUtil.parseSessionId("good-token")).thenReturn("session-1");
        when(loginSessionService.getUserId("session-1")).thenReturn(7L);
    }
}
