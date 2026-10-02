package com.dong.springboot.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.dong.springboot.entity.TeamMember;

public class TeamVO {

    private Integer teamId;
    private String teamName;
    private Integer leaderId;
    private LocalDateTime joinTime;
    private List<TeamMember> members;
    // 鏂板瀛楁锛氭垚鍛樿缁嗕俊鎭紙鍖呭惈鏄电О鍜屽姞鍏ユ椂闂达級
    private List<Map<String, Object>> memberDetails;

    // === 鍦?TeamVO 绫讳腑娣诲姞浠ヤ笅浠ｇ爜 ===
    private String teamCover;

    public String getTeamCover() {
        return teamCover;
    }
    public void setTeamCover(String teamCover) {
        this.teamCover = teamCover;
    }

    public TeamVO() {
    }

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
    public List<Map<String, Object>> getMemberDetails() {
        return memberDetails;
    }
    public void setMemberDetails(List<Map<String, Object>> memberDetails) {
        this.memberDetails = memberDetails;
    }
}