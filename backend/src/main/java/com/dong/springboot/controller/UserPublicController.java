package com.dong.springboot.controller;

import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.mapper.GameMapper;
import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.mapper.UserProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserPublicController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private GameMapper gameMapper;

    @GetMapping("/user/publicInfo")
    public Map<String, Object> publicInfo(@RequestParam Integer userId) {
        Map<String, Object> map = new HashMap<>();

        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            map.put("code", 500);
            map.put("msg", "用户不存在");
            return map;
        }

        UserProfile profile = userProfileMapper.findByUserId(userId);

        map.put("code", 200);
        map.put("nick", user.getUsername());
        map.put("avatar", user.getAvatar());
        map.put("gender", user.getGender() == 1 ? "男" : "女");
        map.put("introduction", user.getIntroduction() != null ? user.getIntroduction() : "");

        String gameName = "未设置";
        if (profile != null && profile.getGameId() != null) {
            Game game = gameMapper.findById(profile.getGameId()).orElse(null);
            if (game != null) {
                gameName = game.getGameName();
            }
        }
        map.put("gameName", gameName);

        String matchNeed = profile != null && profile.getTeamRequirement() != null
                ? profile.getTeamRequirement()
                : "";
        map.put("matchNeed", matchNeed);
        return map;
    }
}
