package com.dong.springboot.service;

import com.dong.springboot.dao.GameRepository;
import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.entity.Game;
import com.dong.springboot.vo.MatchQueryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private GameRepository gameRepository;

    public List<Map<String, Object>> matchUsers(MatchQueryDTO dto) {
        Integer gameId = null;

        log.info("前端传的参数：gameName={}, excludeUserId={}", dto.getGameName(), dto.getExcludeUserId());

        if (dto.getGameName() != null && !dto.getGameName().isEmpty()) {
            Game game = gameRepository.findByGameName(dto.getGameName());
            if (game != null) {
                gameId = game.getGameId();
                log.info("根据游戏名{}查到gameId={}", dto.getGameName(), gameId);
            }
        }

        // ✅ 修改这里：只传两个参数
        List<Map<String, Object>> result = userProfileRepository.matchUsers(
                gameId,
                dto.getExcludeUserId()
        );

        log.info("✅ 匹配到队友数量：" + result.size());
        return result;
    }
}