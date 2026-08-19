package com.ec01.service.impl;

import com.ec01.dto.cart.AddCartItemDTO;
import com.ec01.dto.cart.UpdateCartItemDTO;
import com.ec01.entity.CartItem;
import com.ec01.entity.Product;
import com.ec01.entity.Sku;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.CartItemMapper;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.security.UserContext;
import com.ec01.service.CartService;
import com.ec01.vo.cart.CartItemVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final CartItemMapper cartItemMapper;

    public CartServiceImpl(SkuMapper skuMapper, ProductMapper productMapper, CartItemMapper cartItemMapper) {
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
        this.cartItemMapper = cartItemMapper;
    }

    @Override
    public void addCartItem(AddCartItemDTO dto) {
        Long userId = UserContext.get();

        if (dto == null || dto.getSkuId() == null) {
            throw new BusinessException("SKU不能为空");
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("输入数量不合法，必须大于0");
        }

        Sku sku = skuMapper.selectById(dto.getSkuId());

        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        if (sku.getStatus() != 1) {
            throw new BusinessException("该SKU当前不可购买");
        }

        Product product = productMapper.selectById(sku.getProductId());

        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }

        if (sku.getStock() < dto.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }

        // 后面继续处理购物车逻辑
        CartItem cartItem =
                cartItemMapper.selectByUserIdAndSkuId(userId, dto.getSkuId());

        if (cartItem == null) {

            CartItem newCartItem = new CartItem();
            newCartItem.setUserId(userId);
            newCartItem.setSkuId(dto.getSkuId());
            newCartItem.setQuantity(dto.getQuantity());
            newCartItem.setSelected((byte) 1);

            if (cartItemMapper.insert(newCartItem) <= 0) {
                throw new BusinessException("加入购物车失败");
            }

        } else {

            int newQuantity = cartItem.getQuantity() + dto.getQuantity();

            if (newQuantity > sku.getStock()) {
                throw new BusinessException("商品库存不足");
            }

            cartItem.setQuantity(newQuantity);

            if (cartItemMapper.update(cartItem) <= 0) {
                throw new BusinessException("更新购物车失败");
            }
        }
    }

    @Override
    public List<CartItemVO> getMyCart() {
        Long userId = UserContext.get();

        List<CartItemVO> cartItems = cartItemMapper.selectCartByUserId(userId);

        return cartItems;
    }

    @Override
    public void updateCartItem(Long cartItemId, UpdateCartItemDTO dto) {
        Long userId = UserContext.get();

        if (cartItemId == null || cartItemId <= 0) {
            throw new BusinessException("购物车项ID不合法");
        }

        CartItem cartItem =
                cartItemMapper.selectByIdAndUserId(cartItemId, userId);

        if (cartItem == null) {
            throw new BusinessException("购物车项不存在或无权操作");
        }

        if (dto == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("商品数量必须大于0");
        }

        if (dto.getSelected() != null && dto.getSelected() != 0 && dto.getSelected() != 1) {
            throw new BusinessException("选中状态不合法");
        }

        Sku sku = skuMapper.selectById(cartItem.getSkuId());

        if (sku == null || sku.getStatus() != 1) {
            throw new BusinessException("SKU不存在或不可购买");
        }

        if (dto.getQuantity() > sku.getStock()) {
            throw new BusinessException("商品库存不足");
        }

        // 只修改允许修改的字段
        cartItem.setQuantity(dto.getQuantity());

        if (dto.getSelected() != null) {
            cartItem.setSelected(dto.getSelected());
        }

        if (cartItemMapper.update(cartItem) <= 0) {
            throw new BusinessException("修改购物车失败");
        }
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        Long userId = UserContext.get();

        if (cartItemId == null || cartItemId <= 0) {
            throw new BusinessException("购物车项ID不合法");
        }

        if (cartItemMapper.deleteByIdAndUserId(cartItemId, userId) <= 0) {
            throw new BusinessException("购物车项不存在或无权删除");
        }
    }
}
