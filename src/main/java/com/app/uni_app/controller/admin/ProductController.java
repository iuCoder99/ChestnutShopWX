package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Resource
    private ProductService productService;

    /**
     * 获取热门商品
     *
     * @return
     */
    @GetMapping("/product/hot")
    public Result getHotProduct(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return productService.getHotProduct(limit);
    }

    /**
     * 获取列表商品简单介绍
     *
     * @param productIds
     * @return
     */
    @GetMapping("/product/brief/list")
    public Result getBriefProduct(@RequestParam(value = "productIds", defaultValue = "") String productIds) {
        return productService.getBriefProduct(productIds);
    }

    /**
     * 获取商品详情
     *
     * @return
     */
    @GetMapping("/product/detail")
    public Result getProductDetail(@RequestParam("productId") String productId
            , @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return productService.getProductDetail(productId, userId);
    }

    /**
     * 获取商品列表
     *
     * @return
     */
    @GetMapping("/product/list")
    public Result getProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                 @RequestParam("categoryId") String categoryId) {
        return productService.getProductList(pageNum, pageSize, categoryId);
    }

    /**
     * 搜索商品列表
     *
     * @param pageNum
     * @param pageSize
     * @param keyword
     * @return
     */
    @GetMapping("/product/search")
    public Result searchProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                    @RequestParam(value = "sortType", defaultValue = "default") String sortType,
                                    String firstCategoryId,
                                    String secondCategoryId,
                                    String keyword) {
        return productService.searchProductList(pageNum, pageSize, firstCategoryId, secondCategoryId, sortType, keyword);
    }

    /**
     * 获取相关商品(通过categoryId建立联系)
     *
     * @param productId
     * @param limit
     * @return
     */
    @GetMapping("/product/related")
    public Result getProductRelated(@RequestParam("productId") String productId
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return productService.getProductRelated(productId, limit);
    }

    /**
     * 获取商品规格价格
     *
     * @return
     */
    @GetMapping("/product/spec/price")
    public Result getProductSpecPrice(@RequestParam("productId") String productId, @RequestParam("specId") String specId) {
        return productService.getProductSpecPrice(productId, specId);
    }

    /**
     * 滚动查询的商品列表
     * @return
     */
    @GetMapping("/product/scroll/query/list")
    public Result getSimpleProductByScrollQuery() {
        return productService.getSimpleProductByScrollQuery();
    }


}
