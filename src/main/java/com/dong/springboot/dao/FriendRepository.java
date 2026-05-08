package com.dong.springboot.dao;

import com.dong.springboot.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 我发出/收到的好友关系（status=1）
    List<Friend> findByUserId(Integer userId);
    List<Friend> findByFriendId(Integer friendId);

    // 根据双方ID查记录（任何状态）
    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)")
    Friend findFriendship(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 查找所有申请给我的记录（friendId=userId 且 status=0）
    List<Friend> findByFriendIdAndStatus(Integer friendId, Integer status);

    // 查找我发出的申请（userId=userId 且 status=0）
    List<Friend> findByUserIdAndStatus(Integer userId, Integer status);

    // 根据ID查申请记录
    Friend findById(Integer id);   // 注意 JPA 返回 Optional，这里为了方便直接 Friend
}