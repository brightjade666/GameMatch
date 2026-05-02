package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dao.SystemNotificationRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final SystemNotificationRepository notiRepo;

    public NotificationController(SystemNotificationRepository notiRepo) {
        this.notiRepo = notiRepo;
    }

    @GetMapping("/my")
    public Result myNotifications(@RequestParam Integer userId) {
        return Result.success(notiRepo.findByUserIdOrderByCreateTimeDesc(userId));
    }
}