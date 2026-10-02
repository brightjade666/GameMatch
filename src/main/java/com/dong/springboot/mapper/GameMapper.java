package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.Game;

import java.util.Optional;

public interface GameMapper extends BaseMapper<Game> {

    default Game findByGameName(String gameName) {
        return selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getGameName, gameName))
                .stream()
                .findFirst()
                .orElse(null);
    }

    default Game save(Game game) {
        if (game.getGameId() == null) {
            insert(game);
        } else {
            updateById(game);
        }
        return game;
    }

    default Optional<Game> findById(Integer id) {
        return Optional.ofNullable(selectById(id));
    }
}
