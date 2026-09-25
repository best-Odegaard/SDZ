package com.saidazi.service.impl;

import com.saidazi.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务实现 —— 接口③：图片上传
 *
 * 返回完整 URL，可直接用于远程访问：
 * 例如 http://192.168.x.x:8080/uploads/images/2026/07/27/uuid.jpg
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path}")
    private String uploadBasePath;

    @Value("${file.upload.allowedExtensions}")
    private String allowedExtensionsStr;

    @Autowired
    private HttpServletRequest request;

    /** 允许的图片扩展名集合 */
    private Set<String> allowedExtensions;

    @PostConstruct
    public void init() {
        allowedExtensions = new HashSet<>(
                Arrays.asList(allowedExtensionsStr.split(","))
        );
        try {
            Files.createDirectories(Paths.get(uploadBasePath));
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", uploadBasePath, e);
        }
    }

    @Override
    public String uploadImage(byte[] fileBytes, String originalFilename) {
        if (!isValidImage(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件格式，允许: " + allowedExtensionsStr);
        }
        if (fileBytes.length > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        String relativePath = "images/" + dateDir + "/" + newFilename;
        Path fullPath = Paths.get(uploadBasePath, relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, fileBytes);

            // 动态获取服务器地址，拼成完整 URL
            String baseUrl = getServerBaseUrl();
            String url = baseUrl + "/uploads/" + relativePath.replace("\\", "/");

            log.info("图片上传成功: {} -> {}", originalFilename, url);
            return url;

        } catch (IOException e) {
            log.error("图片上传失败: {}", originalFilename, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isValidImage(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return false;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
                .toLowerCase();
        return allowedExtensions.contains(extension);
    }

    @Override
    public String uploadAttachment(byte[] fileBytes, String originalFilename) {
        // 仅允许 PDF
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("仅支持 PDF 格式附件");
        }
        if (fileBytes.length > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ".pdf";

        String relativePath = "attachments/" + dateDir + "/" + newFilename;
        Path fullPath = Paths.get(uploadBasePath, relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, fileBytes);

            String baseUrl = getServerBaseUrl();
            String url = baseUrl + "/uploads/" + relativePath.replace("\\", "/");
            log.info("PDF附件上传成功: {} -> {}", originalFilename, url);
            return url;
        } catch (IOException e) {
            log.error("PDF附件上传失败: {}", originalFilename, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从完整 URL 中提取相对路径部分
            String relativePath = fileUrl;
            if (fileUrl.contains("/uploads/")) {
                relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + "/uploads/".length());
            }
            Path fullPath = Paths.get(uploadBasePath, relativePath);
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 动态获取服务器基础地址
     * 优先级：X-Forwarded-Host 头 > 请求的 Host > localhost:8080
     */
    private String getServerBaseUrl() {
        String scheme = request.getScheme(); // http 或 https
        String host = request.getHeader("X-Forwarded-Host");

        if (host == null || host.isBlank()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isBlank()) {
            host = "localhost:8080";
        }

        // 如果通过反向代理访问，X-Forwarded-Proto 可能包含真实协议
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            scheme = forwardedProto;
        }

        return scheme + "://" + host;
    }
}
