package com.zhangzc.sharethingchatimpl.controller;

import com.zhangzc.sharethingchatimpl.domain.entity.ChatConversation;
import com.zhangzc.sharethingchatimpl.domain.entity.ChatMessage;
import com.zhangzc.sharethingchatimpl.service.ChatService;

import com.zhangzc.sharethingchatimpl.utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 拉取历史消息
     * @param userId1 自己的ID
     * @param userId2 对方的ID
     * @param lastCreateTime 游标 (上一页最后一条消息的时间)，第一页传空
     * @param size 每页条数
     */
    @GetMapping("/history")
    public List<ChatMessage> getHistory(
            @RequestParam String userId1,
            @RequestParam String userId2,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date lastCreateTime,
            @RequestParam(defaultValue = "20") int size) {
        return chatService.getHistoryMessages(userId1, userId2, lastCreateTime, size);
    }

    /**
     * 标记会话已读
     * @param userId 当前用户ID
     * @param targetUserId 对方ID
     */
    @PostMapping("/read")
    public R<Boolean> markAsRead(@RequestParam String userId, @RequestParam String targetUserId) {
        chatService.markAsRead(userId, targetUserId);
        return R.ok(true);
    }

    /**
     * 获取会话列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     */
    @GetMapping("/conversations")
    public R<Page<ChatConversation>> getConversations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(chatService.getConversationList(userId, page, size));
    }
}
