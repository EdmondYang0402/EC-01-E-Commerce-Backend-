package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.service.AdminProductService;
import com.ec01.vo.admin.product.AdminProductDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminProductControllerTest {
    private AdminProductService adminProductService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminProductService = mock(AdminProductService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(adminProductService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void exposesAdminProductAndSkuRoutes() throws Exception {
        when(adminProductService.getProductPage(argThat(dto ->
                dto.getPage() == 2 && dto.getSize() == 20)))
                .thenReturn(new PageResult<>(List.of(), 0));
        AdminProductDetailVO detail = new AdminProductDetailVO();
        detail.setId(9L);
        when(adminProductService.getProductDetail(9L)).thenReturn(detail);
        when(adminProductService.createProduct(argThat(dto -> "Chair".equals(dto.getName()))))
                .thenReturn(9L);
        when(adminProductService.addSku(eq(9L), argThat(dto -> "CHAIR-BLACK".equals(dto.getSkuCode()))))
                .thenReturn(11L);

        mockMvc.perform(get("/api/admin/products").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(get("/api/admin/products/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(9));
        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Chair\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(9));
        mockMvc.perform(put("/api/admin/products/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated chair\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/admin/products/9/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ON_SHELF\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/products/9/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skuBody("CHAIR-BLACK")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(11));
        mockMvc.perform(put("/api/admin/skus/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"specJson":"{\\"color\\":\\"blue\\"}",
                                 "price":109.50,"stock":8}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/admin/skus/11/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());

        verify(adminProductService).updateProduct(eq(9L), argThat(dto ->
                "Updated chair".equals(dto.getName())));
        verify(adminProductService).changeStatus(eq(9L), argThat(dto ->
                dto.getStatus().name().equals("ON_SHELF")));
        verify(adminProductService).updateSku(eq(11L), argThat(dto ->
                "{\"color\":\"blue\"}".equals(dto.getSpecJson())));
        verify(adminProductService).changeSkuStatus(eq(11L), argThat(dto ->
                dto.getStatus().name().equals("DISABLED")));
    }

    @Test
    void rejectsInvalidSkuBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/admin/products/9/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuCode":"CHAIR","specJson":"{}","price":-1,"stock":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(adminProductService);
    }

    private String skuBody(String skuCode) {
        return """
                {"skuCode":"%s","specJson":"{\\"color\\":\\"black\\"}",
                 "price":99.50,"stock":10}
                """.formatted(skuCode);
    }
}
