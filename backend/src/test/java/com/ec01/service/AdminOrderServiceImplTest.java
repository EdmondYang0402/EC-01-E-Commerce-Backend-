package com.ec01.service;

import com.ec01.common.OrderStatus;
import com.ec01.common.PageResult;
import com.ec01.dto.admin.order.AdminOrderQueryDTO;
import com.ec01.entity.Order;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.OrderItemMapper;
import com.ec01.mapper.OrderMapper;
import com.ec01.service.impl.AdminOrderServiceImpl;
import com.ec01.vo.admin.order.AdminOrderDetailVO;
import com.ec01.vo.admin.order.AdminOrderItemVO;
import com.ec01.vo.admin.order.AdminOrderListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminOrderServiceImplTest {
    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;
    private AdminOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        orderItemMapper = mock(OrderItemMapper.class);
        service = new AdminOrderServiceImpl(orderMapper, orderItemMapper);
    }

    @Test
    void orderPageUsesFiltersAndCorrectOffset() {
        AdminOrderQueryDTO dto = new AdminOrderQueryDTO();
        dto.setPage(2);
        dto.setSize(10);
        dto.setOrderNo(" EC-01 ");
        dto.setStatus(OrderStatus.PAID);
        dto.setUserId(42L);
        Order order = order(7L, "EC-01-7", 42L, (byte) 1);
        when(orderMapper.selectAdminPage("EC-01", (byte) 1, 42L, 10L, 10))
                .thenReturn(List.of(order));
        when(orderMapper.countAdminOrders("EC-01", (byte) 1, 42L)).thenReturn(11L);

        PageResult<AdminOrderListVO> result = service.getOrderPage(dto);

        assertEquals(11L, result.getTotal());
        assertEquals(OrderStatus.PAID, result.getRecords().getFirst().getStatus());
        assertEquals(42L, result.getRecords().getFirst().getUserId());
    }

    @Test
    void orderDetailReturnsSnapshotItems() {
        Order order = order(7L, "EC-01-7", 42L, (byte) 2);
        order.setReceiverName("Alice");
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("Shanghai");
        AdminOrderItemVO item = new AdminOrderItemVO();
        item.setId(8L);
        item.setProductName("Snapshot chair");
        item.setSubtotal(new BigDecimal("199.00"));
        when(orderMapper.selectByOrderNo("EC-01-7")).thenReturn(order);
        when(orderItemMapper.selectAdminByOrderId(7L)).thenReturn(List.of(item));

        AdminOrderDetailVO detail = service.getOrderDetail(" EC-01-7 ");

        assertEquals(OrderStatus.SHIPPED, detail.getStatus());
        assertEquals("Alice", detail.getReceiverName());
        assertEquals("Snapshot chair", detail.getItems().getFirst().getProductName());
        verify(orderItemMapper).selectAdminByOrderId(7L);
    }

    @Test
    void missingOrderReturnsNotFound() {
        when(orderMapper.selectByOrderNo("missing")).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.getOrderDetail("missing"));

        assertEquals(404, exception.getCode());
    }

    @Test
    void rejectsInvalidPageBeforeQueryingMapper() {
        AdminOrderQueryDTO dto = new AdminOrderQueryDTO();
        dto.setPage(0);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.getOrderPage(dto));

        assertEquals(400, exception.getCode());
    }

    private Order order(Long id, String orderNo, Long userId, byte status) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("199.00"));
        return order;
    }
}
