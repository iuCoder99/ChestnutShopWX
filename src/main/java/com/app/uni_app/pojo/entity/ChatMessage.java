package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实时聊天消息记录表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_message")
public class ChatMessage implements Serializable {

    @TableId(type = IdType.ASSIGN_ID) // 使用雪花算法生成ID
    private Long id;

    /**
     * 发送者ID (关联 sys_user.id)
     */
    private Long fromUserId;

    /**
     * 接收者ID (关联 sys_user.id)
     */
    private Long toUserId;

    /**
     * 消息内容 (文本或 JSON 格式)
     */
    private String content;

    /**
     * 消息类型: 0-文本, 1-图片, 2-商品卡片, 3-语音
     */
    private Integer msgType;

    /**
     * 关联商品ID (可选)
     */
    private Long productId;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
