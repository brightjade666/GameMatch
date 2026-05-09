package com.dong.springboot.dao;

import com.dong.springboot.entity.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findByFromIdAndToIdOrderBySendTimeAsc(Integer fromId, Integer toId);
    void deleteByFromId(Integer fromId);
    void deleteByToId(Integer toId);
}