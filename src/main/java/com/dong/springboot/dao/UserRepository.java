package com.dong.springboot.dao;

import com.dong.springboot.entity.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<TbUser, Integer> {

    // 根据用户名查询用户 → 登录专用（JPA 自动实现）
    TbUser findByUsername(String username);
}