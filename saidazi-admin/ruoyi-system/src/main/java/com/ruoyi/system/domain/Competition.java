package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 赛事表 competition
 *
 * @author saidazi
 */
public class Competition extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 赛事ID */
    @Excel(name = "赛事ID")
    private Long id;

    /** 赛事标题 */
    @Excel(name = "赛事标题")
    private String title;

    /** 赛事类型：0校内 1全国 */
    @Excel(name = "赛事类型", readConverterExp = "0=校内,1=全国")
    private Integer type;

    /** 分类 */
    @Excel(name = "分类")
    private String category;

    /** 难度：1入门 2进阶 3高阶 */
    @Excel(name = "难度", readConverterExp = "1=入门,2=进阶,3=高阶")
    private Integer difficulty;

    /** 学院标签 */
    @Excel(name = "学院标签")
    private String collegeTag;

    /** 主办单位 */
    @Excel(name = "主办单位")
    private String organizer;

    /** 报名开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date regStartTime;

    /** 报名截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date regEndTime;

    /** 比赛时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "比赛时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date matchTime;

    /** 赛事简介 */
    private String description;

    /** 报名方式说明 */
    private String regInfo;

    /** 封面图 */
    private String coverUrl;

    /** 状态：0草稿 1已发布 2已结束 */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已发布,2=已结束")
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public String getCollegeTag() { return collegeTag; }
    public void setCollegeTag(String collegeTag) { this.collegeTag = collegeTag; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public Date getRegStartTime() { return regStartTime; }
    public void setRegStartTime(Date regStartTime) { this.regStartTime = regStartTime; }

    public Date getRegEndTime() { return regEndTime; }
    public void setRegEndTime(Date regEndTime) { this.regEndTime = regEndTime; }

    public Date getMatchTime() { return matchTime; }
    public void setMatchTime(Date matchTime) { this.matchTime = matchTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRegInfo() { return regInfo; }
    public void setRegInfo(String regInfo) { this.regInfo = regInfo; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("title", getTitle())
            .append("type", getType())
            .append("category", getCategory())
            .append("difficulty", getDifficulty())
            .append("collegeTag", getCollegeTag())
            .append("organizer", getOrganizer())
            .append("regStartTime", getRegStartTime())
            .append("regEndTime", getRegEndTime())
            .append("matchTime", getMatchTime())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}