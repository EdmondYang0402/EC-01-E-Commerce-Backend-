package com.ec01.service.impl;

import com.ec01.common.OrderStatus;
import com.ec01.common.PageResult;
import com.ec01.dto.admin.order.AdminOrderQueryDTO;
import com.ec01.entity.Order;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.OrderItemMapper;
import com.ec01.mapper.OrderMapper;
import com.ec01.service.AdminOrderService;
import com.ec01.vo.admin.order.AdminOrderDetailVO;
import com.ec01.vo.admin.order.AdminOrderListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public PageResult<AdminOrderListVO> getOrderPage(AdminOrderQueryDTO dto) {
        validatePage(dto);
        String orderNo = normalize(dto.getOrderNo());
        Byte status = dto.getStatus() == null ? null : dto.getStatus().getCode();
        long offset = (long) (dto.getPage() - 1) * dto.getSize();

        List<AdminOrderListVO> records = orderMapper.selectAdminPage(
                        orderNo, status, dto.getUserId(), offset, dto.getSize())
                .stream()
                .map(this::toListVO)
                .toList();
        long total = orderMapper.countAdminOrders(orderNo, status, dto.getUserId());
        return new PageResult<>(records, total);
    }

    @Override
    public AdminOrderDetailVO getOrderDetail(String orderNo) {
        String normalizedOrderNo = normalize(orderNo);
        if (normalizedOrderNo == null) {
            throw new BusinessException(400, "订单号不能为空");
        }
        Order order = orderMapper.selectByOrderNo(normalizedOrderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        AdminOrderDetailVO vo = new AdminOrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setStatus(toOrderStatus(order.getStatus()));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setPayTime(order.getPayTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        vo.setItems(orderItemMapper.selectAdminByOrderId(order.getId()));
        return vo;
    }

    private AdminOrderListVO toListVO(Order order) {
        AdminOrderListVO vo = new AdminOrderListVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setStatus(toOrderStatus(order.getStatus()));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private void validatePage(AdminOrderQueryDTO dto) {
        if (dto == null || dto.getPage() == null || dto.getSize() == null
                || dto.getPage() < 1 || dto.getSize() < 1 || dto.getSize() > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private OrderStatus toOrderStatus(Byte code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (code != null && status.getCode() == code) {
                return status;
            }
        }
        throw new BusinessException(500, "订单状态数据异常");
    }
}
