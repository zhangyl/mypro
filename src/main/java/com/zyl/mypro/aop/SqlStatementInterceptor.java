package com.zyl.mypro.aop;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})


public class SqlStatementInterceptor implements Interceptor {

    @Autowired private SqlStatementProxyHandler sqlStatementProxyHandler;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String origSql = "";
        Object result = realTarget(invocation.getArgs()[0]);

        if(result instanceof PreparedStatementLogger) {
            PreparedStatementLogger preparedStatementLogger = (PreparedStatementLogger) result;
            PreparedStatement preparedStatement = preparedStatementLogger.getPreparedStatement();
            if (preparedStatement instanceof DruidPooledPreparedStatement) {
                DruidPooledPreparedStatement druidPooledPreparedStatement = (DruidPooledPreparedStatement) preparedStatement;
                PreparedStatement rawStatement = druidPooledPreparedStatement.getRawStatement();
                String origSqlClassStr = "";
                if (rawStatement instanceof ClientPreparedStatement) {
                    ClientPreparedStatement statement = (ClientPreparedStatement) rawStatement;
                    origSql = statement.asSql();
                } else if (rawStatement instanceof PreparedStatementProxyImpl) {
                    PreparedStatementProxyImpl statement = (PreparedStatementProxyImpl) rawStatement;
                    origSqlClassStr = statement.getRawObject().toString();
                }
                System.out.println(origSql);

                SQLStatement sqlStatement = SQLUtils.parseSingleStatement(origSql, "mysql");

                if(sqlStatement instanceof SQLUpdateStatement) {
                    System.out.println("执行的SQL result=" + result);
                    System.out.println("执行的SQL: \n" + origSql);
                    System.out.println("执行的SQL sqlStatement: \n" + sqlStatement);

                    List<Map<String, Object>> beforeImageList = sqlStatementProxyHandler.beforeImage((SQLUpdateStatement)sqlStatement);
                    System.out.println("执行SQL前数据: \n" + beforeImageList);

                    Object o = invocation.proceed();

                    List<Map<String, Object>> afterImageList = sqlStatementProxyHandler.afterImage((SQLUpdateStatement)sqlStatement, null, beforeImageList);
                    System.out.println("执行SQL后数据: \n" + afterImageList);

                    return o;
                }
            }

        }
//        String origSql = statementHandler.getBoundSql().getSql();


        Object o = invocation.proceed();
        // 继续执行后续操作
        return o;
    }

    /**
     * 获得真正的处理对象,可能多层代理.
     */
    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h"));
        }
        return (T) target;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以接收配置的属性
    }
}