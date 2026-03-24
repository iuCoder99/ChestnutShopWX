package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.ProductCommentLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductCommentLikeMapper  extends BaseMapper<ProductCommentLike> {
    int batchUpdate (@Param("list") List<ProductCommentLike> productCommentLikeList);
}
