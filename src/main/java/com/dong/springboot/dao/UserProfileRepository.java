package com.dong.springboot.dao;

import com.dong.springboot.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    // 根据用户ID查询游戏档案（你必须要这个）
    UserProfile findByUserId(Integer userId);

}