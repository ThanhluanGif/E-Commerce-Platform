package com.ecommerce.ecommerceapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service("localStorage")
public class LocalStorageService implements StorageService {

    private final Path fileStorageLocation;
    
    @Value("${app.upload.base-url:http://localhost:8080/uploads/}")
    private String baseUrl;

    public LocalStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create local upload directory.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return baseUrl + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu trữ file cục bộ!", ex);
        }
    }
}
