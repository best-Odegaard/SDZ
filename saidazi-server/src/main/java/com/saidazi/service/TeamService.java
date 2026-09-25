package com.saidazi.service;

import com.saidazi.entity.Team;
import com.saidazi.entity.TeamApplication;

import java.util.List;

/**
 * 组队服务
 */
public interface TeamService {

    /**
     * 发布组队需求
     */
    Team publishTeam(Long userId, Team team);

    /**
     * 组队列表（多维度筛选：competitionId / skills / status）
     */
    List<Team> listTeams(Long competitionId, String skills, Integer status, Long page, Long size);

    /**
     * 组队详情
     */
    Team getTeamDetail(Long teamId);

    /**
     * 更新组队信息（仅发布人）
     */
    Team updateTeam(Long userId, Long teamId, Team team);

    /**
     * 关闭/删除组队（仅发布人）
     */
    void deleteTeam(Long userId, Long teamId);

    /**
     * 申请加入组队
     */
    TeamApplication applyTeam(Long userId, Long teamId, Long resumeId);

    /**
     * 处理组队申请（通过/拒绝）
     */
    void processApplication(Long userId, Long applicationId, Integer action);

    /**
     * 获取收到的申请列表（组队发布人）
     */
    List<TeamApplication> getReceivedApplications(Long userId);

    /**
     * 我发布的组队
     */
    List<Team> getMyPublishedTeams(Long userId);

    /**
     * 我的申请记录
     */
    List<TeamApplication> getMyApplications(Long userId);

    /**
     * 检查用户是否有权限处理该申请
     */
    boolean checkApplicationPermission(Long userId, Long applicationId);
}
