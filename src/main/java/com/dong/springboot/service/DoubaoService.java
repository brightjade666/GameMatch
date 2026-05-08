package com.dong.springboot.service;

import com.dong.springboot.dao.AiChatRecordRepository;
import com.dong.springboot.entity.AiChatRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoubaoService {

    @Value("${doubao.api-key}")
    private String apiKey;

    @Value("${doubao.endpoint-id}")
    private String endpointId;

    @Value("${doubao.url}")
    private String apiUrl;

    // 构造器注入（你项目统一风格）
    private final AiChatRecordRepository aiChatRecordRepository;
    private final Map<String, List<Map<String, Object>>> contextMap = new HashMap<>();

    // 构造器注入 → 完全不用 @Resource @Autowired
    public DoubaoService(AiChatRecordRepository aiChatRecordRepository) {
        this.aiChatRecordRepository = aiChatRecordRepository;
    }

    public String chat(Integer userId, String userMessage) {
        try {
            List<Map<String, Object>> messages = contextMap.getOrDefault(userId.toString(), new ArrayList<>());

            if (messages.isEmpty()) {
                Map<String, Object> system = new HashMap<>();
                system.put("role", "system");
                system.put("content", "你是友好的游戏开黑AI助手");
                messages.add(system);
            }

            Map<String, Object> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", userMessage);
            messages.add(user);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", endpointId);
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

            Map<String, Object> resBody = response.getBody();
            List<Map> choices = (List<Map>) resBody.get("choices");
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
            String aiReply = (String) messageObj.get("content");

            // 上下文记忆
            Map<String, Object> assistant = new HashMap<>();
            assistant.put("role", "assistant");
            assistant.put("content", aiReply);
            messages.add(assistant);
            contextMap.put(userId.toString(), messages);

            // ======================
            // 保存聊天记录到数据库
            // ======================
            AiChatRecord record = new AiChatRecord();
            record.setUserId(userId);
            record.setUserMsg(userMessage);
            record.setAiReply(aiReply);
            record.setChatType("doubao");
            record.setCreateTime(LocalDateTime.now());
            aiChatRecordRepository.save(record);

            return aiReply;

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 走神啦，再问我一次~";
        }
    }
}