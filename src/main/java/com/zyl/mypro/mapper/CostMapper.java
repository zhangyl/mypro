package com.zyl.mypro.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.maycur.mybatis.plugin.annotation.MaycurCanarySql;
import com.zyl.mypro.bean.Cost;

@Mapper
public interface CostMapper {

    int insert(Cost record);

    int insertSelective(Cost record);
    @MaycurCanarySql
    Cost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cost record);

    int updateByPrimaryKey(Cost record);
    
    int sum();
}