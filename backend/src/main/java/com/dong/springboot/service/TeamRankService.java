package com.dong.springboot.service;

import com.dong.springboot.common.cache.RedisCacheTool;
import com.dong.springboot.entity.TeamHeatStat;
import com.dong.springboot.mapper.TeamHeatStatMapper;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamRankVO;
import com.dong.springboot.vo.TeamRankCandidateVO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TeamRankService {

    public static final double VIEW_SCORE = 1D;
    public static final double COMMENT_SCORE = 3D;
    public static final double APPLY_SCORE = 5D;
    public static final double PUBLISH_SCORE = 10D;

    private static final Logger log = LoggerFactory.getLogger(TeamRankService.class);
    private static final String HOT_RANK_KEY = "rank:team:hot";
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int PAGE_SIZE = 20;

    private final RedisCacheTool redisCacheTool;
    private final TeamHeatStatMapper teamHeatStatMapper;
    private final TeamDetailCacheService teamDetailCacheService;
    private final Cache<Integer, List<TeamRankVO>> hotRankCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(Duration.ofSeconds(3))
            .recordStats()
            .build();

    public TeamRankService(RedisCacheTool redisCacheTool,
                           TeamHeatStatMapper teamHeatStatMapper,
                           TeamDetailCacheService teamDetailCacheService) {
        this.redisCacheTool = redisCacheTool;
        this.teamHeatStatMapper = teamHeatStatMapper;
        this.teamDetailCacheService = teamDetailCacheService;
    }

    public void recordView(Integer teamId) {
        teamHeatStatMapper.incrementView(teamId);
        incrementScoreAfterCommit(teamId, VIEW_SCORE);
    }

    public void recordComment(Integer teamId) {
        teamHeatStatMapper.incrementComment(teamId);
        incrementScoreAfterCommit(teamId, COMMENT_SCORE);
    }

    public void recordApply(Integer teamId) {
        teamHeatStatMapper.incrementApply(teamId);
        incrementScoreAfterCommit(teamId, APPLY_SCORE);
    }

    public void initializeTeam(Integer teamId) {
        TeamHeatStat stat = new TeamHeatStat();
        stat.setTeamId(teamId);
        stat.setViewCount(0L);
        stat.setCommentCount(0L);
        stat.setApplyCount(0L);
        stat.setPublishScore((long) PUBLISH_SCORE);
        stat.setTotalScore((long) PUBLISH_SCORE);
        teamHeatStatMapper.insert(stat);
        executeAfterCommit(() -> {
            redisCacheTool.setZSetScore(HOT_RANK_KEY, teamId.toString(), PUBLISH_SCORE);
            hotRankCache.invalidateAll();
        });
    }

    public void removeAfterCommit(Integer teamId) {
        executeAfterCommit(() -> {
            if (teamId != null) {
                redisCacheTool.removeZSetMember(HOT_RANK_KEY, teamId.toString());
            }
            hotRankCache.invalidateAll();
        });
    }

    public List<TeamRankVO> getHotTeams(Integer requestedLimit) {
        int limit = normalizeLimit(requestedLimit);
        return hotRankCache.get(limit, this::queryHotTeamsFromRedis);
    }

    public TeamRankVO getTeamRank(Integer teamId) {
        if (teamId == null) {
            return null;
        }
        TeamDetailVO detail = teamDetailCacheService.getTeamDetail(teamId);
        if (detail == null || !Integer.valueOf(1).equals(detail.getStatus())) {
            removeFromRedis(teamId);
            return null;
        }
        Long zeroBasedRank = redisCacheTool.reverseZSetRank(HOT_RANK_KEY, teamId.toString());
        Double score = redisCacheTool.getZSetScore(HOT_RANK_KEY, teamId.toString());
        return zeroBasedRank == null ? null : new TeamRankVO(zeroBasedRank + 1, defaultScore(score), detail);
    }

    public List<TeamRankCandidateVO> getRankCandidates(long offset, int size) {
        if (offset < 0 || size <= 0) {
            return List.of();
        }
        Set<RedisCacheTool.ScoredValue> page = redisCacheTool.reverseZSetWithScores(
                HOT_RANK_KEY, offset, offset + size - 1);
        List<TeamRankCandidateVO> result = new ArrayList<>(page.size());
        int index = 0;
        for (RedisCacheTool.ScoredValue value : page) {
            Integer teamId = parseTeamId(value.getValue());
            if (teamId != null) {
                result.add(new TeamRankCandidateVO(
                        teamId,
                        offset + index + 1,
                        defaultScore(value.getScore())
                ));
            }
            index++;
        }
        return result;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildRankAfterStartup() {
        try {
            rebuildHotRank();
        } catch (RuntimeException e) {
            log.error("启动时重建热门队伍排行榜失败，定时任务将继续重试", e);
        }
    }

    @Scheduled(fixedDelayString = "${team.rank.reconcile-delay:300000}")
    public void reconcileHotRank() {
        try {
            teamHeatStatMapper.initializeMissingActiveTeams();
            List<TeamHeatStat> activeStats = teamHeatStatMapper.selectActiveTeamStats();
            Set<String> activeTeamIds = new HashSet<>();
            for (TeamHeatStat stat : activeStats) {
                String teamId = stat.getTeamId().toString();
                activeTeamIds.add(teamId);
                redisCacheTool.setZSetScore(HOT_RANK_KEY, teamId, stat.getTotalScore().doubleValue());
            }
            for (String redisTeamId : redisCacheTool.getZSetMembers(HOT_RANK_KEY)) {
                if (!activeTeamIds.contains(redisTeamId)) {
                    redisCacheTool.removeZSetMember(HOT_RANK_KEY, redisTeamId);
                }
            }
            hotRankCache.invalidateAll();
        } catch (RuntimeException e) {
            log.error("校准热门队伍排行榜失败", e);
        }
    }

    public void rebuildHotRank() {
        teamHeatStatMapper.initializeMissingActiveTeams();
        redisCacheTool.delete(HOT_RANK_KEY);
        for (TeamHeatStat stat : teamHeatStatMapper.selectActiveTeamStats()) {
            redisCacheTool.setZSetScore(HOT_RANK_KEY, stat.getTeamId().toString(), stat.getTotalScore().doubleValue());
        }
        hotRankCache.invalidateAll();
    }

    private List<TeamRankVO> queryHotTeamsFromRedis(Integer limit) {
        List<TeamRankVO> result = new ArrayList<>(limit);
        long offset = 0;
        while (result.size() < limit) {
            Set<RedisCacheTool.ScoredValue> page = redisCacheTool.reverseZSetWithScores(
                    HOT_RANK_KEY, offset, offset + PAGE_SIZE - 1);
            if (page.isEmpty()) {
                break;
            }
            int pageIndex = 0;
            for (RedisCacheTool.ScoredValue value : page) {
                Integer teamId = parseTeamId(value.getValue());
                TeamDetailVO detail = teamId == null ? null : teamDetailCacheService.getTeamDetail(teamId);
                if (detail != null && Integer.valueOf(1).equals(detail.getStatus())) {
                    result.add(new TeamRankVO((long) (offset + pageIndex + 1), defaultScore(value.getScore()), detail));
                    if (result.size() >= limit) {
                        break;
                    }
                }
                pageIndex++;
            }
            if (page.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }
        return List.copyOf(result);
    }

    private void incrementScoreAfterCommit(Integer teamId, double delta) {
        executeAfterCommit(() -> redisCacheTool.incrementZSetScore(
                HOT_RANK_KEY, teamId.toString(), delta));
    }

    private void removeFromRedis(Integer teamId) {
        if (teamId != null) {
            redisCacheTool.removeZSetMember(HOT_RANK_KEY, teamId.toString());
        }
    }

    private void executeAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private int normalizeLimit(Integer limit) {
        return limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    }

    private Integer parseTeamId(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            redisCacheTool.removeZSetMember(HOT_RANK_KEY, value);
            return null;
        }
    }

    private double defaultScore(Double score) {
        return score == null ? 0D : score;
    }
}
