package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.dto.TeamApplicationProcessDTO;
import com.saidazi.entity.Team;
import com.saidazi.entity.TeamApplication;
import com.saidazi.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组队接口 -- 接口 5.5：组队模块 + 接口④：组队申请审核
 *
 * 功能：发布组队、浏览组队、组队详情、更新/关闭组队、
 *       申请加入、发布人审核申请（通过/拒绝）、查看申请、我的组队
 */
@Tag(name = "4. 组队模块", description = "发布组队、浏览组队、申请加入、申请审核、我的组队")
@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // ==================== 组队发布与浏览 ====================

    @Operation(summary = "发布组队需求",
            description = "**步骤4：找搭子--发布组队**\n\n" +
                    "字段说明（参见手册表7）：\n" +
                    "- title 标题（必填）\n" +
                    "- needCount 需要人数 1~10（必填）\n" +
                    "- skillsRequired 所需技能标签，逗号分隔（必填）\n" +
                    "- description 项目/赛事简介（必填）\n" +
                    "- contact 联系方式（必填）\n" +
                    "- requireResume 是否要求简历 0/1（必填）\n" +
                    "- remark 备注（选填）\n" +
                    "- competitionId 关联赛事 ID（必填）")
    @PostMapping("/publish")
    public Result<Team> publish(
            @Parameter(description = "发布人用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @RequestBody Team team) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(teamService.publishTeam(userId, team));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "组队列表（多维度筛选）",
            description = "**步骤5：看搭子**\n\n支持按 competitionId / skills / status 筛选。")
    @GetMapping("/list")
    public Result<List<Team>> list(
            @Parameter(description = "赛事 ID")
            @RequestParam(required = false) Long competitionId,
            @Parameter(description = "技能标签（子串匹配）", example = "算法")
            @RequestParam(required = false) String skills,
            @Parameter(description = "状态：0招募中 1已满员 2已关闭（默认招募中）", example = "0")
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        return Result.success(teamService.listTeams(competitionId, skills, status, page, size));
    }

    @Operation(summary = "组队详情", description = "展示组队完整信息、要求简历标识、已申请/需要人数")
    @GetMapping("/{id}")
    public Result<Team> detail(
            @Parameter(description = "组队 ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            return Result.success(teamService.getTeamDetail(id));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "更新组队信息（发布人）")
    @PutMapping("/{id}")
    public Result<Team> update(
            @Parameter(description = "当前用户 ID（发布人）", required = true, example = "1")
            @RequestParam Long userId,
            @PathVariable Long id,
            @RequestBody Team team) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(teamService.updateTeam(userId, id, team));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "关闭/删除组队（发布人）", description = "置为已关闭状态，保留历史数据")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "当前用户 ID（发布人）", required = true, example = "1")
            @RequestParam Long userId,
            @PathVariable Long id) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            teamService.deleteTeam(userId, id);
            return Result.success(null, "已关闭组队");
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "我发布的组队", description = "查看当前用户发布的所有组队，按发布时间倒序")
    @GetMapping("/my-publish")
    public Result<List<Team>> myPublish(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        return Result.success(teamService.getMyPublishedTeams(userId));
    }

    @Operation(summary = "我的申请记录", description = "查看当前用户提交的所有组队申请")
    @GetMapping("/my-apply")
    public Result<List<TeamApplication>> myApply(
            @Parameter(description = "用户 ID", required = true, example = "3")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        return Result.success(teamService.getMyApplications(userId));
    }

    // ==================== 组队申请审核 ====================

    @Operation(summary = "申请加入组队",
            description = "用户申请加入指定的组队。\n" +
                    "- 若组队要求简历，则 resumeId 必填，且简历状态必须为「已完成」\n" +
                    "- 同一用户不可重复申请同一组队\n" +
                    "- 申请时自动计算技能匹配度")
    @PostMapping("/apply")
    public Result<TeamApplication> applyTeam(
            @Parameter(description = "申请人用户 ID", required = true, example = "3")
            @RequestParam Long userId,
            @Parameter(description = "目标组队 ID", required = true, example = "1")
            @RequestParam Long teamId,
            @Parameter(description = "简历 ID（若组队要求简历则必填）", example = "1")
            @RequestParam(required = false) Long resumeId) {

        if (userId == null) return Result.badRequest("用户ID不能为空");
        if (teamId == null) return Result.badRequest("组队ID不能为空");

        try {
            TeamApplication application = teamService.applyTeam(userId, teamId, resumeId);
            return Result.success(application);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "处理组队申请（通过/拒绝）",
            description = "组队发布人对申请进行审核。\n" +
                    "- action=1 表示「通过」，action=2 表示「拒绝」\n" +
                    "- 只有组队发布人（队长）才能操作\n" +
                    "- 已处理的申请不可重复操作\n" +
                    "- 通过时会自动检查是否满员")
    @PutMapping("/application/process")
    public Result<Void> processApplication(
            @Parameter(description = "当前操作用户 ID（必须是组队发布人）", required = true, example = "1")
            @RequestParam Long userId,
            @Valid @RequestBody TeamApplicationProcessDTO dto) {

        if (userId == null) return Result.badRequest("用户ID不能为空");

        try {
            teamService.processApplication(userId, dto.getApplicationId(), dto.getAction());
            String actionText = dto.getAction() == 1 ? "通过" : "拒绝";
            return Result.success(null, actionText + "成功");
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "获取收到的组队申请列表",
            description = "查询当前用户发布的所有组队收到的申请列表，按申请时间倒序排列。" +
                    "（即「我的 -> 收到的简历」入口）")
    @GetMapping("/applications/received")
    public Result<List<TeamApplication>> getReceivedApplications(
            @Parameter(description = "用户 ID（组队发布人）", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        List<TeamApplication> applications = teamService.getReceivedApplications(userId);
        return Result.success(applications);
    }
}
