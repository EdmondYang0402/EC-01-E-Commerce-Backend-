package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.order.CreateOrderDTO;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.vo.order.OrderDetailVO;
import com.ec01.vo.order.OrderListVO;

public interface OrderService {

    String createOrder(CreateOrderDTO dto);

    PageResult<OrderListVO> getMyOrders(PageQueryDTO dto);

    OrderDetailVO getOrderDetail(String orderNo);
}
