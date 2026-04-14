package com.dong.springboot.dao;

import com.dong.springboot.entity.TeamRecruit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRecruitRepository extends JpaRepository<TeamRecruit, Integer> {
    List<TeamRecruit> findByStatus(Integer status);
    List<TeamRecruit> findByLeaderId(Integer leaderId);
}