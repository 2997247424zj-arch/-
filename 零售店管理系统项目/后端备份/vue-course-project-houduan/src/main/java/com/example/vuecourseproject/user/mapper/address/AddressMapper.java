package com.example.vuecourseproject.user.mapper.address;

import com.example.vuecourseproject.user.entity.address.Address;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AddressMapper {
    @Select("SELECT * FROM user_address")
    List<Address> findAll();

    @Select("SELECT * FROM user_address WHERE id = #{id}")
    Address selectById(Long id);

    @Insert("INSERT INTO user_address(username, name, phone, province, city, district, detailed_address) " +
            "VALUES(#{username}, #{name}, #{phone}, #{province}, #{city}, #{district}, #{detailedAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    @Update("UPDATE user_address SET username = #{username}, name = #{name}, phone = #{phone}, " +
            "province = #{province}, city = #{city}, district = #{district}, " +
            "detailed_address = #{detailedAddress} WHERE id = #{id}")
    int update(Address address);

    @Delete("DELETE FROM user_address WHERE id = #{id}")
    int delete(Long id);

    default Address findById(Long id) {
        return this.selectById(id);
    }
}