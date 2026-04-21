package com.app.uni_app.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Schema(description = "简单通用游标查询封装类")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleCursorCommonEntity {

    @Schema(description = "末尾查询值")
    private String sortValue;

    @Schema(description = "末尾查询 id")
    private Long sortId;

    @Schema(description = "查询数量 , 默认查询数量 : 20")
    private Integer querySize;


    public Integer getQuerySize() {
        return querySize == null ? 20 : querySize;
    }
}
