package com.dong.springboot.service;

import com.dong.springboot.mapper.GameMapper;
import com.dong.springboot.mapper.UserProfileMapper;
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
    private UserProfileMapper UserProfileMapper;

    @Autowired
    private GameMapper GameMapper;

    // 鏌ユ壘鎴栧垱寤烘父鎴忥紙鏀寔鈥滃叾浠栤€濊嚜瀹氫箟娓告垙锛?
    private Game findOrCreateGame(String gameName) {
        Game game = GameMapper.findByGameName(gameName);
        if (game == null) {
            game = new Game();
            game.setGameName(gameName);
            game.setStatus(1); // 榛樿涓哄惎鐢?
            game = GameMapper.save(game);
            log.info("鍒涘缓鏂版父鎴忥細{}锛実ameId={}", gameName, game.getGameId());
        }
        return game;
    }
    //鏃堕暱鐧惧垎姣旇绠?
    /**
     * 璁＄畻涓や釜鏃堕棿娈电殑閲嶅彔寰楀垎锛?-25鍒嗭級
     * @param frontStart 鍓嶇鏈熸湜寮€濮嬫椂闂?"HH:mm"
     * @param frontEnd   鍓嶇鏈熸湜缁撴潫鏃堕棿 "HH:mm"
     * @param userTimeSlot 鏁版嵁搴撲腑鐢ㄦ埛鐨勬椂闂存锛屾牸寮?"HH:mm~HH:mm"
     * @return 閲嶅彔鍒嗘暟锛?-25锛?
     */
    private int calculateTimeOverlapScore(String frontStart, String frontEnd, String userTimeSlot) {
        if (frontStart == null || frontEnd == null || userTimeSlot == null || userTimeSlot.isEmpty()) {
            return 0;
        }
        try {
            // 1. 灏嗘椂闂村瓧绗︿覆杞崲涓轰粠00:00寮€濮嬬殑鍒嗛挓鏁?
            int frontStartMin = toMinutes(frontStart);
            int frontEndMin = toMinutes(frontEnd);
            if (frontStartMin >= frontEndMin) {
                // 濡傛灉寮€濮嬫椂闂村ぇ浜庣瓑浜庣粨鏉熸椂闂达紝瑙嗕负鏃犳晥锛堜緥濡傝法澶╂儏鍐碉紝鍙畝鍗曞鐞嗕负涓嶅姞鍒嗭級
                return 0;
            }
            int frontDuration = frontEndMin - frontStartMin;
            if (frontDuration <= 0) return 0;

            // 2. 瑙ｆ瀽鐢ㄦ埛鐨勬椂闂存
            // 鍏煎涓ょ鍒嗛殧绗︼細~ 鎴?-
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

            // 3. 璁＄畻閲嶅彔鍖洪棿
            int overlapStart = Math.max(frontStartMin, userStartMin);
            int overlapEnd = Math.min(frontEndMin, userEndMin);
            int overlapDuration = Math.max(0, overlapEnd - overlapStart);

            // 4. 璁＄畻姣斾緥鍜屽垎鏁?
            double ratio = (double) overlapDuration / frontDuration;
            double score = ratio * 25.0;
            return (int) Math.round(score);

        } catch (Exception e) {
            log.warn("璁＄畻鏃堕棿閲嶅彔寰楀垎澶辫触: {}", e.getMessage());
            return 0;
        }
    }

    // 杈呭姪鏂规硶锛氬皢 "HH:mm" 杞崲涓哄垎閽熸暟
    private int toMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    //鐩镐技搴﹁绠?
    public List<Map<String, Object>> matchUsers(MatchQueryDTO dto) {
        // 1. 鏍￠獙娓告垙鍚?
        if (dto.getGameName() == null || dto.getGameName().isEmpty()) {
            return new ArrayList<>();
        }
        Game game = findOrCreateGame(dto.getGameName());
        if (game == null) {
            return new ArrayList<>();
        }
        Integer gameId = game.getGameId();

        // 2. 鑾峰彇鍘熷鐢ㄦ埛鍒楄〃锛堝彧璇?Map锛?
        List<Map<String, Object>> rawUsers = UserProfileMapper.matchUsersWithDetails(gameId, dto.getExcludeUserId());

        // 3. 鍒涘缓鏂扮殑鍙彉鍒楄〃锛岄€愪釜鏋勫缓鍙慨鏀圭殑 Map
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> raw : rawUsers) {
            // 鍒涘缓鍙慨鏀圭殑 HashMap锛屽鍒跺師濮嬫暟鎹?
            Map<String, Object> user = new HashMap<>(raw);

            int score = 25; // 鍩虹鍒?

            // 闇€姹傚尮閰?
            if (dto.getMatchNeed() != null && !dto.getMatchNeed().isEmpty()) {
                String userNeed = (String) user.get("teamRequirement");
                if (dto.getMatchNeed().equals(userNeed)) {
                    score += 35;
                }
            }

            // 鏃堕棿娈靛尮閰?
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

            // 鎬ф牸鍖归厤
            if (dto.getPersonality() != null && !dto.getPersonality().isEmpty()) {
                String userPersonality = (String) user.get("personality");
                if (dto.getPersonality().equals(userPersonality)) {
                    score += 15;
                }
            }

            user.put("matchScore", score);
            result.add(user);
        }

        // 鎸夊尮閰嶅害闄嶅簭鎺掑簭
        result.sort((a, b) -> {
            int sa = (int) a.get("matchScore");
            int sb = (int) b.get("matchScore");
            return Integer.compare(sb, sa);
        });

        return result;
    }

    /**
     * 鍒ゆ柇涓や釜鏃堕棿娈垫槸鍚︽湁閲嶅彔
     * @param start 鍓嶇閫夋嫨鐨勫紑濮嬫椂闂达紝姣斿 "18:00"
     * @param end   鍓嶇閫夋嫨鐨勭粨鏉熸椂闂达紝姣斿 "22:00"
     * @param userTime 鏁版嵁搴撲腑瀛樺偍鐨勬椂闂达紝姣斿 "18:00~22:00"
     * @return 濡傛灉鏈夐噸鍙犺繑鍥?true锛屽惁鍒?false
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

        // 瀛楃涓叉瘮杈?"HH:mm" 鍙互鐩存帴姣旇緝澶у皬
        return start.compareTo(userEnd) <= 0 && end.compareTo(userStart) >= 0;
    }
}