package com.app.uni_app.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "追评请求参数")
public class AppendProductFirstCommentDTO {

    @Schema(description = "评论关联商品 ID")
    private String productId;

    @Schema(description = "订单单号")
    private String orderNo;

    @Schema(description = "追评内容")
    private String content;

    @Schema(description = "追评图片")
    private String imageUrls;
}