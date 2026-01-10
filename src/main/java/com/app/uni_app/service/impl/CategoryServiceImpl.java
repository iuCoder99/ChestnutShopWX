package com.app.uni_app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CategoryDTO;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.Category;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.uni_app.service.CategoryService;
import com.app.uni_app.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 20589
 * @description 针对表【category(商品分类表（支持一级/二级分类）)】的数据库操作Service实现
 * @createDate 2025-12-27 22:09:54
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {
    /**
     * 获取分类树
     *
     * @return
     */
    @Override
    public Result getCategoryTree() {
        //查询全部 category
        List<Category> allCategories = lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort).list();
        List<Category> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId().equals(0L))
                .collect(Collectors.toList());
        buildCategoryTree(allCategories, rootCategories);
        return Result.success(rootCategories);
    }

    /**
     * 递归 为分类树 set List<Category> children
     *
     * @param allCategories
     * @param parentCategories
     */
    private void buildCategoryTree(List<Category> allCategories, List<Category> parentCategories) {
        if (allCategories.isEmpty() || parentCategories.isEmpty()) {
            return;
        }
        Map<Long, List<Category>> groupMap = allCategories.stream().collect(Collectors.groupingBy(Category::getParentId));
        List<Category> nextCategoriesList = new ArrayList<>();
        parentCategories.forEach(category -> {
            List<Category> childrenCategories = groupMap.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(childrenCategories);
            nextCategoriesList.addAll(childrenCategories);
        });
        buildCategoryTree(allCategories, nextCategoriesList);
    }

    /**
     * 获取分类树子节点
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result getCategoryChildren(String categoryId) {
        List<Category> allCategories = lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort).list();
        List<Category> categoryOneList = allCategories.stream()
                .filter(category -> category.getId().toString().equals(categoryId))
                .toList();
        buildCategoryTree(allCategories, categoryOneList);
        return Result.success(categoryOneList);
    }

    /**
     * 添加分类
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public Result addCategory(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        boolean isSuccess = save(category);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return getCategoryChildren(category.getId().toString());

    }

    /**
     * 删除分类
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result deleteCategory(String categoryId) {
        boolean isSuccess = removeById(categoryId);
        if (!isSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        return Result.success();
    }


    /**
     * 更新分类
     *
     * @param id
     * @return
     */
    @Override
    public Result updateCategoryInfo(String id, CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        category.setId(Long.valueOf(id));
        boolean isSuccess = updateById(category);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(category);
    }

    /**
     * 更新分类状态
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public Result updateCategoryStatus(String id, String status) {
        boolean isSuccess = lambdaUpdate().eq(Category::getId, id).set(Category::getStatus, status).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put(Category.Fields.id,id);
        map.put(Category.Fields.status, CommonStatus.getValueByNumber(Integer.valueOf(status)));
        return Result.success(map);
    }
}



