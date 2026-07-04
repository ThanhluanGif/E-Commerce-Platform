package com.ecommerce.ecommerceapi.entity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) return "Customer";
        if (attribute == UserRole.ADMIN) return "Admin";
        if (attribute == UserRole.SELLER) return "Seller";
        return "Customer";
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) return UserRole.CUSTOMER;
        
        String clean = dbData.trim().toUpperCase();
        if (clean.contains("ADMIN")) {
            return UserRole.ADMIN;
        } else if (clean.contains("SELLER")) {
            return UserRole.SELLER;
        }
        return UserRole.CUSTOMER; // Mặc định tất cả các quyền khác về CUSTOMER
    }
}
