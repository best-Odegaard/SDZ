package com.saidazi.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Web MVC 配置：CORS、上传文件静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload.path:target/test-uploads/}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        // 确保上传目录存在
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (Exception e) {
            log.warn("无法创建上传目录: {}", uploadPath);
        }
    }

    /**
     * CORS 跨域配置 —— 允许测试页面跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 上传文件访问映射：将 file.upload.path 路径映射为 /uploads/** HTTP 访问
     *
     * 例如：
     *   配置 file.upload.path = D:/saidazi/uploads/
     *   HTTP 访问 /uploads/images/xxx.jpg → 实际读取 D:/saidazi/uploads/images/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        String resourceLocation = "file:" + absolutePath.replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        log.info("静态资源映射: /uploads/** -> {}", resourceLocation);
    }
}
