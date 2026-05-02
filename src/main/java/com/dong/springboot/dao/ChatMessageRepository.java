package com.dong.springboot.dao;

import com.dong.springboot.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByTeamIdOrderByCreateTimeAsc(Integer teamId);
}