package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.entity.User;
import com.saidazi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户接口 -- 接口 5.1
 *
 * 微信登录（code 换 session）、获取/更新用户信息
 */
@Tag(name = "5. 用户模块", description = "微信登录、用户信息获取与更新")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "微信登录",
            description = "小程序调用 wx.login() 获取 code 后请求本接口。\n" +
                    "后端用 code 换 openId（开发环境以 code 作为 openId），\n" +
                    "新用户自动注册，返回 JWT token 与用户信息。\n\n" +
                    "可携带 nickname、avatarUrl 完善用户资料。")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "wx.login() 返回的 code", required = true, example = "wx_openid_001")
            @RequestParam String code,
            @Parameter(description = "微信昵称")
            @RequestParam(required = false) String nickname,
            @Parameter(description = "微信头像 URL")
            @RequestParam(required = false) String avatarUrl) {

        if (code == null || code.isBlank()) {
            return Result.badRequest("code 不能为空");
        }
        try {
            Map<String, Object> data = userService.login(code, nickname, avatarUrl);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户 ID 查询用户资料")
    @GetMapping("/info")
    public Result<User> getUserInfo(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(userService.getUserInfo(userId));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息",
            description = "更新学院、专业、年级、技能标签、参赛兴趣等资料。\n" +
                    "技能标签越丰富，智能匹配精度越高。")
    @PutMapping("/info")
    public Result<User> updateUserInfo(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @RequestBody User user) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(userService.updateUserInfo(userId, user));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}