package com.dong.springboot.dao;

import com.dong.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 🔥 就加这一行！不加永远500！
    User findByUsername(String username);

}