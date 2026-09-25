package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Competition;
import com.saidazi.entity.Favorite;
import com.saidazi.mapper.CompetitionMapper;
import com.saidazi.mapper.FavoriteMapper;
import com.saidazi.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现 -- 接口 5.3：收藏模块
 */
@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long competitionId) {
        // 校验赛事存在
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            throw new IllegalArgumentException("赛事不存在");
        }
        // 防重复收藏
        if (favoriteMapper.countByUserAndCompetition(userId, competitionId) > 0) {
            throw new IllegalArgumentException("已收藏该赛事，请勿重复收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setCompetitionId(competitionId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        log.info("添加收藏: userId={}, competitionId={}", userId, competitionId);
        return true;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long competitionId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getCompetitionId, competitionId);
        int deleted = favoriteMapper.delete(wrapper);
        if (deleted == 0) {
            throw new IllegalArgumentException("未找到收藏记录");
        }
        log.info("取消收藏: userId={}, competitionId={}", userId, competitionId);
        return true;
    }

    @Override
    public List<Competition> listFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime)
        );
        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> competitionIds = favorites.stream()
                .map(Favorite::getCompetitionId)
                .collect(Collectors.toList());
        return competitionMapper.selectBatchIds(competitionIds);
    }

    @Override
    public boolean checkFavorite(Long userId, Long competitionId) {
        return favoriteMapper.countByUserAndCompetition(userId, competitionId) > 0;
    }
}