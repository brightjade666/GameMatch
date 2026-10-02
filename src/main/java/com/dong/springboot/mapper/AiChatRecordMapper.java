package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.AiChatRecord;

import java.util.List;

public interface AiChatRecordMapper extends BaseMapper<AiChatRecord> {
    default List<AiChatRecord> findByUserIdOrderByCreateTimeAsc(Integer userId) {
        return selectList(new LambdaQueryWrapper<AiChatRecord>()
                .eq(AiChatRecord::getUserId, userId)
                .orderByAsc(AiChatRecord::getCreateTime));
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<AiChatRecord>().eq(AiChatRecord::getUserId, userId));
    }

    default AiChatRecord save(AiChatRecord record) {
        if (record.getId() == null) {
            insert(record);
        } else {
            updateById(record);
        }
        return record;
    }
}
