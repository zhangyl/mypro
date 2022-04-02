package com.zyl.mypro.service;

import com.zyl.mypro.bean.Cost;

public interface CostService {
	void insert(Cost cost) ;
	void insert2(Cost cost) ;
	Cost select(Integer id);
}
