package com.ec01.controller;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.exception.PaymentNotificationException;
import com.ec01.mapper.UserMapper;
import com.ec01.security.JwtInterceptor;
import com.ec01.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {
    private PaymentService paymentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        JwtInterceptor interceptor = new JwtInterceptor(
                mock(JwtUtil.class), mock(LoginSessionService.class), mock(UserMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService))
                .addInterceptors(interceptor)
                .build();
    }

    @Test
    void alipayNotifyIsPublicAndReturnsExactSuccessText() throws Exception {
        mockMvc.perform(post("/api/payments/alipay/notify")
                        .param("out_trade_no", "EC001")
                        .param("sign", "signed"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        verify(paymentService).handleAlipayNotify(any());
    }

    @Test
    void rejectedNotificationReturnsExactFailureText() throws Exception {
        doThrow(new PaymentNotificationException("bad signature"))
                .when(paymentService).handleAlipayNotify(any());

        mockMvc.perform(post("/api/payments/alipay/notify").param("sign", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string("failure"));
    }
}
