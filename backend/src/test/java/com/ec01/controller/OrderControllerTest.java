package com.ec01.controller;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.common.PageResult;
import com.ec01.common.UserRole;
import com.ec01.entity.User;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.mapper.UserMapper;
import com.ec01.security.JwtInterceptor;
import com.ec01.service.OrderService;
import com.ec01.vo.order.OrderDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private OrderService orderService;
    private JwtUtil jwtUtil;
    private LoginSessionService loginSessionService;
    private UserMapper userMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        jwtUtil = mock(JwtUtil.class);
        loginSessionService = mock(LoginSessionService.class);
        userMapper = mock(UserMapper.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService))
                .addInterceptors(new JwtInterceptor(jwtUtil, loginSessionService, userMapper))
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

    @Test
    void authenticatedUserCanCreateOrderAndReadDetail() throws Exception {
        mockValidToken();
        when(orderService.createOrder(argThat(dto ->
                dto.getCartItemIds().equals(List.of(7L))
                        && "Alice".equals(dto.getReceiverName()))))
                .thenReturn("EC202608200001");
        OrderDetailVO detail = new OrderDetailVO();
        detail.setOrderNo("EC202608200001");
        when(orderService.getOrderDetail("EC202608200001")).thenReturn(detail);

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cartItemIds":[7],"receiverName":"Alice",
                                 "receiverPhone":"13800000000","receiverAddress":"Shanghai"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("EC202608200001"));

        mockMvc.perform(get("/api/orders/EC202608200001")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("EC202608200001"));
    }

    private void mockValidToken() {
        when(jwtUtil.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.parseUserId("good-token")).thenReturn(42L);
        when(jwtUtil.parseSessionId("good-token")).thenReturn("session-1");
        when(loginSessionService.getUserId("session-1")).thenReturn(42L);
        when(userMapper.selectById(42L)).thenReturn(activeUser(42L));
    }

    private User activeUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(1);
        user.setRole(UserRole.USER);
        return user;
    }
}
