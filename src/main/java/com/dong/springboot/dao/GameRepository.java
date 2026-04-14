package com.dong.springboot.dao;

import com.dong.springboot.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findByGameName(String gameName); // 根据游戏名查游戏
}