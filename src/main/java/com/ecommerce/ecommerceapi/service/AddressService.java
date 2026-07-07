package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Address;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.AddressRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public Address createAddress(Integer userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        address.setUser(user);

        // If this is the first address, or isDefault is true, unset other defaults
        List<Address> list = addressRepository.findByUserId(userId);
        if (list.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            list.forEach(a -> {
                a.setDefault(false);
                addressRepository.save(a);
            });
        }

        return addressRepository.save(address);
    }

    public Address updateAddress(Integer id, Integer userId, Address updated) {
        Address addr = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ!"));

        if (!addr.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa địa chỉ này!");
        }

        addr.setFullName(updated.getFullName());
        addr.setPhone(updated.getPhone());
        addr.setStreet(updated.getStreet());
        addr.setWard(updated.getWard());
        addr.setDistrict(updated.getDistrict());
        addr.setCity(updated.getCity());

        if (updated.isDefault() && !addr.isDefault()) {
            List<Address> list = addressRepository.findByUserId(userId);
            list.forEach(a -> {
                a.setDefault(false);
                addressRepository.save(a);
            });
            addr.setDefault(true);
        }

        return addressRepository.save(addr);
    }

    public void deleteAddress(Integer id, Integer userId) {
        Address addr = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ!"));

        if (!addr.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa địa chỉ này!");
        }

        addressRepository.delete(addr);

        // If we deleted the default address, set another one as default
        if (addr.isDefault()) {
            List<Address> list = addressRepository.findByUserId(userId);
            if (!list.isEmpty()) {
                list.get(0).setDefault(true);
                addressRepository.save(list.get(0));
            }
        }
    }

    public List<Address> getMyAddresses(Integer userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getDefaultAddress(Integer userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId).orElse(null);
    }

    public void setDefault(Integer id, Integer userId) {
        Address addr = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ!"));

        if (!addr.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa địa chỉ này!");
        }

        List<Address> list = addressRepository.findByUserId(userId);
        list.forEach(a -> {
            a.setDefault(a.getId().equals(id));
            addressRepository.save(a);
        });
    }
}
