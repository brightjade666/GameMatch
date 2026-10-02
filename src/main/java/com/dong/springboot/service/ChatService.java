package com.dong.springboot.service;

import com.dong.springboot.entity.PrivateMessage;
import com.dong.springboot.entity.TeamMember;
import com.dong.springboot.entity.TeamMessage;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.mapper.FriendMapper;
import com.dong.springboot.mapper.PrivateMessageMapper;
import com.dong.springboot.mapper.TeamMemberMapper;
import com.dong.springboot.mapper.TeamMessageMapper;
import com.dong.springboot.mapper.TeamRecruitMapper;
import com.dong.springboot.vo.ChatSessionVO;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final TeamMemberMapper teamMemberRepo;
    private final TeamRecruitMapper teamRecruitRepo;
    private final TeamMessageMapper teamMsgRepo;
    private final FriendMapper friendRepo;
    private final PrivateMessageMapper privateMsgRepo;
    private final UserInfoService userInfoService;
    private final UnreadCountService unreadCountService;

    public ChatService(TeamMemberMapper teamMemberRepo,
                       TeamRecruitMapper teamRecruitRepo,
                       TeamMessageMapper teamMsgRepo,
                       FriendMapper friendRepo,
                       PrivateMessageMapper privateMsgRepo,
                       UserInfoService userInfoService,
                       UnreadCountService unreadCountService) {
        this.teamMemberRepo = teamMemberRepo;
        this.teamRecruitRepo = teamRecruitRepo;
        this.teamMsgRepo = teamMsgRepo;
        this.friendRepo = friendRepo;
        this.privateMsgRepo = privateMsgRepo;
        this.userInfoService = userInfoService;
        this.unreadCountService = unreadCountService;
    }

    public List<ChatSessionVO> getTeamChatList(Integer userId) {
        List<TeamMember> myJoinTeamList = teamMemberRepo.findByUserId(userId);
        List<ChatSessionVO> resultList = new ArrayList<>();

        for (TeamMember teamMember : myJoinTeamList) {
            Integer teamId = teamMember.getTeamId();
            Optional<TeamRecruit> teamOpt = teamRecruitRepo.findById(teamId);
            if (teamOpt.isEmpty()) {
                continue;
            }

            TeamRecruit team = teamOpt.get();
            if (team.getStatus() != 1) {
                continue;
            }

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
                vo.setLastTime(lastMsg.getSendTime().toString());
            } else {
                vo.setLastMsg("暂无消息");
                vo.setLastTime("");
            }
            resultList.add(vo);
        }
        return resultList;
    }

    public List<UserInfoVO> getTeamMemberList(Integer teamId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        List<UserInfoVO> result = new ArrayList<>();
        for (TeamMember member : members) {
            UserInfoVO user = userInfoService.getUserInfo(member.getUserId());
            if (user != null) {
                result.add(user);
            }
        }
        return result;
    }

    public void addTeamMember(Integer teamId, Integer userId) {
        List<TeamMember> exists = teamMemberRepo.findByTeamId(teamId);
        for (TeamMember member : exists) {
            if (member.getUserId().equals(userId)) {
                throw new RuntimeException("该用户已经在队伍中");
            }
        }

        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setJoinTime(LocalDateTime.now());
        teamMemberRepo.save(member);
    }

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
        for (TeamMessage msg : list) {
            UserInfoVO user = userInfoService.getUserInfo(msg.getFromId());
            if (user != null) {
                msg.setUsername(user.getUsername());
                msg.setAvatar(user.getAvatar());
            }
        }
        return list;
    }

    public List<ChatSessionVO> getMyPrivateChatList(Integer userId) {
        return friendRepo.selectPrivateChatSessions(userId);
    }

    public List<UserInfoVO> getPrivateChatMemberList(Integer userId) {
        List<UserInfoVO> list = new ArrayList<>();
        UserInfoVO user = userInfoService.getUserInfo(userId);
        if (user != null) {
            list.add(user);
        }
        return list;
    }

    public PrivateMessage sendPrivateMessage(Integer fromId,
                                             Integer toUserId,
                                             String content,
                                             String fileUrl,
                                             String msgType) {
        PrivateMessage msg = new PrivateMessage();
        msg.setFromId(fromId);
        msg.setToId(toUserId);
        msg.setContent(content);
        msg.setMsgType(msgType);
        msg.setFileUrl(fileUrl);
        msg.setSendTime(LocalDateTime.now());
        msg.setIsRead(0);
        PrivateMessage saved = privateMsgRepo.save(msg);
        unreadCountService.incrementPrivate(toUserId);
        return saved;
    }

    public List<PrivateMessage> getPrivateMessageHistory(Integer userId1, Integer userId2) {
        List<PrivateMessage> a = privateMsgRepo.findByFromIdAndToIdOrderBySendTimeAsc(userId1, userId2);
        List<PrivateMessage> b = privateMsgRepo.findByFromIdAndToIdOrderBySendTimeAsc(userId2, userId1);

        List<PrivateMessage> all = new ArrayList<>();
        all.addAll(a);
        all.addAll(b);
        all.sort(Comparator.comparing(PrivateMessage::getSendTime));

        for (PrivateMessage msg : all) {
            UserInfoVO user = userInfoService.getUserInfo(msg.getFromId());
            if (user != null) {
                msg.setUsername(user.getUsername());
                msg.setAvatar(user.getAvatar());
            }
        }
        return all;
    }

    public Long countUnreadPrivateMessages(Integer userId) {
        return unreadCountService.getPrivateCount(userId);
    }
}
