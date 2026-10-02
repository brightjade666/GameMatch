package com.dong.springboot.service;

import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserMapper UserMapper;

    public User findByUsername(String username) {
        return UserMapper.findByUsername(username);
    }

    public void save(User user) {
        if (user.getContact() == null || user.getContact().isEmpty()) {
            user.setContact(user.getUsername());
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        user.setUpdateTime(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRole() == null) {
            user.setRole(0);
        }
        UserMapper.save(user);
    }
}