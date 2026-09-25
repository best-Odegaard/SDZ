package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 往届成果表
 */
@Data
@TableName("achievement")
public class Achievement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    /** 获奖队伍名称 */
    private String teamName;

    /** 获奖等级 */
    private String award;

    /** 作品简介 */
    private String introduction;

    /** 队员姓名 */
    private String members;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}