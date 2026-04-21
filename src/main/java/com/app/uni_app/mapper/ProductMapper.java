package com.app.uni_app.mapper;


import com.app.uni_app.pojo.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author 20589
 * @description 针对表【product(商品表)】的数据库操作Mapper
 * @createDate 2025-12-23 19:32:49
 * @Entity com.app.uni_app.Product
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    List<Product> selectOrderByDescSalesCountLimit(Integer limit);

    Product selectByProductId(String productId, String userId);

    List<Product> getBriefProduct(@Param("productIdsList") List<Long> productIdsList);


    List<Product> getProductDetailByProductIdSet(@Param("productIdSet") Set<Long> productIdSet);
}








