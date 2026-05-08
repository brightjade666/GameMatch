package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMessageRepository extends JpaRepository<TeamMessage, Integer> {
    // 根据队伍ID 按消息时间倒序
    // 按时间倒序（拿最后一条）
    List<TeamMessage> findByTeamIdOrderBySendTimeDesc(Integer teamId);

    // 按时间正序（加载历史记录）
    List<TeamMessage> findByTeamIdOrderBySendTimeAsc(Integer teamId);
}