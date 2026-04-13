package com.app.uni_app.infrastructure.es.document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Data
@FieldNameConstants
public class ProductDocument {
    @NotNull(message = "商品 ID 不能为空")
    private Long id;

    @NotNull(message = "分类 ID 不能为空")
    private Long categoryId;

    @NotNull(message = "商品名称不能为空")
    private String name;

    private String sellPoint;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

}
