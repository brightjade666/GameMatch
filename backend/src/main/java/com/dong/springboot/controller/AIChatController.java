package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.annotation.LogExecutionTime;
import com.dong.springboot.mapper.AiChatRecordMapper;
import com.dong.springboot.service.DoubaoService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIChatController {

    // 鏋勯€犲櫒娉ㄥ叆 鍏ㄧ▼鏃犱换浣曟敞瑙?
    private final DoubaoService doubaoService;
    private final AiChatRecordMapper AiChatRecordMapper;

    public AIChatController(DoubaoService doubaoService,
                            AiChatRecordMapper AiChatRecordMapper) {
        this.doubaoService = doubaoService;
        this.AiChatRecordMapper = AiChatRecordMapper;
    }

    @PostMapping("/chat")
    @LogExecutionTime
    public Result chat(@RequestBody Map<String, String> params) {
        try {
            String msg = params.get("msg");
            Integer userId = Integer.parseInt(params.getOrDefault("userId", "1"));
            String reply = doubaoService.chat(userId, msg);
            return Result.success(reply);
        } catch (Exception e) {
            return Result.error("AI 鏈嶅姟寮傚父");
        }
    }

    // 鏌ヨAI鑱婂ぉ鍘嗗彶
    @GetMapping("/history")
    @LogExecutionTime
    public Result history(@RequestParam Integer userId) {
        return Result.success(AiChatRecordMapper.findByUserIdOrderByCreateTimeAsc(userId));
    }
}
