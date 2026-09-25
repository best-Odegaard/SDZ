package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.Achievement;

/**
 * 往届成果 业务层
 *
 * @author saidazi
 */
public interface IAchievementService
{
    public Achievement selectAchievementById(Long id);

    public List<Achievement> selectAchievementList(Achievement achievement);

    public int insertAchievement(Achievement achievement);

    public int updateAchievement(Achievement achievement);

    public int deleteAchievementByIds(Long[] ids);

    public int countAchievement();
}