package com.dong.springboot.dao;

import com.dong.springboot.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Integer> {
    List<SystemNotification> findByUserIdOrderByCreateTimeDesc(Integer userId);
}