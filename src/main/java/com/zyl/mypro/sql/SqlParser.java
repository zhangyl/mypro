package com.zyl.mypro.sql;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat.Condition;

/**
 * @author Qi.qingshan
 */
public class SqlParser {
    public static void main(String[] args) throws Exception {
        String sql = "select  a.name, a.id, b.ent_code, b.corp_name from acct a, (select * from ttt where id>5) b where b.dept_id = a.id and a.cola = 'ccc' limit 10";
        String dbType = "mysql";
        System.out.println("原始SQL 为 ： "+sql);
        
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        List<SQLStatement> list = SQLUtils.parseStatements(sql, dbType);
        for(SQLStatement statement : list) {
        	statement.accept(visitor);
        	List<Condition> conditionList = visitor.getConditions();
        	System.out.println("conditionList = "+conditionList);
        }
        
        SQLSelectStatement statement = (SQLSelectStatement) parser(sql, dbType);
        SQLSelect select = statement.getSelect();

        SQLSelectQueryBlock query = (SQLSelectQueryBlock) select.getQuery();
        
        SQLTableSource tableSource = query.getFrom();
        if(tableSource instanceof SQLJoinTableSource) {
        	SQLJoinTableSource joinTable = (SQLJoinTableSource)tableSource;
        	SQLExpr condition = joinTable.getCondition();
        	System.out.println("condition => " + condition);

        }
        
        SQLExpr where = query.getWhere();
        System.out.println("where => " + where);
        
        List<SQLSelectItem> selectList = query.getSelectList();
        System.out.println(selectList);
//        SQLExprTableSource tableSource = (SQLExprTableSource) query.getFrom();
//        String tableName = tableSource.getExpr().toString();
//        System.out.println("获取的表名为  tableName ：" + tableName);
        //修改表名为acct_1
//        tableSource.setExpr("acct_1");
        System.out.println("修改表名后的SQL 为 ： [" + statement.toString() +"]");
    }
    public static SQLStatement parser(String sql,String dbType) throws SQLSyntaxErrorException {
    	List<SQLStatement> list = SQLUtils.parseStatements(sql, dbType);
        if (list.size() > 1) {
            throw new SQLSyntaxErrorException("MultiQueries is not supported,use single query instead ");
        }
        return list.get(0);
    }
}
