package com.dong.springboot.service;

import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.TbUser; // 改为新的 User 类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 查询所有
    public List<TbUser> findAll() {
        return userRepository.findAll();
    }

    // 根据ID查询（参数名从 id 改为 userId，语义更清晰）
    public TbUser findById(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // 新增/修改
    public TbUser save(TbUser user) {
        return userRepository.save(user);
    }

    // 删除（参数名改为 userId）
    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    // 根据用户名查询（登录用）
    public TbUser findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 模糊查询
    public List<TbUser> findByUsernameContaining(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }
}