package com.saidazi.service;

import com.saidazi.dto.DashboardDTO;
import com.saidazi.entity.Admin;
import com.saidazi.entity.Competition;

import java.util.List;
import java.util.Map;

/**
 * 管理员服务
 */
public interface AdminService {

    /**
     * 管理员登录
     * @return 登录成功返回 token 与管理员信息
     */
    Map<String, Object> login(String username, String password);

    /**
     * 数据概览
     */
    DashboardDTO dashboard();

    /**
     * 校内赛事新增
     */
    Competition createCompetition(Competition competition);

    /**
     * 校内赛事编辑
     */
    Competition updateCompetition(Long id, Competition competition);

    /**
     * 校内赛事删除/下架
     */
    void deleteCompetition(Long id);

    /**
     * 全部赛事列表（管理员视角，含草稿/已结束）
     */
    List<Competition> listAllCompetitions();

    /**
     * 查询管理员信息（按 username）
     */
    Admin getAdminByUsername(String username);
}