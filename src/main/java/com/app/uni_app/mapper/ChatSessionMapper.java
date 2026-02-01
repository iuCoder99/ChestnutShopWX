package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.ChatSession;
import com.app.uni_app.pojo.vo.ChatSessionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话列表表 Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 获取当前用户的会话列表 (带联系人昵称、头像、未读数)
     */
    List<ChatSessionVO> selectSessionList(@Param("userId") Long userId);
}
