package com.saidazi.service;

import com.saidazi.entity.Competition;

import java.util.List;

/**
 * 收藏服务
 */
public interface FavoriteService {

    /**
     * 添加收藏
     */
    boolean addFavorite(Long userId, Long competitionId);

    /**
     * 取消收藏
     */
    boolean removeFavorite(Long userId, Long competitionId);

    /**
     * 我的收藏列表（含赛事信息）
     */
    List<Competition> listFavorites(Long userId);

    /**
     * 检查是否已收藏
     */
    boolean checkFavorite(Long userId, Long competitionId);
}