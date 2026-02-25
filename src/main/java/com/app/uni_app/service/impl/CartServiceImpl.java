package com.app.uni_app.service.impl;


import com.app.uni_app.aop.annotation.SaveCartRedisCacheToMysqlAnnotation;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisKeyTtlProperties;
import com.app.uni_app.mapper.CartMapper;
import com.app.uni_app.pojo.dto.CartDTO;
import com.app.uni_app.pojo.dto.CartProductDTO;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.Cart;
import com.app.uni_app.pojo.entity.CartItem;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.entity.ProductSpec;
import com.app.uni_app.service.CartService;
import com.app.uni_app.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    @Resource
    private CartMapper cartMapper;

    @Resource
    private CopyMapper copyMapper;

    @Resource
    private ProductService productService;

    @Resource
    private RedisKeyTtlProperties redisKeyTtlProperties;

    private static final String DELETE_IDS = "deletedIds";
    private static final String SUCCESS_COUNT = "successCount";

    //策略:用户操作redis缓存,当进行修改后,触发延迟任务更新缓存到数据库,如果多次更改,以最后一次修改为准
    //细节:redis里的cart缓存是cartItem完整数据,前端DTO需要后端完善,使用的是productDetail缓存(如果未命中,则更新productDetail缓存)

    /**
     * 获取购物车列表
     *
     */
    @Override
    public Result getCartList() {
        String userId = BaseContext.getUserId();
        String cartKey = RedisKeyGenerator.cartKey(Long.valueOf(userId));
        Map<String, Object> cartMap = RedisConnector.opsForHash().entries(cartKey);
        List<CartItem> cartList;
        if (cartMap.isEmpty()) {
            cartList = cartMapper.getCartList(userId);
            //TODO 空对象
            if (cartList.isEmpty()) {
                return Result.success(CollectionUtils.emptyCollection());
            }
            HashMap<String, CartItem> resultMap = new HashMap<>(cartList.size());
            for (CartItem cartItem : cartList) {
                String hashKey = RedisKeyGenerator.cartHashKey(cartItem.getProductId(), cartItem.getSpecId());
                resultMap.put(hashKey, cartItem);
            }
            RedisConnector.opsForHash().putAll(cartKey, resultMap);
            RedisConnector.expire(cartKey, redisKeyTtlProperties.getCartTtl(), TimeUnit.SECONDS);
            return Result.success(cartList);
        }
        cartList = cartMap.values().stream().map(object -> (CartItem) object).toList();
        return Result.success(cartList);
    }

    private void saveCartListToRedis(List<CartItem> cartList, String cartKey) {
        HashMap<String, Object> resultMap = new HashMap<>(cartList.size());
        Set<Long> productIdSet = cartList.stream().map(CartItem::getProductId).map(Long::valueOf).collect(Collectors.toSet());
        Map<Long, Product> productDetailMap = productService.getProductDetailByProductIdSet(productIdSet);
        for (CartItem cartItem : cartList) {
            String productId = cartItem.getProductId();
            String specId = cartItem.getSpecId();
            String cartHashKey = RedisKeyGenerator.cartHashKey(productId, specId);
            cartItem = replenishCartItem(productDetailMap, cartItem);
            resultMap.put(cartHashKey, cartItem);
        }
        RedisConnector.opsForHash().putAll(cartKey, resultMap);
        RedisConnector.expire(cartKey, redisKeyTtlProperties.getCartTtl(), TimeUnit.SECONDS);
    }

    private CartItem replenishCartItem(Map<Long, Product> productDetailMap, CartItem cartItem) {
        if (Objects.isNull(productDetailMap) || Objects.isNull(cartItem)) {
            return null;

        }
        Product product = productDetailMap.get(Long.valueOf(cartItem.getProductId()));
        List<ProductSpec> specList = product.getSpecList();
        ProductSpec productSpec = null;
        for (ProductSpec productSpecTemp : specList) {
            if (StringUtils.equals(productSpecTemp.getId().toString(), cartItem.getSpecId())) {
                productSpec = productSpecTemp;
            }
        }
        if (Objects.isNull(productSpec)) {
            return null;

        }
        cartItem.setPrice(productSpec.getPrice()).setStock(productSpec.getStock())
                .setProductName(product.getName()).setProductImage(product.getImage())
                .setSpecText(productSpec.getSpecText());
        return cartItem;
    }

    /**
     * 添加商品到购物车
     *
     */
    @Override
    @SaveCartRedisCacheToMysqlAnnotation
    public Result addProductToCart(CartProductDTO cartProductDTO) {
        String userId = BaseContext.getUserId();
        String productId = cartProductDTO.getProductId();
        String specId = cartProductDTO.getSpecId();
        Integer quantity = cartProductDTO.getQuantity();
        String cartKey = RedisKeyGenerator.cartKey(Long.valueOf(userId));
        String cartHashKey = RedisKeyGenerator.cartHashKey(productId, specId);
        Map<String, Object> redisCacheMap = RedisConnector.opsForHash().entries(cartKey);
        //缓存没有该用户的购物车信息,将用户购物车信息查询出更新缓存,然后进行添加
        if (redisCacheMap.isEmpty()) {
            List<CartItem> cartList = cartMapper.getCartList(userId);
            //用户数据库购物车为空
            if (Objects.isNull(cartList) || cartList.isEmpty()) {
                CartItem cartItem = CartItem.builder().userId(userId).productId(productId)
                        .specId(specId).quantity(cartProductDTO.getQuantity()).build();
                Map<Long, Product> productDetailMap = productService.getProductDetailByProductIdSet(Set.of(Long.valueOf(productId)));
                cartItem = replenishCartItem(productDetailMap, cartItem);
                if (Objects.isNull(cartItem)) {
                    return Result.error(MessageConstant.DATA_ERROR);

                }
                RedisConnector.opsForHash().put(cartKey, cartHashKey, cartItem);
                RedisConnector.expire(cartKey, redisKeyTtlProperties.getCartTtl(), TimeUnit.SECONDS);
                return Result.success();

            }
            //用户数据库购物车不为空
            boolean isFind = false;
            for (CartItem item : cartList) {
                if (StringUtils.equals(productId, item.getProductId()) && StringUtils.equals(specId, item.getSpecId())) {
                    isFind = true;
                    item.setQuantity(item.getQuantity() + quantity);

                }
            }
            if (!isFind) {
                CartItem cartItem = CartItem.builder().userId(userId).productId(productId)
                        .specId(specId).quantity(cartProductDTO.getQuantity()).build();
                cartList.add(cartItem);

            }
            saveCartListToRedis(cartList, cartKey);
            return Result.success();

        }
        //命中缓存
        CartItem cartItem;
        if (redisCacheMap.containsKey(cartHashKey)) {
            cartItem = (CartItem) redisCacheMap.get(cartHashKey);
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = CartItem.builder().userId(userId).productId(productId)
                    .specId(specId).quantity(cartProductDTO.getQuantity()).build();
            Map<Long, Product> productDetailMap = productService.getProductDetailByProductIdSet(Set.of(Long.valueOf(productId)));
            cartItem = replenishCartItem(productDetailMap, cartItem);

        }
        redisCacheMap.put(cartHashKey, cartItem);
        RedisConnector.opsForHash().putAll(cartKey, redisCacheMap);
        RedisConnector.expire(cartKey, redisKeyTtlProperties.getCartTtl(), TimeUnit.SECONDS);
        return Result.success();


    }

    /**
     * 清空购物车
     *(未使用)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result clearCart() {
        String userId = BaseContext.getUserId();
        List<Cart> removeCarts = lambdaQuery().eq(Cart::getUserId, userId).list();
        if (CollectionUtils.isEmpty(removeCarts)) {
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
     *(未使用)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCartProduct(String productIds, String specIds) {
        if (StringUtils.isBlank(productIds) || StringUtils.isBlank(specIds)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        String userId = BaseContext.getUserId();
        List<String> productIdsList = Arrays.stream(productIds.split(",")).toList();
        List<String> specIdsList = Arrays.stream(specIds.split(",")).toList();
        if (productIdsList.isEmpty() && specIdsList.isEmpty()) {
            return Result.error(MessageConstant.CART_NOT_EXIST_ERROR);
        }
        if (productIdsList.size() != specIdsList.size()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }

        LambdaQueryWrapper<Cart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        int size = productIdsList.size();
        for (int i = 0; i < size; i++) {
            String productId = productIdsList.get(i);
            String specId = specIdsList.get(i);
            lambdaQueryWrapper.or(wrapper ->
                    wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId).eq(Cart::getSpecId, specId));
        }
        List<Cart> carts = list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(carts)) {
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
     * 将前端的购物车数据(List)更新到Redis
     *
     */
    @Override
    @SaveCartRedisCacheToMysqlAnnotation
    public Result mergeCart(CartDTO cartDTO) {
        List<CartProductDTO> carts = cartDTO.getCartItems();
        if (CollectionUtils.isEmpty(carts)) {
            return Result.success();
        }
        String userId = BaseContext.getUserId();
        String cartKey = RedisKeyGenerator.cartKey(Long.valueOf(userId));
        List<CartItem> cartItemList = carts.stream()
                .map(cartProductDTO -> copyMapper.cartProductDTOToCartItem(cartProductDTO))
                .peek(cartItem -> cartItem.setUserId(userId)).toList();
        RedisConnector.delete(cartKey);
        saveCartListToRedis(cartItemList, cartKey);
        return Result.success(carts);
    }

    /**
     * 将 Redis 缓存中的购物车同步到 MySQL
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCartToMysql(String userId) {
        String cartKey = RedisKeyGenerator.cartKey(Long.valueOf(userId));
        Map<String, Object> cartMap = RedisConnector.opsForHash().entries(cartKey);

        lambdaUpdate().eq(Cart::getUserId, userId).remove();


        if (cartMap.isEmpty()) {
            return;
        }

        List<Cart> cartList = cartMap.values().stream()
                .map(obj -> (CartItem) obj)
                .map(item -> Cart.builder()
                        .userId(Long.valueOf(userId))
                        .productId(Long.valueOf(item.getProductId()))
                        .specId(Long.valueOf(item.getSpecId()))
                        .quantity(item.getQuantity())
                        .checked(CommonStatus.INACTIVE.getNumber())
                        .build())
                .toList();

        saveBatch(cartList);
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
