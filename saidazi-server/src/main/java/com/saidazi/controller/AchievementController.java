package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.entity.Achievement;
import com.saidazi.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 往届成果接口 -- 创新点9
 *
 * 赛事详情页展示本校往届获奖队伍、参赛作品简介
 */
@Tag(name = "11. 往届成果", description = "校内往届获奖成果库：赛事详情页展示往届获奖队伍、作品简介")
@RestController
@RequestMapping("/api/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Operation(summary = "查询赛事的往届获奖成果",
            description = "赛事详情页调用，返回该校该赛事的往届获奖队伍、获奖等级、作品简介、队员。")
    @GetMapping("/list")
    public Result<List<Achievement>> list(
            @Parameter(description = "赛事 ID", required = true, example = "2")
            @RequestParam Long competitionId) {
        if (competitionId == null) return Result.badRequest("赛事ID不能为空");
        return Result.success(achievementService.listByCompetitionId(competitionId));
    }

    @Operation(summary = "录入往届成果（管理员）",
            description = "管理员为指定赛事录入往届获奖信息。\n" +
                    "字段：competitionId（必填）、teamName 队伍名称（必填）、award 获奖等级、introduction 作品简介、members 队员姓名。")
    @PostMapping
    public Result<Achievement> create(@RequestBody Achievement achievement) {
        try {
            return Result.success(achievementService.createAchievement(achievement));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}