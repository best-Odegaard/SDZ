package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.entity.Reminder;
import com.saidazi.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒接口（供小程序调用）
 */
@Slf4j
@Tag(name = "1. 赛事提醒", description = "报名提醒的设置、取消、查询。定时推送由后端 @Scheduled 每分钟自动扫描触发")
@RestController
@RequestMapping("/api/reminder")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Operation(summary = "设置提醒",
            description = "为指定赛事设置报名提醒，可选择提醒时机。\n\n" +
                    "remindType 说明：\n" +
                    "- 1 = 报名开始前提醒\n" +
                    "- 2 = 截止前7天提醒\n" +
                    "- 3 = 截止前3天提醒\n" +
                    "- 4 = 截止前1天提醒\n" +
                    "- 5 = 自定义时间")
    @PostMapping("/add")
    public Result<Reminder> addReminder(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "赛事 ID", required = true, example = "1")
            @RequestParam Long competitionId,
            @Parameter(description = "提醒触发时间 (yyyy-MM-dd HH:mm:ss)", required = true, example = "2026-07-28 10:00:00")
            @RequestParam String remindTime,
            @Parameter(description = "提醒类型 (1~5)", required = true, example = "1")
            @RequestParam Integer remindType) {

        if (userId == null || competitionId == null) return Result.badRequest("参数不完整");

        try {
            LocalDateTime time = LocalDateTime.parse(remindTime.replace(" ", "T"));
            Reminder reminder = reminderService.addReminder(userId, competitionId, time, remindType);
            return Result.success(reminder);
        } catch (Exception e) {
            return Result.badRequest("设置提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消提醒",
            description = "根据提醒 ID 取消已设置的提醒（仅限本人操作）")
    @DeleteMapping("/{id}")
    public Result<Void> cancelReminder(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "提醒记录 ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            reminderService.cancelReminder(userId, id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "查询我的提醒列表",
            description = "获取当前用户的所有有效提醒记录（已取消的不展示），按提醒时间升序排列")
    @GetMapping("/list")
    public Result<List<Reminder>> getReminders(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        List<Reminder> list = reminderService.getUserReminders(userId);
        return Result.success(list);
    }
}
