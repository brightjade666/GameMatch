package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 获取历史消息
    @GetMapping("/messages")
    public Result getMessages(@RequestParam Integer teamId) {
        if (teamId == null || teamId <= 0) {
            return Result.error("队伍ID不能为空");
        }
        try {
            List<Map<String, Object>> msgs = chatService.getMessages(teamId);
            return Result.success(msgs);
        } catch (Exception e) {
            return Result.error("获取消息失败：" + e.getMessage());
        }
    }

    // 发送消息
    @PostMapping("/send")
    public Result sendMessage(@RequestBody Map<String, Object> body) {
        try {
            // 安全获取并转换数字（不会崩溃）
            Integer teamId = parseInteger(body.get("teamId"));
            Integer senderId = parseInteger(body.get("senderId"));
            String content = (String) body.get("content");
            String type = (String) body.get("type");

            // 严格判空
            if (teamId == null || senderId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整：teamId、senderId、content 不能为空");
            }
            if (teamId <= 0 || senderId <= 0) {
                return Result.error("队伍ID或用户ID不合法");
            }

            // 消息类型安全限制
            if (type == null || (!type.equals("text") && !type.equals("image"))) {
                type = "text";
            }

            chatService.saveMessage(teamId, senderId, content, type);
            return Result.success("发送成功");

        } catch (Exception e) {
            return Result.error("发送失败：" + e.getMessage());
        }
    }

    /**
     * 安全转换数字（解决前端传字符串数字导致的崩溃）
     */
    private Integer parseInteger(Object val) {
        if (val == null) return null;
        try {
            return Integer.valueOf(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
}