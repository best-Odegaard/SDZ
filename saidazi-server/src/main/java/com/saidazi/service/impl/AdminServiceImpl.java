package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.dto.DashboardDTO;
import com.saidazi.entity.Admin;
import com.saidazi.entity.Competition;
import com.saidazi.mapper.*;
import com.saidazi.service.AdminService;
import com.saidazi.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现 -- 设计方案第8章：管理员后台
 *
 * 账号密码登录（BCrypt 校验）、数据概览、校内赛事 CRUD
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ReminderMapper reminderMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );
        if (admin == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 生成 token（用 admin_id + 负数前缀避免与普通用户冲突，claim 中标记 role=admin）
        String token = jwtUtil.generateToken(-admin.getId(), "admin:" + admin.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        admin.setPassword(null); // 不返回密码
        result.put("adminInfo", admin);
        log.info("管理员登录成功: username={}", username);
        return result;
    }

    @Override
    public DashboardDTO dashboard() {
        DashboardDTO dto = new DashboardDTO();
        dto.setCompetitionCount(competitionMapper.selectCount(new LambdaQueryWrapper<>()));
        dto.setUserCount(userMapper.selectCount(new LambdaQueryWrapper<>()));
        dto.setTeamCount(teamMapper.selectCount(new LambdaQueryWrapper<>()));
        dto.setPostCount(postMapper.selectCount(new LambdaQueryWrapper<>()));
        dto.setFavoriteCount(favoriteMapper.selectCount(new LambdaQueryWrapper<>()));
        dto.setReminderCount(reminderMapper.selectCount(new LambdaQueryWrapper<>()));
        return dto;
    }

    @Override
    @Transactional
    public Competition createCompetition(Competition competition) {
        validateCompetition(competition);
        if (competition.getStatus() == null) {
            competition.setStatus(1); // 默认已发布
        }
        competition.setCreateTime(LocalDateTime.now());
        competition.setUpdateTime(LocalDateTime.now());
        competitionMapper.insert(competition);
        log.info("管理员新增赛事: id={}, title={}", competition.getId(), competition.getTitle());
        return competition;
    }

    @Override
    @Transactional
    public Competition updateCompetition(Long id, Competition competition) {
        Competition existing = competitionMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("赛事不存在");
        }
        if (competition.getTitle() != null) existing.setTitle(competition.getTitle());
        if (competition.getType() != null) existing.setType(competition.getType());
        if (competition.getCategory() != null) existing.setCategory(competition.getCategory());
        if (competition.getDifficulty() != null) existing.setDifficulty(competition.getDifficulty());
        if (competition.getCollegeTag() != null) existing.setCollegeTag(competition.getCollegeTag());
        if (competition.getOrganizer() != null) existing.setOrganizer(competition.getOrganizer());
        if (competition.getRegStartTime() != null) existing.setRegStartTime(competition.getRegStartTime());
        if (competition.getRegEndTime() != null) existing.setRegEndTime(competition.getRegEndTime());
        if (competition.getMatchTime() != null) existing.setMatchTime(competition.getMatchTime());
        if (competition.getDescription() != null) existing.setDescription(competition.getDescription());
        if (competition.getRegInfo() != null) existing.setRegInfo(competition.getRegInfo());
        if (competition.getCoverUrl() != null) existing.setCoverUrl(competition.getCoverUrl());
        if (competition.getStatus() != null) existing.setStatus(competition.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        competitionMapper.updateById(existing);
        log.info("管理员编辑赛事: id={}", id);
        return existing;
    }

    @Override
    @Transactional
    public void deleteCompetition(Long id) {
        Competition existing = competitionMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("赛事不存在");
        }
        competitionMapper.deleteById(id);
        log.info("管理员删除赛事: id={}", id);
    }

    @Override
    public List<Competition> listAllCompetitions() {
        return competitionMapper.selectList(
                new LambdaQueryWrapper<Competition>().orderByDesc(Competition::getCreateTime)
        );
    }

    @Override
    public Admin getAdminByUsername(String username) {
        return adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );
    }

    private void validateCompetition(Competition c) {
        if (c.getTitle() == null || c.getTitle().isBlank()) {
            throw new IllegalArgumentException("赛事标题不能为空");
        }
        if (c.getType() == null || (c.getType() != 0 && c.getType() != 1)) {
            throw new IllegalArgumentException("赛事类型必须为 0校内 或 1全国");
        }
    }
}