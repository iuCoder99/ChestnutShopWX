package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CategoryDTO;
import com.app.uni_app.pojo.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 20589
 * @description 针对表【category(商品分类表（支持一级/二级分类）)】的数据库操作Service
 * @createDate 2025-12-27 22:09:54
 */
public interface CategoryService extends IService<Category> {

    Result getCategoryTree();

    Result getCategoryChildren(String categoryId);

    Result addCategory(CategoryDTO categoryDTO);

    Result deleteCategory(String categoryId);

    Result updateCategoryInfo(String id,CategoryDTO categoryDTO);

    Result updateCategoryStatus(String id, String status);

}

