package com.dong.springboot.controller;

import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.service.UserInfoService;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
// 👇 删掉所有 @CrossOrigin 注解！！！
public class UserInfoController {

    private final UserInfoService userInfoService;

    @Autowired
    private UserProfileRepository userProfileRepository;

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

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody UserInfoVO vo) {
        userInfoService.updateUserInfo(vo);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "保存成功");
        return map;
    }

    @GetMapping("/match")
    public Map<String, Object> match(@RequestParam Integer gameId) {
        Map<String, Object> map = new HashMap<>();

        // 查询数据库中game_id匹配的记录
        List<UserProfile> profileList = userProfileRepository.findByGameId(gameId);

        // 提取user_id
        List<Integer> userIdList = profileList.stream()
                .map(UserProfile::getUserId)
                .collect(Collectors.toList());

        map.put("code", 200);
        map.put("msg", "匹配成功");
        map.put("data", userIdList);
        return map;
    }
}