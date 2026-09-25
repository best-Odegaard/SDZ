package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.entity.Resume;
import com.saidazi.entity.TeamApplication;
import com.saidazi.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 简历接口 -- 接口 5.7
 *
 * 简历管理：编辑、预览、上传 PDF 附件、查看他人简历、投递历史
 */
@Slf4j
@Tag(name = "8. 简历模块", description = "简历编辑、预览、PDF 附件上传、投递历史")
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Operation(summary = "获取我的简历",
            description = "查询当前用户的简历。若无简历返回 null，前端引导进入「完善简历」流程。")
    @GetMapping("/my")
    public Result<Resume> getMyResume(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        return Result.success(resumeService.getMyResume(userId));
    }

    @Operation(summary = "保存/更新简历",
            description = "**步骤5·情况B：完善简历**\n\n" +
                    "字段说明（手册表8）：\n" +
                    "- name 姓名（必填）\n" +
                    "- collegeMajor 学院/专业（必填，如：信息技术学院·计科）\n" +
                    "- grade 年级（必填）\n" +
                    "- skills 技能标签（必填，逗号分隔）\n" +
                    "- introduction 个人简介（必填）\n" +
                    "- experience 参赛经历（选填）\n" +
                    "- attachmentUrl 附件简历 PDF URL（选填，由 /upload 接口返回）\n" +
                    "- status 状态：0草稿 1已完成（已完成才可用于申请组队）\n\n" +
                    "每个用户仅一份简历，重复保存为更新。")
    @PostMapping("/save")
    public Result<Resume> saveResume(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @RequestBody Resume resume) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(resumeService.saveResume(userId, resume));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "查看他人简历（组队场景）",
            description = "组队发布人查看申请人简历：传入申请人 userId 返回简历详情。")
    @GetMapping("/{userId}")
    public Result<Resume> getResumeByUserId(
            @Parameter(description = "目标用户 ID（简历所属人）", required = true, example = "1")
            @PathVariable Long userId) {
        try {
            return Result.success(resumeService.getResumeByUserId(userId));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "上传附件简历 PDF",
            description = "上传 PDF 格式简历文件，单文件 ≤ 5MB。\n" +
                    "返回可访问的 URL，并自动回填到用户简历的 attachmentUrl 字段。")
    @PostMapping("/upload")
    public Result<String> uploadAttachment(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "PDF 文件", required = true)
            @RequestParam("file") MultipartFile file) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        if (file.isEmpty()) return Result.badRequest("上传文件不能为空");
        try {
            String url = resumeService.uploadAttachment(userId, file.getBytes(), file.getOriginalFilename());
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return Result.error("文件读取失败");
        }
    }

    @Operation(summary = "简历投递历史",
            description = "查看该简历被发送到的所有组队申请记录（手册表10·使用记录）。")
    @GetMapping("/sent-history")
    public Result<List<TeamApplication>> sentHistory(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        return Result.success(resumeService.getSentHistory(userId));
    }
}