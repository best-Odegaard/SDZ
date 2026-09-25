package com.saidazi.service;

import com.saidazi.entity.User;

import java.util.Map;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 微信登录（code 换 session），返回 token 与用户信息
     */
    Map<String, Object> login(String code, String nickname, String avatarUrl);

    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    User updateUserInfo(Long userId, User user);
}