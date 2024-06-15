package com.zyl.mypro.mapper;

import com.zyl.mypro.bean.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectByPrimaryKey(Integer id);
}
