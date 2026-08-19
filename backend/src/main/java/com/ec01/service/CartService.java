package com.ec01.service;

import com.ec01.dto.cart.AddCartItemDTO;
import com.ec01.dto.cart.UpdateCartItemDTO;
import com.ec01.vo.cart.CartItemVO;

import java.util.List;

public interface CartService {

    void addCartItem(AddCartItemDTO dto);

    List<CartItemVO> getMyCart();

    void updateCartItem(Long cartItemId, UpdateCartItemDTO dto);

    void deleteCartItem(Long cartItemId);
}
