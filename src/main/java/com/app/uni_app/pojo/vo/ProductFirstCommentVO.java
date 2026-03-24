package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.util.BusinessTimeFormatUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "商品首评 VO")
public class ProductFirstCommentVO {
    @Schema(description = "评论 id")
    private Long id;

    @Schema(description = "产品 id")
    private String productId;

    @Schema(description = "产品规格 id")
    private String productSpecId;

    @Schema(description = "产品规格简介")
    private String productSpecText;

    @Schema(description = "用户 id")
    private String userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "是否为买家(0/1)")
    private Integer isBuyer;

    @Schema(description = "是否追加评论(0/1)")
    private Integer isAppendComment;

    @Schema(description = "是否匿名(0/1)")
    private Integer isAnonymous;

    @Schema(description = "是否好评(0/1)")
    private Integer isGoodReview;

    @Schema(description = "评分(1-5)")
    private String rating;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "评论图片")
    private String imageUrls;

    @Schema(description = "点赞总数")
    private String likeCount;

    @Schema(description = "当前用户是否点赞")
    private boolean like;

    private LocalDateTime createTime;

    @Schema(description = "创建时间的业务文本,前端直接展示")
    private String createTimeBusinessText;


    public String getCreateTimeBusinessText() {
        return BusinessTimeFormatUtils.formatTimeDiffWithNow(createTime);
    }
}