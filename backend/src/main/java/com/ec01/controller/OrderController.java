package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.order.CreateOrderDTO;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.service.OrderService;
import com.ec01.vo.order.OrderDetailVO;
import com.ec01.vo.order.OrderListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<String> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(dto));
    }

    @GetMapping
    public Result<PageResult<OrderListVO>> getMyOrders(
            @Valid @ModelAttribute PageQueryDTO dto) {
        return Result.success(orderService.getMyOrders(dto));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(orderNo));
    }

    @PatchMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success();
    }
}
