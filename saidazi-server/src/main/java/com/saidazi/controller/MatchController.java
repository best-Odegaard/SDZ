package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.dto.MatchResultDTO;
import com.saidazi.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能匹配接口 —— 接口②
 *
 * 基于 Jaccard 相似度算法，为用户推荐匹配的组队和队友
 */
@Tag(name = "2. 智能匹配", description = "基于 Jaccard 相似度算法，推荐匹配的组队和队友（当前无登录校验，userId 直接传参）")
@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Operation(summary = "智能匹配推荐组队",
            description = "根据当前用户的技能标签，匹配招募中组队的技能需求，按综合匹配度降序排列返回结果。" +
                    "排除用户已申请的组队。\n\n" +
                    "匹配公式：Jaccard(技能)×0.5 + Jaccard(兴趣)×0.3 + 共同收藏×0.2")
    @GetMapping("/teams")
    public Result<List<MatchResultDTO>> recommendTeams(
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) {
            return Result.badRequest("用户ID不能为空");
        }
        List<MatchResultDTO> results = matchService.recommendTeams(userId);
        return Result.success(results);
    }

    @Operation(summary = "智能匹配推荐队友",
            description = "根据技能标签和参赛兴趣，推荐匹配度高的其他用户。" +
                    "自动排除当前用户自身，匹配度低于 20 分的会被过滤。")
    @GetMapping("/mates")
    public Result<List<MatchResultDTO>> recommendMates(
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) {
            return Result.badRequest("用户ID不能为空");
        }
        List<MatchResultDTO> results = matchService.recommendMates(userId);
        return Result.success(results);
    }
}
