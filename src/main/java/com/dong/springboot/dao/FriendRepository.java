package com.dong.springboot.dao;

import com.dong.springboot.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUserId(Integer userId);
    List<Friend> findByFriendId(Integer friendId);

    // ==================== 已修复：返回 List 而不是单个 Friend ====================
    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)")
    List<Friend> findFriendship(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    List<Friend> findByFriendIdAndStatus(Integer friendId, Integer status);
    List<Friend> findByUserIdAndStatus(Integer userId, Integer status);

    // 修复 JPA 规范问题
    default Friend findById(Integer id) {
        return findById(Long.valueOf(id)).orElse(null);
    }

    // ===================== 新增方法：级联删除时使用 =====================

    // 删除某个用户作为 user_id 的所有好友记录
    void deleteByUserId(Integer userId);

    // 删除某个用户作为 friend_id 的所有好友记录
    void deleteByFriendId(Integer friendId);
}