package com.saidazi.dto;

import lombok.Data;

/**
 * 管理员数据概览 DTO
 */
@Data
public class DashboardDTO {
    /** 赛事总数 */
    private long competitionCount;
    /** 用户总数 */
    private long userCount;
    /** 组队总数 */
    private long teamCount;
    /** 帖子总数 */
    private long postCount;
    /** 收藏总数 */
    private long favoriteCount;
    /** 提醒总数 */
    private long reminderCount;
}