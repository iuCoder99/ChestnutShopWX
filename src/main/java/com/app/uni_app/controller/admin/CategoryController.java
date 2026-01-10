package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CategoryDTO;
import com.app.uni_app.service.CartService;
import com.app.uni_app.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.lang.ref.PhantomReference;

@RestController
@RequestMapping("/api")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    /**
     * 获取分类树
     *
     * @return
     */
    @GetMapping("/category/tree")
    public Result getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 获取分类树字节点
     *
     * @return
     */
    @GetMapping("/category/children/{categoryId}")
    public Result getCategoryChildren(@PathVariable String categoryId) {
        return categoryService.getCategoryChildren(categoryId);
    }

    /**
     * 添加分类
     *
     * @return
     */
    @PostMapping("/admin/category/add")
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
    public Result updateCategoryInfo(@PathVariable String id,@RequestBody CategoryDTO categoryDTO) {
        return categoryService.updateCategoryInfo(id,categoryDTO);
    }


    /**
     * 更新分类状态
     *
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/admin/category/{id}/status")
    public Result updateCategoryStatus(@PathVariable String id, @RequestParam String status) {
        return categoryService.updateCategoryStatus(id, status);
    }


}
