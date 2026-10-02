package com.dong.springboot.service;

import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.mapper.GameMapper;
import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.mapper.UserProfileMapper;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserInfoService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final GameMapper gameMapper;

    public UserInfoService(UserMapper userMapper,
                           UserProfileMapper userProfileMapper,
                           GameMapper gameMapper) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.gameMapper = gameMapper;
    }

    public UserInfoVO getUserInfo(Integer userId) {
        return userProfileMapper.selectUserInfoById(userId);
    }

    @Transactional
    public void updateUserInfo(UserInfoVO vo) {
        User user = userMapper.findById(vo.getUser_id()).orElse(null);
        if (user == null) {
            return;
        }

        String avatar = vo.getAvatar();
        if (avatar != null && avatar.startsWith("data:image")) {
            avatar = null;
        }
        user.setAvatar(avatar);
        user.setGender(vo.getGender());
        user.setAge(vo.getAge());
        user.setContact(vo.getContact());
        user.setIntroduction(vo.getIntroduction());
        user.setGameRank(vo.getGame_rank());

        Integer gameId = resolveGameId(vo.getGame_name());
        if (gameId != null) {
            user.setGameId(gameId);
        }
        userMapper.save(user);

        UserProfile profile = userProfileMapper.findByUserId(vo.getUser_id());
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(vo.getUser_id());
            profile.setCreatedAt(LocalDateTime.now());
        }
        if (gameId != null) {
            profile.setGameId(gameId);
        }
        profile.setGameRank(vo.getGame_rank() == null || vo.getGame_rank().isBlank()
                ? "未知"
                : vo.getGame_rank());
        profile.setPersonality(vo.getPersonality());
        profile.setPlayTime(vo.getPlaytime());
        profile.setTeamRequirement(vo.getMatchneed());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.save(profile);
    }

    private Integer resolveGameId(String gameName) {
        if (gameName == null || gameName.isBlank()) {
            return null;
        }
        Game game = gameMapper.findByGameName(gameName);
        if (game == null) {
            game = new Game();
            game.setGameName(gameName);
            game = gameMapper.save(game);
        }
        return game.getGameId();
    }

    public User findById(Integer userId) {
        return userMapper.findById(userId).orElse(null);
    }

    public List<User> findAll() {
        return userMapper.findAllByOrderByCreateTimeDesc();
    }

    public List<User> searchByUsername(String keyword) {
        return userMapper.findByUsernameContaining(keyword);
    }

    public void delete(Integer userId) {
        userMapper.deleteById(userId);
    }

    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.save(user);
    }

    public long count() {
        return userMapper.count();
    }

    public long countByStatus(Integer status) {
        return userMapper.countByStatus(status);
    }

    public UserProfile getUserProfile(Integer userId) {
        return userProfileMapper.findByUserId(userId);
    }
}
