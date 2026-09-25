package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Competition;

/**
 * 赛事 数据层
 *
 * @author saidazi
 */
public interface CompetitionMapper
{
    /**
     * 查询赛事
     *
     * @param id 赛事ID
     * @return 赛事
     */
    public Competition selectCompetitionById(Long id);

    /**
     * 查询赛事列表
     *
     * @param competition 赛事
     * @return 赛事集合
     */
    public List<Competition> selectCompetitionList(Competition competition);

    /**
     * 新增赛事
     *
     * @param competition 赛事
     * @return 结果
     */
    public int insertCompetition(Competition competition);

    /**
     * 修改赛事
     *
     * @param competition 赛事
     * @return 结果
     */
    public int updateCompetition(Competition competition);

    /**
     * 删除赛事
     *
     * @param id 赛事ID
     * @return 结果
     */
    public int deleteCompetitionById(Long id);

    /**
     * 批量删除赛事
     *
     * @param ids 需要删除的赛事ID
     * @return 结果
     */
    public int deleteCompetitionByIds(Long[] ids);

    /**
     * 赛事总数
     */
    public int countCompetition();
}