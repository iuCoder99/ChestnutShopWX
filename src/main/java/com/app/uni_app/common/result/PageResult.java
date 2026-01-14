package com.app.uni_app.common.result;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {

    List<?> list;
    /**
     * 总商品数
     */
    Long total;
    /**
     * 当前页码
     */
    Integer pageNum = 1;
    /**
     * 每页数量
     */
    Integer pageSize = 10;
    /**
     * 总页数
     * 自动计算,无需传入
     */
    Long totalPages;


    public Long getTotalPages() {
        // 1. 边界值判断：总记录数为null或0，直接返回0
        if (total == null || total <= 0) {
            return 0L;
        }
        // 2. 边界值判断：每页数量为null或<=0，默认按1条/页计算（避免除0异常）
        if (pageSize == null || pageSize <= 0) {
            pageSize = 1;
        }
        // 3. 核心计算：总页数 = (总记录数 + 每页数量 - 1) / 每页数量 （向上取整公式）
        return (total + pageSize - 1) / pageSize;
    }


}
