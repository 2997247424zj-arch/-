package com.example.ecommerceback.review.controller;

import com.example.ecommerceback.review.entity.ProductReview;
import com.example.ecommerceback.review.service.ReviewService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 添加评价
     */
    @PostMapping("/add")
    public Result<ProductReview> addReview(HttpSession session, @RequestBody ProductReview review) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        if (review.getProductId() == null) {
            return Result.error(400, "商品ID不能为空");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return Result.error(400, "评分必须在1-5之间");
        }
        
        return reviewService.addReview(userId, review);
    }
    
    /**
     * 获取商品评价列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return reviewService.getProductReviews(productId, userId);
    }
    
    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{reviewId}")
    public Result<String> toggleLike(@PathVariable Long reviewId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        return reviewService.toggleLike(userId, reviewId);
    }
    
    /**
     * 删除评价
     */
    @DeleteMapping("/delete/{reviewId}")
    public Result<String> deleteReview(@PathVariable Long reviewId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        return reviewService.deleteReview(userId, reviewId);
    }
}
