package com.dong.springboot.service;

import com.dong.springboot.dao.*;
import com.dong.springboot.entity.*;
import com.dong.springboot.vo.CommentVO;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRecruitRepository teamRecruitRepo;
    private final UserRepository userRepo;
    private final TeamCommentRepository commentRepo;
    private final TeamMemberRepository teamMemberRepo;
    private final TeamApplyRepository teamApplyRepo;

    public TeamService(TeamRecruitRepository teamRecruitRepo,
                       UserRepository userRepo,
                       TeamCommentRepository commentRepo,TeamMemberRepository teamMemberRepo,TeamApplyRepository teamApplyRepo ) {
        this.teamRecruitRepo = teamRecruitRepo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.teamApplyRepo = teamApplyRepo;
    }

    // 招募列表
    public List<TeamDetailVO> getRecruitList() {
        List<TeamRecruit> teams = teamRecruitRepo.findByStatus(1);
        List<TeamDetailVO> result = new ArrayList<>();

        for (TeamRecruit t : teams) {
            TeamDetailVO vo = new TeamDetailVO();
            vo.setId(t.getTeamId());
            vo.setTitle(t.getTeamName());
            vo.setAvatar(t.getTeamCover());
            vo.setNeed(t.getTeamNeed());
            vo.setDesc(t.getTeamDesc());

            Optional<User> leader = userRepo.findById(t.getLeaderId());
            vo.setLeader(leader.map(User::getUsername).orElse("未知用户"));

            result.add(vo);
        }
        return result;
    }

    // 队伍详情（带评论）
    public TeamDetailVO getDetail(Integer teamId) {
        TeamRecruit t = teamRecruitRepo.findById(teamId).orElse(null);
        if (t == null) return null;

        TeamDetailVO vo = new TeamDetailVO();
        vo.setId(t.getTeamId());
        vo.setTitle(t.getTeamName());
        vo.setAvatar(t.getTeamCover());
        vo.setNeed(t.getTeamNeed());
        vo.setDesc(t.getTeamDesc());

        Optional<User> leader = userRepo.findById(t.getLeaderId());
        vo.setLeader(leader.map(User::getUsername).orElse("未知用户"));

        // 评论
        List<TeamComment> comments = commentRepo.findByTeamId(teamId);
        List<CommentVO> commentVOList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (TeamComment c : comments) {
            CommentVO cv = new CommentVO();
            Optional<User> cu = userRepo.findById(c.getUserId());
            cv.setAuthor(cu.map(User::getUsername).orElse("匿名"));
            cv.setText(c.getContent());
            cv.setTime(c.getCreateTime().format(fmt));
            commentVOList.add(cv);
        }

        vo.setComments(commentVOList);
        return vo;
    }


    // ================== 发布队伍 ==================

    public void publish(TeamRecruit team) {
        team.setStatus(1);
        team.setCurrentNum(1);
        team.setCreateTime(LocalDateTime.now());
        // 先保存队伍，获取自增的 teamId
        teamRecruitRepo.save(team);

        // ==============================================
        // 🔥 关键：创建队伍后，自动把队长加入成员表
        // ==============================================
        TeamMember member = new TeamMember();
        member.setTeamId(team.getTeamId());   // 刚创建的队伍ID
        member.setUserId(team.getLeaderId()); // 队长ID
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);
    }
    // ================== 我的队伍显示 ==================
    public List<TeamVO> getMyTeamsWithMembers(Integer userId) {

        // 1. 我加入的队伍
        List<TeamMember> myMembers = teamMemberRepo.findByUserId(userId);

        List<TeamVO> result = new ArrayList<>();

        for (TeamMember member : myMembers) {
            Integer teamId = member.getTeamId();
            TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
            if (team == null) continue;

            // 2. 查成员
            List<TeamMember> teamMembers = teamMemberRepo.findByTeamId(teamId);

            // ======================
            // 关键：这里用 TeamVO！
            // ======================
            TeamVO vo = new TeamVO();
            vo.setTeamId(team.getTeamId());
            vo.setTeamName(team.getTeamName());
            vo.setLeaderId(team.getLeaderId());
            vo.setJoinTime(member.getJoinTime());
            vo.setMembers(teamMembers);

            result.add(vo);
        }

        return result;
    }
    // ================== 同意入队申请 ==================
    public void agreeApply(Integer applyId) {
        // 这里是 TeamApply，不是 TeamMember！
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply == null) return;

        apply.setStatus(1); // 1=已同意
        teamApplyRepo.save(apply);
    }

    // ================== 拒绝入队申请 ==================
    public void rejectApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply == null) return;

        apply.setStatus(2); // 2=已拒绝
        teamApplyRepo.save(apply);
    }
}