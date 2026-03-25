package com.donghaoyu.sprintpro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_user") // 数据库表名
public class TbUser {

    @Id // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增
    private Integer id;

    private String username;
    private String password;
    private String city;

    // 空构造（必须保留）
    public TbUser() {
    }

    // GETTER / SETTER 全部不变
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}