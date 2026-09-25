package com.saidazi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saidazi.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 检查用户是否已收藏某赛事
     */
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND competition_id = #{competitionId}")
    int countByUserAndCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);
}