package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.Friend;
import com.dong.springboot.vo.ChatSessionVO;
import com.dong.springboot.vo.FriendVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FriendMapper extends BaseMapper<Friend> {

    default List<Friend> findByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId));
    }

    default List<Friend> findByFriendId(Integer friendId) {
        return selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getFriendId, friendId));
    }

    @Select("SELECT * FROM friend WHERE "
            + "(user_id = #{userId1} AND friend_id = #{userId2}) "
            + "OR (user_id = #{userId2} AND friend_id = #{userId1})")
    List<Friend> findFriendship(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    default List<Friend> findByFriendIdAndStatus(Integer friendId, Integer status) {
        return selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getFriendId, friendId)
                .eq(Friend::getStatus, status));
    }

    default List<Friend> findByUserIdAndStatus(Integer userId, Integer status) {
        return selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getStatus, status));
    }

    List<FriendVO> selectFriendList(@Param("userId") Integer userId);

    List<ChatSessionVO> selectPrivateChatSessions(@Param("userId") Integer userId);

    default Friend findById(Integer id) {
        return selectById(id);
    }

    default void deleteByUserId(Integer userId) {
        delete(new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId));
    }

    default void deleteByFriendId(Integer friendId) {
        delete(new LambdaQueryWrapper<Friend>().eq(Friend::getFriendId, friendId));
    }

    default Friend save(Friend friend) {
        if (friend.getId() == null) {
            insert(friend);
        } else {
            updateById(friend);
        }
        return friend;
    }

    default void delete(Friend friend) {
        if (friend != null && friend.getId() != null) {
            deleteById(friend.getId());
        }
    }

    default void deleteAll(List<Friend> friends) {
        if (friends == null) {
            return;
        }
        for (Friend friend : friends) {
            delete(friend);
        }
    }
}
