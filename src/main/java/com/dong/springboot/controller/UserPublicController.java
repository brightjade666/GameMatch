package com.dong.springboot.controller;

import com.dong.springboot.dao.GameRepository;
import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserPublicController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private GameRepository gameRepository;

    @GetMapping("/user/publicInfo")
    public Map<String, Object> publicInfo(@RequestParam Integer userId) {
        Map<String, Object> map = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            map.put("code", 500);
            map.put("msg", "用户不存在");
            return map;
        }

        UserProfile profile = userProfileRepository.findByUserId(userId);

        // 基础信息
        map.put("code", 200);
        map.put("nick", user.getUsername());
        map.put("avatar", user.getAvatar());        // 头像路径
        map.put("gender", user.getGender() == 1 ? "男" : "女");
        map.put("introduction", user.getIntroduction() != null ? user.getIntroduction() : "无");

        // 游戏名称（通过 game_id 查 game 表）
        String gameName = "未设定";
        if (profile != null && profile.getGameId() != null) {
            Game game = gameRepository.findById(profile.getGameId()).orElse(null);
            if (game != null) {
                gameName = game.getGameName();
            }
        }
        map.put("gameName", gameName);

        // 匹配需求（来自 user_profile.team_requirement）
        String matchNeed = (profile != null && profile.getTeamRequirement() != null) ? profile.getTeamRequirement() : "无";
        map.put("matchNeed", matchNeed);

        return map;
    }
}