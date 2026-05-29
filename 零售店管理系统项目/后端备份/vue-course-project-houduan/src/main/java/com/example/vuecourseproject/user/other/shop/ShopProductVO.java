package com.example.vuecourseproject.user.other.shop;


import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import com.example.vuecourseproject.user.entity.shop.ShopProductCategory;
import lombok.Data;

@Data
public class ShopProductVO {
    private Long id;
    private String name;
    private String barcode;
    private Double price;
    private Double originalPrice;
    private String image;
    private String description;
    private Boolean isNew;
    private Double discount;
    private String tags;
    private String specs;
    private String createTime;
    private String updateTime;

    // 分类信息
    private ShopProductCategory category;

    public static ShopProductVO fromProduct(ShopProduct product) {
        ShopProductVO vo = new ShopProductVO();
        // 复制属性
        return vo;
    }
}
