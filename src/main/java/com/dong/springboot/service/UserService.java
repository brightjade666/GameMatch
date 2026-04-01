package com.dong.springboot.service;

import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.TbUser;
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

    // 根据ID查询
    public TbUser findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    // 新增/修改
    public TbUser save(TbUser user) {
        return userRepository.save(user);
    }

    // 删除
    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    // ===================== 新增：登录专用方法 =====================
    /**
     * 根据用户名查询用户（给登录接口用）
     */
    public TbUser findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}