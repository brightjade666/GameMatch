package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.entity.Friend;
import com.dong.springboot.entity.SystemNotification;
import com.dong.springboot.entity.User;
import com.dong.springboot.mapper.FriendMapper;
import com.dong.springboot.mapper.SystemNotificationMapper;
import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.service.UnreadCountService;
import com.dong.springboot.vo.FriendVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friend")
public class FriendController {

    private final FriendMapper friendRepo;
    private final UserMapper userRepo;
    private final SystemNotificationMapper notiRepo;
    private final UnreadCountService unreadCountService;

    public FriendController(FriendMapper friendRepo, UserMapper userRepo, SystemNotificationMapper notiRepo, UnreadCountService unreadCountService) {
        this.friendRepo = friendRepo;
        this.userRepo = userRepo;
        this.notiRepo = notiRepo;
        this.unreadCountService = unreadCountService;
    }

    @PostMapping("/apply")
    public Result apply(@RequestParam Integer fromUserId,
                        @RequestParam Integer toUserId,
                        @RequestParam(required = false) String reason) {
        List<Friend> existList = friendRepo.findFriendship(fromUserId, toUserId);
        Friend exist = existList.isEmpty() ? null : existList.get(0);

        if (exist != null) {
            if (exist.getStatus() == 1) {
                return Result.error("已经是好友了");
            }
            if (exist.getStatus() == 0) {
                return Result.error("已经发送过申请，请等待对方处理");
            }
            exist.setStatus(0);
            exist.setReason(reason);
            exist.setCreateTime(LocalDateTime.now());
            friendRepo.save(exist);
        } else {
            Friend apply = new Friend();
            apply.setUserId(fromUserId);
            apply.setFriendId(toUserId);
            apply.setStatus(0);
            apply.setReason(reason);
            apply.setCreateTime(LocalDateTime.now());
            friendRepo.save(apply);
        }

        SystemNotification noti = new SystemNotification();
        noti.setUserId(toUserId);
        User from = userRepo.findById(fromUserId).orElse(null);
        String nick = from != null ? from.getUsername() : "用户";
        String suffix = reason != null && !reason.isBlank() ? "（原因：" + reason + "）" : "";
        noti.setContent(nick + " 申请添加您为好友" + suffix);
        noti.setType("friend_apply");
        noti.setRelatedId(fromUserId);
        noti.setCreateTime(LocalDateTime.now());
        saveNoti(noti);
        return Result.success("申请已发送");
    }

    @GetMapping("/applies/received")
    public Result receivedApplies(@RequestParam Integer userId) {
        List<Friend> applies = friendRepo.findByFriendIdAndStatus(userId, 0);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Friend apply : applies) {
            User fromUser = userRepo.findById(apply.getUserId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("id", apply.getId());
            item.put("fromUserId", apply.getUserId());
            item.put("fromName", fromUser != null ? fromUser.getUsername() : "未知");
            item.put("reason", apply.getReason());
            item.put("createTime", apply.getCreateTime());
            list.add(item);
        }
        return Result.success(list);
    }

    @PostMapping("/handle")
    @Transactional
    public Result handle(@RequestParam Integer applyId, @RequestParam String action) {
        Friend apply = friendRepo.findById(applyId);
        if (apply == null || apply.getStatus() != 0) {
            return Result.error("申请不存在或已处理");
        }

        if ("accept".equals(action)) {
            apply.setStatus(1);
            friendRepo.save(apply);

            SystemNotification noti = new SystemNotification();
            noti.setUserId(apply.getUserId());
            User toUser = userRepo.findById(apply.getFriendId()).orElse(null);
            String nick = toUser != null ? toUser.getUsername() : "用户";
            noti.setContent(nick + " 已接受您的好友申请");
            noti.setType("friend_accept");
            noti.setCreateTime(LocalDateTime.now());
            saveNoti(noti);
            return Result.success("已同意");
        }

        if ("reject".equals(action)) {
            apply.setStatus(2);
            friendRepo.save(apply);
            return Result.success("已拒绝");
        }

        return Result.error("无效操作");
    }

    @GetMapping("/list")
    public Result friendList(@RequestParam Integer userId) {
        List<FriendVO> result = friendRepo.selectFriendList(userId);
        return Result.success(result);
    }

    @DeleteMapping("/delete")
    @Transactional
    public Result deleteFriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        List<Friend> friendshipList = friendRepo.findFriendship(userId, friendId);
        if (friendshipList.isEmpty()) {
            return Result.error("不是好友关系");
        }

        Friend friendship = friendshipList.get(0);
        if (friendship.getStatus() != 1) {
            return Result.error("不是好友关系");
        }

        friendRepo.deleteAll(friendshipList);

        SystemNotification noti = new SystemNotification();
        noti.setUserId(friendId);
        User user = userRepo.findById(userId).orElse(null);
        noti.setContent((user != null ? user.getUsername() : "用户") + " 已将您删除好友");
        noti.setType("friend_delete");
        noti.setCreateTime(LocalDateTime.now());
        saveNoti(noti);
        return Result.success("删除成功");
    }

    private void saveNoti(SystemNotification noti) {
        notiRepo.save(noti);
        unreadCountService.incrementNotification(noti.getUserId());
    }
}
