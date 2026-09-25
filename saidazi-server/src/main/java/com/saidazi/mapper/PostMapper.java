package com.saidazi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saidazi.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 点赞数 +1
     */
    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{postId}")
    int incrLikeCount(@Param("postId") Long postId);

    /**
     * 点赞数 -1（不低于 0）
     */
    @Update("UPDATE post SET like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END WHERE id = #{postId}")
    int decrLikeCount(@Param("postId") Long postId);

    /**
     * 评论数 +1
     */
    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{postId}")
    int incrCommentCount(@Param("postId") Long postId);

    /**
     * 评论数 -1（不低于 0）
     */
    @Update("UPDATE post SET comment_count = CASE WHEN comment_count > 0 THEN comment_count - 1 ELSE 0 END WHERE id = #{postId}")
    int decrCommentCount(@Param("postId") Long postId);
}