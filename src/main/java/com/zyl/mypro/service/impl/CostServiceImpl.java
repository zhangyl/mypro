package com.zyl.mypro.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.zyl.mypro.bean.Cost;
import com.zyl.mypro.mapper.CostMapper;
import com.zyl.mypro.service.CostService;

import java.util.List;

@Service
public class CostServiceImpl implements CostService {

	@Resource
	private CostMapper costMapper;

	@Override
	public void insert(Cost cost)  {
		System.out.println("insert:" + costMapper.insert(cost));
	}

	@Override
	public void insert2(Cost cost)  {
		System.out.println("insert2:" + costMapper.insert(cost));
	}

	@Override
	public Cost select(Integer id) {
		return costMapper.selectByPrimaryKey(id);
	}

	@Override
	public List<Cost> listByEntCode(String entCode) {
		return costMapper.listByEntCode(entCode);
	}

}