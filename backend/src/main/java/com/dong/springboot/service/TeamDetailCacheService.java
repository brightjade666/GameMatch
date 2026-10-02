package com.dong.springboot.service;

import com.dong.springboot.common.cache.RedisCacheTool;
import com.dong.springboot.mapper.TeamRecruitMapper;
import com.dong.springboot.vo.TeamDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class TeamDetailCacheService {

    private static final String DETAIL_KEY_PREFIX = "team:detail:";
    private static final String NULL_KEY_PREFIX = "team:detail:null:";
    private static final String LOCK_KEY_PREFIX = "team:detail:lock:";
    private static final Duration LOGICAL_TTL = Duration.ofMinutes(30);
    private static final Duration PHYSICAL_TTL = Duration.ofHours(24);
    private static final Duration NULL_TTL = Duration.ofMinutes(3);
    private static final int NULL_TTL_JITTER_MINUTES = 2;
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final int MAX_RETRY = 10;

    private final TeamRecruitMapper teamRecruitMapper;
    private final TeamBloomFilterService teamBloomFilterService;
    private final RedisCacheTool redisCacheTool;

    public TeamDetailCacheService(TeamRecruitMapper teamRecruitMapper,
                                  TeamBloomFilterService teamBloomFilterService,
                                  RedisCacheTool redisCacheTool) {
        this.teamRecruitMapper = teamRecruitMapper;
        this.teamBloomFilterService = teamBloomFilterService;
        this.redisCacheTool = redisCacheTool;
    }

    public TeamDetailVO getTeamDetail(Integer teamId) {
        if (!teamBloomFilterService.mightContain(teamId)) {
            return null;
        }

        String cacheKey = buildDetailKey(teamId);
        String nullKey = buildNullKey(teamId);
        String lockKey = buildLockKey(teamId);

        for (int i = 0; i < MAX_RETRY; i++) {
            RedisCacheTool.CacheRecord<TeamDetailVO> cached = redisCacheTool.readLogicalExpire(cacheKey, TeamDetailVO.class);
            if (cached != null) {
                if (!cached.isExpired()) {
                    return cached.getData();
                }
                if (redisCacheTool.tryLock(lockKey, LOCK_TTL)) {
                    rebuildAsync(teamId, cacheKey, lockKey);
                }
                return cached.getData();
            }

            if (redisCacheTool.hasKey(nullKey)) {
                return null;
            }

            if (redisCacheTool.tryLock(lockKey, LOCK_TTL)) {
                try {
                    cached = redisCacheTool.readLogicalExpire(cacheKey, TeamDetailVO.class);
                    if (cached != null && !cached.isExpired()) {
                        return cached.getData();
                    }
                    if (redisCacheTool.hasKey(nullKey)) {
                        return null;
                    }

                    TeamDetailVO detail = teamRecruitMapper.selectTeamDetailById(teamId);
                    if (detail == null || detail.getId() == null) {
                        redisCacheTool.setNullWithJitter(nullKey, NULL_TTL, NULL_TTL_JITTER_MINUTES);
                        return null;
                    }

                    redisCacheTool.writeLogicalExpire(cacheKey, detail, LOGICAL_TTL, PHYSICAL_TTL, 10);
                    return detail;
                } finally {
                    redisCacheTool.unlock(lockKey);
                }
            }

            sleepQuietly(50L);
        }

        return teamRecruitMapper.selectTeamDetailById(teamId);
    }

    public void deleteTeamDetailCache(Integer teamId) {
        if (teamId == null) {
            return;
        }
        redisCacheTool.delete(buildDetailKey(teamId));
        redisCacheTool.delete(buildNullKey(teamId));
    }

    public void deleteTeamDetailCacheAfterCommit(Integer teamId) {
        if (teamId == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            deleteTeamDetailCache(teamId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteTeamDetailCache(teamId);
            }
        });
    }

    private void rebuildAsync(Integer teamId, String cacheKey, String lockKey) {
        CompletableFuture.runAsync(() -> {
            try {
                TeamDetailVO detail = teamRecruitMapper.selectTeamDetailById(teamId);
                if (detail == null || detail.getId() == null) {
                    redisCacheTool.setNullWithJitter(buildNullKey(teamId), NULL_TTL, NULL_TTL_JITTER_MINUTES);
                    return;
                }
                redisCacheTool.writeLogicalExpire(cacheKey, detail, LOGICAL_TTL, PHYSICAL_TTL, 10);
            } finally {
                redisCacheTool.unlock(lockKey);
            }
        });
    }

    private String buildDetailKey(Integer teamId) {
        return DETAIL_KEY_PREFIX + teamId;
    }

    private String buildNullKey(Integer teamId) {
        return NULL_KEY_PREFIX + teamId;
    }

    private String buildLockKey(Integer teamId) {
        return LOCK_KEY_PREFIX + teamId;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
