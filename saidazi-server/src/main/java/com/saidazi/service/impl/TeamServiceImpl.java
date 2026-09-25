package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Resume;
import com.saidazi.entity.Team;
import com.saidazi.entity.TeamApplication;
import com.saidazi.mapper.ResumeMapper;
import com.saidazi.mapper.TeamApplicationMapper;
import com.saidazi.mapper.TeamMapper;
import com.saidazi.service.TeamService;
import com.saidazi.util.MatchAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 组队服务实现 —— 接口④：组队申请审核
 */
@Slf4j
@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamApplicationMapper teamApplicationMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    // ==================== 组队发布与浏览 -- 接口 5.5 ====================

    @Override
    @Transactional
    public Team publishTeam(Long userId, Team team) {
        // 参数校验
        if (team.getCompetitionId() == null) {
            throw new IllegalArgumentException("请选择关联赛事");
        }
        if (team.getTitle() == null || team.getTitle().isBlank()) {
            throw new IllegalArgumentException("组队标题不能为空");
        }
        if (team.getNeedCount() == null || team.getNeedCount() < 1 || team.getNeedCount() > 10) {
            throw new IllegalArgumentException("需要人数应在 1~10 之间");
        }
        if (team.getContact() == null || team.getContact().isBlank()) {
            throw new IllegalArgumentException("联系方式不能为空");
        }

        team.setUserId(userId);
        team.setStatus(0); // 招募中
        team.setCreateTime(LocalDateTime.now());
        team.setUpdateTime(LocalDateTime.now());

        teamMapper.insert(team);
        log.info("发布组队: userId={}, teamId={}, competitionId={}", userId, team.getId(), team.getCompetitionId());
        return team;
    }

    @Override
    public List<Team> listTeams(Long competitionId, String skills, Integer status, Long page, Long size) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) {
            wrapper.eq(Team::getCompetitionId, competitionId);
        }
        if (status != null) {
            wrapper.eq(Team::getStatus, status);
        } else {
            // 默认只展示招募中
            wrapper.eq(Team::getStatus, 0);
        }
        if (skills != null && !skills.isBlank()) {
            // 简单 LIKE 匹配（按技能标签子串）
            wrapper.like(Team::getSkillsRequired, skills);
        }
        wrapper.orderByDesc(Team::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Team> p =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        page == null ? 1 : page, size == null ? 20 : size);
        return teamMapper.selectPage(p, wrapper).getRecords();
    }

    @Override
    public Team getTeamDetail(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new IllegalArgumentException("组队不存在");
        }
        return team;
    }

    @Override
    @Transactional
    public Team updateTeam(Long userId, Long teamId, Team team) {
        Team existing = teamMapper.selectById(teamId);
        if (existing == null) {
            throw new IllegalArgumentException("组队不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改他人发布的组队");
        }
        // 选择性更新
        if (team.getTitle() != null) existing.setTitle(team.getTitle());
        if (team.getNeedCount() != null) existing.setNeedCount(team.getNeedCount());
        if (team.getSkillsRequired() != null) existing.setSkillsRequired(team.getSkillsRequired());
        if (team.getDescription() != null) existing.setDescription(team.getDescription());
        if (team.getContact() != null) existing.setContact(team.getContact());
        if (team.getRequireResume() != null) existing.setRequireResume(team.getRequireResume());
        if (team.getRemark() != null) existing.setRemark(team.getRemark());
        if (team.getStatus() != null) existing.setStatus(team.getStatus());

        existing.setUpdateTime(LocalDateTime.now());
        teamMapper.updateById(existing);
        log.info("更新组队: teamId={}, userId={}", teamId, userId);
        return existing;
    }

    @Override
    @Transactional
    public void deleteTeam(Long userId, Long teamId) {
        Team existing = teamMapper.selectById(teamId);
        if (existing == null) {
            throw new IllegalArgumentException("组队不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除他人发布的组队");
        }
        // 关闭组队（置为已关闭状态，保留数据）
        existing.setStatus(2);
        existing.setUpdateTime(LocalDateTime.now());
        teamMapper.updateById(existing);
        log.info("关闭组队: teamId={}, userId={}", teamId, userId);
    }

    @Override
    public List<Team> getMyPublishedTeams(Long userId) {
        return teamMapper.selectList(
                new LambdaQueryWrapper<Team>()
                        .eq(Team::getUserId, userId)
                        .orderByDesc(Team::getCreateTime)
        );
    }

    @Override
    public List<TeamApplication> getMyApplications(Long userId) {
        return teamApplicationMapper.selectList(
                new LambdaQueryWrapper<TeamApplication>()
                        .eq(TeamApplication::getApplicantId, userId)
                        .orderByDesc(TeamApplication::getCreateTime)
        );
    }

    // ==================== 组队申请审核 -- 接口④ ====================

    @Override
    @Transactional
    public TeamApplication applyTeam(Long userId, Long teamId, Long resumeId) {
        // 1. 校验组队是否存在且招募中
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new IllegalArgumentException("组队不存在");
        }
        if (team.getStatus() != 0) {
            throw new IllegalArgumentException("该组队已停止招募");
        }

        // 2. 校验是否已经申请过
        int exists = teamApplicationMapper.countByUserAndTeam(teamId, userId);
        if (exists > 0) {
            throw new IllegalArgumentException("您已申请过该组队，请勿重复申请");
        }

        // 3. 若要求简历，校验简历 ID 并计算匹配度
        BigDecimal matchScore = null;
        if (team.getRequireResume() == 1) {
            if (resumeId == null) {
                throw new IllegalArgumentException("该组队要求简历，请完善简历后申请");
            }
            Resume resume = resumeMapper.selectById(resumeId);
            if (resume == null || !resume.getUserId().equals(userId)) {
                throw new IllegalArgumentException("简历不存在或不属于当前用户");
            }
            if (resume.getStatus() != 1) {
                throw new IllegalArgumentException("简历状态不是「已完成」，请先完善简历");
            }

            // 计算技能匹配度
            Set<String> resumeSkills = MatchAlgorithm.toSet(resume.getSkills());
            Set<String> requiredSkills = MatchAlgorithm.toSet(team.getSkillsRequired());
            int score = MatchAlgorithm.calculateResumeMatchScore(resumeSkills, requiredSkills);
            matchScore = BigDecimal.valueOf(score);
        }

        // 4. 创建申请记录
        TeamApplication application = new TeamApplication();
        application.setTeamId(teamId);
        application.setApplicantId(userId);
        application.setResumeId(resumeId);
        application.setMatchScore(matchScore);
        application.setStatus(0); // 待审核
        application.setCreateTime(LocalDateTime.now());

        teamApplicationMapper.insert(application);

        log.info("组队申请提交: userId={}, teamId={}, resumeId={}, matchScore={}",
                userId, teamId, resumeId, matchScore);

        return application;
    }

    /**
     * 处理组队申请 —— 接口④核心方法
     * 发布人 通过/拒绝 申请人的加入请求
     */
    @Override
    @Transactional
    public void processApplication(Long userId, Long applicationId, Integer action) {
        // 1. 校验 action 参数
        if (action == null || (action != 1 && action != 2)) {
            throw new IllegalArgumentException("处理结果无效：1=通过, 2=拒绝");
        }

        // 2. 查询申请记录
        TeamApplication application = teamApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请记录不存在");
        }

        // 3. 权限校验：只有组队发布人才能处理
        Team team = teamMapper.selectById(application.getTeamId());
        if (team == null) {
            throw new IllegalArgumentException("关联组队不存在");
        }
        if (!team.getUserId().equals(userId)) {
            throw new IllegalArgumentException("您不是该组队的发布人，无权处理申请");
        }

        // 4. 校验申请状态：只能处理待审核的申请
        if (application.getStatus() != 0) {
            throw new IllegalArgumentException("该申请已被处理，请勿重复操作");
        }

        // 5. 如果是通过操作，检查组队是否已满员
        if (action == 1) {
            int approvedCount = teamApplicationMapper.countApproved(application.getTeamId());
            if (approvedCount >= team.getNeedCount()) {
                throw new IllegalArgumentException("组队已满员，无法通过更多申请");
            }
        }

        // 6. 更新申请状态
        application.setStatus(action);
        application.setUpdateTime(LocalDateTime.now());
        teamApplicationMapper.updateById(application);

        // 7. 如果通过，检查组队是否达到满员状态
        if (action == 1) {
            teamMapper.autoMarkFull(application.getTeamId());
        }

        log.info("组队申请处理: applicationId={}, action={} (1通过 2拒绝), operatorId={}",
                applicationId, action, userId);
    }

    @Override
    public List<TeamApplication> getReceivedApplications(Long userId) {
        // 查询该用户发布的所有组队的申请
        List<Team> userTeams = teamMapper.selectList(
                new LambdaQueryWrapper<Team>().eq(Team::getUserId, userId)
        );

        if (userTeams.isEmpty()) {
            return List.of();
        }

        List<Long> teamIds = userTeams.stream()
                .map(Team::getId)
                .toList();

        return teamApplicationMapper.selectList(
                new LambdaQueryWrapper<TeamApplication>()
                        .in(TeamApplication::getTeamId, teamIds)
                        .orderByDesc(TeamApplication::getCreateTime)
        );
    }

    @Override
    public boolean checkApplicationPermission(Long userId, Long applicationId) {
        TeamApplication application = teamApplicationMapper.selectById(applicationId);
        if (application == null) return false;
        Team team = teamMapper.selectById(application.getTeamId());
        return team != null && team.getUserId().equals(userId);
    }
}
