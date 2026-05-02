package com.dong.springboot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dong.springboot.dao.SystemNotificationRepository;
import com.dong.springboot.dao.TeamApplyRepository;
import com.dong.springboot.dao.TeamCommentRepository;
import com.dong.springboot.dao.TeamMemberRepository;
import com.dong.springboot.dao.TeamRecruitRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.SystemNotification;
import com.dong.springboot.entity.TeamApply;
import com.dong.springboot.entity.TeamComment;
import com.dong.springboot.entity.TeamMember;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.entity.User;
import com.dong.springboot.vo.CommentVO;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamVO;

@Service
public class TeamService {

    private final TeamRecruitRepository teamRecruitRepo;
    private final UserRepository userRepo;
    private final TeamCommentRepository commentRepo;
    private final TeamMemberRepository teamMemberRepo;
    private final TeamApplyRepository teamApplyRepo;
    private final SystemNotificationRepository notiRepo;

    public TeamService(TeamRecruitRepository teamRecruitRepo,
                       UserRepository userRepo,
                       TeamCommentRepository commentRepo,
                       TeamMemberRepository teamMemberRepo,
                       TeamApplyRepository teamApplyRepo,
                       SystemNotificationRepository notiRepo) {
        this.teamRecruitRepo = teamRecruitRepo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.teamApplyRepo = teamApplyRepo;
        this.notiRepo = notiRepo;
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

    // 获取队长创建的队伍
    public List<TeamRecruit> getCreatedTeams(Integer leaderId) {
        return teamRecruitRepo.findByLeaderId(leaderId);
    }

    // 解散队伍（仅队长，并向所有成员和队长发送通知）
    @Transactional
    public void dissolveTeam(Integer teamId, Integer leaderId) {
        TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
        if (team == null || !team.getLeaderId().equals(leaderId)) {
            throw new RuntimeException("无权操作或队伍不存在");
        }
        team.setStatus(0); // 0-已解散
        teamRecruitRepo.save(team);

        // 通知所有成员（队长除外）
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        for (TeamMember m : members) {
            if (!m.getUserId().equals(leaderId)) {
                SystemNotification noti = new SystemNotification();
                noti.setUserId(m.getUserId());
                noti.setContent("您所在的队伍「" + team.getTeamName() + "」已被队长解散");
                noti.setType("dissolve");
                noti.setRelatedId(teamId);
                noti.setCreateTime(LocalDateTime.now());
                notiRepo.save(noti);
            }
        }
        // 通知队长自己
        SystemNotification noti = new SystemNotification();
        noti.setUserId(leaderId);
        noti.setContent("您已解散队伍「" + team.getTeamName() + "」");
        noti.setType("dissolve");
        noti.setRelatedId(teamId);
        noti.setCreateTime(LocalDateTime.now());
        notiRepo.save(noti);

        // 删除所有成员记录
        teamMemberRepo.deleteAll(members);
    }

    // 退出队伍（仅成员，并通知队长和退出者自己）
    @Transactional
    public void leaveTeam(Integer teamId, Integer userId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        boolean exists = members.stream().anyMatch(m -> m.getUserId().equals(userId));
        if (!exists) {
            throw new RuntimeException("你不是该队成员");
        }

        // 删除该成员
        List<TeamMember> userMembers = teamMemberRepo.findByUserId(userId);
        for (TeamMember m : userMembers) {
            if (m.getTeamId().equals(teamId)) {
                teamMemberRepo.delete(m);
                break;
            }
        }

        TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
        if (team != null) {
            // 更新队伍当前人数
            team.setCurrentNum(team.getCurrentNum() - 1);
            teamRecruitRepo.save(team);

            User user = userRepo.findById(userId).orElse(null);
            String nick = user != null ? user.getUsername() : "玩家";

            // 通知队长
            SystemNotification notiToLeader = new SystemNotification();
            notiToLeader.setUserId(team.getLeaderId());
            notiToLeader.setContent("队员 " + nick + " 退出了队伍「" + team.getTeamName() + "」");
            notiToLeader.setType("leave");
            notiToLeader.setRelatedId(teamId);
            notiToLeader.setCreateTime(LocalDateTime.now());
            notiRepo.save(notiToLeader);

            // 通知退出者自己
            SystemNotification notiToSelf = new SystemNotification();
            notiToSelf.setUserId(userId);
            notiToSelf.setContent("你已退出队伍「" + team.getTeamName() + "」");
            notiToSelf.setType("leave");
            notiToSelf.setRelatedId(teamId);
            notiToSelf.setCreateTime(LocalDateTime.now());
            notiRepo.save(notiToSelf);
        }
    }

    // 发布队伍
    @Transactional
    public void publish(TeamRecruit team) {
        team.setStatus(1);
        team.setCurrentNum(1);
        team.setCreateTime(LocalDateTime.now());
        teamRecruitRepo.save(team);

        // 自动将队长加入成员表
        TeamMember member = new TeamMember();
        member.setTeamId(team.getTeamId());
        member.setUserId(team.getLeaderId());
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);
    }

    // 我的队伍（过滤已解散）
    // 文件：TeamService.java（只替换 getMyTeamsWithMembers 方法）
    public List<TeamVO> getMyTeamsWithMembers(Integer userId) {
        List<TeamMember> myMembers = teamMemberRepo.findByUserId(userId);
        List<TeamVO> result = new ArrayList<>();
        for (TeamMember member : myMembers) {
            TeamRecruit team = teamRecruitRepo.findById(member.getTeamId()).orElse(null);
            if (team == null || team.getStatus() != 1) continue;

            List<TeamMember> teamMembers = teamMemberRepo.findByTeamId(team.getTeamId());
            TeamVO vo = new TeamVO();
            vo.setTeamId(team.getTeamId());
            vo.setTeamName(team.getTeamName());
            vo.setLeaderId(team.getLeaderId());
            vo.setJoinTime(member.getJoinTime());
            vo.setMembers(teamMembers);

            // 构建成员详细信息（包含昵称和加入时间）
            List<Map<String, Object>> details = new ArrayList<>();
            for (TeamMember tm : teamMembers) {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", tm.getUserId());
                item.put("joinTime", tm.getJoinTime() != null ?
                        tm.getJoinTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) : "");
                Optional<User> u = userRepo.findById(tm.getUserId());
                item.put("nick", u.map(User::getUsername).orElse("未知用户"));
                details.add(item);
            }
            vo.setMemberDetails(details);

            result.add(vo);
        }
        return result;
    }

    // 同意申请（包含发送通知给申请人）
    @Transactional
    public void agreeApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply == null || apply.getStatus() != 0) return;

        apply.setStatus(1);
        teamApplyRepo.save(apply);

        // 加入队伍成员
        TeamMember member = new TeamMember();
        member.setTeamId(apply.getTeamId());
        member.setUserId(apply.getUserId());
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);

        // 更新队伍当前人数
        TeamRecruit team = teamRecruitRepo.findById(apply.getTeamId()).orElse(null);
        if (team != null) {
            team.setCurrentNum(team.getCurrentNum() + 1);
            teamRecruitRepo.save(team);
        }

        // 通知申请人
        User applier = userRepo.findById(apply.getUserId()).orElse(null);
        String nick = applier != null ? applier.getUsername() : "玩家";
        SystemNotification noti = new SystemNotification();
        noti.setUserId(apply.getUserId());
        noti.setContent("你已成功加入队伍「" + (team != null ? team.getTeamName() : "未知队伍") + "」");
        noti.setType("join");
        noti.setRelatedId(apply.getTeamId());
        noti.setCreateTime(LocalDateTime.now());
        notiRepo.save(noti);
    }

    // 拒绝申请
    @Transactional
    public void rejectApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply != null) {
            apply.setStatus(2);
            teamApplyRepo.save(apply);
        }
    }

    // 根据用户ID获取用户名（供 Controller 使用）
    public String getUserNameById(Integer userId) {
        return userRepo.findById(userId)
                .map(User::getUsername)
                .orElse("未知用户");
    }
}