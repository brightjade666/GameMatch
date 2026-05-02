package com.dong.springboot.vo;

import lombok.Data;

/**
 * 队友匹配查询参数
 */
@Data
public class MatchQueryDTO {
    private Integer gameId;          // 游戏ID
    private String gameName;
    private String playtimeStart;   // 常玩开始时间
    private String playtimeEnd;     // 常玩结束时间
    private String matchNeed;       // 匹配需求（开黑上分/娱乐休闲等）
    private String personality;     // 性格（稳健/活泼等）
    private Integer excludeUserId;  // 排除当前用户ID
}