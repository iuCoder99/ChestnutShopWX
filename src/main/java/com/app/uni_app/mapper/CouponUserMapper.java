package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.CouponUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponUserMapper extends BaseMapper<CouponUser> {
    // 批量更新同一个字段
    void batchUpdateSameField(@Param("idList") List<Long> idList, @Param("valueList") List<Integer> valueList);
}
