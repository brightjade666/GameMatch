package com.dong.springboot.controller;

import com.dong.springboot.entity.TbUser;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/user/info")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    // 保存/更新用户信息接口
    @PostMapping("/save")
    public Map<String, Object> saveUserInfo(
            @RequestParam Integer userId, // 前端传递的用户ID（真实数据库ID）
            @RequestBody UserProfile profile // 前端传递的档案信息
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 查询用户是否存在
            TbUser existUser = userInfoService.getUserInfo(userId);
            if (existUser == null) {
                result.put("code", 404);
                result.put("msg", "用户不存在");
                return result;
            }

            // 2. 组装用户基本信息（从profile/前端参数中获取）
            TbUser user = new TbUser();
            user.setUsername(existUser.getUsername()); // 保持原用户名，或从前端接收新值
            user.setGender(profile.getGender()); // 从档案中取性别
            user.setAge(profile.getAge()); // 从档案中取年龄
            user.setAvatar(profile.getAvatar()); // 从档案中取头像
            user.setContact(profile.getContact()); // 从档案中取联系方式
            user.setIntroduction(profile.getIntroduction()); // 从档案中取简介

            // 3. 正确调用saveUserInfo：传3个参数（userId, user, profile）
            boolean saveSuccess = userInfoService.saveUserInfo(userId, user, profile);

            if (saveSuccess) {
                result.put("code", 200);
                result.put("msg", "个人信息保存成功");
                result.put("data", existUser); // 返回更新后的用户信息
            } else {
                result.put("code", 400);
                result.put("msg", "个人信息保存失败");
            }
        } catch (RuntimeException e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 查询用户信息接口
    @GetMapping("/get/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TbUser user = userInfoService.getUserInfo(userId);
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
            result.put("msg", "服务器错误：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}