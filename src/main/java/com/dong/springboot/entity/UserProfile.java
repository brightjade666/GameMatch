package com.dong.springboot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer profileId;

    // 用户ID（关联user表，非空+唯一，确保一个用户只有一条资料）
    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    // 性别：1=男 2=女（前端传递，非空）
    @Column(name = "gender", nullable = false)
    private Byte gender;

    // 年龄（前端传递，非空）
    @Column(name = "age", nullable = false)
    private Integer age;

    // 头像地址（前端上传的base64或URL）
    @Column(name = "avatar")
    private String avatar;

    // 个人简介
    @Column(name = "introduction")
    private String introduction;

    // 联系方式（微信/游戏ID，非空）
    @Column(name = "contact", nullable = false)
    private String contact;

    // 游戏ID：1=王者 2=联盟 3=和平精英 4=原神 0=其他（非空）
    @Column(name = "game_id", nullable = false)
    private Integer gameId;

    // 游戏段位（允许为空，后续扩展）
    @Column(name = "game_rank")
    private String gameRank;

    // 主打位置
    @Column(name = "main_position")
    private String mainPosition;

    // 游玩时间段（白天/晚上/深夜等，前端传递）
    @Column(name = "play_time")
    private String playTime;

    // 性格（稳健/活泼/话痨等，前端传递）
    @Column(name = "play_style")
    private String playStyle;

    // 偏好模式
    @Column(name = "preferred_mode")
    private String preferredMode;

    // 擅长英雄
    @Column(name = "favorite_heroes")
    private String favoriteHeroes;

    // 胜率
    @Column(name = "win_rate")
    private Double winRate;

    // 总场次
    @Column(name = "total_matches")
    private Integer totalMatches;

    // 匹配需求（开黑上分/娱乐休闲等，前端传递）
    @Column(name = "team_requirement")
    private String teamRequirement;

    // 创建时间（自动填充）
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 更新时间（自动填充）
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 新增/保存时自动填充创建/更新时间
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 更新时自动填充更新时间
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}