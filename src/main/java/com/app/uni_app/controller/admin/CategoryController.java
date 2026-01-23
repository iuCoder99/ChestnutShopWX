package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CategoryDTO;
import com.app.uni_app.service.CategoryService;
import com.app.uni_app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "分类管理")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @Resource
    private ProductService productService;

    /**
     * 获取分类树
     *
     * @return
     */
    @GetMapping("/category/tree")
    @Operation(summary = "查询分类树", description = "获取全部分类的树形结构数据")
    public Result getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 添加分类
     *
     * @return
     */
    @PostMapping("/admin/category/add")
    @Operation(summary = "管理员新增分类", description = "管理端，管理员新增商品分类")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.addCategory(categoryDTO);
    }

    /**
     * 删除分类
     *
     * @param categoryId
     * @return
     */
    @DeleteMapping("/admin/category/{categoryId}")
    @Operation(summary = "管理员删除分类", description = "管理端，管理员根据分类ID删除分类")
    public Result deleteCategory(@PathVariable String categoryId) {
        return categoryService.deleteCategory(categoryId);
    }

    /**
     * 更新分类
     *
     * @param id
     * @return
     */
    @PutMapping("/admin/category/{id}")
    @Operation(summary = "管理员修改分类信息", description = "管理端，管理员根据分类ID修改分类详情")
    public Result updateCategoryInfo(@PathVariable String id, @RequestBody CategoryDTO categoryDTO) {
        return categoryService.updateCategoryInfo(id, categoryDTO);
    }

    /**
     * 更新分类状态
     *
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/admin/category/{id}/status")
    @Operation(summary = "管理员修改分类状态", description = "管理端，管理员根据分类ID更新分类状态")
    public Result updateCategoryStatus(@PathVariable String id, @RequestParam String status) {
        return categoryService.updateCategoryStatus(id, status);
    }

    /**
     * 查看二级分类下的商品列表
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/category/product/list/{categoryId}/{beginProductId}")
    @Operation(summary = "查询二级分类商品列表", description = "根据二级分类ID和起始商品ID，获取该分类下的商品列表")
    public Result getCategoryProductList(@PathVariable @NotBlank String categoryId, @PathVariable String beginProductId) {
        return productService.getCategoryProductList(categoryId, beginProductId);
    }

}