package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamCommentRepository extends JpaRepository<TeamComment, Integer> {
    // 修改：增加按创建时间降序查询（最新优先）
    List<TeamComment> findByTeamIdOrderByCreateTimeDesc(Integer teamId);
    // TeamCommentRepository.java
    void deleteByUserId(Integer userId);
    void deleteByTeamId(Integer teamId);   // 可选，用于清理队伍下的评论
}