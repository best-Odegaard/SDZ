package com.saidazi.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Admin;
import com.saidazi.mapper.AdminMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 启动时若管理员表为空，则初始化默认管理员账号 admin/admin123
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private AdminMapper adminMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        Long count = adminMapper.selectCount(new LambdaQueryWrapper<>());
        if (count == null || count == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("超级管理员");
            adminMapper.insert(admin);
            log.info("初始化默认管理员账号：admin / admin123");
        }
    }
}