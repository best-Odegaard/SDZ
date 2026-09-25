package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.entity.Competition;
import com.saidazi.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏接口 -- 接口 5.3
 *
 * 收藏赛事、取消收藏、我的收藏列表、检查是否已收藏
 */
@Tag(name = "7. 收藏模块", description = "赛事收藏：添加、取消、我的收藏列表、是否已收藏检查")
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Operation(summary = "添加收藏",
            description = "**步骤3：收藏赛事**\n\n收藏赛事，每个用户对同一赛事仅可收藏一次。")
    @PostMapping("/add")
    public Result<Void> add(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "赛事 ID", required = true, example = "1")
            @RequestParam Long competitionId) {
        if (userId == null || competitionId == null) return Result.badRequest("参数不完整");
        try {
            favoriteService.addFavorite(userId, competitionId);
            return Result.success(null, "收藏成功");
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/remove")
    public Result<Void> remove(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "赛事 ID", required = true, example = "1")
            @RequestParam Long competitionId) {
        if (userId == null || competitionId == null) return Result.badRequest("参数不完整");
        try {
            favoriteService.removeFavorite(userId, competitionId);
            return Result.success(null, "已取消收藏");
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "我的收藏列表", description = "返回当前用户收藏的所有赛事信息，按收藏时间倒序")
    @GetMapping("/list")
    public Result<List<Competition>> list(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        return Result.success(favoriteService.listFavorites(userId));
    }

    @Operation(summary = "检查是否已收藏", description = "前端用于赛事详情页收藏按钮状态展示")
    @GetMapping("/check")
    public Result<Boolean> check(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "赛事 ID", required = true, example = "1")
            @RequestParam Long competitionId) {
        if (userId == null || competitionId == null) return Result.badRequest("参数不完整");
        return Result.success(favoriteService.checkFavorite(userId, competitionId));
    }
}