package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子表
 */
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 帖子标题 */
    private String title;

    /** 分类：1求助答疑 2备赛经验 3资料分享 4赛事闲聊 */
    private Integer category;

    /** 正文内容 */
    private String content;

    /** 图片URL，逗号分隔（最多3张） */
    private String images;

    /** 是否允许评论：0否 1是 */
    private Integer allowComment;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 状态：0已下架 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}