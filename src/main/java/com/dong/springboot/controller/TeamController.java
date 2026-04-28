package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.dao.TeamApplyRepository;
import com.dong.springboot.dao.TeamMemberRepository;
import com.dong.springboot.dao.TeamRecruitRepository;
import com.dong.springboot.entity.TeamApply;
import com.dong.springboot.entity.TeamMember;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.service.TeamService;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TeamController {

    private final TeamService teamService;
    private final TeamRecruitRepository teamRecruitRepo;
    private final TeamApplyRepository teamApplyRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamController(
            TeamService teamService,
            TeamRecruitRepository teamRecruitRepo,
            TeamApplyRepository teamApplyRepository,
            TeamMemberRepository teamMemberRepository
    ) {
        this.teamService = teamService;
        this.teamRecruitRepo = teamRecruitRepo;
        this.teamApplyRepository = teamApplyRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    // 招募列表
    @GetMapping("/team/list")
    public Map<String, Object> list() {
        List<TeamDetailVO> list = teamService.getRecruitList();
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", list);
        return map;
    }

    // 队伍详情
    @GetMapping("/team/detail/{teamId}")
    public Map<String, Object> detail(@PathVariable Integer teamId) {
        TeamDetailVO detail = teamService.getDetail(teamId);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", detail);
        return map;
    }

    // 发布队伍
    @PostMapping("/team/publish")
    public Result publish(@RequestBody TeamRecruit team) {
        teamService.publish(team);
        return Result.success("发布成功");
    }

    // 申请加入
    @PostMapping("/team/apply")
    public Result apply(@RequestBody TeamApply apply) {
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        teamApplyRepository.save(apply);
        return Result.success("申请成功，请等待队长审核");
    }

    // 队长查看申请
    @GetMapping("/team/my/applies")
    public Result myApplies(@RequestParam Integer leaderId) {
        List<TeamRecruit> teams = teamRecruitRepo.findByLeaderId(leaderId);
        List<TeamApply> allApplies = new ArrayList<>();

        for (TeamRecruit team : teams) {
            allApplies.addAll(teamApplyRepository.findByTeamId(team.getTeamId()));
        }
        return Result.success(allApplies);
    }

    // ==============================
    // ✅ 同意申请（完整版 正确逻辑）
    // ==============================
    @PostMapping("/team/agree")
    public Result agree(@RequestParam Integer id) {
        TeamApply apply = teamApplyRepository.findById(id).orElse(null);
        if (apply == null) {
            return Result.error("申请不存在");
        }

        // 1. 修改申请状态为 1（已同意）
        apply.setStatus(1);
        teamApplyRepository.save(apply);

        // 2. 插入队伍成员表（真正加入队伍）
        TeamMember member = new TeamMember();
        member.setTeamId(apply.getTeamId());
        member.setUserId(apply.getUserId());
        member.setJoinTime(LocalDateTime.now());

        // ✅ 这里 不设置 status！因为 team_member 不需要！
        teamMemberRepository.save(member);

        return Result.success("已同意该玩家加入");
    }

    // 拒绝申请
    @PostMapping("/team/reject")
    public Result reject(@RequestParam Integer id) {
        TeamApply apply = teamApplyRepository.findById(id).orElse(null);
        if (apply == null) {
            return Result.error("申请不存在");
        }

        apply.setStatus(2); // 拒绝
        teamApplyRepository.save(apply);

        // ❌ 拒绝不加入队伍
        return Result.success("已拒绝");
    }

    @GetMapping("/team/my/joined")
    public Result getMyTeams(@RequestParam Integer userId) {
        List<TeamVO> list = teamService.getMyTeamsWithMembers(userId);
        return Result.success(list);
    }
}