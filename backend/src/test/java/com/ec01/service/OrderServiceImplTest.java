package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.entity.Order;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.OrderItemMapper;
import com.ec01.mapper.OrderMapper;
import com.ec01.security.UserContext;
import com.ec01.service.impl.OrderServiceImpl;
import com.ec01.vo.order.OrderItemVO;
import com.ec01.vo.order.OrderListVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        orderItemMapper = mock(OrderItemMapper.class);
        orderService = new OrderServiceImpl(orderMapper, orderItemMapper);
        UserContext.set(42L);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void returnsCurrentUsersPageWithTotalAndItems() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(2);
        dto.setSize(10);

        Order order = new Order();
        order.setId(9L);
        order.setOrderNo("EC0009");
        order.setStatus((byte) 1);
        order.setTotalAmount(new BigDecimal("199.00"));
        order.setCreateTime(LocalDateTime.of(2026, 8, 19, 12, 0));
        OrderItemVO item = new OrderItemVO();
        item.setId(91L);
        item.setProductId(3L);

        when(orderMapper.selectPageByUserId(42L, 10L, 10)).thenReturn(List.of(order));
        when(orderMapper.countByUserId(42L)).thenReturn(11L);
        when(orderItemMapper.selectByOrderId(9L)).thenReturn(List.of(item));

        PageResult<OrderListVO> result = orderService.getMyOrders(dto);

        assertEquals(11L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        OrderListVO vo = result.getRecords().getFirst();
        assertEquals(9L, vo.getId());
        assertEquals("EC0009", vo.getOrderNo());
        assertEquals(List.of(item), vo.getItems());
        verify(orderMapper).selectPageByUserId(42L, 10L, 10);
        verify(orderMapper).countByUserId(42L);
        verify(orderItemMapper).selectByOrderId(9L);
    }

    @Test
    void emptyPageReturnsEmptyRecordsAndZeroTotal() {
        PageQueryDTO dto = new PageQueryDTO();
        when(orderMapper.selectPageByUserId(42L, 0L, 10)).thenReturn(List.of());
        when(orderMapper.countByUserId(42L)).thenReturn(0L);

        PageResult<OrderListVO> result = orderService.getMyOrders(dto);

        assertEquals(List.of(), result.getRecords());
        assertEquals(0L, result.getTotal());
        verifyNoInteractions(orderItemMapper);
    }

    @Test
    void rejectsInvalidPaginationBeforeQueryingDatabase() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(0);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> orderService.getMyOrders(dto));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(orderMapper, orderItemMapper);
    }

    @Test
    void rejectsMissingAuthenticatedUser() {
        UserContext.remove();

        BusinessException exception = assertThrows(
                BusinessException.class, () -> orderService.getMyOrders(new PageQueryDTO()));

        assertEquals(401, exception.getCode());
        verifyNoInteractions(orderMapper, orderItemMapper);
    }
}
