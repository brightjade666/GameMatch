package com.dong.springboot.dao;

import com.dong.springboot.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    // 根据用户ID查询档案
    Optional<UserProfile> findByUserId(Integer userId);
    // 判断用户是否已有档案
    boolean existsByUserId(Integer userId);
}