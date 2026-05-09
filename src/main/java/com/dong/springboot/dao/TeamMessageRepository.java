package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMessageRepository extends JpaRepository<TeamMessage, Integer> {
    List<TeamMessage> findByTeamIdOrderBySendTimeDesc(Integer teamId);
    List<TeamMessage> findByTeamIdOrderBySendTimeAsc(Integer teamId);

    void deleteByFromId(Integer fromId);
    void deleteByTeamId(Integer teamId);   // ✅ 新增
}