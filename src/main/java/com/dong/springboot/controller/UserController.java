package com.dong.springboot.controller;

import com.dong.springboot.entity.TbUser;
import com.dong.springboot.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 修复后的登录接口（完全适配TbUser的getUserId()）
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");

            // 空值校验
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "用户名/密码不能为空");
                return result;
            }

            // 1. 按用户名查询（适配UserRepository返回TbUser，非Optional）
            TbUser user = userRepository.findByUsername(username);
            if (user == null) {
                result.put("code", 404);
                result.put("msg", "用户不存在");
                return result;
            }

            // 2. 密码校验（明文，保持你的原有逻辑）
            if (!password.equals(user.getPassword())) {
                result.put("code", 401);
                result.put("msg", "密码错误");
                return result;
            }

            // 3. 组装返回数据（调用TbUser正确的getter方法）
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId()); // 核心：用getUserId()，匹配实体类
            userData.put("username", user.getUsername());
            userData.put("gender", user.getGender());
            userData.put("age", user.getAge());
            userData.put("avatar", user.getAvatar());
            userData.put("contact", user.getContact());
            userData.put("gameId", user.getGameId());
            userData.put("gameRank", user.getGameRank());
            userData.put("introduction", user.getIntroduction());
            userData.put("role", user.getRole());
            userData.put("status", user.getStatus());
            userData.put("createTime", user.getCreateTime());
            userData.put("updateTime", user.getUpdateTime());

            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", userData);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "登录失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 保留你原有所有其他接口（以下是示例，按你的实际代码保留即可）
    @GetMapping("/user/findAll")
    public Map<String, Object> findAll() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<TbUser> userList = userRepository.findAll();
            result.put("code", 200);
            result.put("msg", "查询所有用户成功");
            result.put("data", userList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/user/findById/{userId}")
    public Map<String, Object> findById(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TbUser user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", user);
            } else {
                result.put("code", 404);
                result.put("msg", "用户不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 其他接口（save/delete/batchDelete/search等）按你的原有代码保留即可
}