package com.ec01.controller;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.common.PageResult;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.security.JwtInterceptor;
import com.ec01.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private OrderService orderService;
    private JwtUtil jwtUtil;
    private LoginSessionService loginSessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        jwtUtil = mock(JwtUtil.class);
        loginSessionService = mock(LoginSessionService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService))
                .addInterceptors(new JwtInterceptor(jwtUtil, loginSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void orderListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        verifyNoInteractions(orderService);
    }

    @Test
    void authenticatedUserCanQueryOrderPage() throws Exception {
        mockValidToken();
        when(orderService.getMyOrders(argThat(dto ->
                dto.getPage() == 2 && dto.getSize() == 10)))
                .thenReturn(new PageResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(orderService).getMyOrders(argThat(dto ->
                dto.getPage() == 2 && dto.getSize() == 10));
    }

    @Test
    void invalidPageSizeReturnsBadRequest() throws Exception {
        mockValidToken();

        mockMvc.perform(get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    private void mockValidToken() {
        when(jwtUtil.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.parseUserId("good-token")).thenReturn(42L);
        when(jwtUtil.parseSessionId("good-token")).thenReturn("session-1");
        when(loginSessionService.getUserId("session-1")).thenReturn(42L);
    }
}
