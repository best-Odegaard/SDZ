package com.saidazi.dto;

import lombok.Data;

/**
 * 智能匹配结果 DTO
 */
@Data
public class MatchResultDTO {
    /** 匹配目标 ID（组队ID 或 用户ID） */
    private Long targetId;

    /** 匹配类型：team=组队, user=队友 */
    private String matchType;

    /** 匹配度分数 0~100 */
    private Integer matchScore;

    /** 目标标题/名称 */
    private String title;

    /** 匹配到的共同技能标签（逗号分隔） */
    private String commonSkills;

    /** 需要人数（组队类型） */
    private Integer needCount;

    /** 当前已通过申请数（组队类型） */
    private Integer approvedCount;

    /** 状态 */
    private Integer status;
}
