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
import java.util.*;

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

    // 申请加入（处理重复申请）
    @PostMapping("/team/apply")
    public Result apply(@RequestBody TeamApply apply) {
        // 检查是否已经存在申请记录
        List<TeamApply> existingList = teamApplyRepository.findByTeamIdAndUserId(apply.getTeamId(), apply.getUserId());
        if (existingList != null && !existingList.isEmpty()) {
            TeamApply existing = existingList.get(0);
            // 如果之前被拒绝或已过期，允许重新申请
            existing.setStatus(0);
            existing.setApplyTime(LocalDateTime.now());
            teamApplyRepository.save(existing);
            return Result.success("申请已重新提交，请等待队长审核", null);
        }

        // 新申请
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        teamApplyRepository.save(apply);
        return Result.success("申请成功，请等待队长审核", null);
    }


    // 队长查看申请（返回详细信息，含申请人昵称和队伍名）
    @GetMapping("/team/my/applies")
    public Result myApplies(@RequestParam Integer leaderId) {
        // 先查出队长创建的所有队伍
        List<TeamRecruit> teams = teamRecruitRepo.findByLeaderId(leaderId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TeamRecruit team : teams) {
            List<TeamApply> applies = teamApplyRepository.findByTeamId(team.getTeamId());
            for (TeamApply a : applies) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", a.getId());
                item.put("teamId", a.getTeamId());
                item.put("userId", a.getUserId());
                item.put("status", a.getStatus());
                item.put("applyTime", a.getApplyTime());
                item.put("applierName", teamService.getUserNameById(a.getUserId())); // 申请人昵称
                item.put("teamName", team.getTeamName()); // 队伍名
                result.add(item);
            }
        }
        return Result.success(result);
    }

    // 解散队伍
    @PostMapping("/team/dissolve")
    public Result dissolveTeam(@RequestParam Integer teamId, @RequestParam Integer leaderId) {
        try {
            teamService.dissolveTeam(teamId, leaderId);
            return Result.success("队伍已解散", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 退出队伍
    @PostMapping("/team/leave")
    public Result leaveTeam(@RequestParam Integer teamId, @RequestParam Integer userId) {
        try {
            teamService.leaveTeam(teamId, userId);
            return Result.success("已退出队伍", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 同意申请（实际逻辑在 Service 中，包含发送通知）
    @PostMapping("/team/agree")
    public Result agree(@RequestParam Integer id) {
        teamService.agreeApply(id);
        return Result.success("已同意该玩家加入", null);
    }

    // 拒绝申请
    @PostMapping("/team/reject")
    public Result reject(@RequestParam Integer id) {
        teamService.rejectApply(id);
        return Result.success("已拒绝", null);
    }

    // 我的队伍（已加入的）
    @GetMapping("/team/my/joined")
    public Result getMyTeams(@RequestParam Integer userId) {
        List<TeamVO> list = teamService.getMyTeamsWithMembers(userId);
        return Result.success(list);
    }

    // 获取队长创建的队伍
    @GetMapping("/team/created")
    public Result getCreatedTeams(@RequestParam Integer leaderId) {
        List<TeamRecruit> teams = teamService.getCreatedTeams(leaderId);
        return Result.success(teams);
    }
}