package com.ec01.controller;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.security.JwtInterceptor;
import com.ec01.service.CartService;
import com.ec01.vo.cart.CartItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest {

    private CartService cartService;
    private JwtUtil jwtUtil;
    private LoginSessionService loginSessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        jwtUtil = mock(JwtUtil.class);
        loginSessionService = mock(LoginSessionService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CartController(cartService))
                .addInterceptors(new JwtInterceptor(jwtUtil, loginSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void cartRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        verifyNoInteractions(cartService);
    }

    @Test
    void authenticatedUserCanGetCart() throws Exception {
        mockValidToken();
        CartItemVO item = new CartItemVO();
        item.setCartItemId(1L);
        item.setProductName("Chair");
        when(cartService.getMyCart()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/cart").header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].cartItemId").value(1))
                .andExpect(jsonPath("$.data[0].productName").value("Chair"));
    }

    @Test
    void authenticatedUserCanAddUpdateAndDeleteCartItem() throws Exception {
        mockValidToken();
        mockMvc.perform(post("/api/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\":10,\"quantity\":2}"))
                .andExpect(status().isOk());
        verify(cartService).addCartItem(argThat(dto -> dto.getSkuId() == 10L && dto.getQuantity() == 2));

        mockMvc.perform(put("/api/cart/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3,\"selected\":0}"))
                .andExpect(status().isOk());
        verify(cartService).updateCartItem(eq(1L), argThat(dto ->
                dto.getQuantity() == 3 && dto.getSelected() == 0));

        mockMvc.perform(delete("/api/cart/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk());
        verify(cartService).deleteCartItem(1L);
    }

    @Test
    void invalidAddRequestReturnsBadRequest() throws Exception {
        mockValidToken();
        mockMvc.perform(post("/api/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\":10,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(cartService);
    }

    private void mockValidToken() {
        when(jwtUtil.validateToken("good-token")).thenReturn(true);
        when(jwtUtil.parseUserId("good-token")).thenReturn(7L);
        when(jwtUtil.parseSessionId("good-token")).thenReturn("session-1");
        when(loginSessionService.getUserId("session-1")).thenReturn(7L);
    }
}
