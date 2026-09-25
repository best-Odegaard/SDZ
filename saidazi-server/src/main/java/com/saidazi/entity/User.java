package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String college;
    private String major;
    private String grade;

    /** 技能标签，逗号分隔 */
    private String skills;

    /** 参赛兴趣标签 */
    private String interests;

    /** 角色：0普通用户 1管理员 */
    private Integer role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
