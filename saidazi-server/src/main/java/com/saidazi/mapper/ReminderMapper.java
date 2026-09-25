package com.saidazi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saidazi.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {

    /**
     * 查询指定时间附近待触发的提醒
     */
    @Select("SELECT * FROM reminder WHERE status = 0 AND is_pushed = 0 " +
            "AND remind_time BETWEEN #{start} AND #{end}")
    List<Reminder> selectPendingReminders(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * 标记提醒已推送
     */
    @Update("UPDATE reminder SET status = 1, is_pushed = 1 WHERE id = #{id}")
    int markAsPushed(@Param("id") Long id);
}
