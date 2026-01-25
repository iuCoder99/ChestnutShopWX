package com.app.uni_app.service.impl;


import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.CaffeineUtils;
import com.app.uni_app.mapper.CategoryMapper;
import com.app.uni_app.pojo.dto.CategoryDTO;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.Category;
import com.app.uni_app.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 20589
 * @description 针对表【category(商品分类表（支持一级/二级分类）)】的数据库操作Service实现
 * @createDate 2025-12-27 22:09:54
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {
    @Resource
    private CopyMapper copyMapper;

    @Resource
    private CaffeineUtils caffeineUtils;
    /**
     * 获取分类树
     *
     * @return
     */
    @Override
    public Result getCategoryTree() {
        //返回分类树缓存
        List<Category> rootCategories = caffeineUtils.getCategoryTree();
        return Result.success(rootCategories);
    }

    /**
     * loadingCache 存储分类树缓存
     * @return
     */
    public List<Category> getCategoryTreeCache() {
        //查询全部 category
        List<Category> allCategories = lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort).list();
        List<Category> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId().equals(DataConstant.ZERO_LONG))
                .collect(Collectors.toList());
        buildCategoryTree(allCategories, rootCategories);
        return rootCategories;
    }

    /**
     * 递归 为分类树 set List<Category> children
     *
     * @param allCategories
     * @param parentCategories
     */
    private void buildCategoryTree(List<Category> allCategories, List<Category> parentCategories) {
        if (CollectionUtils.isEmpty(allCategories) || CollectionUtils.isEmpty(parentCategories)) {
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
        List<Category> categoryTree = caffeineUtils.getCategoryTree();
        List<Category> list = categoryTree.stream().filter(category -> StringUtils.equals(categoryId, category.getId().toString())).toList();
        if (list.isEmpty()||list.size()>DataConstant.ONE_INT){
            return Result.error(MessageConstant.DATA_ERROR);
        }
        List<Category> categoryChildrenList = list.get(DataConstant.ZERO_INT).getChildren();
        return Result.success(categoryChildrenList);
    }

    /**
     * 添加分类
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public Result addCategory(CategoryDTO categoryDTO) {
        Category category = copyMapper.categoryDTOToCategory(categoryDTO);
        boolean isSuccess = save(category);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        caffeineUtils.invalidateCategoryTree();
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
        caffeineUtils.invalidateCategoryTree();
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
        Category category = copyMapper.categoryDTOToCategory(categoryDTO);
        category.setId(Long.valueOf(id));
        boolean isSuccess = updateById(category);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        caffeineUtils.invalidateCategoryTree();
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
        map.put(Category.Fields.id, id);
        map.put(Category.Fields.status, CommonStatus.getValueByNumber(Integer.valueOf(status)));
        caffeineUtils.invalidateCategoryTree();
        return Result.success(map);
    }

}



