package com.dong.springboot.service;

import com.dong.springboot.entity.TeamMember;
import com.dong.springboot.entity.TeamRecruit;
import com.dong.springboot.entity.User;
import com.dong.springboot.mapper.TeamMemberMapper;
import com.dong.springboot.mapper.TeamRecruitMapper;
import com.dong.springboot.mapper.UserMapper;
import com.dong.springboot.vo.TeamMatchVO;
import com.dong.springboot.vo.TeamRankCandidateVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TeamMatchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    private static final int PAGE_SIZE = 20;

    private final UserMapper userMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamRecruitMapper teamRecruitMapper;
    private final TeamRankService teamRankService;

    public TeamMatchService(UserMapper userMapper,
                            TeamMemberMapper teamMemberMapper,
                            TeamRecruitMapper teamRecruitMapper,
                            TeamRankService teamRankService) {
        this.userMapper = userMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.teamRecruitMapper = teamRecruitMapper;
        this.teamRankService = teamRankService;
    }

    public List<TeamMatchVO> matchTeams(Integer userId, Integer requestedLimit) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (user.getGameId() == null) {
            throw new IllegalArgumentException("请先设置常玩游戏");
        }

        int limit = normalizeLimit(requestedLimit);
        Set<Integer> joinedTeamIds = new HashSet<>();
        for (TeamMember member : teamMemberMapper.findByUserId(userId)) {
            joinedTeamIds.add(member.getTeamId());
        }

        List<TeamMatchVO> result = new ArrayList<>(limit);
        long offset = 0L;
        while (result.size() < limit) {
            List<TeamRankCandidateVO> candidates = teamRankService.getRankCandidates(offset, PAGE_SIZE);
            if (candidates.isEmpty()) {
                break;
            }
            for (TeamRankCandidateVO candidate : candidates) {
                TeamRecruit team = teamRecruitMapper.findById(candidate.getTeamId()).orElse(null);
                if (isMatchedTeam(team, user, joinedTeamIds)) {
                    result.add(toMatchVO(team, candidate));
                    if (result.size() >= limit) {
                        break;
                    }
                }
            }
            if (candidates.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }
        return result;
    }

    private boolean isMatchedTeam(TeamRecruit team, User user, Set<Integer> joinedTeamIds) {
        if (team == null || !Integer.valueOf(1).equals(team.getStatus())) {
            return false;
        }
        if (!user.getGameId().equals(team.getGameId())) {
            return false;
        }
        if (user.getUserId().equals(team.getLeaderId()) || joinedTeamIds.contains(team.getTeamId())) {
            return false;
        }
        int currentNum = team.getCurrentNum() == null ? 0 : team.getCurrentNum();
        int needNum = team.getNeedNum() == null ? 0 : team.getNeedNum();
        return currentNum < needNum;
    }

    private TeamMatchVO toMatchVO(TeamRecruit team, TeamRankCandidateVO candidate) {
        int currentNum = team.getCurrentNum() == null ? 0 : team.getCurrentNum();
        int needNum = team.getNeedNum() == null ? 0 : team.getNeedNum();
        int remainingNum = Math.max(needNum - currentNum, 0);

        TeamMatchVO vo = new TeamMatchVO();
        vo.setTeamId(team.getTeamId());
        vo.setTeamName(team.getTeamName());
        vo.setTeamCover(team.getTeamCover());
        vo.setGameId(team.getGameId());
        vo.setLeaderId(team.getLeaderId());
        vo.setTeamNeed(team.getTeamNeed());
        vo.setTeamDesc(team.getTeamDesc());
        vo.setCurrentNum(currentNum);
        vo.setNeedNum(needNum);
        vo.setRemainingNum(remainingNum);
        vo.setRank(candidate.getRank());
        vo.setHeat(candidate.getHeat());
        vo.setReasons(List.of("符合你的常玩游戏", "当前热门队伍", "仍可加入" + remainingNum + "人"));
        return vo;
    }

    private int normalizeLimit(Integer limit) {
        return limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    }
}
