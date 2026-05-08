package com.dong.springboot.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_chat_record")
public class AiChatRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_msg", columnDefinition = "TEXT")
    private String userMsg;

    @Column(name = "ai_reply", columnDefinition = "TEXT")
    private String aiReply;

    @Column(name = "chat_type")
    private String chatType;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}