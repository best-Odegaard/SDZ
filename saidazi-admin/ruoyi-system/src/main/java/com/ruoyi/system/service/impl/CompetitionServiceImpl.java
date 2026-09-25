package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.Competition;
import com.ruoyi.system.mapper.CompetitionMapper;
import com.ruoyi.system.service.ICompetitionService;

/**
 * 赛事 服务层实现
 *
 * @author saidazi
 */
@Service
public class CompetitionServiceImpl implements ICompetitionService
{
    @Autowired
    private CompetitionMapper competitionMapper;

    /**
     * 查询赛事
     */
    @Override
    public Competition selectCompetitionById(Long id)
    {
        return competitionMapper.selectCompetitionById(id);
    }

    /**
     * 查询赛事列表
     */
    @Override
    public List<Competition> selectCompetitionList(Competition competition)
    {
        return competitionMapper.selectCompetitionList(competition);
    }

    /**
     * 新增赛事
     */
    @Override
    public int insertCompetition(Competition competition)
    {
        return competitionMapper.insertCompetition(competition);
    }

    /**
     * 修改赛事
     */
    @Override
    public int updateCompetition(Competition competition)
    {
        return competitionMapper.updateCompetition(competition);
    }

    /**
     * 删除赛事
     */
    @Override
    public int deleteCompetitionByIds(Long[] ids)
    {
        return competitionMapper.deleteCompetitionByIds(ids);
    }

    /**
     * 赛事总数
     */
    @Override
    public int countCompetition()
    {
        return competitionMapper.countCompetition();
    }
}