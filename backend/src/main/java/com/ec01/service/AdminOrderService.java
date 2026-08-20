package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.admin.order.AdminOrderQueryDTO;
import com.ec01.vo.admin.order.AdminOrderDetailVO;
import com.ec01.vo.admin.order.AdminOrderListVO;

public interface AdminOrderService {
    PageResult<AdminOrderListVO> getOrderPage(AdminOrderQueryDTO dto);

    AdminOrderDetailVO getOrderDetail(String orderNo);
}
