package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.TeamComment;

import java.util.List;

public interface TeamCommentMapper extends BaseMapper<TeamComment> {
    default List<TeamComment> findByTeamIdOrderByCreateTimeDesc(Integer teamId) {
        return selectList(new LambdaQueryWrapper<TeamComment>()
                .eq(TeamComment::getTeamId, teamId)
                .orderByDesc(TeamComment::getCreateTime));
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<TeamComment>().eq(TeamComment::getUserId, userId));
    }

    default void deleteByTeamId(Integer teamId) {
        delete(new LambdaQueryWrapper<TeamComment>().eq(TeamComment::getTeamId, teamId));
    }

    default TeamComment save(TeamComment comment) {
        if (comment.getId() == null) {
            insert(comment);
        } else {
            updateById(comment);
        }
        return comment;
    }
}
