package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.TeamMessage;

import java.util.List;

public interface TeamMessageMapper extends BaseMapper<TeamMessage> {
    default List<TeamMessage> findByTeamIdOrderBySendTimeDesc(Integer teamId) {
        return selectList(new LambdaQueryWrapper<TeamMessage>()
                .eq(TeamMessage::getTeamId, teamId)
                .orderByDesc(TeamMessage::getSendTime));
    }

    default List<TeamMessage> findByTeamIdOrderBySendTimeAsc(Integer teamId) {
        return selectList(new LambdaQueryWrapper<TeamMessage>()
                .eq(TeamMessage::getTeamId, teamId)
                .orderByAsc(TeamMessage::getSendTime));
    }

    default void deleteByFromId(Integer fromId) {
        delete(new LambdaQueryWrapper<TeamMessage>().eq(TeamMessage::getFromId, fromId));
    }

    default void deleteByTeamId(Integer teamId) {
        delete(new LambdaQueryWrapper<TeamMessage>().eq(TeamMessage::getTeamId, teamId));
    }

    default TeamMessage save(TeamMessage msg) {
        if (msg.getId() == null) {
            insert(msg);
        } else {
            updateById(msg);
        }
        return msg;
    }
}
