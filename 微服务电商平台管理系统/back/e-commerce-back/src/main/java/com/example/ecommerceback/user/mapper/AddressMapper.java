package com.example.ecommerceback.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ecommerceback.user.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, id DESC")
    List<Address> selectByUserId(Long userId);

    @Update("UPDATE user_address SET is_default = #{isDefault} WHERE user_id = #{userId}")
    void updateDefaultByUserId(@Param("userId") Long userId, @Param("isDefault") Integer isDefault);
}
