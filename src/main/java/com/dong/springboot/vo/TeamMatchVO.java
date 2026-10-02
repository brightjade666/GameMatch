package com.dong.springboot.vo;

import java.util.List;

public class TeamMatchVO {

    private Integer teamId;
    private String teamName;
    private String teamCover;
    private Integer gameId;
    private Integer leaderId;
    private String teamNeed;
    private String teamDesc;
    private Integer currentNum;
    private Integer needNum;
    private Integer remainingNum;
    private Long rank;
    private Double heat;
    private List<String> reasons;

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamCover() { return teamCover; }
    public void setTeamCover(String teamCover) { this.teamCover = teamCover; }
    public Integer getGameId() { return gameId; }
    public void setGameId(Integer gameId) { this.gameId = gameId; }
    public Integer getLeaderId() { return leaderId; }
    public void setLeaderId(Integer leaderId) { this.leaderId = leaderId; }
    public String getTeamNeed() { return teamNeed; }
    public void setTeamNeed(String teamNeed) { this.teamNeed = teamNeed; }
    public String getTeamDesc() { return teamDesc; }
    public void setTeamDesc(String teamDesc) { this.teamDesc = teamDesc; }
    public Integer getCurrentNum() { return currentNum; }
    public void setCurrentNum(Integer currentNum) { this.currentNum = currentNum; }
    public Integer getNeedNum() { return needNum; }
    public void setNeedNum(Integer needNum) { this.needNum = needNum; }
    public Integer getRemainingNum() { return remainingNum; }
    public void setRemainingNum(Integer remainingNum) { this.remainingNum = remainingNum; }
    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
    public Double getHeat() { return heat; }
    public void setHeat(Double heat) { this.heat = heat; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}
