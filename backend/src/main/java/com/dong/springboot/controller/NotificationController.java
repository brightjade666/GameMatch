package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dto.LoginUserCacheDTO;
import com.dong.springboot.mapper.SystemNotificationMapper;
import com.dong.springboot.service.UnreadCountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final SystemNotificationMapper notiRepo;
    private final UnreadCountService unreadCountService;

    public NotificationController(SystemNotificationMapper notiRepo, UnreadCountService unreadCountService) {
        this.notiRepo = notiRepo;
        this.unreadCountService = unreadCountService;
    }

    @GetMapping("/my")
    public Result myNotifications(HttpServletRequest request) {
        Integer userId = currentUserId(request);
        Result result = Result.success(notiRepo.findByUserIdOrderByCreateTimeDesc(userId));
        unreadCountService.markAllNotificationsRead(userId);
        return result;
    }

    @GetMapping("/unread-count")
    public Result unreadCount(HttpServletRequest request) {
        Integer userId = currentUserId(request);
        return Result.success(unreadCountService.getNotificationCount(userId));
    }

    private Integer currentUserId(HttpServletRequest request) {
        LoginUserCacheDTO loginUser = (LoginUserCacheDTO) request.getAttribute("loginUser");
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new RuntimeException("未登录");
        }
        return loginUser.getUserId();
    }
}
