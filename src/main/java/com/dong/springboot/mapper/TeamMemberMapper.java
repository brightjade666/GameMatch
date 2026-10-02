package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.TeamMember;

import java.util.List;

public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    default List<TeamMember> findByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId));
    }

    default List<TeamMember> findByTeamId(Integer teamId) {
        return selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId));
    }

    default void deleteByTeamIdAndUserId(Integer teamId, Integer userId) {
        delete(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId));
    }

    default TeamMember save(TeamMember member) {
        if (member.getId() == null) {
            insert(member);
        } else {
            updateById(member);
        }
        return member;
    }

    default void delete(TeamMember member) {
        if (member != null && member.getId() != null) {
            deleteById(member.getId());
        }
    }

    default void deleteAll(List<TeamMember> members) {
        if (members == null) {
            return;
        }
        for (TeamMember member : members) {
            delete(member);
        }
    }
}
