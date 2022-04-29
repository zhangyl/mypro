package com.zyl.mypro.aop;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

/**
 * 查询语句字段追加
 *
 * @author maple
 * @version 1.0
 * @since 2021-11-22 00:50
 */
public class MySelectVisitor extends MySqlASTVisitorAdapter {

    @Override
    public boolean visit(SQLBinaryOpExpr x) {
        // 在此阶段判断上一个节点是什么 并进行各种判断
        SQLObject parent = x.getParent();
        // where块
        if (parent instanceof MySqlSelectQueryBlock) {
            SQLExpr newExpr = getNewExprIn(x);
            ((MySqlSelectQueryBlock) parent).setWhere(newExpr);
            return false;
        }
        
        return false;
    }
    /**
     * sql改写，动态拼接in demo
     * @param x
     * @param where
     * @return
     */
    public SQLExpr getNewExprIn(SQLBinaryOpExpr x) {
		SQLExpr allOpExpr = x;
    	
		SQLInListExpr inListOpExpr = new SQLInListExpr();
		
    	String tableName = "cost";
    	String columnName = "ent_code";
		inListOpExpr.setExpr(new SQLPropertyExpr(tableName, columnName));
		
		List<SQLExpr> list = new ArrayList<>();
		list.add(new SQLCharExpr("13858181234"));
		list.add(new SQLCharExpr("13858181235"));
		inListOpExpr.setTargetList(list);
		
		allOpExpr = SQLBinaryOpExpr.and(allOpExpr, inListOpExpr);
		
        return allOpExpr;
    }
    /**
     * sql改写动态改写 = 
     * @param x
     * @return
     */
    public SQLExpr getNewExprEqual(SQLBinaryOpExpr x) {
    	SQLExpr allOpExpr = x;
    	SQLBinaryOpExpr addOpExpr = new SQLBinaryOpExpr();
    	// 如果别名为空则将表名做别名处理
    	String tableName = "cost";
    	String columnName = "ent_code";
    	addOpExpr.setLeft(new SQLPropertyExpr(tableName, columnName));
    	addOpExpr.setOperator(SQLBinaryOperator.Equality);
    	addOpExpr.setRight(new SQLCharExpr("13858181234"));
    	allOpExpr = SQLBinaryOpExpr.and(allOpExpr, addOpExpr);
    	return allOpExpr;
    }
}
