package com.dong.springboot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.dong.springboot.entity.*;
import com.dong.springboot.dao.*;
import com.dong.springboot.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final GameRepository gameRepository;
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
                       SystemNotificationRepository notiRepo,
                       GameRepository gameRepository) {
        this.teamRecruitRepo = teamRecruitRepo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.teamApplyRepo = teamApplyRepo;
        this.notiRepo = notiRepo;
        this.gameRepository = gameRepository;
    }

    // 招募列表（可选根据用户ID排序）
    public List<TeamDetailVO> getRecruitList(Integer userId) {
        List<TeamRecruit> teams = teamRecruitRepo.findByStatus(1);
        // 获取用户常玩游戏名称
        String userGameName = null;
        if (userId != null) {
            User user = userRepo.findById(userId).orElse(null);
            if (user != null && user.getGameId() != null) {
                Game game = gameRepository.findById(user.getGameId()).orElse(null);
                if (game != null) {
                    userGameName = game.getGameName();
                }
            }
        }
        // 根据队伍需求（即游戏名称）排序：与用户游戏相同或包含的优先
        if (userGameName != null) {
            final String game = userGameName.trim().toLowerCase();
            teams = teams.stream()
                    .sorted(Comparator.comparingInt(t -> {
                        if (t.getTeamNeed() == null) return 1;
                        return t.getTeamNeed().toLowerCase().contains(game) ? 0 : 1;
                    }))
                    .collect(Collectors.toList());
        }
        // 转换为 VO 列表
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

    // 队伍详情（带评论，评论按最新优先）
    public TeamDetailVO getDetail(Integer teamId) {
        TeamRecruit t = teamRecruitRepo.findById(teamId).orElse(null);
        if (t == null) return null;
        TeamDetailVO vo = new TeamDetailVO();
        vo.setId(t.getTeamId());
        vo.setTitle(t.getTeamName());
        vo.setAvatar(t.getTeamCover());
        vo.setNeed(t.getTeamNeed());
        vo.setDesc(t.getTeamDesc());
        vo.setLeaderId(t.getLeaderId());
        Optional<User> leader = userRepo.findById(t.getLeaderId());
        vo.setLeader(leader.map(User::getUsername).orElse("未知用户"));

        // 修改：按创建时间降序获取评论（最新在前）
        List<TeamComment> comments = commentRepo.findByTeamIdOrderByCreateTimeDesc(teamId);
        List<CommentVO> commentVOList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        for (TeamComment c : comments) {
            CommentVO cv = new CommentVO();
            Optional<User> cu = userRepo.findById(c.getUserId());
            User user = cu.orElse(null);
            cv.setAuthor(user != null ? user.getUsername() : "匿名");
            cv.setText(c.getContent());
            cv.setTime(c.getCreateTime().format(fmt));
            cv.setUserId(c.getUserId());
            // 设置头像
            cv.setAvatar(user != null ? user.getAvatar() : null);
            commentVOList.add(cv);
        }
        vo.setComments(commentVOList);
        return vo;
    }

    // 新增：发表评论
    @Transactional
    public CommentVO addComment(Integer teamId, Integer userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        TeamComment comment = new TeamComment();
        comment.setTeamId(teamId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        commentRepo.save(comment);

        // 返回新评论的 VO
        CommentVO vo = new CommentVO();
        User user = userRepo.findById(userId).orElse(null);
        vo.setAuthor(user != null ? user.getUsername() : "匿名");
        vo.setText(content);
        vo.setTime(comment.getCreateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        vo.setUserId(userId);
        vo.setAvatar(user != null ? user.getAvatar() : null); // 设置头像
        return vo;
    }

    public List<TeamRecruit> getCreatedTeams(Integer leaderId) {
        return teamRecruitRepo.findByLeaderId(leaderId);
    }

    @Transactional
    public void dissolveTeam(Integer teamId, Integer leaderId) {
        TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
        if (team == null || !team.getLeaderId().equals(leaderId)) {
            throw new RuntimeException("无权操作或队伍不存在");
        }
        team.setStatus(0);
        teamRecruitRepo.save(team);

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
        SystemNotification noti = new SystemNotification();
        noti.setUserId(leaderId);
        noti.setContent("您已解散队伍「" + team.getTeamName() + "」");
        noti.setType("dissolve");
        noti.setRelatedId(teamId);
        noti.setCreateTime(LocalDateTime.now());
        notiRepo.save(noti);

        teamMemberRepo.deleteAll(members);
    }

    @Transactional
    public void leaveTeam(Integer teamId, Integer userId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        boolean exists = members.stream().anyMatch(m -> m.getUserId().equals(userId));
        if (!exists) {
            throw new RuntimeException("你不是该队成员");
        }

        List<TeamMember> userMembers = teamMemberRepo.findByUserId(userId);
        for (TeamMember m : userMembers) {
            if (m.getTeamId().equals(teamId)) {
                teamMemberRepo.delete(m);
                break;
            }
        }

        TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
        if (team != null) {
            team.setCurrentNum(team.getCurrentNum() - 1);
            teamRecruitRepo.save(team);

            User user = userRepo.findById(userId).orElse(null);
            String nick = user != null ? user.getUsername() : "玩家";

            SystemNotification notiToLeader = new SystemNotification();
            notiToLeader.setUserId(team.getLeaderId());
            notiToLeader.setContent("队员 " + nick + " 退出了队伍「" + team.getTeamName() + "」");
            notiToLeader.setType("leave");
            notiToLeader.setRelatedId(teamId);
            notiToLeader.setCreateTime(LocalDateTime.now());
            notiRepo.save(notiToLeader);

            SystemNotification notiToSelf = new SystemNotification();
            notiToSelf.setUserId(userId);
            notiToSelf.setContent("你已退出队伍「" + team.getTeamName() + "」");
            notiToSelf.setType("leave");
            notiToSelf.setRelatedId(teamId);
            notiToSelf.setCreateTime(LocalDateTime.now());
            notiRepo.save(notiToSelf);
        }
    }

    @Transactional
    public void publish(TeamRecruit team) {
        team.setStatus(1);
        team.setCurrentNum(1);
        team.setCreateTime(LocalDateTime.now());
        teamRecruitRepo.save(team);

        TeamMember member = new TeamMember();
        member.setTeamId(team.getTeamId());
        member.setUserId(team.getLeaderId());
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);
    }

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

    @Transactional
    public void agreeApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply == null || apply.getStatus() != 0) return;

        apply.setStatus(1);
        teamApplyRepo.save(apply);

        TeamMember member = new TeamMember();
        member.setTeamId(apply.getTeamId());
        member.setUserId(apply.getUserId());
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);

        TeamRecruit team = teamRecruitRepo.findById(apply.getTeamId()).orElse(null);
        if (team != null) {
            team.setCurrentNum(team.getCurrentNum() + 1);
            teamRecruitRepo.save(team);
        }

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

    @Transactional
    public void rejectApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply != null) {
            apply.setStatus(2);
            teamApplyRepo.save(apply);
        }
    }

    public String getUserNameById(Integer userId) {
        return userRepo.findById(userId)
                .map(User::getUsername)
                .orElse("未知用户");
    }

    // 队长移除成员（完整实现）
    @Transactional
    public void removeMember(Integer teamId, Integer leaderId, Integer userId) {
        TeamRecruit team = teamRecruitRepo.findById(teamId).orElseThrow(() -> new RuntimeException("队伍不存在"));
        if (!team.getLeaderId().equals(leaderId)) throw new RuntimeException("无权限");

        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        boolean exists = members.stream().anyMatch(m -> m.getUserId().equals(userId));
        if (!exists) throw new RuntimeException("该用户不在队伍中");

        // 删除成员
        teamMemberRepo.deleteByTeamIdAndUserId(teamId, userId); // 需要在 repository 中声明该方法
        team.setCurrentNum(team.getCurrentNum() - 1);
        teamRecruitRepo.save(team);

        SystemNotification noti = new SystemNotification();
        noti.setUserId(userId);
        noti.setContent("你已被队长移出队伍「" + team.getTeamName() + "」");
        noti.setType("kick");
        noti.setCreateTime(LocalDateTime.now());
        notiRepo.save(noti);
    }
}