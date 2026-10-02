package com.dong.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.springboot.entity.TeamHeatStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeamHeatStatMapper extends BaseMapper<TeamHeatStat> {

    int incrementView(@Param("teamId") Integer teamId);

    int incrementComment(@Param("teamId") Integer teamId);

    int incrementApply(@Param("teamId") Integer teamId);

    int initializeMissingActiveTeams();

    List<TeamHeatStat> selectActiveTeamStats();
}
