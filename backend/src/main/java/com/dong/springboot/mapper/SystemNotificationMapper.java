package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.SystemNotification;

import java.util.List;

public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {
    default List<SystemNotification> findByUserIdOrderByCreateTimeDesc(Integer userId) {
        return selectList(new LambdaQueryWrapper<SystemNotification>()
                .eq(SystemNotification::getUserId, userId)
                .orderByDesc(SystemNotification::getCreateTime));
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<SystemNotification>().eq(SystemNotification::getUserId, userId));
    }

    default Long countUnreadByUserId(Integer userId) {
        return selectCount(new LambdaQueryWrapper<SystemNotification>()
                .eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0));
    }

    default SystemNotification save(SystemNotification notification) {
        if (notification.getId() == null) {
            insert(notification);
        } else {
            updateById(notification);
        }
        return notification;
    }
}
