package com.saidazi.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事日历条目 DTO
 * 一个赛事对应多条记录（报名开始/截止/比赛三类节点）
 */
@Data
public class CalendarEventDTO {
    /** 赛事 ID */
    private Long competitionId;
    /** 赛事标题 */
    private String title;
    /** 节点类型：1=报名开始 2=报名截止 3=比赛日 */
    private Integer eventType;
    /** 节点日期时间 */
    private LocalDateTime eventTime;
    /** 赛事类型：0校内 1全国 */
    private Integer type;
}