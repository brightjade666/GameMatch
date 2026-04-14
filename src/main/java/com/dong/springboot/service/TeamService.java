package com.dong.springboot.service;

import com.dong.springboot.dao.TeamCommentRepository;
import com.dong.springboot.dao.TeamMemberRepository;
import com.dong.springboot.dao.TeamRecruitRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.TeamComment;
import com.dong.springboot.entity.TeamMember;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.entity.User;
import com.dong.springboot.vo.CommentVO;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamVO;
import org.springframework.stereotype.Service;

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


    public TeamService(TeamRecruitRepository teamRecruitRepo,
                       UserRepository userRepo,
                       TeamCommentRepository commentRepo,TeamMemberRepository teamMemberRepo) {
        this.teamRecruitRepo = teamRecruitRepo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
        this.teamMemberRepo = teamMemberRepo;
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
        teamRecruitRepo.save(team);
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
}