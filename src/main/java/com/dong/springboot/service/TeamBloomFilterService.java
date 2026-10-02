package com.dong.springboot.service;

import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.mapper.TeamRecruitMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TeamBloomFilterService {

    private static final String BLOOM_KEY = "team:bloom:detail";
    private static final int BIT_SIZE = 1 << 20;
    private static final int[] SEEDS = {5, 7, 11, 13, 31};

    private final StringRedisTemplate stringRedisTemplate;
    private final TeamRecruitMapper teamRecruitMapper;

    public TeamBloomFilterService(StringRedisTemplate stringRedisTemplate, TeamRecruitMapper teamRecruitMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.teamRecruitMapper = teamRecruitMapper;
    }

    //初始化布隆过滤器
    @PostConstruct
    public void init() {
        List<TeamRecruit> teams = teamRecruitMapper.findAll();
        for (TeamRecruit team : teams) {
            put(team.getTeamId());
        }
    }
    // 用 5 个 seed 算出 5 个 bit 位，全部设成 1
    public void put(Integer teamId) {
        if (teamId == null) {
            return;
        }
        for (int seed : SEEDS) {
            long offset = hash(teamId.toString(), seed);
            stringRedisTemplate.opsForValue().setBit(BLOOM_KEY, offset, true);
        }
    }

    // 用 5 个 seed 算出 5 个 bit 位，全部为 1 的时候，才表示 teamId 存在
    public boolean mightContain(Integer teamId) {
        if (teamId == null) {
            return false;
        }
        for (int seed : SEEDS) {
            long offset = hash(teamId.toString(), seed);
            Boolean bit = stringRedisTemplate.opsForValue().getBit(BLOOM_KEY, offset);
            if (!Boolean.TRUE.equals(bit)) {
                return false;
            }
        }
        return true;
    }

    // 计算 hash 值
    private long hash(String value, int seed) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        long result = 0L;
        for (byte b : bytes) {
            result = result * seed + (b & 0xff);
        }
        return (result & Long.MAX_VALUE) % BIT_SIZE;
    }
}
