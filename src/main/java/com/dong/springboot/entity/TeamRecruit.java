package com.dong.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_recruit")
public class TeamRecruit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamId;

    private Integer gameId;
    private String teamName;
    private String teamCover;
    private String teamNeed;
    private String teamDesc;
    private Integer leaderId;
    private Integer needNum;
    private Integer currentNum;
    private Integer status;
    private LocalDateTime createTime;

    // getter & setter
    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public Integer getGameId() { return gameId; }
    public void setGameId(Integer gameId) { this.gameId = gameId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamCover() { return teamCover; }
    public void setTeamCover(String teamCover) { this.teamCover = teamCover; }
    public String getTeamNeed() { return teamNeed; }
    public void setTeamNeed(String teamNeed) { this.teamNeed = teamNeed; }
    public String getTeamDesc() { return teamDesc; }
    public void setTeamDesc(String teamDesc) { this.teamDesc = teamDesc; }
    public Integer getLeaderId() { return leaderId; }
    public void setLeaderId(Integer leaderId) { this.leaderId = leaderId; }
    public Integer getNeedNum() { return needNum; }
    public void setNeedNum(Integer needNum) { this.needNum = needNum; }
    public Integer getCurrentNum() { return currentNum; }
    public void setCurrentNum(Integer currentNum) { this.currentNum = currentNum; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}