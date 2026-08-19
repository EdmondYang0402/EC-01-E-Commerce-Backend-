package com.ec01.controller;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.common.PageResult;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.security.JwtInterceptor;
import com.ec01.service.ProductService;
import com.ec01.vo.product.ProductDetailVO;
import com.ec01.vo.product.ProductListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    private ProductService productService;
    private JwtUtil jwtUtil;
    private LoginSessionService loginSessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        jwtUtil = mock(JwtUtil.class);
        loginSessionService = mock(LoginSessionService.class);
        JwtInterceptor interceptor = new JwtInterceptor(jwtUtil, loginSessionService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService))
                .addInterceptors(interceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listEndpointIsPublicAndReturnsPage() throws Exception {
        ProductListVO product = new ProductListVO();
        product.setId(1L);
        product.setName("Chair");
        product.setMinPrice(new BigDecimal("499.00"));
        when(productService.getProductPage(any()))
                .thenReturn(new PageResult<>(List.of(product), 1L));

        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "20")
                        .param("keyword", "chair"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].minPrice").value(499.00));

        verifyNoInteractions(jwtUtil, loginSessionService);
    }

    @Test
    void detailEndpointIsPublicAndReturnsProduct() throws Exception {
        ProductDetailVO product = new ProductDetailVO();
        product.setId(1L);
        product.setName("Chair");
        when(productService.getProductDetail(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Chair"));

        verifyNoInteractions(jwtUtil, loginSessionService);
    }

    @Test
    void invalidPaginationReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/products").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(productService);
    }
}
