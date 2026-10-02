package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.vo.UserInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserProfileMapper extends BaseMapper<UserProfile> {

    default UserProfile findByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId))
                .stream()
                .findFirst()
                .orElse(null);
    }

    default List<UserProfile> findByGameId(Integer gameId) {
        return selectList(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getGameId, gameId));
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId));
    }

    default UserProfile save(UserProfile profile) {
        if (profile.getProfileId() == null) {
            insert(profile);
        } else {
            updateById(profile);
        }
        return profile;
    }

    default Optional<UserProfile> findById(Integer id) {
        return Optional.ofNullable(selectById(id));
    }

    UserInfoVO selectUserInfoById(@Param("userId") Integer userId);

    List<Map<String, Object>> matchUsersWithDetails(@Param("gameId") Integer gameId,
                                                    @Param("excludeUserId") Integer excludeUserId);
}
