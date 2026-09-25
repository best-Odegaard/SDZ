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
import com.ruoyi.system.domain.Competition;
import com.ruoyi.system.service.ICompetitionService;

/**
 * 赛事管理 信息操作处理
 *
 * @author saidazi
 */
@RestController
@RequestMapping("/saidazi/competition")
public class CompetitionController extends BaseController
{
    @Autowired
    private ICompetitionService competitionService;

    /**
     * 获取赛事列表
     */
    @PreAuthorize("@ss.hasPermi('saidazi:competition:list')")
    @GetMapping("/list")
    public TableDataInfo list(Competition competition)
    {
        startPage();
        List<Competition> list = competitionService.selectCompetitionList(competition);
        return getDataTable(list);
    }

    /**
     * 导出赛事列表
     */
    @PreAuthorize("@ss.hasPermi('saidazi:competition:export')")
    @Log(title = "赛事管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Competition competition)
    {
        List<Competition> list = competitionService.selectCompetitionList(competition);
        ExcelUtil<Competition> util = new ExcelUtil<Competition>(Competition.class);
        util.exportExcel(response, list, "赛事数据");
    }

    /**
     * 根据ID获取赛事详细信息
     */
    @PreAuthorize("@ss.hasPermi('saidazi:competition:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(competitionService.selectCompetitionById(id));
    }

    /**
     * 新增赛事
     */
    @PreAuthorize("@ss.hasPermi('saidazi:competition:add')")
    @Log(title = "赛事管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Competition competition)
    {
        competition.setCreateBy(getUsername());
        return toAjax(competitionService.insertCompetition(competition));
    }

    /**
     * 修改赛事
     */
    @PreAuthorize("@ss.hasPermi('saidazi:competition:edit')")
    @Log(title = "赛事管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Competition competition)
    {
        competition.setUpdateBy(getUsername());
        return toAjax(competitionService.updateCompetition(competition));
    }

    /**
     * 删除赛事
     */
    @PreAuthorize("@ss.hasPermi('saidazi:competition:remove')")
    @Log(title = "赛事管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(competitionService.deleteCompetitionByIds(ids));
    }
}