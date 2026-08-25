package com.ec01.service.impl;

import com.ec01.common.OrderStatus;
import com.ec01.common.PageResult;
import com.ec01.dto.order.CreateOrderDTO;
import com.ec01.dto.order.PageQueryDTO;
import com.ec01.entity.*;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.*;
import com.ec01.security.UserContext;
import com.ec01.service.OrderService;
import com.ec01.vo.order.OrderDetailVO;
import com.ec01.vo.order.OrderListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.ec01.common.OrderStatus.PENDING_PAYMENT;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, CartItemMapper cartItemMapper, SkuMapper skuMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createOrder(CreateOrderDTO dto) {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }

        if (dto == null
                || dto.getCartItemIds() == null
                || dto.getCartItemIds().isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }

        if (dto.getReceiverName() == null || dto.getReceiverName().isBlank()) {
            throw new BusinessException("收货人不能为空");
        }

        if (dto.getReceiverPhone() == null || dto.getReceiverPhone().isBlank()) {
            throw new BusinessException("收货电话不能为空");
        }

        if (dto.getReceiverAddress() == null || dto.getReceiverAddress().isBlank()) {
            throw new BusinessException("收货地址不能为空");
        }

        List<Long> cartItemIds = dto.getCartItemIds();

        // 1. 查询本次结算的购物车项，同时校验归属
        List<CartItem> cartItems =
                cartItemMapper.selectByUserIdAndCartItemIds(
                        userId,
                        cartItemIds
                );

        if (cartItems.size() != cartItemIds.size()) {
            throw new BusinessException("购物车项不存在或无权操作");
        }

        // 2. 校验商品，并根据数据库真实价格计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Sku sku = skuMapper.selectById(cartItem.getSkuId());

            if (sku == null) {
                throw new BusinessException("SKU不存在");
            }

            if (sku.getStatus() != 1) {
                throw new BusinessException("SKU不可购买");
            }

            Product product = productMapper.selectById(sku.getProductId());

            if (product == null) {
                throw new BusinessException("商品不存在");
            }

            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架");
            }

            if (sku.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("商品库存不足");
            }

            BigDecimal subtotal =
                    sku.getPrice().multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    );

            totalAmount = totalAmount.add(subtotal);
        }

        // 3. 条件扣减库存，防止并发超卖
        for (CartItem cartItem : cartItems) {
            int rows = skuMapper.deductStock(
                    cartItem.getSkuId(),
                    cartItem.getQuantity()
            );

            if (rows <= 0) {
                throw new BusinessException("商品库存不足");
            }
        }

        // 4. 创建订单主表
        Order order = new Order();

        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setStatus((byte) 0); // 待支付
        order.setTotalAmount(totalAmount);

        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());

        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        if (orderMapper.insert(order) <= 0) {
            throw new BusinessException("订单创建失败");
        }

        // 5. 创建 OrderItem 商品快照
        for (CartItem cartItem : cartItems) {
            Sku sku = skuMapper.selectById(cartItem.getSkuId());

            if (sku == null) {
                throw new BusinessException("SKU不存在");
            }

            Product product = productMapper.selectById(sku.getProductId());

            if (product == null) {
                throw new BusinessException("商品不存在");
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setSkuId(sku.getId());

            orderItem.setProductName(product.getName());
            orderItem.setSkuSpec(sku.getSpecJson());
            orderItem.setCoverUrl(product.getCoverUrl());

            orderItem.setPrice(sku.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal subtotal =
                    sku.getPrice().multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    );

            orderItem.setSubtotal(subtotal);
            orderItem.setCreateTime(now);

            if (orderItemMapper.insert(orderItem) <= 0) {
                throw new BusinessException("订单明细创建失败");
            }
        }

        // 6. 删除本次已经结算的购物车项
        for (CartItem cartItem : cartItems) {
            int rows = cartItemMapper.deleteByIdAndUserId(
                    cartItem.getId(),
                    userId
            );

            if (rows <= 0) {
                throw new BusinessException("购物车清理失败");
            }
        }

        // 7. 返回业务订单号
        return order.getOrderNo();
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
        Long userId = UserContext.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException(400, "订单号不能为空");
        }

        Order order = orderMapper.selectByOrderNoAndUserId(orderNo.trim(), userId);

        if (order == null) {
            throw new BusinessException("订单不存在或无权查看");
        }

        OrderDetailVO vo = new OrderDetailVO();

        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());

        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());

        vo.setPayTime(order.getPayTime());
        vo.setCreateTime(order.getCreateTime());

        vo.setItems(
                orderItemMapper.selectByOrderId(order.getId())
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {

        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException("订单号不能为空");
        }

        Long userId = UserContext.get();

        // 1. 查询当前用户自己的订单
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 检查当前订单状态是否允许取消
        if (order.getStatus() != PENDING_PAYMENT.getCode()) {
            throw new BusinessException("当前订单状态无法取消");
        }

        // 3. 条件更新订单状态，防止重复取消 / 并发取消
        int updated = orderMapper.updateStatus(
                order.getId(),
                PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELLED.getCode()
        );

        if (updated != 1) {
            throw new BusinessException("订单状态已发生变化，请刷新后重试");
        }

        // 4. 查询订单项
        List<OrderItem> items =
                orderItemMapper.selectEntitiesByOrderId(order.getId());

        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单商品数据异常");
        }

        // 5. 恢复库存
        for (OrderItem item : items) {

            int affected = skuMapper.increaseStock(
                    item.getSkuId(),
                    item.getQuantity()
            );

            if (affected != 1) {
                throw new BusinessException("库存恢复失败");
            }
        }
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        int random = ThreadLocalRandom.current()
                .nextInt(100000, 1000000);

        return time + random;
    }
}
