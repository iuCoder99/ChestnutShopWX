package com.app.uni_app.controller.user;

import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.PageResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.entity.ChatMessage;
import com.app.uni_app.pojo.vo.ChatSessionVO;
import com.app.uni_app.service.ChatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实时聊天相关接口
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "聊天接口", description = "实时聊天、会话列表相关接口")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/sessions")
    @Operation(summary = "获取当前用户的会话列表")
    public Result<List<ChatSessionVO>> getSessionList() {
        Long userId = Long.valueOf(BaseContext.getUserId());
        List<ChatSessionVO> list = chatService.getSessionList(userId);
        return Result.success(list);
    }

    @GetMapping("/history/{contactId}")
    @Operation(summary = "分页获取与某人的聊天历史")
    public Result<PageResult> getChatHistory(
            @PathVariable Long contactId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = Long.valueOf(BaseContext.getUserId());
        Page<ChatMessage> chatPage = chatService.getChatHistory(userId, contactId, page, size);
        
        PageResult result = PageResult.builder()
                .list(chatPage.getRecords())
                .total(chatPage.getTotal())
                .pageNum(page)
                .pageSize(size)
                .build();
        
        return Result.success(result);
    }

    @PostMapping("/clearUnread/{contactId}")
    @Operation(summary = "手动清除某会话的未读数")
    public Result<Void> clearUnread(@PathVariable Long contactId) {
        Long userId = Long.valueOf(BaseContext.getUserId());
        chatService.clearUnread(userId, contactId);
        return Result.success();
    }
}
