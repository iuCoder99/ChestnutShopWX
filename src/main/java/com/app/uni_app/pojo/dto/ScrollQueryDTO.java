package com.app.uni_app.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScrollQueryDTO {
    @Schema( name = "游标分页开始商品 ID")
    private Long beginId;
}
