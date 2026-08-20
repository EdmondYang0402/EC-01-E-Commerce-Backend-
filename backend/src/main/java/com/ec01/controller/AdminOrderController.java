package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.admin.order.AdminOrderQueryDTO;
import com.ec01.service.AdminOrderService;
import com.ec01.vo.admin.order.AdminOrderDetailVO;
import com.ec01.vo.admin.order.AdminOrderListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    @GetMapping
    public Result<PageResult<AdminOrderListVO>> getOrderPage(
            @Valid @ModelAttribute AdminOrderQueryDTO dto) {
        return Result.success(adminOrderService.getOrderPage(dto));
    }

    @GetMapping("/{orderNo}")
    public Result<AdminOrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        return Result.success(adminOrderService.getOrderDetail(orderNo));
    }
}
