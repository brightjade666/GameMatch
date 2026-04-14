package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamCommentRepository extends JpaRepository<TeamComment, Integer> {
    List<TeamComment> findByTeamId(Integer teamId);
}