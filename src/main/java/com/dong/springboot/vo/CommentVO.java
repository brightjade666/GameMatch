package com.dong.springboot.vo;

public class CommentVO {
    private String author;
    private String text;
    private String time;
    private Integer userId;  // 新增
    private String avatar;
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}