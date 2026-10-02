package com.dong.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("team_heat_stat")
public class TeamHeatStat {

    @TableId(value = "team_id", type = IdType.INPUT)
    private Integer teamId;
    private Long viewCount;
    private Long commentCount;
    private Long applyCount;
    private Long publishScore;
    private Long totalScore;
    private LocalDateTime updateTime;

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public Long getApplyCount() { return applyCount; }
    public void setApplyCount(Long applyCount) { this.applyCount = applyCount; }
    public Long getPublishScore() { return publishScore; }
    public void setPublishScore(Long publishScore) { this.publishScore = publishScore; }
    public Long getTotalScore() { return totalScore; }
    public void setTotalScore(Long totalScore) { this.totalScore = totalScore; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
