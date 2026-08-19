package com.ec01.controller;

import com.ec01.common.Result;
import com.ec01.dto.cart.AddCartItemDTO;
import com.ec01.dto.cart.UpdateCartItemDTO;
import com.ec01.service.CartService;
import com.ec01.vo.cart.CartItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItemVO>> getMyCart() {
        return Result.success(cartService.getMyCart());
    }

    @PostMapping
    public Result<Void> addCartItem(@Valid @RequestBody AddCartItemDTO dto) {
        cartService.addCartItem(dto);
        return Result.success(null);
    }

    @PutMapping("/{cartItemId}")
    public Result<Void> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemDTO dto) {
        cartService.updateCartItem(cartItemId, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{cartItemId}")
    public Result<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return Result.success(null);
    }
}
