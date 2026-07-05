package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private StorageService storageService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn một file để upload!");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Chỉ chấp nhận các file ảnh!");
        }

        String fileUrl = storageService.storeFile(file);
        return ResponseEntity.ok(ApiResponse.success("Upload ảnh thành công!", fileUrl));
    }
}
