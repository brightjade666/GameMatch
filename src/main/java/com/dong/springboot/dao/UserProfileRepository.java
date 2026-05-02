package com.dong.springboot.dao;

import com.dong.springboot.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    UserProfile findByUserId(Integer userId);
    List<UserProfile> findByGameId(Integer gameId);

    // 🔥 直接返回 Map！永远不会报错！
    @Query(value = "SELECT " +
            "u.user_id AS userId, " +
            "u.username, " +
            "u.avatar, " +
            "u.gender, " +
            "u.age, " +
            "g.game_name AS gameName, " +
            "up.game_rank AS gameRank, " +
            "up.play_time AS playTime, " +
            "up.personality, " +
            "up.team_requirement AS teamRequirement, " +
            "up.win_rate AS winRate, " +
            "90 AS matchScore " +
            "FROM user_profile up " +
            "JOIN user u ON up.user_id = u.user_id " +
            "JOIN game g ON up.game_id = g.game_id " +
            "WHERE up.game_id = :gameId " +
            "AND u.user_id != :excludeUserId " +
            "AND u.status = 1",
            nativeQuery = true)
    List<Map<String, Object>> matchUsers(
            @Param("gameId") Integer gameId,
            @Param("playtimeStart") String playtimeStart,
            @Param("playtimeEnd") String playtimeEnd,
            @Param("matchNeed") String matchNeed,
            @Param("personality") String personality,
            @Param("excludeUserId") Integer excludeUserId
    );
}