package com.example.vuecourseproject.cashier.service;


import com.example.vuecourseproject.cashier.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> search(String query);
    List<Product> findAll();
}
