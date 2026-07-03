package com.ecommerce.ecommerceapi.entity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) return "Customer";
        return attribute == UserRole.ADMIN ? "Admin" : "Customer";
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) return UserRole.CUSTOMER;
        
        String clean = dbData.trim().toUpperCase();
        if (clean.contains("ADMIN")) {
            return UserRole.ADMIN;
        }
        return UserRole.CUSTOMER; // Mặc định tất cả các quyền khác (USER, CUSTOMER, ROLE_USER, vv.) về CUSTOMER để tránh lỗi khởi chạy
    }
}
