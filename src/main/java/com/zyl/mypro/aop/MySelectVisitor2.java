package com.zyl.mypro.aop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

/**
 * 查询语句字段追加
 */
public class MySelectVisitor2 extends MySqlASTVisitorAdapter {

    @Override
    public boolean visit(SQLBinaryOpExpr x) {
        // 在此阶段判断上一个节点是什么 并进行各种判断
        SQLObject parent = x.getParent();
        // where块
        if (parent instanceof MySqlSelectQueryBlock) {
            // 获取表信息时忽略右表
            List<TableInfo> tableInfoList = getTableInfoList(((MySqlSelectQueryBlock) parent).getFrom(),
                    new ArrayList<>(), Boolean.FALSE);
            SQLExpr newExpr = getNewExprIn(tableInfoList, x);
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
    public SQLExpr getNewExprIn(List<TableInfo> tableInfoList, SQLBinaryOpExpr x) {
		SQLExpr allOpExpr = x;
		for (TableInfo item : tableInfoList) {
			SQLInListExpr inListOpExpr = new SQLInListExpr();
			String tableName = item.getTableName();
			if(StringUtils.isNotBlank(item.getAlias())) {
				tableName = item.getAlias();
			}
//	    	String tableName = "cost";
	    	String columnName = "ent_code"; //
			inListOpExpr.setExpr(new SQLPropertyExpr(tableName, columnName));
			
			List<SQLExpr> list = new ArrayList<>();
			list.add(new SQLCharExpr("13858181234"));
			list.add(new SQLCharExpr("13858181235"));
			inListOpExpr.setTargetList(list);
			
			allOpExpr = SQLBinaryOpExpr.and(allOpExpr, inListOpExpr);
		}
        return allOpExpr;
    }

    /**
     * 遍历并获得当前层级下的表别名
     *
     * @param tableSource   表信息 由form块获取
     * @param tableInfoList 用于迭代的表信息集合
     * @param isGetRight    关联查询是是否获取右表信息
     * @return 表信息集合
     */
    private List<TableInfo> getTableInfoList(SQLTableSource tableSource, List<TableInfo> tableInfoList,
                                             Boolean isGetRight) {
        if (tableSource instanceof SQLSubqueryTableSource) {
        	//子查询表名为空
            tableInfoList.add(new TableInfo("", tableSource.getAlias()));
        }

        if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinSource = (SQLJoinTableSource) tableSource;
            getTableInfoList(joinSource.getLeft(), tableInfoList, isGetRight);
            // 这里如果是join语句在where条件中是不需要加入右表的 因为关联查询关联表不应该影响数据条数 应该只影响关联结果
            if (isGetRight) {
                getTableInfoList(joinSource.getRight(), tableInfoList, true);
            }
        }
        else if (tableSource instanceof SQLExprTableSource) {
            tableInfoList.add(new TableInfo(String.valueOf(tableSource), tableSource.getAlias()));
        }
        return tableInfoList;
    }
}
