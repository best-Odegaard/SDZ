package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒表
 */
@Data
@TableName("reminder")
public class Reminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long competitionId;

    /** 提醒触发时间 */
    private LocalDateTime remindTime;

    /** 提醒类型：1报名开始前 2截止前X天 */
    private Integer remindType;

    /** 状态：0待触发 1已触发 2已取消 */
    private Integer status;

    /** 是否已推送：0否 1是 */
    private Integer isPushed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
