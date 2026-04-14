package com.dong.springboot.vo;

import java.time.LocalDateTime;
import java.util.List;
import com.dong.springboot.entity.TeamMember;

public class TeamVO {

    // 字段
    private Integer teamId;
    private String teamName;
    private Integer leaderId;
    private LocalDateTime joinTime;
    private List<TeamMember> members;

    // 空构造（必须要有！）
    public TeamVO() {
    }

    // getter 和 setter 👇👇👇
    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
    }
}