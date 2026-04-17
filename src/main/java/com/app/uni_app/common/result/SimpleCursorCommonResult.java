package com.app.uni_app.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游标查询通用返回类
 */
@Data
@Schema(description = "简单游标查询通用返回类")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleCursorCommonResult {

    @Schema(description = "通用游标返回实体")
    private SimpleCursorCommonEntity simpleCursorCommonEntity;

    @Schema(description = "查询结果列表")
    private List<?> list;

    @Schema(description = "是否查完")
    private Boolean isEnd = false;
}
