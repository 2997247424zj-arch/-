package com.example.ecommerceback.product.service;

import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.entity.ProductCategory;
import com.example.ecommerceback.product.mapper.ProductCategoryMapper;
import com.example.ecommerceback.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductInitService implements CommandLineRunner {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public void run(String... args) {
        initCategoriesIfNeeded();
        initProductsIfNeeded();
    }

    private void initCategoriesIfNeeded() {
        if (!productCategoryMapper.selectList(null).isEmpty()) {
            return;
        }

        List<ProductCategory> categories = new ArrayList<>();
        categories.add(createCategory(1L, "Electronics", 0L, 1));
        categories.add(createCategory(2L, "Phones", 1L, 1));
        categories.add(createCategory(3L, "Computers", 1L, 2));
        categories.add(createCategory(4L, "Clothing", 0L, 2));
        categories.add(createCategory(5L, "Men", 4L, 1));
        categories.add(createCategory(6L, "Women", 4L, 2));
        categories.add(createCategory(7L, "Audio", 1L, 3));
        categories.add(createCategory(8L, "Books", 0L, 3));
        categories.add(createCategory(9L, "Home", 0L, 4));
        categories.add(createCategory(10L, "Sports", 0L, 5));
        categories.add(createCategory(11L, "Outdoor", 10L, 1));
        categories.add(createCategory(12L, "Kitchen", 9L, 1));

        categories.forEach(productCategoryMapper::insert);
        log.info("Initialized {} product categories", categories.size());
    }

    private void initProductsIfNeeded() {
        if (!productMapper.selectList(null).isEmpty()) {
            log.info("商品数据已存在，跳过初始化");
            return;
        }

        List<Product> products = new ArrayList<>();
        products.add(createProduct("iPhone 15", 2L, 5999.00, 100, "Apple smartphone with strong camera and battery life", "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Huawei Mate 60", 2L, 4999.00, 80, "Flagship smartphone for photography and communication", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("MacBook Pro 14", 3L, 12999.00, 50, "Laptop for development, design and heavy office work", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("ThinkPad X1 Carbon", 3L, 9999.00, 40, "Lightweight business laptop with strong keyboard", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Sony WH-1000XM5", 7L, 2399.00, 120, "Noise-cancelling headphones for travel and office", "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("AirPods Pro 2", 7L, 1799.00, 140, "Wireless earbuds with active noise cancellation", "https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Men Casual Jacket", 5L, 299.00, 180, "Lightweight everyday jacket for spring and autumn", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Women Knit Dress", 6L, 199.00, 160, "Comfortable knit dress for commuting and daily wear", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Running Shoes Air Flow", 10L, 599.00, 120, "Breathable running shoes with soft cushioning", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Sapiens", 8L, 68.00, 150, "Popular history book about human civilization", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Smart Desk Lamp", 9L, 299.00, 110, "Adjustable desk lamp for reading and work", "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&auto=format&fit=crop&q=80"));
        products.add(createProduct("Camping Tent 2P", 11L, 599.00, 60, "Portable tent for weekend camping", "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80"));

        products.forEach(productMapper::insert);
        log.info("Initialized {} sample products", products.size());
    }

    private ProductCategory createCategory(Long id, String name, Long parentId, Integer sort) {
        ProductCategory category = new ProductCategory();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setSort(sort);
        category.setStatus(1);
        return category;
    }

    private Product createProduct(String name, Long categoryId, Double price, Integer stock, String description, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(description);
        product.setCoverImg(imageUrl);
        product.setImgUrl(imageUrl);
        product.setImage(imageUrl);
        product.setStatus(1);
        return product;
    }
}
