package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 往届成果表 achievement
 *
 * @author saidazi
 */
public class Achievement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成果ID */
    @Excel(name = "成果ID")
    private Long id;

    /** 赛事ID */
    @Excel(name = "赛事ID")
    private Long competitionId;

    /** 赛事标题（联表查询） */
    @Excel(name = "赛事标题")
    private String competitionTitle;

    /** 获奖队伍名称 */
    @Excel(name = "获奖队伍")
    private String teamName;

    /** 获奖等级 */
    @Excel(name = "获奖等级")
    private String award;

    /** 作品简介 */
    private String introduction;

    /** 队员姓名 */
    @Excel(name = "队员")
    private String members;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompetitionId() { return competitionId; }
    public void setCompetitionId(Long competitionId) { this.competitionId = competitionId; }

    public String getCompetitionTitle() { return competitionTitle; }
    public void setCompetitionTitle(String competitionTitle) { this.competitionTitle = competitionTitle; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getAward() { return award; }
    public void setAward(String award) { this.award = award; }

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public String getMembers() { return members; }
    public void setMembers(String members) { this.members = members; }

    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("competitionId", getCompetitionId())
            .append("competitionTitle", getCompetitionTitle())
            .append("teamName", getTeamName())
            .append("award", getAward())
            .append("members", getMembers())
            .append("createTime", getCreateTime())
            .toString();
    }
}