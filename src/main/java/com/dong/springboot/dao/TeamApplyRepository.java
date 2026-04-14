package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamApply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamApplyRepository extends JpaRepository<TeamApply, Integer> {
    List<TeamApply> findByTeamId(Integer teamId);
}