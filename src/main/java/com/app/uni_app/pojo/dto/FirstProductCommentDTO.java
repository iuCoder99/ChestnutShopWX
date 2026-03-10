package com.app.uni_app.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品一级评论提交 DTO
 */
@Data
@Schema(name = "FirstProductCommentDTO", description = "商品一级评论提交参数")
public class FirstProductCommentDTO {

    @Schema(description = "商品 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    @NotBlank(message = "商品 ID不能为空")
    private String productId;

    @Schema(description = "商品规格 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20001")
    @NotBlank(message = "商品规格 ID不能为空")
    private String productSpecId;

    @Schema(description = "商品规格文本", requiredMode = Schema.RequiredMode.REQUIRED, example = "颜色：黑色 | 尺寸：XL")
    @NotBlank(message = "商品规格文本不能为空")
    private String productSpecText;

    @Schema(description = "订单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30001")
    @NotBlank(message = "订单 ID不能为空")
    private String orderId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三123")
    @NotBlank(message = "用户昵称不能为空")
    private String userNickname;

    @Schema(description = "用户头像 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/avatar.jpg")
    @NotBlank(message = "用户头像不能为空")
    private String userAvatar;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "这款商品质量很好，值得购买！")
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片URL集合（JSON数组格式）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"https://example.com/img1.jpg\",\"https://example.com/img2.jpg\"]")
    @NotBlank(message = "评论图片不能为空")
    private String imageUrls;

    @Schema(description = "商品评分:1-5星",requiredMode = Schema.RequiredMode.REQUIRED)
    private int rating;

    @Schema(description = "是否匿名评论（0=否，1=是）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0", defaultValue = "0")
    private int isAnonymous = 0;
}