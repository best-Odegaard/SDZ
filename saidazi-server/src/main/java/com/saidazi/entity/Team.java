package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组队表
 */
@Data
@TableName("team")
public class Team {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;
    private Long userId;

    /** 组队标题 */
    private String title;

    /** 需要人数 */
    private Integer needCount;

    /** 所需技能标签，逗号分隔 */
    private String skillsRequired;

    /** 项目/赛事简介 */
    private String description;

    /** 联系方式 */
    private String contact;

    /** 是否要求简历：0否 1是 */
    private Integer requireResume;

    /** 备注 */
    private String remark;

    /** 状态：0招募中 1已满员 2已关闭 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
