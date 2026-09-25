package com.saidazi.service;

import com.saidazi.dto.MatchResultDTO;

import java.util.List;

/**
 * 智能匹配服务
 */
public interface MatchService {

    /**
     * 智能匹配推荐组队
     * 基于用户技能标签与组队需求标签的 Jaccard 相似度
     */
    List<MatchResultDTO> recommendTeams(Long userId);

    /**
     * 智能匹配推荐队友（其他用户）
     * 基于技能互补度、赛事兴趣重合度
     */
    List<MatchResultDTO> recommendMates(Long userId);
}
