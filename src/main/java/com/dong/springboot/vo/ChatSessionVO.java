package com.dong.springboot.vo;

import lombok.Data;

@Data
public class ChatSessionVO {
    private Integer sessionId;     // 会话ID：队伍ID / 私聊对方用户ID
    private String sessionType;    // team 队伍 / private 私聊
    private String name;           // 队伍名 / 好友昵称
    private String avatar;         // 队伍封面 / 好友头像
    private String lastMsg;        // 最后一条消息
    private String lastTime;       // 最后消息时间

    // 只给队伍用：队长ID；私聊自动为null，前端判断有值才显示「队长」标签
    private Integer ownerId;
}