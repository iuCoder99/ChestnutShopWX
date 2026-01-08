package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CartDTO;
import com.app.uni_app.pojo.dto.CartProductDTO;
import com.app.uni_app.service.CartService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/cart")
@RestController
public class CartController {
    @Resource
    private CartService cartService;

    /**
     * 获取购物车列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result getCartList() {
        return cartService.getCartList();
    }

    /**
     * 添加商品到购物车
     *
     * @return
     */
    @PostMapping("/add")
    public Result addProductToCart(@RequestBody CartProductDTO cartProductDTO) {
        return cartService.addProductToCart(cartProductDTO);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clear")
    public Result clearCart() {
        return cartService.clearCart();
    }

    /**
     * 批量删除购物车商品(单个+批量)
     *
     * @return
     */
    @DeleteMapping("/products")
    public Result deleteCartProduct(@RequestParam("productIds") String productIds, @RequestParam("specIds") String specIds) {
        return cartService.deleteCartProduct(productIds, specIds);
    }


    /**
     * 按商品和规格更新购物车商品数量
     *
     * @return
     */
    @PutMapping("/products/{productId}/specs/{specId}")
    public Result updateCartProductQuantity(@PathVariable String productId
            , @PathVariable String specId
            , @RequestParam("quantity") String quantity) {
        return cartService.updateCartProductQuantity(productId, specId, quantity);
    }


    /**
     * 合并购物车到云端
     *
     * @param cartDTO
     * @return
     */
    @PostMapping("/merge")
    public Result mergeCart(@RequestBody CartDTO cartDTO) {
        return cartService.mergeCart(cartDTO);
    }
}
