package com.example.vuecourseproject.user.entity.address;

import lombok.Data;

@Data
public class Address {
    private Long id;
    private String username; // redundant username for display
    private String name;     // receiver name (foreign key to sys_user.username)
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
}