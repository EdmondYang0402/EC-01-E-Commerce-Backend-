package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.order.CreateOrderDTO;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.service.OrderService;
import com.ec01.service.PaymentService;
import com.ec01.vo.order.OrderDetailVO;
import com.ec01.vo.order.OrderListVO;
import com.ec01.vo.payment.PaymentCreateVO;
import com.ec01.vo.payment.PaymentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

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

    @PostMapping("/{orderNo}/payment")
    public Result<PaymentCreateVO> createPayment(@PathVariable String orderNo) {
        return Result.success(paymentService.createPayment(orderNo));
    }

    @GetMapping("/{orderNo}/payment")
    public Result<PaymentVO> getPayment(@PathVariable String orderNo) {
        return Result.success(paymentService.getPaymentByOrderNo(orderNo));
    }

    @PatchMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Result.success();
    }

    @PatchMapping("/{orderId}/receive")
    public Result<Void> confirmReceive(@PathVariable Long orderId) {
        orderService.confirmReceive(orderId);
        return Result.success();
    }
}
