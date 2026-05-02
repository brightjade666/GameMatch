package com.dong.springboot.service;

import com.dong.springboot.dao.ChatMessageRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.ChatMessage;
import com.dong.springboot.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepo;
    private final UserRepository userRepo;

    public ChatService(ChatMessageRepository chatMessageRepo, UserRepository userRepo) {
        this.chatMessageRepo = chatMessageRepo;
        this.userRepo = userRepo;
    }

    public void saveMessage(Integer teamId, Integer senderId, String content, String type) {
        ChatMessage msg = new ChatMessage();
        msg.setTeamId(teamId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setType(type);
        msg.setCreateTime(LocalDateTime.now());
        chatMessageRepo.save(msg);
    }

    public List<Map<String, Object>> getMessages(Integer teamId) {
        List<ChatMessage> list = chatMessageRepo.findByTeamIdOrderByCreateTimeAsc(teamId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage msg : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("teamId", msg.getTeamId());
            map.put("senderId", msg.getSenderId());
            map.put("content", msg.getContent());
            map.put("type", msg.getType());
            map.put("createTime", msg.getCreateTime().toString());

            User sender = userRepo.findById(msg.getSenderId()).orElse(null);
            if (sender != null) {
                map.put("senderName", sender.getUsername());
                map.put("senderAvatar", sender.getAvatar());
            } else {
                map.put("senderName", "未知用户");
                map.put("senderAvatar", "");
            }
            result.add(map);
        }
        return result;
    }
}