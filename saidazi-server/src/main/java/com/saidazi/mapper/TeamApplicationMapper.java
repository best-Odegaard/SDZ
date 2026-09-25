package com.saidazi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saidazi.entity.TeamApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TeamApplicationMapper extends BaseMapper<TeamApplication> {

    /**
     * 查询组队的已通过申请数
     */
    @Select("SELECT COUNT(*) FROM team_application WHERE team_id = #{teamId} AND status = 1")
    int countApproved(@Param("teamId") Long teamId);

    /**
     * 查询用户是否已经申请过该组队
     */
    @Select("SELECT COUNT(*) FROM team_application WHERE team_id = #{teamId} AND applicant_id = #{userId}")
    int countByUserAndTeam(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 批量查询用户申请过的组队 ID 列表
     */
    @Select("SELECT team_id FROM team_application WHERE applicant_id = #{userId}")
    List<Long> selectAppliedTeamIds(@Param("userId") Long userId);
}
