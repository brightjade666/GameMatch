package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamRecruit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRecruitRepository extends JpaRepository<TeamRecruit, Integer> {
    List<TeamRecruit> findByStatus(Integer status);
    List<TeamRecruit> findByLeaderId(Integer leaderId);

    // ===================== 新增方法 =====================

    // 查找所有队伍
    List<TeamRecruit> findAll();

    // 统计总数
    long count();

    // 按队伍名模糊搜索
    List<TeamRecruit> findByTeamNameContaining(String keyword);
}