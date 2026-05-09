package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {

    List<TeamMember> findByUserId(Integer userId);
    List<TeamMember> findByTeamId(Integer teamId);
    void deleteByTeamIdAndUserId(Integer teamId, Integer userId);

    // 新增：删除某个用户的所有成员记录（管理员级联删除用）
    void deleteByUserId(Integer userId);
}