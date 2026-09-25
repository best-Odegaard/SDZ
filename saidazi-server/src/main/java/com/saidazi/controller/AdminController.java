package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.dto.DashboardDTO;
import com.saidazi.entity.Admin;
import com.saidazi.entity.Competition;
import com.saidazi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员后台接口 -- 设计方案第8章
 *
 * 账号密码登录、数据概览、校内赛事 CRUD
 */
@Tag(name = "10. 管理员后台", description = "管理员登录、数据概览、校内赛事管理（新增/编辑/删除/上下架）")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Operation(summary = "管理员登录",
            description = "账号密码登录（BCrypt 校验）。默认账号 admin / admin123。\n" +
                    "返回 JWT token 与管理员信息。")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户名", required = true, example = "admin")
            @RequestParam String username,
            @Parameter(description = "密码", required = true, example = "admin123")
            @RequestParam String password) {
        try {
            return Result.success(adminService.login(username, password));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "登出",
            description = "当前阶段为无状态 JWT，登出由前端清除本地 token 即可。")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null, "已登出");
    }

    @Operation(summary = "数据概览",
            description = "返回赛事数、用户数、组队数、帖子数、收藏数、提醒数等基本统计。")
    @GetMapping("/dashboard")
    public Result<DashboardDTO> dashboard() {
        return Result.success(adminService.dashboard());
    }

    // ==================== 赛事管理 CRUD ====================

    @Operation(summary = "全部赛事列表（管理员视角）",
            description = "返回全部赛事（含草稿/已结束），按创建时间倒序。")
    @GetMapping("/competition/list")
    public Result<List<Competition>> listCompetitions() {
        return Result.success(adminService.listAllCompetitions());
    }

    @Operation(summary = "新增赛事",
            description = "管理员录入校内/全国赛事。\n" +
                    "校内赛事 type=0，需填 college_tag 学院标签。\n" +
                    "报名方式说明 reg_info（含网址则直接写在文字中，支持长按复制）。")
    @PostMapping("/competition")
    public Result<Competition> createCompetition(@RequestBody Competition competition) {
        try {
            return Result.success(adminService.createCompetition(competition));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "编辑赛事")
    @PutMapping("/competition/{id}")
    public Result<Competition> updateCompetition(
            @PathVariable Long id,
            @RequestBody Competition competition) {
        try {
            return Result.success(adminService.updateCompetition(id, competition));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "删除赛事")
    @DeleteMapping("/competition/{id}")
    public Result<Void> deleteCompetition(@PathVariable Long id) {
        try {
            adminService.deleteCompetition(id);
            return Result.success(null, "已删除赛事");
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}