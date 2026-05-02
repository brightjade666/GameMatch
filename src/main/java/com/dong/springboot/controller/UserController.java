package com.dong.springboot.controller;

import com.dong.springboot.entity.User;
import com.dong.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
// 跨域配置 → 解决前端访问后端报错
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;
    // ====================== 登录接口（对接你的前端）======================
    /**
     * 登录接口
     * 前端地址：http://localhost:8081/login
     */

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取前端传的用户名密码
        String username = user.getUsername();
        String password = user.getPassword();

        // 2. 非空校验
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            result.put("code", 500);
            result.put("msg", "用户名和密码不能为空");
            return result;
        }

        // 3. 查询用户
        User loginUser = userService.findByUsername(username);

        // 4. 用户不存在
        if (loginUser == null) {
            result.put("code", 500);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        // 5. 密码错误
        if (!password.equals(loginUser.getPassword())) {
            result.put("code", 500);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        // 6. 登录成功（前端能正常识别、跳转）
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("user_id", loginUser.getUserId());
        result.put("username", loginUser.getUsername());
        result.put("token", "LOGIN_SUCCESS_" + System.currentTimeMillis()); // 给前端需要的token
        return result;
    }

    // ====================== 注册接口 ======================
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 1. 判空
        if (user.getUsername() == null || user.getPassword() == null) {
            result.put("code", 500);
            result.put("msg", "用户名或密码不能为空");
            return result;
        }

        // 2. 判断用户名是否已存在
        User exist = userService.findByUsername(user.getUsername());
        if (exist != null) {
            result.put("code", 500);
            result.put("msg", "用户名已存在");
            return result;
        }

        // 3. 保存用户
        userService.save(user);

        // 4. 返回成功
        result.put("code", 200);
        result.put("msg", "注册成功！请登录");
        return result;
    }

}