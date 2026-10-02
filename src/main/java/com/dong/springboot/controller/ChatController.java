package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dto.LoginUserCacheDTO;
import com.dong.springboot.service.ChatService;
import com.dong.springboot.service.UnreadCountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UnreadCountService unreadCountService;

    public ChatController(ChatService chatService, UnreadCountService unreadCountService) {
        this.chatService = chatService;
        this.unreadCountService = unreadCountService;
    }

    @GetMapping("/team/list")
    public Result getTeamChatList(HttpServletRequest request) {
        Integer userId = currentUserId(request);
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

    @PostMapping("/team/send")
    public Result sendTeamMsg(HttpServletRequest request,
                              @RequestParam Integer teamId,
                              @RequestParam(required = false) String content,
                              @RequestParam(required = false) String fileUrl,
                              @RequestParam(required = false) String msgType) {
        Integer fromId = currentUserId(request);
        return Result.success(chatService.sendTeamMessage(teamId, fromId, content, fileUrl, msgType));
    }

    @GetMapping("/team/history")
    public Result getTeamHistory(@RequestParam Integer teamId) {
        return Result.success(chatService.getTeamMessageHistory(teamId));
    }

    @GetMapping("/private/my")
    public Result myPrivateChats(HttpServletRequest request) {
        Integer userId = currentUserId(request);
        return Result.success(chatService.getMyPrivateChatList(userId));
    }

    @GetMapping("/private/members")
    public Result privateMembers(HttpServletRequest request) {
        Integer userId = currentUserId(request);
        return Result.success(chatService.getPrivateChatMemberList(userId));
    }

    @PostMapping("/private/send")
    public Result sendPrivateMsg(HttpServletRequest request,
                                 @RequestParam Integer toUserId,
                                 @RequestParam(required = false) String content,
                                 @RequestParam(required = false) String fileUrl,
                                 @RequestParam(required = false) String msgType) {
        Integer fromId = currentUserId(request);
        return Result.success(chatService.sendPrivateMessage(fromId, toUserId, content, fileUrl, msgType));
    }

    @GetMapping("/private/history")
    public Result getPrivateHistory(HttpServletRequest request, @RequestParam Integer userId1, @RequestParam Integer userId2) {
        Integer currentUserId = currentUserId(request);
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            throw new RuntimeException("未登录");
        }
        Integer otherUserId = currentUserId.equals(userId1) ? userId2 : userId1;
        Result result = Result.success(chatService.getPrivateMessageHistory(userId1, userId2));
        unreadCountService.markPrivateRead(currentUserId, otherUserId);
        return result;
    }

    @GetMapping("/private/unread-count")
    public Result privateUnreadCount(HttpServletRequest request) {
        Integer userId = currentUserId(request);
        return Result.success(unreadCountService.getPrivateCount(userId));
    }

    private Integer currentUserId(HttpServletRequest request) {
        LoginUserCacheDTO loginUser = (LoginUserCacheDTO) request.getAttribute("loginUser");
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new RuntimeException("未登录");
        }
        return loginUser.getUserId();
    }
}
