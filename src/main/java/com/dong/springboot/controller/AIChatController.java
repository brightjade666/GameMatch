package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dao.AiChatRecordRepository;
import com.dong.springboot.service.DoubaoService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIChatController {

    // 构造器注入 全程无任何注解
    private final DoubaoService doubaoService;
    private final AiChatRecordRepository aiChatRecordRepository;

    public AIChatController(DoubaoService doubaoService,
                            AiChatRecordRepository aiChatRecordRepository) {
        this.doubaoService = doubaoService;
        this.aiChatRecordRepository = aiChatRecordRepository;
    }

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> params) {
        try {
            String msg = params.get("msg");
            Integer userId = Integer.parseInt(params.getOrDefault("userId", "1"));
            String reply = doubaoService.chat(userId, msg);
            return Result.success(reply);
        } catch (Exception e) {
            return Result.error("AI 服务异常");
        }
    }

    // 查询AI聊天历史
    @GetMapping("/history")
    public Result history(@RequestParam Integer userId) {
        return Result.success(aiChatRecordRepository.findByUserIdOrderByCreateTimeAsc(userId));
    }
}