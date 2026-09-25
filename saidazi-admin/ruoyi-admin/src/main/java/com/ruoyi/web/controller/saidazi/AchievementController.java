package com.ruoyi.web.controller.saidazi;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.Achievement;
import com.ruoyi.system.service.IAchievementService;

/**
 * 往届成果 信息操作处理
 *
 * @author saidazi
 */
@RestController
@RequestMapping("/saidazi/achievement")
public class AchievementController extends BaseController
{
    @Autowired
    private IAchievementService achievementService;

    /**
     * 获取往届成果列表
     */
    @PreAuthorize("@ss.hasPermi('saidazi:achievement:list')")
    @GetMapping("/list")
    public TableDataInfo list(Achievement achievement)
    {
        startPage();
        List<Achievement> list = achievementService.selectAchievementList(achievement);
        return getDataTable(list);
    }

    /**
     * 导出往届成果列表
     */
    @PreAuthorize("@ss.hasPermi('saidazi:achievement:export')")
    @Log(title = "往届成果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Achievement achievement)
    {
        List<Achievement> list = achievementService.selectAchievementList(achievement);
        ExcelUtil<Achievement> util = new ExcelUtil<Achievement>(Achievement.class);
        util.exportExcel(response, list, "往届成果数据");
    }

    /**
     * 根据ID获取往届成果详细信息
     */
    @PreAuthorize("@ss.hasPermi('saidazi:achievement:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(achievementService.selectAchievementById(id));
    }

    /**
     * 新增往届成果
     */
    @PreAuthorize("@ss.hasPermi('saidazi:achievement:add')")
    @Log(title = "往届成果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Achievement achievement)
    {
        achievement.setCreateBy(getUsername());
        return toAjax(achievementService.insertAchievement(achievement));
    }

    /**
     * 修改往届成果
     */
    @PreAuthorize("@ss.hasPermi('saidazi:achievement:edit')")
    @Log(title = "往届成果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Achievement achievement)
    {
        achievement.setUpdateBy(getUsername());
        return toAjax(achievementService.updateAchievement(achievement));
    }

    /**
     * 删除往届成果
     */
    @PreAuthorize("@ss.hasPermi('saidazi:achievement:remove')")
    @Log(title = "往届成果", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(achievementService.deleteAchievementByIds(ids));
    }
}