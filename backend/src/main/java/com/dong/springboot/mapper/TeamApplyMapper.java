package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.TeamApply;

import java.util.List;
import java.util.Optional;

public interface TeamApplyMapper extends BaseMapper<TeamApply> {
    default List<TeamApply> findByTeamId(Integer teamId) {
        return selectList(new LambdaQueryWrapper<TeamApply>().eq(TeamApply::getTeamId, teamId));
    }

    default List<TeamApply> findByTeamIdAndUserId(Integer teamId, Integer userId) {
        return selectList(new LambdaQueryWrapper<TeamApply>()
                .eq(TeamApply::getTeamId, teamId)
                .eq(TeamApply::getUserId, userId));
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<TeamApply>().eq(TeamApply::getUserId, userId));
    }

    default void deleteByTeamId(Integer teamId) {
        delete(new LambdaQueryWrapper<TeamApply>().eq(TeamApply::getTeamId, teamId));
    }

    default TeamApply save(TeamApply apply) {
        if (apply.getId() == null) {
            insert(apply);
        } else {
            updateById(apply);
        }
        return apply;
    }

    default Optional<TeamApply> findById(Integer id) {
        return Optional.ofNullable(selectById(id));
    }
}
