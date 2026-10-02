package com.dong.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("team_member")
public class TeamMember {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer teamId;
    private Integer userId;
    private LocalDateTime joinTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
}
