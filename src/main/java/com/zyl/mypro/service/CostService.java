package com.zyl.mypro.service;

import com.zyl.mypro.bean.Cost;

import java.util.Date;
import java.util.List;

public interface CostService {
	void insert(Cost cost) ;
	void insert2(Cost cost) ;
	Cost select(Integer id);

	List<Cost> listByEntCode(String entCode, Date date);
}
