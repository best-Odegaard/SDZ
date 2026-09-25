package com.saidazi.service;

import com.saidazi.entity.Achievement;

import java.util.List;

/**
 * 往届成果服务
 */
public interface AchievementService {

    /**
     * 查询赛事的往届获奖成果
     */
    List<Achievement> listByCompetitionId(Long competitionId);

    /**
     * 管理员录入往届成果
     */
    Achievement createAchievement(Achievement achievement);
}