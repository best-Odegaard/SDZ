package com.ruoyi.web.controller.saidazi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.mapper.AchievementMapper;
import com.ruoyi.system.mapper.CompetitionMapper;
import com.ruoyi.system.mapper.DashboardMapper;

/**
 * 数据概览
 *
 * @author saidazi
 */
@RestController
@RequestMapping("/saidazi/dashboard")
public class DashboardController extends BaseController
{
    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    /**
     * 管理端首页数据概览
     */
    @PreAuthorize("@ss.hasPermi('saidazi:dashboard:view')")
    @GetMapping
    public AjaxResult index()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("competitionCount", competitionMapper.countCompetition());
        data.put("achievementCount", achievementMapper.countAchievement());
        data.put("userCount", dashboardMapper.countUser());
        data.put("teamCount", dashboardMapper.countTeam());
        data.put("postCount", dashboardMapper.countPost());
        List<Map<String, Object>> categoryStat = dashboardMapper.countCompetitionByCategory();
        data.put("categoryStat", categoryStat);
        return success(data);
    }
}