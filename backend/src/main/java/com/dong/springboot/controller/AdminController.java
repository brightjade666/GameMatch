package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dto.LoginUserCacheDTO;
import com.dong.springboot.entity.Game;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.entity.User;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.mapper.GameMapper;
import com.dong.springboot.service.AdminService;
import com.dong.springboot.service.LoginTokenService;
import com.dong.springboot.service.TeamService;
import com.dong.springboot.service.UserInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserInfoService userInfoService;
    private final TeamService teamService;
    private final GameMapper gameMapper;
    private final LoginTokenService loginTokenService;

    public AdminController(AdminService adminService,
                           UserInfoService userInfoService,
                           TeamService teamService,
                           GameMapper gameMapper,
                           LoginTokenService loginTokenService) {
        this.adminService = adminService;
        this.userInfoService = userInfoService;
        this.teamService = teamService;
        this.gameMapper = gameMapper;
        this.loginTokenService = loginTokenService;
    }

    @GetMapping("/users")
    public Result getAllUsers() {
        adminService.checkAdmin(currentAdminId());
        List<User> users = userInfoService.findAll();
        Map<String, Object> data = new HashMap<>();
        data.put("list", users);
        data.put("total", users.size());
        return Result.success(data);
    }

    @GetMapping("/user/{userId}")
    public Result getUserDetail(@PathVariable Integer userId) {
        adminService.checkAdmin(currentAdminId());
        User user = userInfoService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        UserProfile profile = userInfoService.getUserProfile(userId);
        List<TeamRecruit> createdTeams = teamService.getCreatedTeams(userId);
        List<TeamRecruit> joinedTeams = teamService.getJoinedTeams(userId);

        Map<String, Object> profileMap = new HashMap<>();
        if (profile != null) {
            profileMap.put("gameId", profile.getGameId());
            profileMap.put("gameRank", profile.getGameRank());
            profileMap.put("winRate", profile.getWinRate());
            profileMap.put("totalMatches", profile.getTotalMatches());
            if (profile.getGameId() != null) {
                Game game = gameMapper.findById(profile.getGameId()).orElse(null);
                profileMap.put("gameName", game != null ? game.getGameName() : "未知");
            } else {
                profileMap.put("gameName", "未设置");
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
                                   @RequestParam Integer status) {
        adminService.checkAdmin(currentAdminId());
        User user = userInfoService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userInfoService.update(user);
        if (status != null && status == 0) {
            loginTokenService.removeUserLogin(userId);
        }
        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    @DeleteMapping("/user/{userId}")
    public Result deleteUser(@PathVariable Integer userId) {
        Integer adminId = currentAdminId();
        adminService.checkAdmin(adminId);
        if (userId.equals(adminId)) {
            return Result.error("管理员不能删除自己");
        }
        adminService.deleteUser(userId, adminId);
        return Result.success("用户及其关联数据已删除");
    }

    @GetMapping("/teams")
    public Result getAllTeams() {
        adminService.checkAdmin(currentAdminId());
        List<TeamRecruit> teams = teamService.getAllTeams();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TeamRecruit team : teams) {
            Map<String, Object> map = new HashMap<>();
            map.put("teamId", team.getTeamId());
            map.put("gameId", team.getGameId());
            map.put("teamName", team.getTeamName());
            map.put("teamCover", team.getTeamCover());
            map.put("teamNeed", team.getTeamNeed());
            map.put("teamDesc", team.getTeamDesc());
            map.put("leaderId", team.getLeaderId());
            User leader = userInfoService.findById(team.getLeaderId());
            map.put("leaderName", leader != null ? leader.getUsername() : "未知");
            map.put("needNum", team.getNeedNum());
            map.put("currentNum", team.getCurrentNum());
            map.put("status", team.getStatus());
            map.put("createTime", team.getCreateTime());
            resultList.add(map);
        }
        return Result.success(resultList);
    }

    @GetMapping("/team/{teamId}")
    public Result getTeamDetail(@PathVariable Integer teamId) {
        adminService.checkAdmin(currentAdminId());
        return Result.success(teamService.getDetail(teamId));
    }

    @DeleteMapping("/team/{teamId}")
    public Result deleteTeam(@PathVariable Integer teamId) {
        Integer adminId = currentAdminId();
        adminService.checkAdmin(adminId);
        adminService.dissolveAnyTeam(teamId, adminId);
        return Result.success("队伍已解散");
    }

    @GetMapping("/users/search")
    public Result searchUsers(@RequestParam String keyword) {
        adminService.checkAdmin(currentAdminId());
        return Result.success(userInfoService.searchByUsername(keyword));
    }

    @GetMapping("/teams/search")
    public Result searchTeams(@RequestParam String keyword) {
        adminService.checkAdmin(currentAdminId());
        return Result.success(teamService.searchByTeamName(keyword));
    }

    @GetMapping("/statistics")
    public Result getStatistics() {
        adminService.checkAdmin(currentAdminId());
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userInfoService.count());
        stats.put("teamCount", teamService.countTeams());
        stats.put("activeUserCount", userInfoService.countByStatus(1));
        stats.put("disabledUserCount", userInfoService.countByStatus(0));
        return Result.success(stats);
    }

    private Integer currentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserCacheDTO loginUser) || loginUser.getUserId() == null) {
            throw new RuntimeException("当前未登录");
        }
        return loginUser.getUserId();
    }
}
