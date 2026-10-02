package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserMapper extends BaseMapper<User> {

    default User findByUsername(String username) {
        return selectList(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username))
                .stream()
                .findFirst()
                .orElse(null);
    }

    default List<User> findAllByOrderByCreateTimeDesc() {
        return selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
    }

    default List<User> findByUsernameContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAllByOrderByCreateTimeDesc();
        }
        return selectList(new LambdaQueryWrapper<User>().like(User::getUsername, keyword));
    }

    default long count() {
        return selectCount(null);
    }

    default long countByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, status));
    }

    default void save(User user) {
        if (user.getUserId() == null) {
            insert(user);
        } else {
            updateById(user);
        }
    }

    default Optional<User> findById(Integer id) {
        return Optional.ofNullable(selectById(id));
    }
}
