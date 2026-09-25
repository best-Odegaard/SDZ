package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 组队申请表
 */
@Data
@TableName("team_application")
public class TeamApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;
    private Long applicantId;

    /** 简历ID（若要求简历） */
    private Long resumeId;

    /** 技能匹配度分数 */
    private BigDecimal matchScore;

    /** 状态：0待审核 1已通过 2已拒绝 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
