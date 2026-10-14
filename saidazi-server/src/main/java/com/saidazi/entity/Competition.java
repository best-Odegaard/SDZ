package com.saidazi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事表
 */
@Data
@TableName("competition")
public class Competition {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 赛事标题 */
    private String title;

    /** 赛事类型：0校内 1全国 */
    private Integer type;

    /** 分类：学科竞赛/创新创业/体育竞技/文艺活动/其他 */
    private String category;

    /** 难度：1入门 2进阶 3高阶 */
    private Integer difficulty;

    /** 学院标签（校内赛事） */
    private String collegeTag;

    /** 主办单位 */
    private String organizer;

    private LocalDateTime regStartTime;
    private LocalDateTime regEndTime;
    private LocalDateTime matchTime;

    /** 发布时间（爬虫解析写入） */
    private LocalDateTime publishTime;

    /** 赛事简介/参赛要求/奖项设置 */
    private String description;

    /** 报名方式说明（含网址文字） */
    private String regInfo;

    /** 封面图 */
    private String coverUrl;

    /** 来源链接（爬虫抓取来源，用于去重） */
    private String sourceUrl;

    /** 状态：0草稿 1已发布 2已结束 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
