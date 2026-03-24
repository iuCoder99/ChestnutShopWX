package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.ProductComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {
    int updateProductCommentLikeCount(@Param("map") Map<Long,Integer> dataMap);
}
