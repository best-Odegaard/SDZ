package com.saidazi.service;

import com.saidazi.common.PageResult;
import com.saidazi.dto.CalendarEventDTO;
import com.saidazi.entity.Competition;

import java.util.List;

/**
 * 赛事服务
 */
public interface CompetitionService {

    /**
     * 赛事列表（双 Tab + 分类 + 报名状态筛选，分页）
     *
     * @param type     0校内 1全国（null 全部）
     * @param category 分类（null 全部）
     * @param regStatus 报名状态筛选：1报名中 2未开始 3已截止（null 全部）
     * @param page     页码
     * @param size     每页条数
     */
    PageResult<Competition> listCompetitions(Integer type, String category, Integer regStatus, Long page, Long size);

    /**
     * 赛事详情
     */
    Competition getCompetitionDetail(Long id);

    /**
     * 关键词搜索赛事
     */
    PageResult<Competition> searchCompetitions(String keyword, Long page, Long size);

    /**
     * 赛事日历数据（按年月返回报名开始/截止/比赛三类时间节点）
     */
    List<CalendarEventDTO> getCalendarEvents(Integer year, Integer month);

    /**
     * 自动更新赛事状态：报名已截止 → status=2（已结束）
     */
    int autoUpdateExpiredStatus();
}