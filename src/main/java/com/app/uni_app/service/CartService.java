package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CartDTO;
import com.app.uni_app.pojo.dto.CartProductDTO;
import com.app.uni_app.pojo.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CartService extends IService<Cart> {
    Result getCartList();


    Result addProductToCart(CartProductDTO cartProductDTO);

    Result clearCart();


    Result deleteCartProduct(String productIds, String specIds);


    Result mergeCart(CartDTO cartDTO);

}


