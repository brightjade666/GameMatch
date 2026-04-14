package com.dong.springboot.service;

import com.dong.springboot.dao.GameRepository;
import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final GameRepository gameRepository;

    public UserInfoService(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.gameRepository = gameRepository;
    }

    public UserInfoVO getUserInfo(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        UserProfile profile = userProfileRepository.findByUserId(userId);

        Game game = null;
        if (user.getGameId() != null) {
            game = gameRepository.findById(user.getGameId()).orElse(null);
        }

        UserInfoVO vo = new UserInfoVO();

        vo.setUser_id(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNick(user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setAge(user.getAge());
        vo.setContact(user.getContact());
        vo.setIntroduction(user.getIntroduction());

        vo.setGame_name(game == null ? "" : game.getGameName());
        vo.setGame_rank(user.getGameRank());

        if (profile != null) {
            vo.setPersonality(profile.getPersonality());
            vo.setPlaytime(profile.getPlayTime());
            vo.setMatchneed(profile.getTeamRequirement());
        }

        vo.setCreate_time(user.getCreateTime().toString());
        vo.setUpdate_time(user.getUpdateTime().toString());

        return vo;
    }
    @Transactional
    public void updateUserInfo(UserInfoVO vo) {
        User user = userRepository.findById(vo.getUser_id()).orElse(null);
        if (user == null) return;

        // ========== 头像只存路径 ==========
        String avatar = vo.getAvatar();
        if (avatar != null && avatar.startsWith("data:image")) {
            avatar = null;
        }
        user.setAvatar(vo.getAvatar());
        user.setGender(vo.getGender());
        user.setAge(vo.getAge());
        user.setContact(vo.getContact());
        user.setIntroduction(vo.getIntroduction());
        userRepository.save(user);

        UserProfile profile = userProfileRepository.findByUserId(vo.getUser_id());
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(vo.getUser_id());
        }

        // ======================
        // 【必须加这一行！】
        // ======================
        profile.setGameId(user.getGameId());  // <-- 加这行！

        profile.setPersonality(vo.getPersonality());
        profile.setPlayTime(vo.getPlaytime());
        profile.setTeamRequirement(vo.getMatchneed());
        userProfileRepository.save(profile);
    }
}