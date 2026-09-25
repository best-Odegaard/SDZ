package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.Achievement;
import com.ruoyi.system.mapper.AchievementMapper;
import com.ruoyi.system.service.IAchievementService;

/**
 * 往届成果 服务层实现
 *
 * @author saidazi
 */
@Service
public class AchievementServiceImpl implements IAchievementService
{
    @Autowired
    private AchievementMapper achievementMapper;

    @Override
    public Achievement selectAchievementById(Long id)
    {
        return achievementMapper.selectAchievementById(id);
    }

    @Override
    public List<Achievement> selectAchievementList(Achievement achievement)
    {
        return achievementMapper.selectAchievementList(achievement);
    }

    @Override
    public int insertAchievement(Achievement achievement)
    {
        return achievementMapper.insertAchievement(achievement);
    }

    @Override
    public int updateAchievement(Achievement achievement)
    {
        return achievementMapper.updateAchievement(achievement);
    }

    @Override
    public int deleteAchievementByIds(Long[] ids)
    {
        return achievementMapper.deleteAchievementByIds(ids);
    }

    @Override
    public int countAchievement()
    {
        return achievementMapper.countAchievement();
    }
}