package com.saidazi.service;

/**
 * 文件上传服务
 */
public interface FileService {

    /**
     * 上传图片文件
     *
     * @param fileBytes  文件字节数组
     * @param originalFilename 原始文件名
     * @return 可访问的 URL 路径
     */
    String uploadImage(byte[] fileBytes, String originalFilename);

    /**
     * 校验文件是否为允许的图片类型
     */
    boolean isValidImage(String originalFilename);

    /**
     * 上传附件文件（PDF）
     *
     * @param fileBytes       文件字节数组
     * @param originalFilename 原始文件名
     * @return 可访问的 URL 路径
     */
    String uploadAttachment(byte[] fileBytes, String originalFilename);

    /**
     * 删除文件
     */
    boolean deleteFile(String fileUrl);
}
