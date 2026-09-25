package com.saidazi.service;

import com.saidazi.entity.Resume;
import com.saidazi.entity.TeamApplication;

import java.util.List;

/**
 * 简历服务
 */
public interface ResumeService {

    /**
     * 获取我的简历
     */
    Resume getMyResume(Long userId);

    /**
     * 保存/更新简历（upsert：每个用户一份简历）
     */
    Resume saveResume(Long userId, Resume resume);

    /**
     * 查看他人简历（组队场景）
     */
    Resume getResumeByUserId(Long userId);

    /**
     * 上传附件简历 PDF
     */
    String uploadAttachment(Long userId, byte[] fileBytes, String originalFilename);

    /**
     * 简历投递历史：该简历被发送到的组队申请记录
     */
    List<TeamApplication> getSentHistory(Long userId);
}