package com.dong.springboot.service;

import com.dong.springboot.entity.PrivateMessage;
import com.dong.springboot.entity.SystemNotification;
import com.dong.springboot.mapper.PrivateMessageMapper;
import com.dong.springboot.mapper.SystemNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@Service
public class UnreadCountService {

    private static final String NOTI_PREFIX = "unread:noti:";
    private static final String PRIVATE_PREFIX = "unread:private:";
    private static final Duration TTL = Duration.ofDays(30);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SystemNotificationMapper systemNotificationMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    public void incrementNotification(Integer userId) {
        increment(NOTI_PREFIX + userId, 1L);
    }

    public void incrementPrivate(Integer userId) {
        increment(PRIVATE_PREFIX + userId, 1L);
    }

    public long getNotificationCount(Integer userId) {
        return getCountOrSync(NOTI_PREFIX + userId, systemNotificationMapper.countUnreadByUserId(userId));
    }

    public long getPrivateCount(Integer userId) {
        return getCountOrSync(PRIVATE_PREFIX + userId, privateMessageMapper.countUnreadByToId(userId));
    }

    public void markAllNotificationsRead(Integer userId) {
        List<SystemNotification> list = systemNotificationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getUserId, userId)
                        .eq(SystemNotification::getIsRead, 0)
        );
        if (list.isEmpty()) {
            setCount(NOTI_PREFIX + userId, 0L);
            return;
        }
        for (SystemNotification notification : list) {
            notification.setIsRead(1);
            systemNotificationMapper.updateById(notification);
        }
        setCount(NOTI_PREFIX + userId, 0L);
    }

    public void markPrivateRead(Integer userId, Integer fromId) {
        List<PrivateMessage> list = privateMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PrivateMessage>()
                        .eq(PrivateMessage::getFromId, fromId)
                        .eq(PrivateMessage::getToId, userId)
                        .eq(PrivateMessage::getIsRead, 0)
        );
        if (list.isEmpty()) {
            return;
        }
        for (PrivateMessage msg : list) {
            msg.setIsRead(1);
            privateMessageMapper.updateById(msg);
        }
        decrement(PRIVATE_PREFIX + userId, list.size());
    }

    private long getCountOrSync(String key, Long dbCount) {
        String redisValue = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(redisValue)) {
            return Long.parseLong(redisValue);
        }
        long count = dbCount == null ? 0L : dbCount;
        setCount(key, count);
        return count;
    }

    private void increment(String key, long delta) {
        Long value = stringRedisTemplate.opsForValue().increment(key, delta);
        if (value != null && value == delta) {
            stringRedisTemplate.expire(key, TTL);
        }
    }

    private void decrement(String key, long delta) {
        Long value = stringRedisTemplate.opsForValue().increment(key, -delta);
        if (value == null || value < 0) {
            setCount(key, 0L);
        }
    }

    private void setCount(String key, long value) {
        stringRedisTemplate.opsForValue().set(key, Long.toString(Math.max(value, 0L)), TTL);
    }
}
