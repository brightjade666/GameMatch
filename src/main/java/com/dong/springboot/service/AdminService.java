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
        // 权限校验
        if (userId.equals(adminId)) {
            throw new RuntimeException("管理员不能删除自己");
        }
        userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        System.out.println("========================================");
        System.out.println("开始执行用户【" + userId + "】的级联删除任务");
        System.out.println("========================================");

        // 1. 好友关系（发起方）
        System.out.println("[1] 删除表：friend（用户作为发起方的好友关系） userId=" + userId);
        friendRepo.deleteByUserId(userId);

        // 2. 好友关系（被添加方）
        System.out.println("[2] 删除表：friend（用户作为被添加方的好友关系） userId=" + userId);
        friendRepo.deleteByFriendId(userId);

        // 3. 入队申请
        System.out.println("[3] 删除表：team_apply（用户提交的入队申请） userId=" + userId);
        teamApplyRepo.deleteByUserId(userId);

        // 4. 团队评论
        System.out.println("[4] 删除表：team_comment（用户发表的评论） userId=" + userId);
        teamCommentRepo.deleteByUserId(userId);

        // 5. 团队成员记录
        System.out.println("[5] 删除表：team_member（用户加入的团队） userId=" + userId);
        teamMemberRepo.deleteByUserId(userId);

        // 6. 处理用户创建的团队（队长）
        List<TeamRecruit> leadTeams = teamRecruitRepo.findByLeaderId(userId);
        System.out.println("[6] 发现用户创建的团队数量：" + leadTeams.size() + " 个，开始清理团队相关数据");
        for (TeamRecruit team : leadTeams) {
            Integer teamId = team.getTeamId();
            System.out.println("  → 清理团队 teamId=" + teamId + " 相关数据");

            System.out.println("    - 删除表：team_apply（团队申请）");
            teamApplyRepo.deleteByTeamId(teamId);

            System.out.println("    - 删除表：team_comment（团队评论）");
            teamCommentRepo.deleteByTeamId(teamId);

            System.out.println("    - 删除表：team_message（团队消息）");
            teamMessageRepo.deleteByTeamId(teamId);

            System.out.println("    - 解散团队并清理成员 team_member");
            teamService.adminDissolveTeam(teamId, adminId);

            System.out.println("    - 删除表：team_recruit（团队本身）");
            teamRecruitRepo.delete(team);
        }

        // 7. 用户游戏档案
        System.out.println("[7] 删除表：user_profile（用户游戏档案） userId=" + userId);
        userProfileRepo.deleteByUserId(userId);

        // 8. 私聊消息（发送）
        System.out.println("[8] 删除表：private_message（发送的私聊） userId=" + userId);
        privateMessageRepo.deleteByFromId(userId);

        // 9. 私聊消息（接收）
        System.out.println("[9] 删除表：private_message（接收的私聊） userId=" + userId);
        privateMessageRepo.deleteByToId(userId);

        // 10. 队伍聊天消息
        System.out.println("[10] 删除表：team_message（用户发送的队伍消息） userId=" + userId);
        teamMessageRepo.deleteByFromId(userId);

        // 11. 系统通知
        System.out.println("[11] 删除表：system_notification（系统通知） userId=" + userId);
        notiRepo.deleteByUserId(userId);

        // 12. AI聊天记录
        System.out.println("[12] 删除表：ai_chat_record（AI对话记录） userId=" + userId);
        aiChatRecordRepo.deleteByUserId(userId);

        // 13. 删除用户主记录
        System.out.println("[13] 删除表：user（用户主数据） userId=" + userId);
        userRepo.deleteById(userId);

        System.out.println("========================================");
        System.out.println("用户【" + userId + "】所有级联数据删除完成！");
        System.out.println("========================================");
    }

    public void dissolveAnyTeam(Integer teamId, Integer adminId) {
        System.out.println("管理员强制解散团队，teamId=" + teamId);
        teamService.adminDissolveTeam(teamId, adminId);
    }
}