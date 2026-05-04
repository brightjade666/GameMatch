package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ==================== 队伍接口 ====================
    @GetMapping("/team/list")
    public Result getTeamChatList(@RequestParam Integer userId) {
        return Result.success(chatService.getTeamChatList(userId));
    }

    @GetMapping("/team/members")
    public Result getTeamMembers(@RequestParam Integer teamId) {
        return Result.success(chatService.getTeamMemberList(teamId));
    }

    @PostMapping("/team/member/add")
    public Result addTeamMember(@RequestParam Integer teamId, @RequestParam Integer userId) {
        chatService.addTeamMember(teamId, userId);
        return Result.success("添加成功");
    }

    // ✅ 支持 文字 / 图片 / 视频
    @PostMapping("/team/send")
    public Result sendTeamMsg(
            @RequestParam Integer teamId,
            @RequestParam Integer fromId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) String msgType
    ) {
        return Result.success(chatService.sendTeamMessage(teamId, fromId, content, fileUrl, msgType));
    }

    @GetMapping("/team/history")
    public Result getTeamHistory(@RequestParam Integer teamId) {
        return Result.success(chatService.getTeamMessageHistory(teamId));
    }

    // ==================== 私聊接口 ====================
    @GetMapping("/private/my")
    public Result myPrivateChats(@RequestParam Integer userId) {
        return Result.success(chatService.getMyPrivateChatList(userId));
    }

    @GetMapping("/private/members")
    public Result privateMembers(@RequestParam Integer userId) {
        return Result.success(chatService.getPrivateChatMemberList(userId));
    }

    // ✅ 支持 文字 / 图片 / 视频
    @PostMapping("/private/send")
    public Result sendPrivateMsg(
            @RequestParam Integer fromId,
            @RequestParam Integer toUserId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) String msgType
    ) {
        return Result.success(chatService.sendPrivateMessage(fromId, toUserId, content, fileUrl, msgType));
    }

    @GetMapping("/private/history")
    public Result getPrivateHistory(@RequestParam Integer userId1, @RequestParam Integer userId2) {
        return Result.success(chatService.getPrivateMessageHistory(userId1, userId2));
    }
}