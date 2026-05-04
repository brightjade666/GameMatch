package com.dong.springboot.service;

import com.dong.springboot.dao.*;
import com.dong.springboot.entity.*;
import com.dong.springboot.vo.ChatSessionVO;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatService {

    private final TeamMemberRepository teamMemberRepo;
    private final TeamRecruitRepository teamRecruitRepo;
    private final TeamMessageRepository teamMsgRepo;
    private final FriendRepository friendRepo;
    private final PrivateMessageRepository privateMsgRepo;
    private final UserInfoService userInfoService;

    public ChatService(TeamMemberRepository teamMemberRepo,
                       TeamRecruitRepository teamRecruitRepo,
                       TeamMessageRepository teamMsgRepo,
                       FriendRepository friendRepo,
                       PrivateMessageRepository privateMsgRepo,
                       UserInfoService userInfoService) {
        this.teamMemberRepo = teamMemberRepo;
        this.teamRecruitRepo = teamRecruitRepo;
        this.teamMsgRepo = teamMsgRepo;
        this.friendRepo = friendRepo;
        this.privateMsgRepo = privateMsgRepo;
        this.userInfoService = userInfoService;
    }

    // ================================
    // 队伍聊天（已修复 leaderId → ownerId）
    // ================================

    public List<ChatSessionVO> getTeamChatList(Integer userId) {
        List<TeamMember> myJoinTeamList = teamMemberRepo.findByUserId(userId);
        List<ChatSessionVO> resultList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (TeamMember teamMember : myJoinTeamList) {
            Integer teamId = teamMember.getTeamId();
            Optional<TeamRecruit> teamOpt = teamRecruitRepo.findById(teamId);
            if (teamOpt.isEmpty()) continue;

            TeamRecruit team = teamOpt.get();
            if (team.getStatus() != 1) continue;

            ChatSessionVO vo = new ChatSessionVO();
            vo.setOwnerId(team.getLeaderId());
            vo.setSessionId(teamId);
            vo.setSessionType("team");
            vo.setName(team.getTeamName());
            vo.setAvatar(team.getTeamCover());

            List<TeamMessage> msgList = teamMsgRepo.findByTeamIdOrderBySendTimeDesc(teamId);
            if (!msgList.isEmpty()) {
                TeamMessage lastMsg = msgList.get(0);
                vo.setLastMsg(lastMsg.getContent());
                vo.setLastTime(lastMsg.getSendTime().format(fmt));
            } else {
                vo.setLastMsg("暂无聊天记录");
                vo.setLastTime("");
            }
            resultList.add(vo);
        }
        return resultList;
    }

    public List<UserInfoVO> getTeamMemberList(Integer teamId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        List<UserInfoVO> result = new ArrayList<>();
        for (TeamMember tm : members) {
            UserInfoVO user = userInfoService.getUserInfo(tm.getUserId());
            if (user != null) result.add(user);
        }
        return result;
    }

    public void addTeamMember(Integer teamId, Integer userId) {
        List<TeamMember> exists = teamMemberRepo.findByTeamId(teamId);
        for (TeamMember m : exists) {
            if (m.getUserId().equals(userId)) {
                throw new RuntimeException("已在队伍中");
            }
        }
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);
    }

    // ======================
    // ✅ 修改这里：支持图片/视频
    // ======================
    public TeamMessage sendTeamMessage(Integer teamId, Integer fromId, String content, String fileUrl, String msgType) {
        TeamMessage msg = new TeamMessage();
        msg.setTeamId(teamId);
        msg.setFromId(fromId);
        msg.setContent(content);
        msg.setMsgType(msgType);
        msg.setFileUrl(fileUrl);
        msg.setSendTime(LocalDateTime.now());
        return teamMsgRepo.save(msg);
    }

    public List<TeamMessage> getTeamMessageHistory(Integer teamId) {
        List<TeamMessage> list = teamMsgRepo.findByTeamIdOrderBySendTimeAsc(teamId);

        // 填充用户名、头像
        for (TeamMessage msg : list) {
            UserInfoVO user = userInfoService.getUserInfo(msg.getFromId());
            if (user != null) {
                msg.setUsername(user.getUsername());
                msg.setAvatar(user.getAvatar());
            }
        }

        return list;
    }

    // ================================
    // 私聊功能
    // ================================

    public List<ChatSessionVO> getMyPrivateChatList(Integer userId) {
        List<Friend> list1 = friendRepo.findByUserId(userId);
        List<Friend> list2 = friendRepo.findByFriendId(userId);

        Set<Integer> friendIds = new HashSet<>();
        for (Friend f : list1) friendIds.add(f.getFriendId());
        for (Friend f : list2) friendIds.add(f.getUserId());

        List<ChatSessionVO> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (Integer toUserId : friendIds) {
            UserInfoVO user = userInfoService.getUserInfo(toUserId);
            if (user == null) continue;

            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId(toUserId);
            vo.setSessionType("private");
            vo.setName(user.getUsername());
            vo.setAvatar(user.getAvatar());

            List<PrivateMessage> allMsg = getPrivateMessageHistory(userId, toUserId);
            if (!allMsg.isEmpty()) {
                PrivateMessage last = allMsg.get(allMsg.size() - 1);
                vo.setLastMsg(last.getContent());
                vo.setLastTime(last.getSendTime().format(fmt));
            } else {
                vo.setLastMsg("暂无消息");
            }
            result.add(vo);
        }
        return result;
    }

    public List<UserInfoVO> getPrivateChatMemberList(Integer userId) {
        List<UserInfoVO> list = new ArrayList<>();
        UserInfoVO user = userInfoService.getUserInfo(userId);
        if (user != null) list.add(user);
        return list;
    }

    // ======================
    // ✅ 修改这里：支持图片/视频
    // ======================
    public PrivateMessage sendPrivateMessage(Integer fromId, Integer toUserId, String content, String fileUrl, String msgType) {
        PrivateMessage msg = new PrivateMessage();
        msg.setFromId(fromId);
        msg.setToId(toUserId);
        msg.setContent(content);
        msg.setMsgType(msgType);
        msg.setFileUrl(fileUrl);
        msg.setSendTime(LocalDateTime.now());
        msg.setIsRead(0);
        return privateMsgRepo.save(msg);
    }

    public List<PrivateMessage> getPrivateMessageHistory(Integer userId1, Integer userId2) {
        List<PrivateMessage> a = privateMsgRepo.findByFromIdAndToIdOrderBySendTimeAsc(userId1, userId2);
        List<PrivateMessage> b = privateMsgRepo.findByFromIdAndToIdOrderBySendTimeAsc(userId2, userId1);

        List<PrivateMessage> all = new ArrayList<>();
        all.addAll(a);
        all.addAll(b);
        all.sort(Comparator.comparing(PrivateMessage::getSendTime));

        // 填充用户名、头像
        for (PrivateMessage msg : all) {
            UserInfoVO user = userInfoService.getUserInfo(msg.getFromId());
            if (user != null) {
                msg.setUsername(user.getUsername());
                msg.setAvatar(user.getAvatar());
            }
        }

        return all;
    }
}