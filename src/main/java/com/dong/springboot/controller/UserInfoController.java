package com.dong.springboot.controller;

import com.dong.springboot.service.UserInfoService;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserInfoController {

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping("/info")
    public Map<String, Object> info(@RequestParam Integer userId) {
        UserInfoVO vo = userInfoService.getUserInfo(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", vo);
        return map;
    }
    // ======================
    // 【新增】保存资料接口
    // ======================
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody UserInfoVO vo) {
        userInfoService.updateUserInfo(vo);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "保存成功");
        return map;
    }
}