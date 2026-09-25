package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.dto.MatchResultDTO;
import com.saidazi.entity.Resume;
import com.saidazi.entity.Team;
import com.saidazi.entity.TeamApplication;
import com.saidazi.entity.User;
import com.saidazi.mapper.*;
import com.saidazi.service.MatchService;
import com.saidazi.util.MatchAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能匹配服务实现 —— 接口②：队友智能匹配算法
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamApplicationMapper teamApplicationMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public List<MatchResultDTO> recommendTeams(Long userId) {
        // 1. 获取当前用户信息
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        Set<String> userSkills = MatchAlgorithm.toSet(currentUser.getSkills());
        Set<String> userInterests = MatchAlgorithm.toSet(currentUser.getInterests());

        // 2. 查询当前用户已申请的组队 ID，排除已申请的
        List<Long> appliedTeamIds = teamApplicationMapper.selectAppliedTeamIds(userId);
        Set<Long> excludeTeamIds = new HashSet<>(appliedTeamIds);

        // 3. 查询所有招募中的组队
        List<Team> allTeams = teamMapper.selectList(
                new LambdaQueryWrapper<Team>()
                        .eq(Team::getStatus, 0) // 招募中
        );

        // 4. 计算每个组队的匹配度
        List<MatchResultDTO> results = new ArrayList<>();
        for (Team team : allTeams) {
            if (excludeTeamIds.contains(team.getId())) {
                continue; // 跳过已申请的
            }

            Set<String> teamSkills = MatchAlgorithm.toSet(team.getSkillsRequired());

            // 查询组队发布人的参赛兴趣（作为补充匹配维度）
            User teamLeader = userMapper.selectById(team.getUserId());
            Set<String> leaderInterests = teamLeader != null
                    ? MatchAlgorithm.toSet(teamLeader.getInterests())
                    : new HashSet<>();

            // 计算技能匹配度 + 兴趣匹配度
            int matchScore = MatchAlgorithm.calculateMatchScore(
                    userSkills, teamSkills,
                    userInterests, leaderInterests,
                    false // 简化处理，不查共同收藏
            );

            MatchResultDTO dto = new MatchResultDTO();
            dto.setTargetId(team.getId());
            dto.setMatchType("team");
            dto.setMatchScore(matchScore);
            dto.setTitle(team.getTitle());
            dto.setNeedCount(team.getNeedCount());
            dto.setApprovedCount(teamApplicationMapper.countApproved(team.getId()));

            // 共同技能
            Set<String> common = new HashSet<>(userSkills);
            common.retainAll(teamSkills);
            dto.setCommonSkills(String.join(",", common));

            dto.setStatus(team.getStatus());
            results.add(dto);
        }

        // 5. 按匹配度降序排列
        results.sort((a, b) -> b.getMatchScore() - a.getMatchScore());

        log.info("智能匹配组队: userId={}, 推荐数量={}", userId, results.size());
        return results;
    }

    @Override
    public List<MatchResultDTO> recommendMates(Long userId) {
        // 1. 获取当前用户信息
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        Set<String> userSkills = MatchAlgorithm.toSet(currentUser.getSkills());
        Set<String> userInterests = MatchAlgorithm.toSet(currentUser.getInterests());

        // 2. 查询所有其他用户（排除自身）
        List<User> otherUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .ne(User::getId, userId)
                        .isNotNull(User::getSkills) // 必须有技能标签
                        .ne(User::getSkills, "")
        );

        // 3. 计算每个用户的匹配度
        List<MatchResultDTO> results = new ArrayList<>();
        for (User other : otherUsers) {
            Set<String> otherSkills = MatchAlgorithm.toSet(other.getSkills());
            Set<String> otherInterests = MatchAlgorithm.toSet(other.getInterests());

            int matchScore = MatchAlgorithm.calculateMatchScore(
                    userSkills, otherSkills,
                    userInterests, otherInterests,
                    false
            );

            if (matchScore < 20) {
                continue; // 过滤低匹配度
            }

            MatchResultDTO dto = new MatchResultDTO();
            dto.setTargetId(other.getId());
            dto.setMatchType("user");
            dto.setMatchScore(matchScore);
            dto.setTitle(other.getNickname());

            // 共同技能
            Set<String> common = new HashSet<>(userSkills);
            common.retainAll(otherSkills);
            dto.setCommonSkills(String.join(",", common));

            results.add(dto);
        }

        // 4. 按匹配度降序排列
        results.sort((a, b) -> b.getMatchScore() - a.getMatchScore());

        log.info("智能匹配队友: userId={}, 推荐数量={}", userId, results.size());
        return results;
    }
}
