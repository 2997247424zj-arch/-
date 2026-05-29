package com.example.vuecourseproject.user.service.address;

import com.example.vuecourseproject.user.entity.address.Address;
import java.util.List;

public interface AddressService {
    // 新增方法：获取所有地址
    List<Address> getAllAddresses();
    Address getAddressById(Long id);
    void addAddress(Address address);
    void updateAddress(Address address);
    void deleteAddress(Long id);
}