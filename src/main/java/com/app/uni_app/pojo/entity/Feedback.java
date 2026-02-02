package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.FeedbackStatusEnum;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户反馈表 实体类
 * 对应数据表：feedback
 */
@Data
@TableName(value = "feedback")
@Builder
public class Feedback implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID（外键）
     */
    @TableField(value = "user_id")
    private Long userId;


    /**
     * 反馈内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 反馈图片URL（多个URL用英文逗号分隔，示例：url1,url2）
     */
    @TableField(value = "image_urls")
    private String imageUrls;

    /**
     * 联系方式
     */
    @TableField(value = "contact")
    private String contact;

    /**
     * 回复内容
     */
    @TableField(value = "reply_content")
    private String replyContent;

    /**
     * 回复的管理员 ID
     */
    @TableField(value = "reply_admin_id")
    private Long replyAdminId;

    /**
     * 处理状态
     * 对应数据表 enum
     */
    @TableField(value = "status")
    private FeedbackStatusEnum status;

    /**
     * 反馈时间
     * 对应数据表 default CURRENT_TIMESTAMP
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充（需配置填充处理器）
    private LocalDateTime createTime;

    /**
     * 回复时间
     */
    @TableField(value = "reply_time")
    private LocalDateTime replyTime;
}