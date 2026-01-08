package com.app.uni_app.mapper;


import com.app.uni_app.pojo.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 20589
 * @description 针对表【product(商品表)】的数据库操作Mapper
 * @createDate 2025-12-23 19:32:49
 * @Entity com.app.uni_app.Product
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    List<Product> selectOrderByDescSalesCountLimit(Integer limit);

    Product selectByProductId(String productId);

    IPage<Product> selectByCategoryIdPage(Page<Product> productPage, String categoryId);

    IPage<Product> selectByFirstCategoryIdAndKeywordPage(Page<Product> productPage, String firstCategoryId, String dbValue, String keyword);

    IPage<Product> selectBySecondCategoryIdAndKeywordPage(Page<Product> productPage,String firstCategoryId,String secondCategoryId,String dbValue, String keyword);

    List<Product> selectRelatedByCategoryId(String productId, Integer limit);
}







