package com.dong.springboot.service;

import com.dong.springboot.dao.GameRepository;
import com.dong.springboot.dao.UserProfileRepository;
import com.dong.springboot.entity.Game;
import com.dong.springboot.vo.MatchQueryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private GameRepository gameRepository;

    // 查找或创建游戏（支持“其他”自定义游戏）
    private Game findOrCreateGame(String gameName) {
        Game game = gameRepository.findByGameName(gameName);
        if (game == null) {
            game = new Game();
            game.setGameName(gameName);
            game.setStatus(1); // 默认为启用
            game = gameRepository.save(game);
            log.info("创建新游戏：{}，gameId={}", gameName, game.getGameId());
        }
        return game;
    }
    //时长百分比计算
    /**
     * 计算两个时间段的重叠得分（0-25分）
     * @param frontStart 前端期望开始时间 "HH:mm"
     * @param frontEnd   前端期望结束时间 "HH:mm"
     * @param userTimeSlot 数据库中用户的时间段，格式 "HH:mm~HH:mm"
     * @return 重叠分数（0-25）
     */
    private int calculateTimeOverlapScore(String frontStart, String frontEnd, String userTimeSlot) {
        if (frontStart == null || frontEnd == null || userTimeSlot == null || userTimeSlot.isEmpty()) {
            return 0;
        }
        try {
            // 1. 将时间字符串转换为从00:00开始的分钟数
            int frontStartMin = toMinutes(frontStart);
            int frontEndMin = toMinutes(frontEnd);
            if (frontStartMin >= frontEndMin) {
                // 如果开始时间大于等于结束时间，视为无效（例如跨天情况，可简单处理为不加分）
                return 0;
            }
            int frontDuration = frontEndMin - frontStartMin;
            if (frontDuration <= 0) return 0;

            // 2. 解析用户的时间段
            // 兼容两种分隔符：~ 或 -
            String[] userParts;
            if (userTimeSlot.contains("~")) {
                userParts = userTimeSlot.split("~");
            } else if (userTimeSlot.contains("-")) {
                userParts = userTimeSlot.split("-");
            } else {
                return 0;
            }
            if (userParts.length != 2) return 0;
            int userStartMin = toMinutes(userParts[0].trim());
            int userEndMin = toMinutes(userParts[1].trim());

            // 3. 计算重叠区间
            int overlapStart = Math.max(frontStartMin, userStartMin);
            int overlapEnd = Math.min(frontEndMin, userEndMin);
            int overlapDuration = Math.max(0, overlapEnd - overlapStart);

            // 4. 计算比例和分数
            double ratio = (double) overlapDuration / frontDuration;
            double score = ratio * 25.0;
            return (int) Math.round(score);

        } catch (Exception e) {
            log.warn("计算时间重叠得分失败: {}", e.getMessage());
            return 0;
        }
    }

    // 辅助方法：将 "HH:mm" 转换为分钟数
    private int toMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    //相似度计算
    public List<Map<String, Object>> matchUsers(MatchQueryDTO dto) {
        // 1. 校验游戏名
        if (dto.getGameName() == null || dto.getGameName().isEmpty()) {
            return new ArrayList<>();
        }
        Game game = findOrCreateGame(dto.getGameName());
        if (game == null) {
            return new ArrayList<>();
        }
        Integer gameId = game.getGameId();

        // 2. 获取原始用户列表（只读 Map）
        List<Map<String, Object>> rawUsers = userProfileRepository.matchUsersWithDetails(gameId, dto.getExcludeUserId());

        // 3. 创建新的可变列表，逐个构建可修改的 Map
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> raw : rawUsers) {
            // 创建可修改的 HashMap，复制原始数据
            Map<String, Object> user = new HashMap<>(raw);

            int score = 25; // 基础分

            // 需求匹配
            if (dto.getMatchNeed() != null && !dto.getMatchNeed().isEmpty()) {
                String userNeed = (String) user.get("teamRequirement");
                if (dto.getMatchNeed().equals(userNeed)) {
                    score += 35;
                }
            }

            // 时间段匹配
            if (dto.getPlaytimeStart() != null && dto.getPlaytimeEnd() != null &&
                    !dto.getPlaytimeStart().isEmpty() && !dto.getPlaytimeEnd().isEmpty()) {
                String userTime = (String) user.get("playTime");
                int timeScore = calculateTimeOverlapScore(
                        dto.getPlaytimeStart(),
                        dto.getPlaytimeEnd(),
                        (String) user.get("playTime")
                );
                score += timeScore;
            }

            // 性格匹配
            if (dto.getPersonality() != null && !dto.getPersonality().isEmpty()) {
                String userPersonality = (String) user.get("personality");
                if (dto.getPersonality().equals(userPersonality)) {
                    score += 15;
                }
            }

            user.put("matchScore", score);
            result.add(user);
        }

        // 按匹配度降序排序
        result.sort((a, b) -> {
            int sa = (int) a.get("matchScore");
            int sb = (int) b.get("matchScore");
            return Integer.compare(sb, sa);
        });

        return result;
    }

    /**
     * 判断两个时间段是否有重叠
     * @param start 前端选择的开始时间，比如 "18:00"
     * @param end   前端选择的结束时间，比如 "22:00"
     * @param userTime 数据库中存储的时间，比如 "18:00~22:00"
     * @return 如果有重叠返回 true，否则 false
     */
    private boolean isTimeOverlap(String start, String end, String userTime) {
        if (userTime == null || userTime.isEmpty()) {
            return false;
        }
        String[] parts = userTime.split("~");
        if (parts.length != 2) {
            return false;
        }
        String userStart = parts[0].trim();
        String userEnd = parts[1].trim();

        // 字符串比较 "HH:mm" 可以直接比较大小
        return start.compareTo(userEnd) <= 0 && end.compareTo(userStart) >= 0;
    }
}