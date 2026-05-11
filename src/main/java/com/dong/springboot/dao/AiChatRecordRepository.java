package com.dong.springboot.dao;

import com.dong.springboot.entity.AiChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiChatRecordRepository extends JpaRepository<AiChatRecord, Long> {
    List<AiChatRecord> findByUserIdOrderByCreateTimeAsc(Integer userId);


    void deleteByUserId(Integer userId);
}