package com.example.vuecourseproject.user.service.address;

import com.example.vuecourseproject.user.entity.address.Address;
import com.example.vuecourseproject.user.mapper.address.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<Address> getAllAddresses() {
        return addressMapper.findAll();
    }

    @Override
    public Address getAddressById(Long id) {
        return addressMapper.findById(id);
    }

    @Override
    @Transactional
    public void addAddress(Address address) {
        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void updateAddress(Address address) {
        addressMapper.update(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        addressMapper.delete(id);
    }
}