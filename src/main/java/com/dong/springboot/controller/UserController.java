package com.dong.springboot.controller;

import com.dong.springboot.entity.TbUser;
import com.dong.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
// 跨域配置 → 解决前端访问后端报错
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // ====================== 你原来的接口（保留不动）======================
    // 查询所有
    @GetMapping("/user/findAll")
    public List<TbUser> findAll() {
        return userService.findAll();
    }

    // 根据ID查询
    @GetMapping("/user/findById/{id}")
    public TbUser findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    // 新增（注册）
    @PostMapping("/user/save")
    public TbUser save(@RequestBody TbUser user) {
        return userService.save(user);
    }

    // 删除
    @GetMapping("/user/delete/{id}")
    public String delete(@PathVariable Integer id) {
        userService.delete(id);
        return "删除成功";
    }

    // ====================== 新增：登录接口（对接你的前端）======================
    /**
     * 登录接口
     * 前端地址：http://localhost:8081/login
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody TbUser user) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取前端传的用户名密码
        String username = user.getUsername();
        String password = user.getPassword();
        System.out.println(username);
        System.out.println(password);
        // 2. 后端校验
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            result.put("code", 500);
            result.put("msg", "用户名和密码不能为空");
            return result;
        }

        // 3. 查询用户
        TbUser loginUser = userService.findByUsername(username);

        // 4. 判断用户是否存在 + 密码是否正确
        if (loginUser == null) {
            result.put("code", 500);
            result.put("msg", "用户名不存在");
            return result;
        }

        if (!password.equals(loginUser.getPassword())) {
            result.put("code", 500);
            result.put("msg", "密码错误");
            return result;
        }

        // 5. 登录成功
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("user", loginUser);
        return result;
    }
}