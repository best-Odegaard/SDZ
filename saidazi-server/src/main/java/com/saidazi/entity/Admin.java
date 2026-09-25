package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员表
 */
@Data
@TableName("admin")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** BCrypt 加密密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}