package com.saidazi.task;

import com.saidazi.service.CompetitionService;
import com.saidazi.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 赛搭子定时任务
 *
 * 1. 报名提醒调度（接口①）：每分钟扫描待触发的提醒并推送微信订阅消息
 * 2. 赛事状态自动更新（设计方案 7.2）：每天凌晨 2 点把报名已截止的赛事置为已结束
 *
 * 基于 Spring Boot @Scheduled 注解，单节点即可满足校内使用需求，
 * 无需部署 Redis 分布式定时组件
 */
@Slf4j
@Component
public class ReminderScheduledTask {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private CompetitionService competitionService;

    /**
     * 每分钟执行一次，扫描即将到期的提醒并推送
     *
     * cron = "0 * * * * ?"
     * ┬ ┬ ┬ ┬ ┬ ┬
     * │ │ │ │ │ └── 星期 (0-7, 0/7=日)
     * │ │ │ │ └──── 月份 (1-12)
     * │ │ │ └────── 日期 (1-31)
     * │ │ └──────── 小时 (0-23)
     * │ └────────── 分钟 (0-59)
     * └──────────── 秒 (0-59)
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanAndPushReminders() {
        log.debug("定时任务启动：扫描待推送提醒...");

        try {
            int pushedCount = reminderService.processPendingReminders();
            if (pushedCount > 0) {
                log.info("定时提醒推送完成：本次成功推送 {} 条", pushedCount);
            }
        } catch (Exception e) {
            log.error("定时提醒任务执行异常", e);
        }
    }

    /**
     * 每天凌晨 2 点，把报名已截止的赛事自动归档为「已结束」(status=2)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateCompetitionStatus() {
        log.info("定时任务：自动归档已截止赛事");
        try {
            int count = competitionService.autoUpdateExpiredStatus();
            log.info("赛事状态自动更新完成：本次归档 {} 条", count);
        } catch (Exception e) {
            log.error("赛事状态自动更新异常", e);
        }
    }

    /**
     * 每天凌晨 2 点 30 分，清理已推送超过 30 天的提醒记录
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanExpiredReminders() {
        log.info("定时任务：清理过期提醒记录");
        // 可在此补充清理逻辑
    }
}
