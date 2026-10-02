package com.dong.springboot.service;

import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.entity.User;
import com.dong.springboot.mapper.AiChatRecordMapper;
import com.dong.springboot.mapper.FriendMapper;
import com.dong.springboot.mapper.PrivateMessageMapper;
import com.dong.springboot.mapper.SystemNotificationMapper;
import com.dong.springboot.mapper.TeamApplyMapper;
import com.dong.springboot.mapper.TeamCommentMapper;
import com.dong.springboot.mapper.TeamMemberMapper;
import com.dong.springboot.mapper.TeamMessageMapper;
import com.dong.springboot.mapper.TeamRecruitMapper;
import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserMapper userRepo;
    private final FriendMapper friendRepo;
    private final TeamMemberMapper teamMemberRepo;
    private final TeamApplyMapper teamApplyRepo;
    private final TeamRecruitMapper teamRecruitRepo;
    private final PrivateMessageMapper privateMessageRepo;
    private final TeamMessageMapper teamMessageRepo;
    private final SystemNotificationMapper notiRepo;
    private final TeamService teamService;
    private final AiChatRecordMapper aiChatRecordRepo;
    private final TeamCommentMapper teamCommentRepo;
    private final UserProfileMapper userProfileRepo;
    private final LoginTokenService loginTokenService;

    public AdminService(UserMapper userRepo,
                        FriendMapper friendRepo,
                        TeamMemberMapper teamMemberRepo,
                        TeamApplyMapper teamApplyRepo,
                        TeamRecruitMapper teamRecruitRepo,
                        PrivateMessageMapper privateMessageRepo,
                        TeamMessageMapper teamMessageRepo,
                        SystemNotificationMapper notiRepo,
                        AiChatRecordMapper aiChatRecordRepo,
                        TeamService teamService,
                        TeamCommentMapper teamCommentRepo,
                        UserProfileMapper userProfileRepo,
                        LoginTokenService loginTokenService) {
        this.userRepo = userRepo;
        this.friendRepo = friendRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.teamApplyRepo = teamApplyRepo;
        this.teamRecruitRepo = teamRecruitRepo;
        this.privateMessageRepo = privateMessageRepo;
        this.teamMessageRepo = teamMessageRepo;
        this.notiRepo = notiRepo;
        this.aiChatRecordRepo = aiChatRecordRepo;
        this.teamService = teamService;
        this.teamCommentRepo = teamCommentRepo;
        this.userProfileRepo = userProfileRepo;
        this.loginTokenService = loginTokenService;
    }

    public void checkAdmin(Integer userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null || user.getRole() == null || user.getRole() != 9) {
            throw new RuntimeException("当前用户不是管理员");
        }
    }

    @Transactional
    public void deleteUser(Integer userId, Integer adminId) {
        if (userId.equals(adminId)) {
            throw new RuntimeException("管理员不能删除自己");
        }

        userRepo.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));

        friendRepo.deleteByUserId(userId);
        friendRepo.deleteByFriendId(userId);
        teamApplyRepo.deleteByUserId(userId);
        teamCommentRepo.deleteByUserId(userId);
        teamMemberRepo.deleteByUserId(userId);

        List<TeamRecruit> leadTeams = teamRecruitRepo.findByLeaderId(userId);
        for (TeamRecruit team : leadTeams) {
            Integer teamId = team.getTeamId();
            teamApplyRepo.deleteByTeamId(teamId);
            teamCommentRepo.deleteByTeamId(teamId);
            teamMessageRepo.deleteByTeamId(teamId);
            teamService.adminDissolveTeam(teamId, adminId);
            teamRecruitRepo.delete(team);
        }

        userProfileRepo.deleteByUserId(userId);
        privateMessageRepo.deleteByFromId(userId);
        privateMessageRepo.deleteByToId(userId);
        teamMessageRepo.deleteByFromId(userId);
        notiRepo.deleteByUserId(userId);
        aiChatRecordRepo.deleteByUserId(userId);
        loginTokenService.removeUserLogin(userId);
        userRepo.deleteById(userId);
    }

    public void dissolveAnyTeam(Integer teamId, Integer adminId) {
        teamService.adminDissolveTeam(teamId, adminId);
    }
}
