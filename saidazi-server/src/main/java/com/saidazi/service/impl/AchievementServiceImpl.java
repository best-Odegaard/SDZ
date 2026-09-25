package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Achievement;
import com.saidazi.entity.Competition;
import com.saidazi.mapper.AchievementMapper;
import com.saidazi.mapper.CompetitionMapper;
import com.saidazi.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 往届成果服务实现 -- 创新点9：校内往届获奖成果库
 */
@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Override
    public List<Achievement> listByCompetitionId(Long competitionId) {
        return achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getCompetitionId, competitionId)
                        .orderByDesc(Achievement::getCreateTime)
        );
    }

    @Override
    @Transactional
    public Achievement createAchievement(Achievement achievement) {
        if (achievement.getCompetitionId() == null) {
            throw new IllegalArgumentException("请选择关联赛事");
        }
        Competition competition = competitionMapper.selectById(achievement.getCompetitionId());
        if (competition == null) {
            throw new IllegalArgumentException("关联赛事不存在");
        }
        if (achievement.getTeamName() == null || achievement.getTeamName().isBlank()) {
            throw new IllegalArgumentException("获奖队伍名称不能为空");
        }
        achievement.setCreateTime(LocalDateTime.now());
        achievementMapper.insert(achievement);
        log.info("录入往届成果: competitionId={}, teamName={}",
                achievement.getCompetitionId(), achievement.getTeamName());
        return achievement;
    }
}