package com.dong.springboot.dao;

import com.dong.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 已有方法
    User findByUsername(String username);

    // ========== 为管理员功能添加的方法 ==========
    // 查询所有用户，按创建时间倒序（最新注册的在最前面）
    List<User> findAllByOrderByCreateTimeDesc();

    // 按用户名模糊搜索（like %keyword%）
    List<User> findByUsernameContaining(String keyword);

    // 统计用户总数
    long count();

    // 按状态统计用户数量（1=启用，0=禁用）
    long countByStatus(Integer status);
}