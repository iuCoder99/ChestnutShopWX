package com.app.uni_app.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ScrollQueryResult {
    @Schema(name = "游标分页结束商品 ID")
    private long  endId;
    @Schema(name = "查询列表")
    List<?> list;
}
