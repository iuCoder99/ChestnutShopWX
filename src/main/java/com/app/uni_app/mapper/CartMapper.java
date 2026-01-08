package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.Cart;
import com.app.uni_app.pojo.entity.CartItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    List<CartItem> getCartList(String userId);

    CartItem getCartItem(String userId, String productId, String specId);


    void updateCartItemQuantity(String userId, String productId, String specId, Integer quantity);
}


