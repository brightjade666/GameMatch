package com.dong.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("team_message")
public class TeamMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer teamId;
    private Integer fromId;
    private String content;
    private String msgType;
    private String fileUrl;
    private LocalDateTime sendTime;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatar;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public Integer getFromId() { return fromId; }
    public void setFromId(Integer fromId) { this.fromId = fromId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
