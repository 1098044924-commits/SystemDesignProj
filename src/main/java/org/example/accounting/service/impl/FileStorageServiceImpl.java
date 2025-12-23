package org.example.accounting.service.impl;

import org.example.accounting.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件存储服务实现。
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建文件存储目录: " + this.fileStorageLocation, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return storedFilename;
        } catch (IOException ex) {
            throw new RuntimeException("存储文件失败: " + file.getOriginalFilename(), ex);
        }
    }

    @Override
    public Path getFilePath(String storedFilename) {
        return this.fileStorageLocation.resolve(storedFilename).normalize();
    }

    @Override
    public void deleteFile(String storedFilename) {
        try {
            Path filePath = getFilePath(storedFilename);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("删除文件失败: " + storedFilename, ex);
        }
    }
}

