package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.util.BusinessTimeFormatUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "ProductSecondCommentVO", description = "二级评论 VO")
public class ProductSecondCommentVO {
    @Schema(description = "评论 id")
    private Long id;

    @Schema(description = "产品 id")
    private String productId;

    @Schema(description = "用户 id")
    private String userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "是否为买家(0/1)", allowableValues = {"0", "1"})
    private Integer isBuyer;

    @Schema(description = "是否匿名(0/1)", allowableValues = {"0", "1"})
    private Integer isAnonymous;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "评论图片")
    private String imageUrls;

    @Schema(description = "点赞总数")
    private String likeCount;

    @Schema(description = "被回复的用户 id")
    private String replyUserId;

    @Schema(description = "被回复的用户昵称")
    private String replyUserNickname;

    private LocalDateTime createTime;

    @Schema(description = "创建时间的业务文本,前端直接展示")
    private String createTimeBusinessText;


    public String getCreateTimeBusinessText() {
        return BusinessTimeFormatUtils.formatTimeDiffWithNow(createTime);
    }
}