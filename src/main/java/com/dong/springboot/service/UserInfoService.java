package com.dong.springboot.service;

import com.dong.springboot.entity.TbUser;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserInfoService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    // 保存用户信息（适配你的UserProfile字段）
    @Transactional // 事务保证数据一致性
    public boolean saveUserInfo(Integer userId, TbUser user, UserProfile profile) {
        try {
            // 1. 校验用户是否存在
            Optional<TbUser> existUserOpt = userRepository.findById(userId);
            if (existUserOpt.isEmpty()) {
                return false;
            }

            // 2. 更新用户基本信息
            TbUser existUser = existUserOpt.get();
            existUser.setUsername(user.getUsername());
            existUser.setGender(user.getGender());
            existUser.setAge(user.getAge());
            existUser.setAvatar(user.getAvatar());
            existUser.setContact(user.getContact());
            existUser.setIntroduction(user.getIntroduction());
            userRepository.save(existUser);

            // 3. 更新/新增用户档案（完全匹配你的UserProfile字段）
            Optional<UserProfile> existProfileOpt = profileRepository.findByUserId(userId);
            if (existProfileOpt.isPresent()) {
                UserProfile existProfile = existProfileOpt.get();
                // 替换所有错误的字段调用，匹配你的实体类
                existProfile.setGameId(profile.getGameId());
                existProfile.setGameRank(profile.getGameRank());
                existProfile.setMainPosition(profile.getMainPosition()); // 新增：主打位置
                existProfile.setPlayTime(profile.getPlayTime()); // 游玩时间段
                existProfile.setPlayStyle(profile.getPlayStyle()); // 性格（替换原personality）
                existProfile.setPreferredMode(profile.getPreferredMode()); // 偏好模式
                existProfile.setFavoriteHeroes(profile.getFavoriteHeroes()); // 擅长英雄
                existProfile.setWinRate(profile.getWinRate()); // 胜率
                existProfile.setTotalMatches(profile.getTotalMatches()); // 总场次
                existProfile.setTeamRequirement(profile.getTeamRequirement()); // 匹配需求
                profileRepository.save(existProfile);
            } else {
                profile.setUserId(userId);
                // 补充必填字段（你的UserProfile中gender/age/contact/game_id是非空的）
                profile.setGender(profile.getGender());
                profile.setAge(profile.getAge());
                profile.setContact(profile.getContact());
                profileRepository.save(profile);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 查询用户信息
    public TbUser getUserInfo(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }
}