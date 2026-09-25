package com.saidazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Competition;
import com.saidazi.entity.Reminder;
import com.saidazi.entity.User;
import com.saidazi.mapper.CompetitionMapper;
import com.saidazi.mapper.ReminderMapper;
import com.saidazi.mapper.UserMapper;
import com.saidazi.service.ReminderService;
import com.saidazi.util.WxMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒服务实现 —— 接口①：赛事定时提醒
 */
@Slf4j
@Service
public class ReminderServiceImpl implements ReminderService {

    @Autowired
    private ReminderMapper reminderMapper;

    @Autowired
    private WxMessageUtil wxMessageUtil;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Reminder addReminder(Long userId, Long competitionId, LocalDateTime remindTime, Integer remindType) {
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setCompetitionId(competitionId);
        reminder.setRemindTime(remindTime);
        reminder.setRemindType(remindType);
        reminder.setStatus(0);   // 待触发
        reminder.setIsPushed(0); // 未推送
        reminder.setCreateTime(LocalDateTime.now());

        reminderMapper.insert(reminder);
        log.info("创建提醒: userId={}, competitionId={}, remindTime={}", userId, competitionId, remindTime);
        return reminder;
    }

    @Override
    @Transactional
    public void cancelReminder(Long userId, Long reminderId) {
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getId, reminderId)
                .eq(Reminder::getUserId, userId);

        Reminder reminder = reminderMapper.selectOne(wrapper);
        if (reminder == null) {
            throw new IllegalArgumentException("提醒记录不存在或无权操作");
        }

        reminder.setStatus(2); // 已取消
        reminderMapper.updateById(reminder);
        log.info("取消提醒: reminderId={}, userId={}", reminderId, userId);
    }

    @Override
    public List<Reminder> getUserReminders(Long userId) {
        return reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getUserId, userId)
                        .ne(Reminder::getStatus, 2) // 不查询已取消的
                        .orderByAsc(Reminder::getRemindTime)
        );
    }

    /**
     * 核心方法：处理待触发的提醒
     * 每分钟扫描一次，将到时间的提醒进行推送
     */
    @Override
    @Transactional
    public int processPendingReminders() {
        LocalDateTime now = LocalDateTime.now();
        // 扫描前后1分钟内待触发的提醒
        LocalDateTime start = now.minusSeconds(30);
        LocalDateTime end = now.plusSeconds(30);

        List<Reminder> pendingList = reminderMapper.selectPendingReminders(start, end);
        if (pendingList.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Reminder reminder : pendingList) {
            try {
                // 查询赛事标题与用户 openId，构造推送内容
                Competition competition = competitionMapper.selectById(reminder.getCompetitionId());
                User user = userMapper.selectById(reminder.getUserId());
                String title = competition != null ? competition.getTitle() : "赛事提醒";
                String openId = user != null ? user.getOpenId() : String.valueOf(reminder.getUserId());
                String remindTime = reminder.getRemindTime().toString();
                String remindType = getRemindTypeDesc(reminder.getRemindType());

                // 发送微信订阅消息
                boolean pushed = wxMessageUtil.sendSubscribeMessage(
                        openId, title, remindTime, remindType
                );

                if (pushed) {
                    reminderMapper.markAsPushed(reminder.getId());
                    successCount++;
                    log.info("提醒推送成功: reminderId={}", reminder.getId());
                }
            } catch (Exception e) {
                log.error("提醒推送失败: reminderId={}", reminder.getId(), e);
            }
        }

        return successCount;
    }

    private String getRemindTypeDesc(Integer remindType) {
        return switch (remindType) {
            case 1 -> "报名开始前提醒";
            case 2 -> "报名截止前7天提醒";
            case 3 -> "报名截止前3天提醒";
            case 4 -> "报名截止前1天提醒";
            case 5 -> "自定义提醒";
            default -> "系统提醒";
        };
    }
}
