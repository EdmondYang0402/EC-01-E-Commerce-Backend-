package com.ec01.service;

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
import com.ec01.service.impl.CartServiceImpl;
import com.ec01.vo.cart.CartItemVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private SkuMapper skuMapper;
    @Mock private ProductMapper productMapper;
    @Mock private CartItemMapper cartItemMapper;
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(skuMapper, productMapper, cartItemMapper);
        UserContext.set(7L);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void addCartItemCreatesSelectedItemForCurrentUser() {
        mockPurchasableSku(10L, 5);
        when(cartItemMapper.selectByUserIdAndSkuId(7L, 10L)).thenReturn(null);
        when(cartItemMapper.insert(any())).thenReturn(1);

        cartService.addCartItem(addDto(10L, 2));

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals(10L, captor.getValue().getSkuId());
        assertEquals(2, captor.getValue().getQuantity());
        assertEquals((byte) 1, captor.getValue().getSelected());
    }

    @Test
    void addSameSkuIncreasesExistingQuantityWithoutDuplicateInsert() {
        mockPurchasableSku(10L, 5);
        CartItem existing = cartItem(1L, 7L, 10L, 2, (byte) 1);
        when(cartItemMapper.selectByUserIdAndSkuId(7L, 10L)).thenReturn(existing);
        when(cartItemMapper.update(existing)).thenReturn(1);

        cartService.addCartItem(addDto(10L, 2));

        assertEquals(4, existing.getQuantity());
        verify(cartItemMapper).update(existing);
        verify(cartItemMapper, never()).insert(any());
    }

    @Test
    void addSameSkuRejectsCombinedQuantityOverStock() {
        mockPurchasableSku(10L, 3);
        when(cartItemMapper.selectByUserIdAndSkuId(7L, 10L))
                .thenReturn(cartItem(1L, 7L, 10L, 2, (byte) 1));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.addCartItem(addDto(10L, 2)));

        assertEquals("商品库存不足", exception.getMessage());
        verify(cartItemMapper, never()).update(any());
    }

    @Test
    void getMyCartOnlyQueriesCurrentUser() {
        CartItemVO item = new CartItemVO();
        item.setCartItemId(1L);
        when(cartItemMapper.selectCartByUserId(7L)).thenReturn(List.of(item));

        List<CartItemVO> result = cartService.getMyCart();

        assertEquals(1L, result.getFirst().getCartItemId());
        verify(cartItemMapper).selectCartByUserId(7L);
    }

    @Test
    void updateCartItemChecksOwnershipStockAndSelectedState() {
        CartItem item = cartItem(1L, 7L, 10L, 1, (byte) 1);
        when(cartItemMapper.selectByIdAndUserId(1L, 7L)).thenReturn(item);
        Sku sku = new Sku();
        sku.setId(10L);
        sku.setStatus((byte) 1);
        sku.setStock(5);
        when(skuMapper.selectById(10L)).thenReturn(sku);
        when(cartItemMapper.update(item)).thenReturn(1);
        UpdateCartItemDTO dto = new UpdateCartItemDTO();
        dto.setQuantity(3);
        dto.setSelected((byte) 0);

        cartService.updateCartItem(1L, dto);

        assertEquals(3, item.getQuantity());
        assertEquals((byte) 0, item.getSelected());
        verify(cartItemMapper).selectByIdAndUserId(1L, 7L);
        verify(cartItemMapper).update(item);
    }

    @Test
    void updateCartItemCannotAccessAnotherUsersItem() {
        when(cartItemMapper.selectByIdAndUserId(9L, 7L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.updateCartItem(9L, updateDto(1, (byte) 1)));

        assertTrue(exception.getMessage().contains("无权操作"));
        verify(cartItemMapper, never()).update(any());
    }

    @Test
    void deleteCartItemUsesBothItemIdAndCurrentUserId() {
        when(cartItemMapper.deleteByIdAndUserId(1L, 7L)).thenReturn(1);

        cartService.deleteCartItem(1L);

        verify(cartItemMapper).deleteByIdAndUserId(1L, 7L);
    }

    private void mockPurchasableSku(Long skuId, int stock) {
        Sku sku = new Sku();
        sku.setId(skuId);
        sku.setProductId(20L);
        sku.setStatus((byte) 1);
        sku.setStock(stock);
        Product product = new Product();
        product.setId(20L);
        product.setStatus((byte) 1);
        when(skuMapper.selectById(skuId)).thenReturn(sku);
        when(productMapper.selectById(20L)).thenReturn(product);
    }

    private AddCartItemDTO addDto(Long skuId, int quantity) {
        AddCartItemDTO dto = new AddCartItemDTO();
        dto.setSkuId(skuId);
        dto.setQuantity(quantity);
        return dto;
    }

    private UpdateCartItemDTO updateDto(int quantity, byte selected) {
        UpdateCartItemDTO dto = new UpdateCartItemDTO();
        dto.setQuantity(quantity);
        dto.setSelected(selected);
        return dto;
    }

    private CartItem cartItem(Long id, Long userId, Long skuId, int quantity, byte selected) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        item.setSelected(selected);
        return item;
    }
}
