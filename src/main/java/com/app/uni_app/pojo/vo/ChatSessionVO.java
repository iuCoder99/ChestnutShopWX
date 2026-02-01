package com.app.uni_app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 前端展示会话列表的 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionVO {

    private Long contactId;

    private String contactNickname;

    private String contactAvatar;

    private String lastMsgContent;

    private LocalDateTime lastMsgTime;

    private Integer unreadCount;
}
