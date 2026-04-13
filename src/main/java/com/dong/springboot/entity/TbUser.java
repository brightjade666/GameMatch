package com.dong.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// 1. 映射到数据库的 user 表
@Entity
@Table(name = "user")
public class TbUser { // 可选：类名从 TbUser 改为 User（更贴合表名）

    // 2. 主键映射：user 表的主键是 user_id（而非 id）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId; // 字段名改为 userId（驼峰对应下划线）

    // 3. 原有字段对齐 user 表（保留原有核心字段，新增 user 表的扩展字段）
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    // ===== user 表新增字段（补充完整）=====
    @Column(name = "gender")
    private Byte gender; // 0=不显示 1=男 2=女

    @Column(name = "age")
    private Integer age;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "contact", unique = true, nullable = false)
    private String contact;

    @Column(name = "game_id")
    private Integer gameId;

    @Column(name = "game_rank")
    private String gameRank;

    @Column(name = "introduction")
    private String introduction;

    @Column(name = "role")
    private Byte role; // 0=普通用户 1=队长 9=管理员

    @Column(name = "status")
    private Byte status; // 1=正常 0=禁用

    // 4. 时间字段：对齐 user 表的字段名 + 自动填充
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 空构造器（JPA必需）
    public TbUser() {}

    // 时间自动填充（保留）
    @PrePersist // 新增时触发
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate // 更新时触发
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // ===== GET/SET 方法（全部更新为新字段）=====
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Byte getGender() { return gender; }
    public void setGender(Byte gender) { this.gender = gender; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Integer getGameId() { return gameId; }
    public void setGameId(Integer gameId) { this.gameId = gameId; }

    public String getGameRank() { return gameRank; }
    public void setGameRank(String gameRank) { this.gameRank = gameRank; }

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public Byte getRole() { return role; }
    public void setRole(Byte role) { this.role = role; }

    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}