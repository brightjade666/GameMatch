package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.annotation.LogExecutionTime;
import com.dong.springboot.dto.LoginUserCacheDTO;
import com.dong.springboot.entity.TeamApply;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.mapper.TeamApplyMapper;
import com.dong.springboot.mapper.TeamRecruitMapper;
import com.dong.springboot.service.TeamRankService;
import com.dong.springboot.service.TeamMatchService;
import com.dong.springboot.service.TeamService;
import com.dong.springboot.vo.CommentVO;
import com.dong.springboot.vo.TeamDetailVO;
import com.dong.springboot.vo.TeamVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TeamController {

    private final TeamService teamService;
    private final TeamRecruitMapper teamRecruitMapper;
    private final TeamApplyMapper teamApplyMapper;
    private final TeamRankService teamRankService;
    private final TeamMatchService teamMatchService;

    public TeamController(TeamService teamService,
                          TeamRecruitMapper teamRecruitMapper,
                          TeamApplyMapper teamApplyMapper,
                          TeamRankService teamRankService,
                          TeamMatchService teamMatchService) {
        this.teamService = teamService;
        this.teamRecruitMapper = teamRecruitMapper;
        this.teamApplyMapper = teamApplyMapper;
        this.teamRankService = teamRankService;
        this.teamMatchService = teamMatchService;
    }

    @GetMapping("/team/list")
    public Result list(@RequestParam(required = false) Integer userId) {
        List<TeamDetailVO> list = teamService.getRecruitList(userId);
        return Result.success(list);
    }

    @GetMapping("/team/hot/top")
    @LogExecutionTime
    public Result hotTeams(@RequestParam(required = false) Integer limit) {
        return Result.success(teamRankService.getHotTeams(limit));
    }

    @GetMapping("/team/match")
    @LogExecutionTime
    public Result matchTeams(@AuthenticationPrincipal LoginUserCacheDTO loginUser,
                             @RequestParam(required = false) Integer limit) {
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(teamMatchService.matchTeams(loginUser.getUserId(), limit));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/team/hot/rank/{teamId}")
    @LogExecutionTime
    public Result hotTeamRank(@PathVariable Integer teamId) {
        Object rank = teamRankService.getTeamRank(teamId);
        return rank == null ? Result.error("队伍不存在或已失效") : Result.success(rank);
    }

    @GetMapping("/team/detail/{teamId}")
    @LogExecutionTime
    public Result detail(@PathVariable Integer teamId) {
        TeamDetailVO detail = teamService.getDetail(teamId);
        return detail == null ? Result.error("队伍不存在") : Result.success(detail);
    }

    @PostMapping("/team/publish")
    public Result publish(@RequestBody TeamRecruit team) {
        teamService.publish(team);
        return Result.success("发布成功");
    }

    @PostMapping("/team/comment")
    public Result addComment(@RequestParam Integer teamId,
                             @RequestParam Integer userId,
                             @RequestParam String content) {
        try {
            CommentVO vo = teamService.addComment(teamId, userId, content);
            return Result.success(vo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/team/apply")
    public Result apply(@RequestBody TeamApply apply) {
        boolean firstApply = teamService.submitApply(apply);
        String message = firstApply ? "申请已发送，请等待队长处理" : "申请已重新提交";
        return Result.success(message, null);
    }

    @GetMapping("/team/my/applies")
    public Result myApplies(@RequestParam Integer leaderId) {
        List<TeamRecruit> teams = teamRecruitMapper.findByLeaderId(leaderId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TeamRecruit team : teams) {
            List<TeamApply> applies = teamApplyMapper.findByTeamId(team.getTeamId());
            for (TeamApply apply : applies) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", apply.getId());
                item.put("teamId", apply.getTeamId());
                item.put("userId", apply.getUserId());
                item.put("status", apply.getStatus());
                item.put("applyTime", apply.getApplyTime());
                item.put("applierName", teamService.getUserNameById(apply.getUserId()));
                item.put("teamName", team.getTeamName());
                result.add(item);
            }
        }
        return Result.success(result);
    }

    @PostMapping("/team/dissolve")
    public Result dissolveTeam(@RequestParam Integer teamId, @RequestParam Integer leaderId) {
        try {
            teamService.dissolveTeam(teamId, leaderId);
            return Result.success("队伍已解散", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/team/leave")
    public Result leaveTeam(@RequestParam Integer teamId, @RequestParam Integer userId) {
        try {
            teamService.leaveTeam(teamId, userId);
            return Result.success("已退出队伍", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/team/agree")
    public Result agree(@RequestParam Integer id) {
        teamService.agreeApply(id);
        return Result.success("已同意申请", null);
    }

    @PostMapping("/team/reject")
    public Result reject(@RequestParam Integer id) {
        teamService.rejectApply(id);
        return Result.success("已拒绝申请", null);
    }

    @GetMapping("/team/my/joined")
    public Result getMyTeams(@RequestParam Integer userId) {
        List<TeamVO> list = teamService.getMyTeamsWithMembers(userId);
        return Result.success(list);
    }

    @GetMapping("/team/created")
    public Result getCreatedTeams(@RequestParam Integer leaderId) {
        return Result.success(teamService.getCreatedTeams(leaderId));
    }

    @PostMapping("/team/remove")
    public Result removeMember(@RequestParam Integer teamId,
                               @RequestParam Integer leaderId,
                               @RequestParam Integer userId) {
        teamService.removeMember(teamId, leaderId, userId);
        return Result.success("移除成功");
    }
}
