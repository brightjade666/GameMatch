package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dao.FriendRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.Friend;
import com.dong.springboot.entity.SystemNotification;
import com.dong.springboot.entity.User;
import com.dong.springboot.dao.SystemNotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/friend")
public class FriendController {

    private final FriendRepository friendRepo;
    private final UserRepository userRepo;
    private final SystemNotificationRepository notiRepo;

    public FriendController(FriendRepository friendRepo,
                            UserRepository userRepo,
                            SystemNotificationRepository notiRepo) {
        this.friendRepo = friendRepo;
        this.userRepo = userRepo;
        this.notiRepo = notiRepo;
    }

    // 1. 发送好友申请
    @PostMapping("/apply")
    public Result apply(@RequestParam Integer fromUserId,
                        @RequestParam Integer toUserId,
                        @RequestParam(required = false) String reason) {
        // 检查是否已是好友
        Friend exist = friendRepo.findFriendship(fromUserId, toUserId);
        if (exist != null) {
            if (exist.getStatus() == 1) return Result.error("已经是好友了");
            if (exist.getStatus() == 0) return Result.error("已经发送过申请，请等待对方处理");
            // status=2 可以重新申请
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

        // 通知被申请人
        SystemNotification noti = new SystemNotification();
        noti.setUserId(toUserId);
        User from = userRepo.findById(fromUserId).orElse(null);
        String nick = from != null ? from.getUsername() : "用户";
        noti.setContent(nick + " 申请添加您为好友" + (reason != null ? "（原因：" + reason + "）" : ""));
        noti.setType("friend_apply");
        noti.setRelatedId(fromUserId);
        noti.setCreateTime(LocalDateTime.now());
        notiRepo.save(noti);

        return Result.success("申请已发送");
    }

    // 2. 查看收到的好友申请（status=0 且 friendId=userId）
    @GetMapping("/applies/received")
    public Result receivedApplies(@RequestParam Integer userId) {
        List<Friend> applies = friendRepo.findByFriendIdAndStatus(userId, 0);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Friend a : applies) {
            User fromUser = userRepo.findById(a.getUserId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("fromUserId", a.getUserId());
            item.put("fromName", fromUser != null ? fromUser.getUsername() : "未知");
            item.put("reason", a.getReason());
            item.put("createTime", a.getCreateTime());
            list.add(item);
        }
        return Result.success(list);
    }

    // 3. 处理好友申请（接受 / 拒绝）
    @PostMapping("/handle")
    @Transactional
    public Result handle(@RequestParam Integer applyId,
                         @RequestParam String action) {
        Friend apply = friendRepo.findById(applyId);
        if (apply == null || apply.getStatus() != 0) {
            return Result.error("申请不存在或已处理");
        }

        if ("accept".equals(action)) {
            apply.setStatus(1);
            friendRepo.save(apply);
            // 通知申请人
            SystemNotification noti = new SystemNotification();
            noti.setUserId(apply.getUserId());
            User toUser = userRepo.findById(apply.getFriendId()).orElse(null);
            String nick = toUser != null ? toUser.getUsername() : "用户";
            noti.setContent(nick + " 已接受您的好友申请");
            noti.setType("friend_accept");
            noti.setCreateTime(LocalDateTime.now());
            notiRepo.save(noti);
            return Result.success("已同意");
        } else if ("reject".equals(action)) {
            apply.setStatus(2);
            friendRepo.save(apply);
            return Result.success("已拒绝");
        }
        return Result.error("无效操作");
    }

    // 4. 获取好友列表（返回对方基础信息）
    @GetMapping("/list")
    public Result friendList(@RequestParam Integer userId) {
        List<Friend> list1 = friendRepo.findByUserId(userId);
        List<Friend> list2 = friendRepo.findByFriendId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Friend f : list1) {
            if (f.getStatus() == 1) {
                User friendUser = userRepo.findById(f.getFriendId()).orElse(null);
                if (friendUser != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("friendId", friendUser.getUserId());
                    map.put("name", friendUser.getUsername());
                    map.put("avatar", friendUser.getAvatar());
                    map.put("gameRank", friendUser.getGameRank());
                    result.add(map);
                }
            }
        }
        for (Friend f : list2) {
            if (f.getStatus() == 1) {
                User friendUser = userRepo.findById(f.getUserId()).orElse(null);
                if (friendUser != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("friendId", friendUser.getUserId());
                    map.put("name", friendUser.getUsername());
                    map.put("avatar", friendUser.getAvatar());
                    map.put("gameRank", friendUser.getGameRank());
                    result.add(map);
                }
            }
        }
        return Result.success(result);
    }

    // 5. 删除好友（双向断开）
    @DeleteMapping("/delete")
    @Transactional
    public Result deleteFriend(@RequestParam Integer userId,
                               @RequestParam Integer friendId) {
        Friend friendship = friendRepo.findFriendship(userId, friendId);
        if (friendship == null || friendship.getStatus() != 1) {
            return Result.error("不是好友关系");
        }
        friendRepo.delete(friendship);
        // 通知对方（可选）
        SystemNotification noti = new SystemNotification();
        noti.setUserId(friendId);
        User user = userRepo.findById(userId).orElse(null);
        noti.setContent((user != null ? user.getUsername() : "用户") + " 已将您删除好友");
        noti.setType("friend_delete");
        noti.setCreateTime(LocalDateTime.now());
        notiRepo.save(noti);
        return Result.success("删除成功");
    }
}