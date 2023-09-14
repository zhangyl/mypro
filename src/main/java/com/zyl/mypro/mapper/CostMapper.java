package com.zyl.mypro.mapper;

import com.zyl.mypro.bean.Cost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CostMapper {

    int insert(Cost record);

    int insertSelective(Cost record);

    Cost selectByPrimaryKey(Integer id);

    List<Cost> listByEntCode(String entCode);

    int updateByPrimaryKeySelective(Cost record);

    int updateByPrimaryKey(Cost record);
    
    int sum();
}