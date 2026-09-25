package com.saidazi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saidazi.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 更新组队人数状态（当申请数达到需满人数时置为已满员）
     */
    @Update("UPDATE team SET status = 1 WHERE id = #{teamId} " +
            "AND status = 0 AND need_count <= " +
            "(SELECT COUNT(*) FROM team_application WHERE team_id = #{teamId} AND status = 1)")
    int autoMarkFull(@Param("teamId") Long teamId);
}
