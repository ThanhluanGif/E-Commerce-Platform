package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.AddressDTO;
import com.ecommerce.ecommerceapi.entity.Address;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    // 1. GET: Lấy danh sách địa chỉ
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getMyAddresses(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        List<AddressDTO> list = addressService.getMyAddresses(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách địa chỉ thành công!", list));
    }

    // 2. POST: Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(Principal principal, @RequestBody AddressDTO dto) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        Address address = Address.builder()
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .street(dto.getStreet())
                .ward(dto.getWard())
                .district(dto.getDistrict())
                .city(dto.getCity())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();
        Address saved = addressService.createAddress(userId, address);
        return ResponseEntity.ok(ApiResponse.success("Thêm địa chỉ thành công!", convertToDTO(saved)));
    }

    // 3. PUT: Sửa địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(Principal principal, @PathVariable Integer id, @RequestBody AddressDTO dto) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        Address address = Address.builder()
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .street(dto.getStreet())
                .ward(dto.getWard())
                .district(dto.getDistrict())
                .city(dto.getCity())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();
        Address saved = addressService.updateAddress(id, userId, address);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công!", convertToDTO(saved)));
    }

    // 4. DELETE: Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công!"));
    }

    // 5. PUT: Đặt địa chỉ làm mặc định
    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<Void>> setDefault(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        addressService.setDefault(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Đặt địa chỉ mặc định thành công!"));
    }

    private AddressDTO convertToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .isDefault(address.isDefault())
                .build();
    }
}
