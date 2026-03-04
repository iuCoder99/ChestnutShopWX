package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.entity.ProductSearchKeyword;
import com.app.uni_app.service.ProductSearchKeywordService;
import com.app.uni_app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "商品管理")
public class ProductController {
    @Resource
    private ProductService productService;

    @Resource
    private ProductSearchKeywordService productSearchKeywordService;

    /**
     * 获取热门商品
     *
     * @return
     */
    @GetMapping("/product/hot")
    @Operation(summary = "查询热门商品", description = "获取热门商品列表，默认最多返回10条，可通过limit参数指定数量")
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
    @Operation(summary = "查询商品简单信息列表", description = "根据商品ID（支持批量，逗号分隔，为空则返回空）获取商品简要信息")
    public Result getBriefProduct(@RequestParam(value = "productIds", defaultValue = "") String productIds) {
        return productService.getBriefProduct(productIds);
    }

    /**
     * 获取商品详情
     *
     * @return
     */
    @GetMapping("/product/detail")
    @Operation(summary = "查询商品详情", description = "根据商品ID获取商品详细信息，可选传递用户ID（X-User-Id请求头）")
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
    @Operation(summary = "查询分类商品列表", description = "分页获取指定分类下的商品列表，默认页码1、每页10条")
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
    @Operation(summary = "搜索商品", description = "分页搜索商品，支持一级分类ID、二级分类ID、关键词筛选，默认页码1、每页80条，默认排序方式为default")
    public Result searchProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "80") Integer pageSize,
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
    @Operation(summary = "查询相关商品", description = "根据商品ID关联分类，获取相关商品列表，默认最多返回10条，可通过limit参数指定数量")
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
    @Operation(summary = "查询商品规格价格", description = "根据商品 ID和规格 ID获取对应规格的商品价格")
    public Result getProductSpecPrice(@RequestParam("productId") String productId, @RequestParam("specId") String specId) {
        return productService.getProductSpecPrice(productId, specId);
    }

    /**
     * 滚动查询的商品列表
     * @return
     */
    @GetMapping("/product/scroll/query/list")
    @Operation(summary = "滚动查询商品列表", description = "以滚动加载方式获取商品列表")
    public Result getSimpleProductByScrollQuery() {
        return productService.getSimpleProductByScrollQuery();
    }


    /**
     * 用户获取热门搜索关键词列表
     * @return
     */
    @GetMapping("/product/user/keyword/list")
    @Operation(summary = "查询用户端热门搜索关键词", description = "获取用户端展示的热门搜索关键词列表")
    public Result getProductSearchKeywordListUser() {
        return productSearchKeywordService.getProductSearchKeywordListUser();
    }

    /**
     * 管理员获取搜索关键词列表
     * @return
     */
    @GetMapping("/product/admin/keyword/list")
    @Operation(summary = "管理员查询搜索关键词列表", description = "管理端，管理员获取所有搜索关键词列表")
    public Result getProductSearchKeywordListAdmin(){
        return productSearchKeywordService.getProductSearchKeywordListAdmin();
    }

    /**
     * 管理员修改搜索关键词
     * @param productSearchKeywordList
     * @return
     */
    @PutMapping("/product/admin/keyword/update")
    @Operation(summary = "管理员修改搜索关键词", description = "管理端，管理员批量更新搜索关键词列表")
    public Result updateProductSearchListAdmin(@RequestBody List<ProductSearchKeyword> productSearchKeywordList){
        return productSearchKeywordService.updateProductSearchListAdmin(productSearchKeywordList);
    }
}