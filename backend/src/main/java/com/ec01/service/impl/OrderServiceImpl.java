package com.ec01.service.impl;

import com.ec01.common.PageResult;
import com.ec01.dto.order.CreateOrderDTO;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.entity.Order;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.OrderItemMapper;
import com.ec01.mapper.OrderMapper;
import com.ec01.security.UserContext;
import com.ec01.service.OrderService;
import com.ec01.vo.order.OrderDetailVO;
import com.ec01.vo.order.OrderListVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }


    @Override
    public String createOrder(CreateOrderDTO dto) {
        Long userId = UserContext.get();
        return null;
    }

    @Override
    public PageResult<OrderListVO> getMyOrders(PageQueryDTO dto) {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        if (dto == null || dto.getPage() == null || dto.getSize() == null
                || dto.getPage() < 1 || dto.getSize() < 1 || dto.getSize() > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }

        long offset = (long) (dto.getPage() - 1) * dto.getSize();
        List<Order> orders = orderMapper.selectPageByUserId(userId, offset, dto.getSize());
        long total = orderMapper.countByUserId(userId);
        List<OrderListVO> orderList = new ArrayList<>();

        for (Order order : orders) {
            OrderListVO vo = new OrderListVO();

            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setStatus(order.getStatus());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setCreateTime(order.getCreateTime());
            vo.setItems(orderItemMapper.selectByOrderId(order.getId()));

            orderList.add(vo);
        }
        return new PageResult<>(orderList, total);
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderNo) {
        return null;
    }
}
