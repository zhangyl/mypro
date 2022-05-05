package com.zyl.mypro.aop;

/**
 * 表信息
 */

public class TableInfo {
    private String tableName;
    private String alias;
    
    public TableInfo(String tableName, String alias) {
    	this.alias = alias;
    	this.tableName = tableName;
    }
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
    
}
