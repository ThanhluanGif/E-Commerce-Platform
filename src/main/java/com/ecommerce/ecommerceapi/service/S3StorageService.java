package com.ecommerce.ecommerceapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service("s3Storage")
public class S3StorageService implements StorageService {

    @Value("${aws.s3.bucket:ecommerce-bucket}")
    private String bucketName;

    @Override
    public String storeFile(MultipartFile file) {
        // Mô phỏng / hoặc tích hợp thực tế AWS S3 SDK
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        // AWS S3 upload implementation placeholder:
        // s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
        
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }
}
