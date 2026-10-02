package com.dong.springboot.vo;

public class TeamRankCandidateVO {

    private Integer teamId;
    private Long rank;
    private Double heat;

    public TeamRankCandidateVO(Integer teamId, Long rank, Double heat) {
        this.teamId = teamId;
        this.rank = rank;
        this.heat = heat;
    }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
    public Double getHeat() { return heat; }
    public void setHeat(Double heat) { this.heat = heat; }
}
