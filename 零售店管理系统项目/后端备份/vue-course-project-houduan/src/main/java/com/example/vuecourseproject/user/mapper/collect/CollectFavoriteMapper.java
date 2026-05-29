package com.example.vuecourseproject.user.mapper.collect;

import com.example.vuecourseproject.user.entity.collect.CollectFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface
CollectFavoriteMapper {

    @Insert("INSERT INTO user_favorites(product_id, username, name, description, price, create_time) " +
            "VALUES(#{productId}, #{username}, #{name}, #{description}, #{price}, NOW())")
    int insert(CollectFavorite favorite);

    @Delete("DELETE FROM user_favorites WHERE product_id = #{productId} AND username = #{username}")
    int deleteByProductIdAndUsername(@Param("productId") Long productId, @Param("username") String username);

    @Select("SELECT * FROM user_favorites WHERE product_id = #{productId} AND username = #{username}")
    CollectFavorite selectByProductIdAndUsername(@Param("productId") Long productId, @Param("username") String username);

    @Select("SELECT * FROM user_favorites WHERE username = #{username} " +
            "AND (name LIKE CONCAT('%',#{keyword},'%') OR description LIKE CONCAT('%',#{keyword},'%')) " +
            "LIMIT #{size} OFFSET #{offset}")
    List<CollectFavorite> selectByKeywordAndUsername(
            @Param("keyword") String keyword,
            @Param("username") String username,
            @Param("offset") int offset,
            @Param("size") int size);

    @Select("SELECT * FROM user_favorites WHERE username = #{username} " +
            "LIMIT #{size} OFFSET #{offset}")
    List<CollectFavorite> selectAllByUsername(
            @Param("username") String username,
            @Param("offset") int offset,
            @Param("size") int size);

    @Delete("DELETE FROM user_favorites WHERE username = #{username}")
    int deleteAllByUsername(String username);

    @Select("SELECT COUNT(*) FROM user_favorites WHERE product_id = #{productId} AND username = #{username}")
    boolean existsByProductIdAndUsername(@Param("productId") Long productId, @Param("username") String username);
}