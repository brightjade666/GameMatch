package com.donghaoyu.sprintpro.service;

import com.donghaoyu.sprintpro.dao.UserRepository;
import com.donghaoyu.sprintpro.entity.TbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. 查询所有
    public List<TbUser> findAll() {
        return userRepository.findAll();
    }

    // 2. 根据ID查询
    public TbUser findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    // 3. 新增 / 修改
    public TbUser save(TbUser user) {
        return userRepository.save(user);
    }

    // 4. 删除
    public void delete(Integer id) {
        userRepository.deleteById(id);
    }
}