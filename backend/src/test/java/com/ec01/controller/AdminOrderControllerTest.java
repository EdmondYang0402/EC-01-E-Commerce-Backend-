package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.service.AdminOrderService;
import com.ec01.vo.admin.order.AdminOrderDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class AdminOrderControllerTest {
    private AdminOrderService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AdminOrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminOrderController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void exposesOrderPageAndDetailQueries() throws Exception {
        when(service.getOrderPage(argThat(dto ->
                dto.getPage() == 2 && dto.getSize() == 10 && dto.getUserId() == 42L)))
                .thenReturn(new PageResult<>(List.of(), 0));
        AdminOrderDetailVO detail = new AdminOrderDetailVO();
        detail.setOrderNo("EC-01-7");
        when(service.getOrderDetail("EC-01-7")).thenReturn(detail);

        mockMvc.perform(get("/api/admin/orders")
                        .param("page", "2")
                        .param("size", "10")
                        .param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(get("/api/admin/orders/EC-01-7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("EC-01-7"));

        verify(service).getOrderDetail("EC-01-7");
    }

    @Test
    void rejectsOversizedPageBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(service);
    }
}
