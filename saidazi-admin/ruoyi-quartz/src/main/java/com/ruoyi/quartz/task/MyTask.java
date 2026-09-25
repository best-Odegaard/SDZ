package com.ruoyi.quartz.task;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MyTask {

    public void showTime(){
        System.out.println("定时任务开始启动" + new Date());
    }
}
