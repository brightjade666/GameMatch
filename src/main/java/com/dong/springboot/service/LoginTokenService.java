package com.dong.springboot.service;

import com.dong.springboot.config.JwtProperties;
import com.dong.springboot.dto.LoginUserCacheDTO;
import com.dong.springboot.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class LoginTokenService {

    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";
    private static final String VERSION_PREFIX = "auth:version:";
    private static final String REFRESH_JTI_PREFIX = "auth:refresh:jti:";
    private static final String USER_REFRESH_PREFIX = "auth:refresh:user:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public LoginTokenService(StringRedisTemplate stringRedisTemplate, JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair issueTokenPair(User user) {
        long version = currentVersion(user.getUserId());
        String accessToken = buildAccessToken(user, version);
        String refreshToken = buildRefreshToken(user, version);
        saveRefreshToken(user.getUserId(), refreshToken);
        return new TokenPair(accessToken, refreshToken, jwtProperties.getAccessTtl(), jwtProperties.getRefreshTtl());
    }

    public LoginUserCacheDTO getLoginUser(String token) {
        Claims claims = parseAndValidate(token, ACCESS_TYPE);
        if (claims == null) {
            return null;
        }
        Integer userId = extractUserId(claims);
        if (userId == null || !isVersionValid(userId, claims.get("ver", Number.class))) {
            return null;
        }
        return toCache(claims);
    }

    public TokenPair refreshToken(String refreshToken) {
        Claims claims = parseAndValidate(refreshToken, REFRESH_TYPE);
        if (claims == null) {
            return null;
        }

        Integer userId = extractUserId(claims);
        String jti = claims.getId();
        if (userId == null || !isVersionValid(userId, claims.get("ver", Number.class))) {
            return null;
        }
        String currentJti = stringRedisTemplate.opsForValue().get(USER_REFRESH_PREFIX + userId);
        if (!StringUtils.hasText(currentJti) || !currentJti.equals(jti)) {
            return null;
        }

        LoginUserCacheDTO cache = toCache(claims);
        User user = toUser(cache);
        deleteRefreshToken(userId);
        TokenPair pair = issueTokenPair(user);
        return pair;
    }

    public void removeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        Claims claims = parseToken(token);
        if (claims == null) {
            return;
        }
        Integer userId = claims.get("uid", Integer.class);
        if (userId != null) {
            removeUserLogin(userId);
        }
    }

    public void removeUserLogin(Integer userId) {
        if (userId == null) {
            return;
        }
        incrementVersion(userId);
        deleteRefreshToken(userId);
    }

    public String getTokenByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }
        return stringRedisTemplate.opsForValue().get(USER_REFRESH_PREFIX + userId);
    }

    private String buildAccessToken(User user, long version) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(user.getUserId()))
                .id(UUID.randomUUID().toString().replace("-", ""))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessTtl())))
                .claim("uid", user.getUserId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("status", user.getStatus())
                .claim("avatar", user.getAvatar())
                .claim("ver", version)
                .claim("typ", ACCESS_TYPE)
                .signWith(secretKey)
                .compact();
    }

    private String buildRefreshToken(User user, long version) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(user.getUserId()))
                .id(UUID.randomUUID().toString().replace("-", ""))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getRefreshTtl())))
                .claim("uid", user.getUserId())
                .claim("ver", version)
                .claim("typ", REFRESH_TYPE)
                .signWith(secretKey)
                .compact();
    }

    private void saveRefreshToken(Integer userId, String refreshToken) {
        Claims claims = parseToken(refreshToken);
        if (claims == null) {
            return;
        }
        String jti = claims.getId();
        Duration ttl = Duration.between(Instant.now(), claims.getExpiration().toInstant());
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = jwtProperties.getRefreshTtl();
        }
        stringRedisTemplate.opsForValue().set(USER_REFRESH_PREFIX + userId, jti, ttl);
        stringRedisTemplate.opsForValue().set(REFRESH_JTI_PREFIX + jti, String.valueOf(userId), ttl);
    }

    private void deleteRefreshToken(Integer userId) {
        String jti = stringRedisTemplate.opsForValue().get(USER_REFRESH_PREFIX + userId);
        if (StringUtils.hasText(jti)) {
            stringRedisTemplate.delete(REFRESH_JTI_PREFIX + jti);
        }
        stringRedisTemplate.delete(USER_REFRESH_PREFIX + userId);
    }

    private Claims parseAndValidate(String token, String expectedType) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object type = claims.get("typ");
        if (!expectedType.equals(type)) {
            return null;
        }
        return claims;
    }

    private Claims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    private boolean isVersionValid(Integer userId, Number tokenVersion) {
        long currentVersion = currentVersion(userId);
        long version = tokenVersion == null ? 0L : tokenVersion.longValue();
        return currentVersion == version;
    }

    private long currentVersion(Integer userId) {
        String key = VERSION_PREFIX + userId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            stringRedisTemplate.opsForValue().setIfAbsent(key, "0");
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            stringRedisTemplate.opsForValue().set(key, "0");
            return 0L;
        }
    }

    private long incrementVersion(Integer userId) {
        Long version = stringRedisTemplate.opsForValue().increment(VERSION_PREFIX + userId);
        return version == null ? 0L : version;
    }

    private LoginUserCacheDTO toCache(Claims claims) {
        LoginUserCacheDTO cache = new LoginUserCacheDTO();
        cache.setUserId(extractUserId(claims));
        cache.setUsername(claims.get("username", String.class));
        cache.setRole(claims.get("role", Integer.class));
        cache.setStatus(claims.get("status", Integer.class));
        cache.setAvatar(claims.get("avatar", String.class));
        return cache;
    }

    private User toUser(LoginUserCacheDTO cache) {
        User user = new User();
        user.setUserId(cache.getUserId());
        user.setUsername(cache.getUsername());
        user.setRole(cache.getRole());
        user.setStatus(cache.getStatus());
        user.setAvatar(cache.getAvatar());
        return user;
    }

    private Integer extractUserId(Claims claims) {
        Object uid = claims.get("uid");
        if (uid instanceof Number number) {
            return number.intValue();
        }
        if (uid instanceof String text && StringUtils.hasText(text)) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final Duration accessTtl;
        private final Duration refreshTtl;

        public TokenPair(String accessToken, String refreshToken, Duration accessTtl, Duration refreshTtl) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessTtl = accessTtl;
            this.refreshTtl = refreshTtl;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Duration getAccessTtl() {
            return accessTtl;
        }

        public Duration getRefreshTtl() {
            return refreshTtl;
        }
    }
}
