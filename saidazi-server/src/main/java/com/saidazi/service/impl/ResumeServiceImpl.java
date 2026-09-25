package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Resume;
import com.saidazi.entity.TeamApplication;
import com.saidazi.mapper.ResumeMapper;
import com.saidazi.mapper.TeamApplicationMapper;
import com.saidazi.service.FileService;
import com.saidazi.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 简历服务实现 -- 接口 5.7：简历模块
 *
 * 简历双向互审：申请人发送简历，发布人在线预览审核
 */
@Slf4j
@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private TeamApplicationMapper teamApplicationMapper;

    @Autowired
    private FileService fileService;

    @Override
    public Resume getMyResume(Long userId) {
        return resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId)
        );
    }

    @Override
    @Transactional
    public Resume saveResume(Long userId, Resume resume) {
        // 必填字段校验（手册表8）
        if (resume.getName() == null || resume.getName().isBlank()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (resume.getCollegeMajor() == null || resume.getCollegeMajor().isBlank()) {
            throw new IllegalArgumentException("学院/专业不能为空");
        }
        if (resume.getGrade() == null || resume.getGrade().isBlank()) {
            throw new IllegalArgumentException("年级不能为空");
        }
        if (resume.getSkills() == null || resume.getSkills().isBlank()) {
            throw new IllegalArgumentException("技能标签不能为空");
        }
        if (resume.getIntroduction() == null || resume.getIntroduction().isBlank()) {
            throw new IllegalArgumentException("个人简介不能为空");
        }

        Resume existing = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId)
        );

        if (existing == null) {
            // 新建简历
            resume.setUserId(userId);
            if (resume.getStatus() == null) {
                resume.setStatus(0); // 默认草稿
            }
            resume.setCreateTime(LocalDateTime.now());
            resume.setUpdateTime(LocalDateTime.now());
            resumeMapper.insert(resume);
            log.info("新建简历: userId={}, resumeId={}", userId, resume.getId());
            return resume;
        } else {
            // 更新已有简历
            existing.setName(resume.getName());
            existing.setCollegeMajor(resume.getCollegeMajor());
            existing.setGrade(resume.getGrade());
            existing.setSkills(resume.getSkills());
            if (resume.getExperience() != null) existing.setExperience(resume.getExperience());
            existing.setIntroduction(resume.getIntroduction());
            if (resume.getAttachmentUrl() != null) existing.setAttachmentUrl(resume.getAttachmentUrl());
            if (resume.getStatus() != null) existing.setStatus(resume.getStatus());
            existing.setUpdateTime(LocalDateTime.now());
            resumeMapper.updateById(existing);
            log.info("更新简历: userId={}, resumeId={}", userId, existing.getId());
            return existing;
        }
    }

    @Override
    public Resume getResumeByUserId(Long userId) {
        Resume resume = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId)
        );
        if (resume == null) {
            throw new IllegalArgumentException("该用户暂未创建简历");
        }
        return resume;
    }

    @Override
    @Transactional
    public String uploadAttachment(Long userId, byte[] fileBytes, String originalFilename) {
        String url = fileService.uploadAttachment(fileBytes, originalFilename);

        // 将附件 URL 回填到用户的简历
        Resume existing = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId)
        );
        if (existing != null) {
            existing.setAttachmentUrl(url);
            existing.setUpdateTime(LocalDateTime.now());
            resumeMapper.updateById(existing);
        }
        log.info("简历附件上传: userId={}, url={}", userId, url);
        return url;
    }

    @Override
    public List<TeamApplication> getSentHistory(Long userId) {
        // 查询该用户简历被发送到的所有组队申请（按 resume.user_id 关联）
        Resume myResume = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId)
        );
        if (myResume == null) {
            return List.of();
        }
        return teamApplicationMapper.selectList(
                new LambdaQueryWrapper<TeamApplication>()
                        .eq(TeamApplication::getResumeId, myResume.getId())
                        .orderByDesc(TeamApplication::getCreateTime)
        );
    }
}