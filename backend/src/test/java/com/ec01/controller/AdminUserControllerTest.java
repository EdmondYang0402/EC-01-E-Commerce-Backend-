package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.exception.GlobalExceptionHandler;
import com.ec01.service.AdminUserService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {
    private AdminUserService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AdminUserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void exposesUserPageAndStatusUpdate() throws Exception {
        when(service.getUserPage(argThat(dto ->
                dto.getPage() == 2 && dto.getSize() == 20 && "alice".equals(dto.getKeyword()))))
                .thenReturn(new PageResult<>(List.of(), 0));

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "2")
                        .param("keyword", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(patch("/api/admin/users/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());

        verify(service).changeUserStatus(eq(5L), argThat(dto ->
                dto.getStatus().name().equals("DISABLED")));
    }

    @Test
    void rejectsMissingStatusBeforeServiceCall() throws Exception {
        mockMvc.perform(patch("/api/admin/users/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(service);
    }
}
