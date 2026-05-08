package com.dong.springboot.vo;

import java.util.List;

public class TeamDetailVO {
    private Integer id;
    private String title;
    private String avatar;
    private String leader;
    private String need;
    private Integer leaderId;
    private String desc;
    private List<CommentVO> comments;

    public Integer getLeaderId() { return leaderId; }
    public void setLeaderId(Integer leaderId) { this.leaderId = leaderId; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getLeader() { return leader; }
    public void setLeader(String leader) { this.leader = leader; }
    public String getNeed() { return need; }
    public void setNeed(String need) { this.need = need; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public List<CommentVO> getComments() { return comments; }
    public void setComments(List<CommentVO> comments) { this.comments = comments; }
}