package com.example.ecommerceback.review.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ecommerceback.review.entity.ProductReview;
import com.example.ecommerceback.review.entity.ReviewLike;
import com.example.ecommerceback.review.mapper.ProductReviewMapper;
import com.example.ecommerceback.review.mapper.ReviewLikeMapper;
import com.example.ecommerceback.user.entity.User;
import com.example.ecommerceback.user.mapper.UserMapper;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ProductReviewMapper reviewMapper;
    private final ReviewLikeMapper likeMapper;
    private final UserMapper userMapper;
    
    /**
     * 添加评价
     */
    @Transactional
    public Result<ProductReview> addReview(Long userId, ProductReview review) {
        try {
            review.setUserId(userId);
            review.setCreateTime(LocalDateTime.now());
            review.setUpdateTime(LocalDateTime.now());
            
            reviewMapper.insert(review);
            return Result.success(review);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "添加评价失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取商品评价列表
     */
    public Result<List<ProductReview>> getProductReviews(Long productId, Long currentUserId) {
        try {
            QueryWrapper<ProductReview> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("product_id", productId);
            queryWrapper.orderByDesc("create_time");
            
            List<ProductReview> reviews = reviewMapper.selectList(queryWrapper);
            
            // 填充用户信息和点赞信息
            for (ProductReview review : reviews) {
                // 获取用户信息
                User user = userMapper.selectById(review.getUserId());
                if (user != null) {
                    review.setUsername(user.getUsername());
                    review.setUserAvatar(user.getAvatar());
                }
                
                // 获取点赞数
                QueryWrapper<ReviewLike> likeQuery = new QueryWrapper<>();
                likeQuery.eq("review_id", review.getId());
                review.setLikeCount(Math.toIntExact(likeMapper.selectCount(likeQuery)));
                
                // 检查当前用户是否点赞
                if (currentUserId != null) {
                    QueryWrapper<ReviewLike> userLikeQuery = new QueryWrapper<>();
                    userLikeQuery.eq("review_id", review.getId());
                    userLikeQuery.eq("user_id", currentUserId);
                    review.setIsLiked(likeMapper.selectCount(userLikeQuery) > 0);
                } else {
                    review.setIsLiked(false);
                }
            }
            
            return Result.success(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取评价列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 点赞/取消点赞
     */
    @Transactional
    public Result<String> toggleLike(Long userId, Long reviewId) {
        try {
            QueryWrapper<ReviewLike> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("review_id", reviewId);
            queryWrapper.eq("user_id", userId);
            
            ReviewLike existingLike = likeMapper.selectOne(queryWrapper);
            
            if (existingLike != null) {
                // 已点赞，取消点赞
                likeMapper.deleteById(existingLike.getId());
                return Result.success("已取消点赞");
            } else {
                // 未点赞，添加点赞
                ReviewLike like = new ReviewLike();
                like.setReviewId(reviewId);
                like.setUserId(userId);
                like.setCreateTime(LocalDateTime.now());
                likeMapper.insert(like);
                return Result.success("点赞成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除评价
     */
    @Transactional
    public Result<String> deleteReview(Long userId, Long reviewId) {
        try {
            ProductReview review = reviewMapper.selectById(reviewId);
            if (review == null) {
                return Result.error(404, "评价不存在");
            }
            
            if (!review.getUserId().equals(userId)) {
                return Result.error(403, "无权删除他人评价");
            }
            
            // 删除评价的点赞记录
            QueryWrapper<ReviewLike> likeQuery = new QueryWrapper<>();
            likeQuery.eq("review_id", reviewId);
            likeMapper.delete(likeQuery);
            
            // 删除评价
            reviewMapper.deleteById(reviewId);
            
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
}
