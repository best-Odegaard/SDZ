package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Achievement;

/**
 * 往届成果 数据层
 *
 * @author saidazi
 */
public interface AchievementMapper
{
    /**
     * 查询往届成果
     */
    public Achievement selectAchievementById(Long id);

    /**
     * 查询往届成果列表（联表 competition 取标题）
     */
    public List<Achievement> selectAchievementList(Achievement achievement);

    /**
     * 新增往届成果
     */
    public int insertAchievement(Achievement achievement);

    /**
     * 修改往届成果
     */
    public int updateAchievement(Achievement achievement);

    /**
     * 删除往届成果
     */
    public int deleteAchievementById(Long id);

    /**
     * 批量删除往届成果
     */
    public int deleteAchievementByIds(Long[] ids);

    /**
     * 往届成果总数
     */
    public int countAchievement();
}