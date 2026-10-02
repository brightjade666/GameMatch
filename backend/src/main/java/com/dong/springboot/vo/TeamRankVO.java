package com.dong.springboot.vo;

public class TeamRankVO {

    private Long rank;
    private Double heat;
    private TeamDetailVO team;

    public TeamRankVO() {
    }

    public TeamRankVO(Long rank, Double heat, TeamDetailVO team) {
        this.rank = rank;
        this.heat = heat;
        this.team = team;
    }

    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
    public Double getHeat() { return heat; }
    public void setHeat(Double heat) { this.heat = heat; }
    public TeamDetailVO getTeam() { return team; }
    public void setTeam(TeamDetailVO team) { this.team = team; }
}
