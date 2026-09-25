package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saidazi.common.PageResult;
import com.saidazi.entity.Comment;
import com.saidazi.entity.Post;
import com.saidazi.entity.PostLike;
import com.saidazi.mapper.CommentMapper;
import com.saidazi.mapper.PostLikeMapper;
import com.saidazi.mapper.PostMapper;
import com.saidazi.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 社区服务实现 -- 接口 5.8：社区模块
 *
 * 帖子发布、分类浏览、点赞、单层评论、帖子管理
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private CommentMapper commentMapper;

    private static final int MAX_IMAGES = 3;

    @Override
    public PageResult<Post> listPosts(Integer category, Long page, Long size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1); // 仅正常帖子
        if (category != null) {
            wrapper.eq(Post::getCategory, category);
        }
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> p = new Page<>(page == null ? 1 : page, size == null ? 20 : size);
        Page<Post> result = postMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Post getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new IllegalArgumentException("帖子不存在或已下架");
        }
        return post;
    }

    @Override
    @Transactional
    public Post publishPost(Long userId, Post post) {
        // 校验（手册表9）
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            throw new IllegalArgumentException("帖子标题不能为空");
        }
        if (post.getCategory() == null || post.getCategory() < 1 || post.getCategory() > 4) {
            throw new IllegalArgumentException("请选择帖子分类（1求助答疑 2备赛经验 3资料分享 4赛事闲聊）");
        }
        if (post.getContent() == null || post.getContent().isBlank()) {
            throw new IllegalArgumentException("正文内容不能为空");
        }
        // 图片数量校验 ≤ 3 张
        if (post.getImages() != null && !post.getImages().isBlank()) {
            long imageCount = Arrays.stream(post.getImages().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .count();
            if (imageCount > MAX_IMAGES) {
                throw new IllegalArgumentException("图片数量不能超过 " + MAX_IMAGES + " 张");
            }
        }

        post.setUserId(userId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        if (post.getAllowComment() == null) {
            post.setAllowComment(1);
        }
        post.setStatus(1); // 正常
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());

        postMapper.insert(post);
        log.info("发布帖子: userId={}, postId={}, category={}", userId, post.getId(), post.getCategory());
        return post;
    }

    @Override
    @Transactional
    public Post updatePost(Long userId, Long postId, Post post) {
        Post existing = postMapper.selectById(postId);
        if (existing == null || existing.getStatus() == 0) {
            throw new IllegalArgumentException("帖子不存在或已下架");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权编辑他人帖子");
        }
        if (post.getTitle() != null) existing.setTitle(post.getTitle());
        if (post.getCategory() != null) existing.setCategory(post.getCategory());
        if (post.getContent() != null) existing.setContent(post.getContent());
        if (post.getImages() != null) {
            long imageCount = Arrays.stream(post.getImages().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).count();
            if (imageCount > MAX_IMAGES) {
                throw new IllegalArgumentException("图片数量不能超过 " + MAX_IMAGES + " 张");
            }
            existing.setImages(post.getImages());
        }
        existing.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(existing);
        log.info("编辑帖子: postId={}, userId={}", postId, userId);
        return existing;
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post existing = postMapper.selectById(postId);
        if (existing == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除他人帖子");
        }
        // 置为下架（手册：删除后同步清除全部互动数据）
        existing.setStatus(0);
        existing.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(existing);

        // 同步清空该帖子的点赞和评论
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, postId));
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        log.info("删除帖子（下架）: postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional
    public Post toggleComment(Long userId, Long postId) {
        Post existing = postMapper.selectById(postId);
        if (existing == null || existing.getStatus() == 0) {
            throw new IllegalArgumentException("帖子不存在或已下架");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作他人帖子");
        }
        existing.setAllowComment(existing.getAllowComment() == 1 ? 0 : 1);
        existing.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(existing);
        log.info("切换评论权限: postId={}, allowComment={}", postId, existing.getAllowComment());
        return existing;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new IllegalArgumentException("帖子不存在或已下架");
        }
        // 是否已点赞
        if (postLikeMapper.countByPostAndUser(postId, userId) > 0) {
            // 取消点赞
            postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getPostId, postId)
                    .eq(PostLike::getUserId, userId));
            postMapper.decrLikeCount(postId);
            log.info("取消点赞: postId={}, userId={}", postId, userId);
            return false;
        } else {
            // 点赞
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            postLikeMapper.insert(like);
            postMapper.incrLikeCount(postId);
            log.info("点赞: postId={}, userId={}", postId, userId);
            return true;
        }
    }

    @Override
    @Transactional
    public Comment addComment(Long userId, Long postId, String content) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new IllegalArgumentException("帖子不存在或已下架");
        }
        if (post.getAllowComment() == 0) {
            throw new IllegalArgumentException("该帖子已关闭评论");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        // 帖子评论数 +1
        postMapper.incrCommentCount(postId);
        log.info("发表评论: postId={}, userId={}", postId, userId);
        return comment;
    }

    @Override
    public List<Comment> listComments(Long postId) {
        return commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .orderByAsc(Comment::getCreateTime)
        );
    }

    @Override
    public PageResult<Post> getMyPosts(Long userId, Long page, Long size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime);
        Page<Post> p = new Page<>(page == null ? 1 : page, size == null ? 20 : size);
        Page<Post> result = postMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
}