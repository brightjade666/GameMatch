package com.dong.springboot.service;

import com.dong.springboot.annotation.OperationLog;
import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.SystemNotification;
import com.dong.springboot.entity.TeamApply;
import com.dong.springboot.entity.TeamComment;
import com.dong.springboot.entity.TeamMember;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.entity.User;
import com.dong.springboot.mapper.GameMapper;
import com.dong.springboot.mapper.SystemNotificationMapper;
import com.dong.springboot.mapper.TeamApplyMapper;
import com.dong.springboot.mapper.TeamCommentMapper;
import com.dong.springboot.mapper.TeamMemberMapper;
import com.dong.springboot.mapper.TeamRecruitMapper;
import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.vo.CommentVO;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamService {

    private final GameMapper gameMapper;
    private final TeamRecruitMapper teamRecruitRepo;
    private final UserMapper userRepo;
    private final TeamMemberMapper teamMemberRepo;
    private final TeamApplyMapper teamApplyRepo;
    private final TeamCommentMapper commentRepo;
    private final SystemNotificationMapper notiRepo;
    private final UnreadCountService unreadCountService;
    private final TeamDetailCacheService teamDetailCacheService;
    private final TeamBloomFilterService teamBloomFilterService;
    private final TeamRankService teamRankService;

    public TeamService(TeamRecruitMapper teamRecruitRepo,
                       UserMapper userRepo,
                       TeamMemberMapper teamMemberRepo,
                       TeamApplyMapper teamApplyRepo,
                       TeamCommentMapper commentRepo,
                       SystemNotificationMapper notiRepo,
                       GameMapper gameMapper,
                       UnreadCountService unreadCountService,
                       TeamDetailCacheService teamDetailCacheService,
                       TeamBloomFilterService teamBloomFilterService,
                       TeamRankService teamRankService) {
        this.teamRecruitRepo = teamRecruitRepo;
        this.userRepo = userRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.teamApplyRepo = teamApplyRepo;
        this.commentRepo = commentRepo;
        this.notiRepo = notiRepo;
        this.gameMapper = gameMapper;
        this.unreadCountService = unreadCountService;
        this.teamDetailCacheService = teamDetailCacheService;
        this.teamBloomFilterService = teamBloomFilterService;
        this.teamRankService = teamRankService;
    }

    public List<TeamDetailVO> getRecruitList(Integer userId) {
        List<TeamRecruit> teams = teamRecruitRepo.findByStatus(1);
        String userGameName = null;
        if (userId != null) {
            User user = userRepo.findById(userId).orElse(null);
            if (user != null && user.getGameId() != null) {
                Game game = gameMapper.findById(user.getGameId()).orElse(null);
                if (game != null) {
                    userGameName = game.getGameName();
                }
            }
        }

        if (userGameName != null) {
            String keyword = userGameName.trim().toLowerCase();
            teams = teams.stream()
                    .sorted(Comparator.comparingInt(team ->
                            team.getTeamNeed() != null && team.getTeamNeed().toLowerCase().contains(keyword) ? 0 : 1))
                    .toList();
        }

        List<TeamDetailVO> result = new ArrayList<>();
        for (TeamRecruit team : teams) {
            TeamDetailVO vo = new TeamDetailVO();
            vo.setId(team.getTeamId());
            vo.setTitle(team.getTeamName());
            vo.setAvatar(team.getTeamCover());
            vo.setNeed(team.getTeamNeed());
            vo.setDesc(team.getTeamDesc());
            vo.setLeader(userRepo.findById(team.getLeaderId()).map(User::getUsername).orElse("unknown"));
            result.add(vo);
        }
        return result;
    }

    @Transactional
    @OperationLog("查看队伍详情")
    public TeamDetailVO getDetail(Integer teamId) {
        TeamDetailVO detail = teamDetailCacheService.getTeamDetail(teamId);
        if (detail != null && Integer.valueOf(1).equals(detail.getStatus())) {
            teamRankService.recordView(teamId);
        }
        return detail;
    }

    @Transactional
    @OperationLog("发表评论")
    public CommentVO addComment(Integer teamId, Integer userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment cannot be empty");
        }

        TeamComment comment = new TeamComment();
        comment.setTeamId(teamId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        commentRepo.save(comment);
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(teamId);
        CommentVO vo = new CommentVO();
        User user = userRepo.findById(userId).orElse(null);
        vo.setAuthor(user != null ? user.getUsername() : "anonymous");
        vo.setText(content);
        vo.setTime(comment.getCreateTime().toString());
        vo.setUserId(userId);
        vo.setAvatar(user != null ? user.getAvatar() : null);
        teamRankService.recordComment(teamId);
        return vo;
    }

    public List<TeamRecruit> getCreatedTeams(Integer leaderId) {
        return teamRecruitRepo.findByLeaderId(leaderId);
    }

    @Transactional
    @OperationLog("解散队伍")
    public void dissolveTeam(Integer teamId, Integer leaderId) {
        TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
        if (team == null || !team.getLeaderId().equals(leaderId)) {
            throw new RuntimeException("No permission or team not found");
        }

        team.setStatus(0);
        teamRecruitRepo.save(team);

        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        for (TeamMember member : members) {
            if (!member.getUserId().equals(leaderId)) {
                saveNoti(buildNoti(member.getUserId(), "dissolve", teamId,
                        "Your team [" + team.getTeamName() + "] has been dissolved"));
            }
        }

        saveNoti(buildNoti(leaderId, "dissolve", teamId,
                "You have dissolved team [" + team.getTeamName() + "]"));

        teamMemberRepo.deleteAll(members);
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(teamId);
        teamRankService.removeAfterCommit(teamId);
    }

    @Transactional
    @OperationLog("退出队伍")
    public void leaveTeam(Integer teamId, Integer userId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        boolean exists = members.stream().anyMatch(member -> member.getUserId().equals(userId));
        if (!exists) {
            throw new RuntimeException("Not in team");
        }

        teamMemberRepo.deleteByTeamIdAndUserId(teamId, userId);

        TeamRecruit team = teamRecruitRepo.findById(teamId).orElse(null);
        if (team == null) {
            return;
        }

        team.setCurrentNum(Math.max(0, team.getCurrentNum() - 1));
        teamRecruitRepo.save(team);

        User user = userRepo.findById(userId).orElse(null);
        String nick = user != null ? user.getUsername() : "user";

        saveNoti(buildNoti(team.getLeaderId(), "leave", teamId,
                "Member " + nick + " left team [" + team.getTeamName() + "]"));
        saveNoti(buildNoti(userId, "leave", teamId,
                "You left team [" + team.getTeamName() + "]"));
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(teamId);
    }

    @Transactional
    @OperationLog("发布队伍")
    public void publish(TeamRecruit team) {
        team.setStatus(1);
        team.setCurrentNum(1);
        team.setCreateTime(LocalDateTime.now());
        teamRecruitRepo.save(team);
        teamBloomFilterService.put(team.getTeamId());

        TeamMember member = new TeamMember();
        member.setTeamId(team.getTeamId());
        member.setUserId(team.getLeaderId());
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);
        teamRankService.initializeTeam(team.getTeamId());
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(team.getTeamId());
    }

    @Transactional
    @OperationLog("申请加入队伍")
    public boolean submitApply(TeamApply apply) {
        List<TeamApply> existingList = teamApplyRepo.findByTeamIdAndUserId(apply.getTeamId(), apply.getUserId());
        if (existingList != null && !existingList.isEmpty()) {
            TeamApply existing = existingList.get(0);
            existing.setStatus(0);
            existing.setApplyTime(LocalDateTime.now());
            teamApplyRepo.save(existing);
            return false;
        }

        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        teamApplyRepo.save(apply);
        teamRankService.recordApply(apply.getTeamId());
        return true;
    }

    public List<TeamVO> getMyTeamsWithMembers(Integer userId) {
        List<TeamMember> myMembers = teamMemberRepo.findByUserId(userId);
        List<TeamVO> result = new ArrayList<>();
        for (TeamMember member : myMembers) {
            TeamRecruit team = teamRecruitRepo.findById(member.getTeamId()).orElse(null);
            if (team == null || team.getStatus() != 1) {
                continue;
            }

            List<TeamMember> teamMembers = teamMemberRepo.findByTeamId(team.getTeamId());
            TeamVO vo = new TeamVO();
            vo.setTeamId(team.getTeamId());
            vo.setTeamName(team.getTeamName());
            vo.setLeaderId(team.getLeaderId());
            vo.setJoinTime(member.getJoinTime());
            vo.setMembers(teamMembers);

            List<Map<String, Object>> details = new ArrayList<>();
            for (TeamMember teamMember : teamMembers) {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", teamMember.getUserId());
                item.put("joinTime", teamMember.getJoinTime() != null ? teamMember.getJoinTime().toString() : "");
                item.put("nick", userRepo.findById(teamMember.getUserId()).map(User::getUsername).orElse("unknown"));
                details.add(item);
            }
            vo.setMemberDetails(details);
            result.add(vo);
        }
        return result;
    }

    @Transactional
    @OperationLog("同意加入申请")
    public void agreeApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply == null || apply.getStatus() != 0) {
            return;
        }

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

        saveNoti(buildNoti(apply.getUserId(), "join", apply.getTeamId(),
                "You joined team [" + (team != null ? team.getTeamName() : "unknown") + "]"));
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(apply.getTeamId());
    }

    @Transactional
    @OperationLog("拒绝加入申请")
    public void rejectApply(Integer applyId) {
        TeamApply apply = teamApplyRepo.findById(applyId).orElse(null);
        if (apply != null) {
            apply.setStatus(2);
            teamApplyRepo.save(apply);
        }
    }

    public String getUserNameById(Integer userId) {
        return userRepo.findById(userId).map(User::getUsername).orElse("unknown");
    }

    @Transactional
    @OperationLog("移除队伍成员")
    public void removeMember(Integer teamId, Integer leaderId, Integer userId) {
        TeamRecruit team = teamRecruitRepo.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        if (!team.getLeaderId().equals(leaderId)) {
            throw new RuntimeException("No permission");
        }

        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        boolean exists = members.stream().anyMatch(member -> member.getUserId().equals(userId));
        if (!exists) {
            throw new RuntimeException("User not in team");
        }

        teamMemberRepo.deleteByTeamIdAndUserId(teamId, userId);
        team.setCurrentNum(Math.max(0, team.getCurrentNum() - 1));
        teamRecruitRepo.save(team);

        saveNoti(buildNoti(userId, "kick", teamId,
                "You were removed from team [" + team.getTeamName() + "]"));
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(teamId);
    }

    public List<TeamRecruit> getAllTeams() {
        return teamRecruitRepo.findAll();
    }

    public long countTeams() {
        return teamRecruitRepo.count();
    }

    public List<TeamRecruit> searchByTeamName(String keyword) {
        return teamRecruitRepo.findByTeamNameContaining(keyword);
    }

    public List<TeamRecruit> getJoinedTeams(Integer userId) {
        List<TeamMember> members = teamMemberRepo.findByUserId(userId);
        List<TeamRecruit> result = new ArrayList<>();
        for (TeamMember member : members) {
            TeamRecruit team = teamRecruitRepo.findById(member.getTeamId()).orElse(null);
            if (team != null) {
                result.add(team);
            }
        }
        return result;
    }

    @Transactional
    @OperationLog("管理员解散队伍")
    public void adminDissolveTeam(Integer teamId, Integer adminId) {
        TeamRecruit team = teamRecruitRepo.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        if (team.getStatus() == 0) {
            return;
        }

        team.setStatus(0);
        teamRecruitRepo.save(team);

        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        for (TeamMember member : members) {
            saveNoti(buildNoti(member.getUserId(), "admin_dissolve", teamId,
                    "Team [" + team.getTeamName() + "] was dissolved by admin"));
        }

        teamMemberRepo.deleteAll(members);
        teamDetailCacheService.deleteTeamDetailCacheAfterCommit(teamId);
        teamRankService.removeAfterCommit(teamId);
    }

    private SystemNotification buildNoti(Integer userId, String type, Integer relatedId, String content) {
        SystemNotification noti = new SystemNotification();
        noti.setUserId(userId);
        noti.setType(type);
        noti.setRelatedId(relatedId);
        noti.setContent(content);
        noti.setCreateTime(LocalDateTime.now());
        return noti;
    }

    private void saveNoti(SystemNotification noti) {
        notiRepo.save(noti);
        unreadCountService.incrementNotification(noti.getUserId());
    }
}
