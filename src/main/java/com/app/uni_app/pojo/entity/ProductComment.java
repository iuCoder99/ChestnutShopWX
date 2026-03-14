package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 商品评论实体类
 * 对应表：product_comment
 */
@Data
@TableName(value = "product_comment")
@Accessors(chain = true)
@FieldNameConstants
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductComment {

    /**
     * 评论唯一ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID，关联商品表
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 商品规格 id
     */
    @TableField("product_spec_id")
    private Long productSpecId;

    /**
     * 商品规格简介文本
     */
    @TableField("product_spec_text")
    private String productSpecText;

    /**
     * 订单ID，关联订单表
     */
    @TableField("order_no")
    private String orderNo;


    /**
     * 评论人用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 父评论ID：0=一级评论（直接评商品），>0=二级回复（对应本表的评论ID）
     */
    @TableField("parent_id")
    private Long parentId = 0L;

    /**
     * 被回复人用户ID，二级回复专用，前端展示「回复@XXX」
     */
    @TableField("reply_user_id")
    private Long replyUserId;

    /**
     * 是否为买家
     */
    @TableField("is_buyer")
    private int isBuyer = 0;


    /**
     * 是否追评
     */
    @TableField("is_append_comment")
    private int isAppendComment = 0;


    /**
     * 是否匿名评论
     */
    @TableField("is_anonymous")
    private int isAnonymous = 0;

    /**
     * 是否为好评
     * 评分大于等于四星
     */
    @TableField("is_good_review")
    private int isGoodReview ;


    /**
     * 回复用户昵称
     */
    @TableField("reply_user_nickname")
    private String replyUserNickname;

    /**
     * 商品评分：1-5星（仅一级评论必填，二级回复默认0）
     */
    @TableField("rating")
    private Byte rating = 0;

    /**
     * 评论/回复内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论图片，JSON数组格式存储，例：["url1","url2"]
     */
    @TableField("image_urls")
    private String imageUrls;

    /**
     * 点赞总数，冗余字段，从Redis同步过来，用于排序展示
     */
    @TableField("like_count")
    private Integer likeCount = 0;

    /**
     * 审核状态：0=待审核，1=已通过，2=已驳回
     */
    @TableField("status")
    private Byte status = 0;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    public int getIsGoodReview() {
        if (this.rating == null) {
            return 0;
        }
        return this.rating >= 4 ? 1 : 0;
    }

}
