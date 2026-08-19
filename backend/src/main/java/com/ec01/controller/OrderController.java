package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.service.OrderService;
import com.ec01.vo.order.OrderListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderListVO>> getMyOrders(
            @Valid @ModelAttribute PageQueryDTO dto) {
        return Result.success(orderService.getMyOrders(dto));
    }
}
