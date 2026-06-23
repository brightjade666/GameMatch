package com.dong.springboot.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dong.springboot.dao.GameRepository;
import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.dao.UserRepository;
import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.vo.UserInfoVO;

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

        // ========== 1. 处理头像 ==========
        String avatar = vo.getAvatar();
        if (avatar != null && avatar.startsWith("data:image")) {
            avatar = null;
        }
        user.setAvatar(avatar);

        // ========== 2. 保存基础信息 ==========
        user.setGender(vo.getGender());
        user.setAge(vo.getAge());
        user.setContact(vo.getContact());
        user.setIntroduction(vo.getIntroduction());
        user.setGameRank(vo.getGame_rank());  // ✅ 添加：保存段位到 user 表

        // ========== 3. 处理游戏 ==========
        String gameName = vo.getGame_name();
        Integer gameId = null;

        if (gameName != null && !gameName.isBlank()) {
            Game game = gameRepository.findByGameName(gameName);
            if (game == null) {
                game = new Game();
                game.setGameName(gameName);
                game = gameRepository.save(game);
            }
            gameId = game.getGameId();
            user.setGameId(gameId);
        }

        userRepository.save(user);

        // ========== 4. 保存到 user_profile ==========
        UserProfile profile = userProfileRepository.findByUserId(vo.getUser_id());
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(vo.getUser_id());
            profile.setCreatedAt(LocalDateTime.now());  // ✅ 添加创建时间
        }

        if (gameId != null) {
            profile.setGameId(gameId);
        }

        // ✅ 关键修复：设置 game_rank
        if (vo.getGame_rank() != null && !vo.getGame_rank().isBlank()) {
            profile.setGameRank(vo.getGame_rank());
        } else {
            profile.setGameRank("未知");  // 或者设置默认值
        }

        profile.setPersonality(vo.getPersonality());
        profile.setPlayTime(vo.getPlaytime());
        profile.setTeamRequirement(vo.getMatchneed());
        profile.setUpdatedAt(LocalDateTime.now());  // ✅ 添加更新时间

        userProfileRepository.save(profile);
    }

    // ===================== 新增方法 =====================
    // 这些方法供 AdminController 使用

    public User findById(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAllByOrderByCreateTimeDesc();
    }

    public List<User> searchByUsername(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    public void update(User user) {
        // 保持创建时间不变，只更新修改时间
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    public long count() {
        return userRepository.count();
    }

    public long countByStatus(Integer status) {
        return userRepository.countByStatus(status);
    }

    //获取用户的游戏档案
    public UserProfile getUserProfile(Integer userId) {
        return userProfileRepository.findByUserId(userId);
    }
}