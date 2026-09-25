package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;

/**
 * 赛搭子 数据概览 数据层
 * 直接对业务表执行 count 查询，无需实体
 *
 * @author saidazi
 */
public interface DashboardMapper
{
    /**
     * 用户总数
     */
    public int countUser();

    /**
     * 组队总数
     */
    public int countTeam();

    /**
     * 帖子总数
     */
    public int countPost();

    /**
     * 各分类赛事数量
     */
    public List<Map<String, Object>> countCompetitionByCategory();
}