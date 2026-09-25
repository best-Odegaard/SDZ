package com.saidazi.util;

import java.util.*;

/**
 * 组队智能匹配算法
 * 基于 Jaccard 相似度计算技能标签重合度，
 * 结合用户参赛历史、收藏赛事偏好综合打分排序
 */
public class MatchAlgorithm {

    /**
     * 计算 Jaccard 相似度
     * J(A, B) = |A ∩ B| / |A ∪ B|
     */
    public static double jaccardSimilarity(Set<String> setA, Set<String> setB) {
        if (setA == null || setB == null) return 0.0;
        if (setA.isEmpty() && setB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        return (double) intersection.size() / union.size();
    }

    /**
     * 将逗号分隔的字符串转为 Set<String>
     */
    public static Set<String> toSet(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        Set<String> set = new HashSet<>();
        for (String s : csv.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }

    /**
     * 综合匹配度评分（0 ~ 100 分）
     *
     * @param userSkills        用户技能标签集合
     * @param targetSkills      目标用户/组队的技能标签集合
     * @param userInterests     用户参赛兴趣标签
     * @param targetInterests   目标用户的参赛兴趣标签
     * @param hasCommonFavorites 是否有共同收藏赛事
     * @return 匹配度分数 (0~100)
     */
    public static int calculateMatchScore(
            Set<String> userSkills,
            Set<String> targetSkills,
            Set<String> userInterests,
            Set<String> targetInterests,
            boolean hasCommonFavorites) {

        // 权重：技能标签 50%，参赛兴趣 30%，共同收藏 20%
        double skillScore = jaccardSimilarity(userSkills, targetSkills);
        double interestScore = jaccardSimilarity(userInterests, targetInterests);
        double favScore = hasCommonFavorites ? 1.0 : 0.0;

        double totalScore = skillScore * 0.5 + interestScore * 0.3 + favScore * 0.2;

        // 转为百分制
        return (int) Math.round(totalScore * 100);
    }

    /**
     * 计算简历与组队需求的匹配度
     *
     * @param resumeSkills   简历中的技能标签
     * @param requiredSkills 组队所需的技能标签
     * @return 匹配度 (0~100)
     */
    public static int calculateResumeMatchScore(Set<String> resumeSkills, Set<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(resumeSkills);
        intersection.retainAll(requiredSkills);

        // match = |R ∩ T| / |T|
        double match = (double) intersection.size() / requiredSkills.size();
        return (int) Math.round(match * 100);
    }
}
