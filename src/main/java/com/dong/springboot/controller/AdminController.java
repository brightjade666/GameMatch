package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dao.GameRepository;
import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.service.AdminService;
import com.dong.springboot.service.TeamService;
import com.dong.springboot.service.UserInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserInfoService userInfoService;
    private final TeamService teamService;
    private final GameRepository gameRepository;   // ✅ 注入 GameRepository

    public AdminController(AdminService adminService,
                           UserInfoService userInfoService,
                           TeamService teamService,
                           GameRepository gameRepository) {
        this.adminService = adminService;
        this.userInfoService = userInfoService;
        this.teamService = teamService;
        this.gameRepository = gameRepository;
    }

    @GetMapping("/users")
    public Result getAllUsers(@RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        List<User> users = userInfoService.findAll();
        Map<String, Object> data = new HashMap<>();
        data.put("list", users);
        data.put("total", users.size());
        return Result.success(data);
    }

    @GetMapping("/user/{userId}")
    public Result getUserDetail(@PathVariable Integer userId,
                                @RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        User user = userInfoService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserProfile profile = userInfoService.getUserProfile(userId);
        List<TeamRecruit> createdTeams = teamService.getCreatedTeams(userId);
        List<TeamRecruit> joinedTeams = teamService.getJoinedTeams(userId);

        // ✅ 组装 profile 数据，包含 gameName
        Map<String, Object> profileMap = new HashMap<>();
        if (profile != null) {
            profileMap.put("gameId", profile.getGameId());
            profileMap.put("gameRank", profile.getGameRank());
            profileMap.put("winRate", profile.getWinRate());
            profileMap.put("totalMatches", profile.getTotalMatches());
            if (profile.getGameId() != null) {
                Game game = gameRepository.findById(profile.getGameId()).orElse(null);
                profileMap.put("gameName", game != null ? game.getGameName() : "未知");
            } else {
                profileMap.put("gameName", "未设定");
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("profile", profileMap);
        data.put("createdTeams", createdTeams);
        data.put("joinedTeams", joinedTeams);
        return Result.success(data);
    }

    @PutMapping("/user/{userId}/status")
    public Result updateUserStatus(@PathVariable Integer userId,
                                   @RequestParam Integer adminId,
                                   @RequestParam Integer status) {
        adminService.checkAdmin(adminId);
        User user = userInfoService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userInfoService.update(user);
        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    @DeleteMapping("/user/{userId}")
    public Result deleteUser(@PathVariable Integer userId,
                             @RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        // ✅ 禁止删除自己
        if (userId.equals(adminId)) {
            return Result.error("管理员不能删除自己");
        }
        adminService.deleteUser(userId, adminId);
        return Result.success("用户及其关联数据已删除");
    }

    @GetMapping("/teams")
    public Result getAllTeams(@RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        List<TeamRecruit> teams = teamService.getAllTeams();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TeamRecruit t : teams) {
            Map<String, Object> map = new HashMap<>();
            map.put("teamId", t.getTeamId());
            map.put("gameId", t.getGameId());
            map.put("teamName", t.getTeamName());
            map.put("teamCover", t.getTeamCover());
            map.put("teamNeed", t.getTeamNeed());
            map.put("teamDesc", t.getTeamDesc());
            map.put("leaderId", t.getLeaderId());
            // 查询队长用户名
            User leader = userInfoService.findById(t.getLeaderId());
            map.put("leaderName", leader != null ? leader.getUsername() : "未知");
            map.put("needNum", t.getNeedNum());
            map.put("currentNum", t.getCurrentNum());
            map.put("status", t.getStatus());
            map.put("createTime", t.getCreateTime());
            resultList.add(map);
        }
        return Result.success(resultList);
    }
    @GetMapping("/team/{teamId}")
    public Result getTeamDetail(@PathVariable Integer teamId,
                                @RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        return Result.success(teamService.getDetail(teamId));
    }

    @DeleteMapping("/team/{teamId}")
    public Result deleteTeam(@PathVariable Integer teamId,
                             @RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        adminService.dissolveAnyTeam(teamId, adminId);
        return Result.success("队伍已解散");
    }

    @GetMapping("/users/search")
    public Result searchUsers(@RequestParam String keyword,
                              @RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        List<User> users = userInfoService.searchByUsername(keyword);
        return Result.success(users);
    }

    @GetMapping("/teams/search")
    public Result searchTeams(@RequestParam String keyword,
                              @RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        List<TeamRecruit> teams = teamService.searchByTeamName(keyword);
        return Result.success(teams);
    }

    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam Integer adminId) {
        adminService.checkAdmin(adminId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userInfoService.count());
        stats.put("teamCount", teamService.countTeams());
        stats.put("activeUserCount", userInfoService.countByStatus(1));
        stats.put("disabledUserCount", userInfoService.countByStatus(0));
        return Result.success(stats);
    }
}