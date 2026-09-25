package com.saidazi.controller;

import com.saidazi.common.Result;
import com.saidazi.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片上传接口 —— 接口③
 *
 * 功能：支持单张/多张图片上传，用于帖子配图等场景
 * 限制：单文件 ≤ 5MB，格式 jpg/jpeg/png/gif/bmp
 */
@Slf4j
@Tag(name = "3. 图片上传", description = "单张/批量图片上传、删除，支持 jpg/png/gif/bmp，单文件 ≤ 5MB")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Operation(summary = "单张图片上传", description = "上传一张图片文件，返回可访问的 URL 地址")
    @PostMapping("/upload")
    public Result<String> uploadImage(
            @Parameter(description = "图片文件 (jpg/png/gif/bmp)", required = true)
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("上传文件不能为空");
        }
        try {
            String url = fileService.uploadImage(file.getBytes(), file.getOriginalFilename());
            return Result.success(url);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return Result.error("文件读取失败");
        }
    }

    @Operation(summary = "多张图片批量上传", description = "批量上传图片（最多 3 张），返回 URL 数组")
    @PostMapping("/upload-multiple")
    public Result<List<String>> uploadMultipleImages(
            @Parameter(description = "图片文件数组 (最多3张)", required = true)
            @RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.badRequest("请选择要上传的文件");
        }
        if (files.length > 3) {
            return Result.badRequest("图片数量不能超过 3 张");
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String url = fileService.uploadImage(file.getBytes(), file.getOriginalFilename());
                urls.add(url);
            } catch (IOException e) {
                log.error("读取上传文件失败: {}", file.getOriginalFilename(), e);
                return Result.error("文件读取失败: " + file.getOriginalFilename());
            }
        }
        return Result.success(urls);
    }

    @Operation(summary = "删除已上传图片", description = "根据图片 URL 删除已上传的文件")
    @DeleteMapping("/delete")
    public Result<Void> deleteFile(
            @Parameter(description = "图片完整 URL 路径", required = true, example = "/uploads/images/2026/07/27/xxx.jpg")
            @RequestParam("url") String fileUrl) {
        boolean deleted = fileService.deleteFile(fileUrl);
        if (deleted) {
            return Result.success();
        } else {
            return Result.error("文件删除失败");
        }
    }
}
