package com.dong.springboot.service;

import com.dong.springboot.dao.*;
import com.dong.springboot.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final FriendRepository friendRepo;
    private final TeamMemberRepository teamMemberRepo;
    private final TeamApplyRepository teamApplyRepo;
    private final TeamRecruitRepository teamRecruitRepo;
    private final PrivateMessageRepository privateMessageRepo;
    private final TeamMessageRepository teamMessageRepo;
    private final SystemNotificationRepository notiRepo;
    private final TeamService teamService;
    private final AiChatRecordRepository aiChatRecordRepo;
    private final TeamCommentRepository teamCommentRepo;
    private final UserProfileRepository userProfileRepo;

    public AdminService(UserRepository userRepo,
                        FriendRepository friendRepo,
                        TeamMemberRepository teamMemberRepo,
                        TeamApplyRepository teamApplyRepo,
                        TeamRecruitRepository teamRecruitRepo,
                        PrivateMessageRepository privateMessageRepo,
                        TeamMessageRepository teamMessageRepo,
                        SystemNotificationRepository notiRepo,
                        AiChatRecordRepository aiChatRecordRepo,
                        TeamService teamService,
                        TeamCommentRepository teamCommentRepo,
                        UserProfileRepository userProfileRepo) {
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
    }

    public void checkAdmin(Integer userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null || user.getRole() == null || user.getRole() != 9) {
            throw new RuntimeException("无管理员权限");
        }
    }

    @Transactional
    public void deleteUser(Integer userId, Integer adminId) {
        if (userId.equals(adminId)) {
            throw new RuntimeException("管理员不能删除自己");
        }
        userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 1. 好友关系（双向）
        friendRepo.deleteByUserId(userId);
        friendRepo.deleteByFriendId(userId);

        // 2. 入队申请
        teamApplyRepo.deleteByUserId(userId);

        // 3. 该用户发表的评论
        teamCommentRepo.deleteByUserId(userId);

        // 4. 队伍成员记录
        teamMemberRepo.deleteByUserId(userId);

        // 5. 处理该用户作为队长的队伍：彻底删除
        List<TeamRecruit> leadTeams = teamRecruitRepo.findByLeaderId(userId);
        for (TeamRecruit team : leadTeams) {
            // 5.1 删除队伍下的申请记录
            teamApplyRepo.deleteByTeamId(team.getTeamId());
            // 5.2 删除队伍下的评论
            teamCommentRepo.deleteByTeamId(team.getTeamId());
            // 5.3 删除队伍下的聊天消息
            teamMessageRepo.deleteByTeamId(team.getTeamId());
            // 5.4 通知成员并清除成员记录
            teamService.adminDissolveTeam(team.getTeamId(), adminId);
            // 5.5 删除队伍记录本身
            teamRecruitRepo.delete(team);
        }

        // 6. 用户游戏档案
        userProfileRepo.deleteByUserId(userId);

        // 7. 私聊消息（发送/接收）
        privateMessageRepo.deleteByFromId(userId);
        privateMessageRepo.deleteByToId(userId);

        // 8. 队伍聊天消息（fromId）
        teamMessageRepo.deleteByFromId(userId);

        // 9. 系统通知
        notiRepo.deleteByUserId(userId);

        // 10. AI聊天记录
        aiChatRecordRepo.deleteByUserId(userId);

        // 11. 删除用户
        userRepo.deleteById(userId);
    }

    public void dissolveAnyTeam(Integer teamId, Integer adminId) {
        teamService.adminDissolveTeam(teamId, adminId);
    }
}