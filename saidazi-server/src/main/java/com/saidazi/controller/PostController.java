package com.saidazi.controller;

import com.saidazi.common.PageResult;
import com.saidazi.common.Result;
import com.saidazi.entity.Comment;
import com.saidazi.entity.Post;
import com.saidazi.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区接口 -- 接口 5.8
 *
 * 帖子发布、分类浏览、点赞、评论、帖子管理
 */
@Tag(name = "9. 社区模块", description = "校园竞赛发帖社区：发帖、分类浏览、点赞、评论、帖子管理")
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Operation(summary = "帖子列表（分类筛选）",
            description = "**步骤8.2：社区浏览**\n\n" +
                    "category：1求助答疑 2备赛经验 3资料分享 4赛事闲聊\n" +
                    "category 不传则返回全量帖子。")
    @GetMapping("/list")
    public Result<PageResult<Post>> list(
            @Parameter(description = "分类：1求助答疑 2备赛经验 3资料分享 4赛事闲聊", example = "2")
            @RequestParam(required = false) Integer category,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        return Result.success(postService.listPosts(category, page, size));
    }

    @Operation(summary = "帖子详情",
            description = "返回帖子正文、图片、点赞/评论数。")
    @GetMapping("/{id}")
    public Result<Post> detail(
            @Parameter(description = "帖子 ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            return Result.success(postService.getPostDetail(id));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "发布帖子",
            description = "**步骤8.3：发布新帖子**\n\n" +
                    "字段（手册表9）：\n" +
                    "- title 标题（必填）\n" +
                    "- category 分类 1~4（必填）\n" +
                    "- content 正文内容（必填）\n" +
                    "- images 图片 URL，逗号分隔，最多 3 张（选填，由 /api/file/upload 返回）\n" +
                    "- allowComment 是否允许评论 0/1（必填，默认 1）\n\n" +
                    "发布后同步展示至社区列表、对应分类列表、我的帖子。")
    @PostMapping("/publish")
    public Result<Post> publish(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @RequestBody Post post) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(postService.publishPost(userId, post));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "编辑帖子（发布人）",
            description = "**步骤8.5：我的帖子管理**\n\n编辑文字与图片内容，仅发布人可操作。")
    @PutMapping("/{id}")
    public Result<Post> update(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @PathVariable Long id,
            @RequestBody Post post) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(postService.updatePost(userId, id, post));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "删除帖子（发布人）",
            description = "帖子删除后同步清空全部点赞、评论数据。")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @PathVariable Long id) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            postService.deletePost(userId, id);
            return Result.success(null, "已删除帖子");
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "开关评论（发布人）",
            description = "切换 allowComment：1→0 或 0→1。关闭后无法发表评论。")
    @PutMapping("/{id}/comment-toggle")
    public Result<Post> toggleComment(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @PathVariable Long id) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(postService.toggleComment(userId, id));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "点赞 / 取消点赞",
            description = "**步骤8.4：基础互动**\n\n" +
                    "toggle 模式：未点赞则点赞，已点赞则取消。返回 true=已点赞 false=已取消。")
    @PostMapping("/{id}/like")
    public Result<Boolean> like(
            @Parameter(description = "用户 ID", required = true, example = "2")
            @RequestParam Long userId,
            @PathVariable Long id) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(postService.toggleLike(userId, id));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "发表评论",
            description = "单层文字评论，无楼层回复、@用户功能。若帖子已关闭评论则禁止。")
    @PostMapping("/{id}/comment")
    public Result<Comment> comment(
            @Parameter(description = "用户 ID", required = true, example = "2")
            @RequestParam Long userId,
            @PathVariable Long id,
            @Parameter(description = "评论内容", required = true)
            @RequestParam String content) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        try {
            return Result.success(postService.addComment(userId, id, content));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "获取评论列表",
            description = "按评论时间升序返回帖子下所有评论。")
    @GetMapping("/{id}/comments")
    public Result<List<Comment>> comments(
            @Parameter(description = "帖子 ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(postService.listComments(id));
    }

    @Operation(summary = "我的帖子列表",
            description = "查看当前用户发布的所有帖子（含已下架），按发布时间倒序。")
    @GetMapping("/my")
    public Result<PageResult<Post>> myPosts(
            @Parameter(description = "用户 ID", required = true, example = "1")
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        if (userId == null) return Result.badRequest("用户ID不能为空");
        return Result.success(postService.getMyPosts(userId, page, size));
    }
}