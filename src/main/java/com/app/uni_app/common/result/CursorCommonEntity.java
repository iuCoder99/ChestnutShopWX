package com.app.uni_app.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Schema(description = "通用游标查询封装类")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CursorCommonEntity {

    @Schema(description = "查询种类")
    private String sortType;

    @Schema(description = "末尾查询值")
    private String sortValue ;

    @Schema(description = "末尾查询 id")
    private Long sortId;

    @Schema(description = "查询数量")
    private Integer querySize = 20;
}
