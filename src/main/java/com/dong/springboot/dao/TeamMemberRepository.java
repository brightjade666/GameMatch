package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamMember;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface TeamMemberRepository extends CrudRepository<TeamMember, Integer> {

    // 根据用户ID → 查询他加入了哪些队伍
    List<TeamMember> findByUserId(Integer userId);

    // 根据队伍ID → 查询这个队伍有哪些成员
    List<TeamMember> findByTeamId(Integer teamId);
}