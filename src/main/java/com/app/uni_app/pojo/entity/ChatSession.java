package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话列表表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_session")
public class ChatSession implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID (较小值)
     */
    private Long userId;

    /**
     * 联系人ID (较大值)
     */
    private Long contactId;

    /**
     * 最后一条消息摘要
     */
    private String lastMsgContent;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMsgTime;

    /**
     * userId 的未读数
     */
    private Integer unreadCountA;

    /**
     * contactId 的未读数
     */
    private Integer unreadCountB;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
