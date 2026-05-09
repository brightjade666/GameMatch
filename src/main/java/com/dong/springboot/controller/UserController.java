package com.dong.springboot.controller;

import com.dong.springboot.entity.User;
import com.dong.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            result.put("code", 500);
            result.put("msg", "用户名和密码不能为空");
            return result;
        }

        User loginUser = userService.findByUsername(username);

        if (loginUser == null) {
            result.put("code", 500);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        if (!password.equals(loginUser.getPassword())) {
            result.put("code", 500);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        // 检查用户是否被禁用
        if (loginUser.getStatus() == 0) {
            result.put("code", 500);
            result.put("msg", "账号已被禁用，请联系管理员");
            return result;
        }

        // 登录成功（包含 role 字段）
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("user_id", loginUser.getUserId());
        result.put("username", loginUser.getUsername());
        result.put("role", loginUser.getRole());
        result.put("token", "LOGIN_SUCCESS_" + System.currentTimeMillis());
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getUsername() == null || user.getPassword() == null) {
            result.put("code", 500);
            result.put("msg", "用户名或密码不能为空");
            return result;
        }

        User exist = userService.findByUsername(user.getUsername());
        if (exist != null) {
            result.put("code", 500);
            result.put("msg", "用户名已存在");
            return result;
        }

        userService.save(user);

        result.put("code", 200);
        result.put("msg", "注册成功！请登录");
        return result;
    }
}