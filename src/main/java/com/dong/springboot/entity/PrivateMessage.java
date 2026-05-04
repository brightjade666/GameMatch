package com.dong.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "private_message")
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer fromId;
    private Integer toId;
    private String content;
    private String msgType;
    private String fileUrl;
    private LocalDateTime sendTime;
    private Integer isRead;

    // 用于前端显示：昵称 + 头像
    @Transient  // 不存入数据库，只用于接口返回
    private String username;

    @Transient  // 不存入数据库，只用于接口返回
    private String avatar;

    // ==================== GETTER & SETTER ====================
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}