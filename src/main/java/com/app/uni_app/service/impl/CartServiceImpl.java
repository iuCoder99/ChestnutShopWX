package com.app.uni_app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.CartMapper;
import com.app.uni_app.pojo.dto.CartDTO;
import com.app.uni_app.pojo.dto.CartProductDTO;
import com.app.uni_app.pojo.entity.Cart;
import com.app.uni_app.pojo.entity.CartItem;
import com.app.uni_app.service.CartService;
import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.models.auth.In;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    @Resource
    private CartMapper cartMapper;

    private static final String DELETE_IDS = "deletedIds";
    private static final String SUCCESS_COUNT = "successCount";

    /**
     * 获取购物车列表
     *
     * @return
     */
    @Override
    public Result getCartList() {
        String userid = BaseContext.getUserInfo().getId();
        List<CartItem> cartList = cartMapper.getCartList(userid);
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     *
     * @param cartProductDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addProductToCart(CartProductDTO cartProductDTO) {
        String userId = BaseContext.getUserInfo().getId();
        String productId = cartProductDTO.getProductId();
        String specId = cartProductDTO.getSpecId();
        CartItem cartItem = cartMapper.getCartItem(userId, productId, specId);
        if (cartItem == null) {
            Cart cart = Cart.builder().userId(Long.valueOf(userId))
                    .productId(Long.valueOf(productId))
                    .specId(Long.valueOf(specId))
                    .quantity(cartProductDTO.getQuantity()).build();
            save(cart);
            return Result.success(cart.getId());
        }
        cartMapper.updateCartItemQuantity(userId, productId, specId, cartProductDTO.getQuantity());
        return Result.success(cartItem.getId());

    }

    /**
     * 清空购物车
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result clearCart() {
        String userId = BaseContext.getUserInfo().getId();
        List<Cart> removeCarts = lambdaQuery().eq(Cart::getUserId, userId).list();
        if (removeCarts.isEmpty()) {
            return Result.success(removeCarts);
        }
        List<Long> removeCartIds = removeCarts.stream().map(Cart::getId).toList();
        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Cart::getUserId, userId);
        int deletedRowsCount = cartMapper.delete(lambdaQueryWrapper);
        Map<String, Object> map = new HashMap<>(2);
        map.put(DELETE_IDS, removeCartIds);
        map.put(SUCCESS_COUNT, deletedRowsCount);
        return Result.success(map);
    }

    /**
     * 批量删除购物车商品(单个+批量)
     *
     * @param productIds
     * @param specIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCartProduct(String productIds, String specIds) {
        log.error("--------productIds"+productIds+";specIds:"+specIds+"----------");
        if (productIds == null || specIds == null) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        String userId = BaseContext.getUserInfo().getId();
        List<String> productIdsList = Arrays.stream(productIds.split(",")).toList();
        List<String> specIdsList = Arrays.stream(specIds.split(",")).toList();
        if (productIdsList.isEmpty() && specIdsList.isEmpty()) {
            return Result.error(MessageConstant.CART_NOT_EXIST_ERROR);
        }
        if (productIdsList.size() != specIdsList.size()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }

        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i < productIdsList.size(); i++) {
            String productId = productIdsList.get(i);
            String specId = specIdsList.get(i);
            lambdaQueryWrapper.or(wrapper ->
                    wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId).eq(Cart::getSpecId, specId));
        }
        List<Cart> carts = list(lambdaQueryWrapper);
        if (carts.isEmpty()) {
            return Result.error(MessageConstant.CART_NOT_EXIST_ERROR);
        }
        if (carts.size() != productIdsList.size()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        boolean removeIsSuccess = remove(lambdaQueryWrapper);
        if (!removeIsSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(DELETE_IDS, productIdsList);
        map.put(SUCCESS_COUNT, productIdsList.size());
        return Result.success(map);
    }


    /**
     * 将前端的购物车数据(List)更新到数据库
     *
     * @param cartDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result mergeCart(CartDTO cartDTO) {
        List<CartProductDTO> carts = cartDTO.getCartItems();
        if (carts.isEmpty()) {
            return Result.success();
        }
        String userId = BaseContext.getUserInfo().getId();
        List<Cart> cartList = BeanUtil.copyToList(carts, Cart.class).stream()
                .peek(cart -> cart.setUserId(Long.valueOf(userId))).toList();
        remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        saveBatch(cartList);
        return Result.success(carts);
    }


    /*
      合并购物车到云端(业务调整废弃,旧版功能逻辑:将数据库的购物车与前端传递的购物车进行合并)
      @param cartDTO
     * @return
     */
//    @Override
//    @Transactional
//    public Result mergeCart(CartDTO cartDTO) {
//        List<CartProductDTO> cartsNoStored = cartDTO.getCartItems();
//        Integer mergedCount = cartsNoStored.stream().map(CartProductDTO::getQuantity).reduce(0, Integer::sum);
//        if (cartsNoStored.isEmpty()) {
//            return Result.success();
//        }
//        String userId = BaseContext.getUserInfo().getId();
//        List<Cart> cartList = cartsNoStored.stream().map(cartNoStored -> BeanUtil.copyProperties(cartNoStored, Cart.class)).toList();
//        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(Cart::getUserId, userId);
//        List<Cart> cartsStored = list(lambdaQueryWrapper);
//        //云端购物车为空
//        if (cartsStored.isEmpty()) {
//            return saveAndReturnResult(cartList, mergedCount, mergedCount, userId);
//        }
//        remove(lambdaQueryWrapper);
//        Integer totalCount = Stream.concat(cartList.stream(), cartsStored.stream()).map(Cart::getQuantity).reduce(0, Integer::sum);
//        List<Cart> mergedCartList = Stream.concat(cartsStored.stream(), cartList.stream())
//                .collect(Collectors.groupingBy((Cart cart) -> Map.entry(cart.getProductId(), cart.getSpecId())))
//                .values().stream().map(carts -> {
//                    Integer quantity = carts.stream().map(Cart::getQuantity).reduce(0, Integer::sum);
//                    Cart cart = carts.get(0);
//                    cart.setQuantity(quantity);
//                    return cart;
//                }).toList();
//        mergedCartList.forEach(cart -> cart.setUserId(Long.valueOf(userId)));
//        return saveAndReturnResult(mergedCartList, mergedCount, totalCount, userId);
//    }
//
//    private @NonNull Result<?> saveAndReturnResult(List<Cart> cartList, Integer mergedCount, Integer totalCount, String userId) {
//        cartList.forEach(cart -> cart.setUserId(Long.valueOf(userId)));
//        boolean isSuccess = applicationContext.getBean(CartServiceImpl.class).saveBatch(cartList);
//        if (!isSuccess) {
//            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
//        }
//        HashMap<String, Object> map = new HashMap<>(2);
//        map.put("mergedCount", mergedCount);
//        map.put("totalCount", totalCount);
//        return Result.success(map);
//    }
}
