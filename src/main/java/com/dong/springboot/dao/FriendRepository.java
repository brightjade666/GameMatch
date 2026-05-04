package com.dong.springboot.dao;

import com.dong.springboot.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 我是用户本人
    List<Friend> findByUserId(Integer userId);

    // 我是对方的好友
    List<Friend> findByFriendId(Integer friendId);
}