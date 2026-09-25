package com.saidazi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saidazi.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    /**
     * 查询用户是否已对某帖子点赞
     */
    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int countByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}