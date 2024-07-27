package com.zyl.mypro.mapper;

import com.zyl.mypro.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectByPrimaryKey(@Param("id") Integer id);

    int updateById(@Param("idList") List<Integer> id);

    int updateUserById(@Param("user") User user);
}
