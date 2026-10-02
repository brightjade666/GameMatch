package com.dong.springboot.vo;

public class FriendVO {
    private Integer friendId;
    private String name;
    private String avatar;
    private String gameRank;

    public Integer getFriendId() { return friendId; }
    public void setFriendId(Integer friendId) { this.friendId = friendId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getGameRank() { return gameRank; }
    public void setGameRank(String gameRank) { this.gameRank = gameRank; }
}
