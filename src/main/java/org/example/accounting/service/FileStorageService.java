package org.example.accounting.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * 文件存储服务接口。
 */
public interface FileStorageService {
    
    /**
     * 存储文件并返回存储的文件名。
     */
    String storeFile(MultipartFile file);
    
    /**
     * 获取文件的存储路径。
     */
    Path getFilePath(String storedFilename);
    
    /**
     * 删除文件。
     */
    void deleteFile(String storedFilename);
}

