package com.dong.springboot.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RedisCacheTool {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheTool(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> CacheRecord<T> readLogicalExpire(String key, Class<T> type) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            CacheRecord raw = objectMapper.readValue(json, CacheRecord.class);
            T data = objectMapper.convertValue(raw.getData(), type);
            return new CacheRecord<>(data, raw.getExpireAt());
        } catch (JsonProcessingException e) {
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    public void writeLogicalExpire(String key, Object data, Duration logicalTtl, Duration physicalTtl, int jitterMinutes) {
        CacheRecord<Object> record = new CacheRecord<>();
        record.setData(data);
        record.setExpireAt(System.currentTimeMillis() + logicalTtl.toMillis());
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(record), randomizeTtl(physicalTtl, jitterMinutes));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("缓存写入失败", e);
        }
    }

    public boolean tryLock(String lockKey, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, token, ttl);
        if (Boolean.TRUE.equals(locked)) {
            stringRedisTemplate.opsForValue().set(lockKey + ":token", token, ttl);
            return true;
        }
        return false;
    }

    public void unlock(String lockKey) {
        String token = stringRedisTemplate.opsForValue().get(lockKey + ":token");
        if (!StringUtils.hasText(token)) {
            return;
        }
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), token);
        stringRedisTemplate.delete(lockKey + ":token");
    }

    public void setNullWithJitter(String key, Duration ttl, int jitterMinutes) {
        stringRedisTemplate.opsForValue().set(key, "1", randomizeTtl(ttl, jitterMinutes));
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public void incrementZSetScore(String key, String member, double score) {
        stringRedisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    public void addZSetMemberIfAbsent(String key, String member, double score) {
        stringRedisTemplate.opsForZSet().addIfAbsent(key, member, score);
    }

    public void setZSetScore(String key, String member, double score) {
        stringRedisTemplate.opsForZSet().add(key, member, score);
    }

    public Set<ScoredValue> reverseZSetWithScores(String key) {
        return reverseZSetWithScores(key, 0, -1);
    }

    public Set<ScoredValue> reverseZSetWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, start, end);
        Set<ScoredValue> result = new LinkedHashSet<>();
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                result.add(new ScoredValue(tuple.getValue(), tuple.getScore()));
            }
        }
        return result;
    }

    public Set<String> getZSetMembers(String key) {
        Set<String> members = stringRedisTemplate.opsForZSet().range(key, 0, -1);
        return members == null ? Collections.emptySet() : members;
    }

    public Long reverseZSetRank(String key, String member) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, member);
    }

    public Double getZSetScore(String key, String member) {
        return stringRedisTemplate.opsForZSet().score(key, member);
    }

    public void removeZSetMember(String key, String member) {
        stringRedisTemplate.opsForZSet().remove(key, member);
    }

    private Duration randomizeTtl(Duration baseTtl, int jitterMinutes) {
        int extra = jitterMinutes <= 0 ? 0 : ThreadLocalRandom.current().nextInt(jitterMinutes + 1);
        return baseTtl.plusMinutes(extra);
    }

    public static class CacheRecord<T> {
        private T data;
        private Long expireAt;

        public CacheRecord() {
        }

        public CacheRecord(T data, Long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Long getExpireAt() {
            return expireAt;
        }

        public void setExpireAt(Long expireAt) {
            this.expireAt = expireAt;
        }

        public boolean isExpired() {
            return expireAt != null && expireAt <= System.currentTimeMillis();
        }
    }

    public static class ScoredValue {
        private final String value;
        private final Double score;

        public ScoredValue(String value, Double score) {
            this.value = value;
            this.score = score;
        }

        public String getValue() { return value; }
        public Double getScore() { return score; }
    }
}
