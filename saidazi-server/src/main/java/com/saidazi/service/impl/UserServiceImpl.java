package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.User;
import com.saidazi.mapper.UserMapper;
import com.saidazi.service.UserService;
import com.saidazi.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现 —— 接口 5.1：用户模块
 *
 * 微信登录流程：
 *   小程序 wx.login() → code → 后端 → jscode2session → openId
 * 实际环境需调用微信 API；此处为开发联调，若 code 为空或调用失败，
 * 则以 code 作为 openId 直接登录，便于小程序开发与 H2 测试。
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public Map<String, Object> login(String code, String nickname, String avatarUrl) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("登录 code 不能为空");
        }

        // 实际环境：调用 https://api.weixin.qq.com/sns/jscode2session 获取 openId
        // 此处用 code 作为 openId，兼容开发联调与 H2 测试
        String openId = code;

        // 按 openId 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenId, openId)
        );

        if (user == null) {
            // 首次登录，自动注册
            user = new User();
            user.setOpenId(openId);
            user.setNickname(nickname != null ? nickname : "微信用户");
            user.setAvatarUrl(avatarUrl);
            user.setRole(0);
            userMapper.insert(user);
            log.info("新用户注册: userId={}, openId={}", user.getId(), openId);
        } else {
            // 已存在用户，更新昵称头像（如有）
            boolean dirty = false;
            if (nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
                dirty = true;
            }
            if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                dirty = true;
            }
            if (dirty) {
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getOpenId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        log.info("用户登录成功: userId={}, openId={}", user.getId(), openId);
        return result;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUserInfo(Long userId, User user) {
        User existing = userMapper.selectById(userId);
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 选择性更新字段（避免覆盖 openId / role 等敏感字段）
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getAvatarUrl() != null) existing.setAvatarUrl(user.getAvatarUrl());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getCollege() != null) existing.setCollege(user.getCollege());
        if (user.getMajor() != null) existing.setMajor(user.getMajor());
        if (user.getGrade() != null) existing.setGrade(user.getGrade());
        if (user.getSkills() != null) existing.setSkills(user.getSkills());
        if (user.getInterests() != null) existing.setInterests(user.getInterests());

        existing.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(existing);
        log.info("更新用户信息: userId={}", userId);
        return existing;
    }
}