package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamApply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamApplyRepository extends JpaRepository<TeamApply, Integer> {
    List<TeamApply> findByTeamId(Integer teamId);
    List<TeamApply> findByTeamIdAndUserId(Integer teamId, Integer userId);

    void deleteByUserId(Integer userId);
    void deleteByTeamId(Integer teamId);   // ✅ 新增
}