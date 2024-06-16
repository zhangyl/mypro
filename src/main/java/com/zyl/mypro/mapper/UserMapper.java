package com.zyl.mypro.mapper;

import com.zyl.mypro.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User selectByPrimaryKey(@Param("id") Integer id);

    int updateById(@Param("id") Integer id);
}
