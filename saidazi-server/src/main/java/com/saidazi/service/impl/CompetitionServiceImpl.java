package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saidazi.common.PageResult;
import com.saidazi.dto.CalendarEventDTO;
import com.saidazi.entity.Competition;
import com.saidazi.mapper.CompetitionMapper;
import com.saidazi.service.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 赛事服务实现 -- 接口 5.2：赛事模块
 *
 * 双轨赛事聚合（校内 type=0 / 全国 type=1）、分类筛选、报名状态实时计算、
 * 关键词搜索、日历视图、报名截止自动归档
 */
@Slf4j
@Service
public class CompetitionServiceImpl implements CompetitionService {

    @Autowired
    private CompetitionMapper competitionMapper;

    @Override
    public PageResult<Competition> listCompetitions(Integer type, String category, Integer regStatus, Long page, Long size) {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        // 只展示已发布（status=1）的赛事
        wrapper.eq(Competition::getStatus, 1);
        if (type != null) {
            wrapper.eq(Competition::getType, type);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(Competition::getCategory, category);
        }
        wrapper.orderByDesc(Competition::getRegStartTime);

        // 取出分页数据
        Page<Competition> p = new Page<>(page == null ? 1 : page, size == null ? 20 : size);
        Page<Competition> result = competitionMapper.selectPage(p, wrapper);
        List<Competition> records = result.getRecords();

        // 报名状态筛选（在内存中过滤，因报名状态是实时计算的）
        if (regStatus != null) {
            records = records.stream()
                    .filter(c -> calcRegStatus(c) == regStatus)
                    .toList();
        }

        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Competition getCompetitionDetail(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new IllegalArgumentException("赛事不存在");
        }
        return competition;
    }

    @Override
    public PageResult<Competition> searchCompetitions(String keyword, Long page, Long size) {
        if (keyword == null || keyword.isBlank()) {
            return listCompetitions(null, null, null, page, size);
        }
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1);
        wrapper.and(w -> w.like(Competition::getTitle, keyword)
                .or().like(Competition::getOrganizer, keyword)
                .or().like(Competition::getCategory, keyword)
                .or().like(Competition::getDescription, keyword));
        wrapper.orderByDesc(Competition::getRegStartTime);

        Page<Competition> p = new Page<>(page == null ? 1 : page, size == null ? 20 : size);
        Page<Competition> result = competitionMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<CalendarEventDTO> getCalendarEvents(Integer year, Integer month) {
        // 默认当前年月
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59);

        // 查询本月范围内有任何时间节点的已发布赛事
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1);
        wrapper.and(w -> w.isNotNull(Competition::getRegStartTime)
                .or().isNotNull(Competition::getRegEndTime)
                .or().isNotNull(Competition::getMatchTime));

        List<Competition> competitions = competitionMapper.selectList(wrapper);

        List<CalendarEventDTO> events = new ArrayList<>();
        for (Competition c : competitions) {
            addIfInRange(events, c, c.getRegStartTime(), 1, monthStart, monthEnd);
            addIfInRange(events, c, c.getRegEndTime(), 2, monthStart, monthEnd);
            addIfInRange(events, c, c.getMatchTime(), 3, monthStart, monthEnd);
        }

        // 按时间升序
        events.sort((a, b) -> a.getEventTime().compareTo(b.getEventTime()));
        return events;
    }

    @Override
    @Transactional
    public int autoUpdateExpiredStatus() {
        // 报名已截止（reg_end_time < now）且仍为已发布(status=1)的赛事 → 置为已结束(status=2)
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1)
                .isNotNull(Competition::getRegEndTime)
                .lt(Competition::getRegEndTime, LocalDateTime.now());

        List<Competition> expired = competitionMapper.selectList(wrapper);
        int count = 0;
        for (Competition c : expired) {
            c.setStatus(2);
            c.setUpdateTime(LocalDateTime.now());
            competitionMapper.updateById(c);
            count++;
        }
        if (count > 0) {
            log.info("自动归档已截止赛事: {} 条", count);
        }
        return count;
    }

    // ============================ 内部工具 ============================

    /**
     * 计算赛事的实时报名状态
     * @return 1报名中 2未开始 3已截止
     */
    private int calcRegStatus(Competition c) {
        LocalDateTime now = LocalDateTime.now();
        if (c.getRegStartTime() != null && now.isBefore(c.getRegStartTime())) {
            return 2; // 未开始
        }
        if (c.getRegEndTime() != null && now.isAfter(c.getRegEndTime())) {
            return 3; // 已截止
        }
        return 1; // 报名中
    }

    private void addIfInRange(List<CalendarEventDTO> events, Competition c,
                              LocalDateTime time, int eventType,
                              LocalDateTime start, LocalDateTime end) {
        if (time == null) return;
        if (!time.isBefore(start) && !time.isAfter(end)) {
            CalendarEventDTO dto = new CalendarEventDTO();
            dto.setCompetitionId(c.getId());
            dto.setTitle(c.getTitle());
            dto.setEventType(eventType);
            dto.setEventTime(time);
            dto.setType(c.getType());
            events.add(dto);
        }
    }
}