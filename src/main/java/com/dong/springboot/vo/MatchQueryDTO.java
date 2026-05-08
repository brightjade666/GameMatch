package com.dong.springboot.vo;

public class MatchQueryDTO {
    private Integer gameId;
    private String gameName;
    private String playtimeStart;
    private String playtimeEnd;
    private String matchNeed;
    private String personality;
    private Integer excludeUserId;

    // ====== 手动 getter/setter ======
    public Integer getGameId() { return gameId; }
    public void setGameId(Integer gameId) { this.gameId = gameId; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getPlaytimeStart() { return playtimeStart; }
    public void setPlaytimeStart(String playtimeStart) { this.playtimeStart = playtimeStart; }

    public String getPlaytimeEnd() { return playtimeEnd; }
    public void setPlaytimeEnd(String playtimeEnd) { this.playtimeEnd = playtimeEnd; }

    public String getMatchNeed() { return matchNeed; }
    public void setMatchNeed(String matchNeed) { this.matchNeed = matchNeed; }

    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }

    public Integer getExcludeUserId() { return excludeUserId; }
    public void setExcludeUserId(Integer excludeUserId) { this.excludeUserId = excludeUserId; }
}