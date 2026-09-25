package com.saidazi.service;

import com.saidazi.common.PageResult;
import com.saidazi.entity.Comment;
import com.saidazi.entity.Post;

import java.util.List;

/**
 * 社区服务
 */
public interface PostService {

    /**
     * 帖子列表（分类筛选 + 分页）
     */
    PageResult<Post> listPosts(Integer category, Long page, Long size);

    /**
     * 帖子详情
     */
    Post getPostDetail(Long postId);

    /**
     * 发布帖子（图片 ≤ 3 张校验）
     */
    Post publishPost(Long userId, Post post);

    /**
     * 编辑帖子（仅发布人）
     */
    Post updatePost(Long userId, Long postId, Post post);

    /**
     * 删除帖子（仅发布人，同步清空点赞评论）
     */
    void deletePost(Long userId, Long postId);

    /**
     * 开关评论（仅发布人）
     */
    Post toggleComment(Long userId, Long postId);

    /**
     * 点赞 / 取消点赞（toggle）
     * @return true=点赞成功 false=取消点赞
     */
    boolean toggleLike(Long userId, Long postId);

    /**
     * 发表评论
     */
    Comment addComment(Long userId, Long postId, String content);

    /**
     * 获取评论列表
     */
    List<Comment> listComments(Long postId);

    /**
     * 我的帖子
     */
    PageResult<Post> getMyPosts(Long userId, Long page, Long size);
}