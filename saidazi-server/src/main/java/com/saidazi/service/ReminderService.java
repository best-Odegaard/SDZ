package com.saidazi.service;

import com.saidazi.entity.Reminder;
import com.saidazi.entity.Team;
import com.saidazi.entity.TeamApplication;

import java.util.List;

/**
 * 提醒服务
 */
public interface ReminderService {

    /**
     * 设置提醒
     */
    Reminder addReminder(Long userId, Long competitionId, java.time.LocalDateTime remindTime, Integer remindType);

    /**
     * 取消提醒
     */
    void cancelReminder(Long userId, Long reminderId);

    /**
     * 获取用户的提醒列表
     */
    List<Reminder> getUserReminders(Long userId);

    /**
     * 查询待触发的提醒并推送
     */
    int processPendingReminders();
}
