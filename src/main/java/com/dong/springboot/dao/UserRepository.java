package com.dong.springboot.dao;

import com.dong.springboot.entity.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<TbUser, Integer> {

    // 根据用户名查询用户 → 登录专用（原有方法，保留）
    TbUser findByUsername(String username);

    // ===== 新增：模糊查询（加在原有方法后面）=====
    List<TbUser> findByUsernameContaining(String keyword);
}