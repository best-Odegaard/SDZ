package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.Competition;

/**
 * 赛事 业务层
 *
 * @author saidazi
 */
public interface ICompetitionService
{
    /**
     * 查询赛事
     */
    public Competition selectCompetitionById(Long id);

    /**
     * 查询赛事列表
     */
    public List<Competition> selectCompetitionList(Competition competition);

    /**
     * 新增赛事
     */
    public int insertCompetition(Competition competition);

    /**
     * 修改赛事
     */
    public int updateCompetition(Competition competition);

    /**
     * 删除赛事
     */
    public int deleteCompetitionByIds(Long[] ids);

    /**
     * 赛事总数
     */
    public int countCompetition();
}