package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.dto.order.CreateOrderDTO;
import com.ec01.entity.CartItem;
import com.ec01.entity.Order;
import com.ec01.entity.OrderItem;
import com.ec01.entity.Product;
import com.ec01.entity.Sku;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.OrderItemMapper;
import com.ec01.mapper.OrderMapper;
import com.ec01.mapper.CartItemMapper;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.security.UserContext;
import com.ec01.service.impl.OrderServiceImpl;
import com.ec01.vo.order.OrderItemVO;
import com.ec01.vo.order.OrderDetailVO;
import com.ec01.vo.order.OrderListVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;
    private CartItemMapper cartItemMapper;
    private SkuMapper skuMapper;
    private ProductMapper productMapper;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        orderItemMapper = mock(OrderItemMapper.class);
        cartItemMapper = mock(CartItemMapper.class);
        skuMapper = mock(SkuMapper.class);
        productMapper = mock(ProductMapper.class);
        orderService = new OrderServiceImpl(
                orderMapper,
                orderItemMapper,
                cartItemMapper,
                skuMapper,
                productMapper);
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

    @Test
    void createsOrderFromOwnedCartItemsAndPersistsSnapshots() {
        CartItem cartItem = new CartItem();
        cartItem.setId(7L);
        cartItem.setUserId(42L);
        cartItem.setSkuId(11L);
        cartItem.setQuantity(2);

        Sku sku = new Sku();
        sku.setId(11L);
        sku.setProductId(3L);
        sku.setStatus((byte) 1);
        sku.setStock(5);
        sku.setPrice(new BigDecimal("99.50"));
        sku.setSpecJson("{\"color\":\"black\"}");

        Product product = new Product();
        product.setId(3L);
        product.setName("Chair");
        product.setCoverUrl("/chair.png");
        product.setStatus((byte) 1);

        when(cartItemMapper.selectByUserIdAndCartItemIds(42L, List.of(7L)))
                .thenReturn(List.of(cartItem));
        when(skuMapper.selectById(11L)).thenReturn(sku);
        when(productMapper.selectById(3L)).thenReturn(product);
        when(skuMapper.deductStock(11L, 2)).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            invocation.<Order>getArgument(0).setId(90L);
            return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartItemMapper.deleteByIdAndUserId(7L, 42L)).thenReturn(1);

        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setCartItemIds(List.of(7L));
        dto.setReceiverName("Alice");
        dto.setReceiverPhone("13800000000");
        dto.setReceiverAddress("Shanghai");

        String orderNo = orderService.createOrder(dto);

        assertNotNull(orderNo);
        verify(orderMapper).insert(argThat(order ->
                order.getUserId() == 42L
                        && new BigDecimal("199.00").compareTo(order.getTotalAmount()) == 0
                        && "Alice".equals(order.getReceiverName())
                        && "13800000000".equals(order.getReceiverPhone())
                        && "Shanghai".equals(order.getReceiverAddress())));
        verify(orderItemMapper).insert(argThat(item ->
                item.getOrderId() == 90L
                        && item.getProductId() == 3L
                        && item.getSkuId() == 11L
                        && "Chair".equals(item.getProductName())
                        && new BigDecimal("199.00").compareTo(item.getSubtotal()) == 0));
        verify(cartItemMapper).deleteByIdAndUserId(7L, 42L);
    }

    @Test
    void orderDetailUsesCurrentUserAndPurchaseSnapshots() {
        Order order = new Order();
        order.setId(90L);
        order.setOrderNo("EC0090");
        order.setReceiverName("Alice");
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("Shanghai");
        OrderItemVO item = new OrderItemVO();
        item.setProductName("Historical chair");

        when(orderMapper.selectByOrderNoAndUserId("EC0090", 42L)).thenReturn(order);
        when(orderItemMapper.selectByOrderId(90L)).thenReturn(List.of(item));

        OrderDetailVO detail = orderService.getOrderDetail(" EC0090 ");

        assertEquals("EC0090", detail.getOrderNo());
        assertEquals("Alice", detail.getReceiverName());
        assertEquals(List.of(item), detail.getItems());
        verify(orderMapper).selectByOrderNoAndUserId("EC0090", 42L);
    }
}
