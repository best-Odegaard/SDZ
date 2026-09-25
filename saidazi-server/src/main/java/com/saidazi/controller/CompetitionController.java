package com.saidazi.controller;

import com.saidazi.common.PageResult;
import com.saidazi.common.Result;
import com.saidazi.dto.CalendarEventDTO;
import com.saidazi.entity.Competition;
import com.saidazi.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 赛事接口 -- 接口 5.2
 *
 * 双轨赛事聚合（校内/全国 Tab）、分类筛选、关键词搜索、赛事日历
 */
@Tag(name = "6. 赛事模块", description = "校内/全国双 Tab 赛事浏览、分类筛选、搜索、日历视图")
@RestController
@RequestMapping("/api/competition")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    @Operation(summary = "赛事列表（双 Tab + 分类 + 报名状态筛选）",
            description = "**步骤1：赛事浏览**\n\n" +
                    "- type=0 校内 Tab，type=1 全国 Tab\n" +
                    "- category：学科竞赛/创新创业/体育竞技/文艺活动/其他\n" +
                    "- regStatus：1报名中 2未开始 3已截止\n" +
                    "- 返回分页数据，按报名开始时间倒序\n\n" +
                    "报名状态为实时计算，不依赖赛事 status 字段")
    @GetMapping("/list")
    public Result<PageResult<Competition>> list(
            @Parameter(description = "赛事类型：0校内 1全国", example = "0")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "分类：学科竞赛/创新创业/体育竞技/文艺活动/其他", example = "学科竞赛")
            @RequestParam(required = false) String category,
            @Parameter(description = "报名状态筛选：1报名中 2未开始 3已截止", example = "1")
            @RequestParam(required = false) Integer regStatus,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页条数", example = "20")
            @RequestParam(defaultValue = "20") Long size) {
        return Result.success(competitionService.listCompetitions(type, category, regStatus, page, size));
    }

    @Operation(summary = "赛事详情",
            description = "**步骤2：查看报名信息**\n\n" +
                    "返回赛事完整信息，包括 reg_info（报名方式文字说明，含网址则长按复制）。\n" +
                    "前端可调用收藏/提醒/找搭子/看搭子接口。")
    @GetMapping("/{id}")
    public Result<Competition> detail(
            @Parameter(description = "赛事 ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            return Result.success(competitionService.getCompetitionDetail(id));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "赛事搜索", description = "按关键词搜索赛事标题/主办方/简介/分类")
    @GetMapping("/search")
    public Result<PageResult<Competition>> search(
            @Parameter(description = "关键词", required = true, example = "ACM")
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        return Result.success(competitionService.searchCompetitions(keyword, page, size));
    }

    @Operation(summary = "赛事日历数据",
            description = "**步骤7：赛事日历**\n\n" +
                    "按年月返回赛事时间节点（报名开始/报名截止/比赛日）。\n" +
                    "- 蓝色(1) 报名开始日期\n" +
                    "- 红色(2) 报名截止日期\n" +
                    "- 绿色(3) 比赛日期")
    @GetMapping("/calendar")
    public Result<List<CalendarEventDTO>> calendar(
            @Parameter(description = "年", example = "2026")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "月 (1-12)", example = "7")
            @RequestParam(required = false) Integer month) {
        return Result.success(competitionService.getCalendarEvents(year, month));
    }
}