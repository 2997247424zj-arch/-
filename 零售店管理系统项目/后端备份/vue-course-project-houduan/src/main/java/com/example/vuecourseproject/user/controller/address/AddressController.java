package com.example.vuecourseproject.user.controller.address;

import com.example.vuecourseproject.user.entity.address.Address;
import com.example.vuecourseproject.user.service.address.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddress(@PathVariable Long id) {
        Address address = addressService.getAddressById(id);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<Map<String, Serializable>> addAddress(@RequestBody Address address) {
        addressService.addAddress(address);
        return ResponseEntity.ok(Map.of("code", 200, "message", "添加成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Serializable>> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        address.setId(id);
        addressService.updateAddress(address);
        return ResponseEntity.ok(Map.of("code", 200, "message", "更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Serializable>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
    }
}