package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.PrivateMessage;

import java.util.List;

public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
    default List<PrivateMessage> findByFromIdAndToIdOrderBySendTimeAsc(Integer fromId, Integer toId) {
        return selectList(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getFromId, fromId)
                .eq(PrivateMessage::getToId, toId)
                .orderByAsc(PrivateMessage::getSendTime));
    }

    default void deleteByFromId(Integer fromId) {
        delete(new LambdaQueryWrapper<PrivateMessage>().eq(PrivateMessage::getFromId, fromId));
    }

    default void deleteByToId(Integer toId) {
        delete(new LambdaQueryWrapper<PrivateMessage>().eq(PrivateMessage::getToId, toId));
    }

    default Long countUnreadByToId(Integer toId) {
        return selectCount(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getToId, toId)
                .eq(PrivateMessage::getIsRead, 0));
    }

    default PrivateMessage save(PrivateMessage msg) {
        if (msg.getId() == null) {
            insert(msg);
        } else {
            updateById(msg);
        }
        return msg;
    }
}
