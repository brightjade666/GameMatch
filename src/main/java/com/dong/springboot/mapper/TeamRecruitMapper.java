package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.vo.TeamDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRecruitMapper extends BaseMapper<TeamRecruit> {

    default List<TeamRecruit> findByStatus(Integer status) {
        return selectList(new LambdaQueryWrapper<TeamRecruit>()
                .eq(TeamRecruit::getStatus, status));
    }

    default List<TeamRecruit> findByLeaderId(Integer leaderId) {
        return selectList(new LambdaQueryWrapper<TeamRecruit>()
                .eq(TeamRecruit::getLeaderId, leaderId));
    }

    default List<TeamRecruit> findAll() {
        return selectList(new LambdaQueryWrapper<TeamRecruit>()
                .orderByDesc(TeamRecruit::getCreateTime));
    }

    default long count() {
        return selectCount(null);
    }

    default List<TeamRecruit> findByTeamNameContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return selectList(new LambdaQueryWrapper<TeamRecruit>()
                .like(TeamRecruit::getTeamName, keyword)
                .orderByDesc(TeamRecruit::getCreateTime));
    }

    default TeamRecruit save(TeamRecruit team) {
        if (team.getTeamId() == null) {
            insert(team);
        } else {
            updateById(team);
        }
        return team;
    }

    default Optional<TeamRecruit> findById(Integer id) {
        return Optional.ofNullable(selectById(id));
    }

    default void delete(TeamRecruit team) {
        if (team != null && team.getTeamId() != null) {
            deleteById(team.getTeamId());
        }
    }

    TeamDetailVO selectTeamDetailById(@Param("teamId") Integer teamId);
}
