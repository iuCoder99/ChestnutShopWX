package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.Cart;
import com.app.uni_app.pojo.entity.CartItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    List<CartItem> getCartList(String userId);

    CartItem getCartItem(@Param("userId") String userId,@Param("productId") String productId,@Param("specId") String specId);


    void updateCartItemQuantity(String userId, String productId, String specId, Integer quantity);
}


