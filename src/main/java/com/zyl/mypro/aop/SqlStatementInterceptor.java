package com.zyl.mypro.aop;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;

@Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class,Object.class}),
//    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
//    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})


public class SqlStatementInterceptor implements Interceptor {

    @Autowired private SqlStatementProxyHandler sqlStatementProxyHandler;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        String sqlId = ms.getId();
        Configuration configuration = ms.getConfiguration();
        BoundSql boundSql = ms.getBoundSql(parameter);

        String origSql = realSqlParam(configuration, boundSql);

        SQLStatement sqlStatement = SQLUtils.parseSingleStatement(origSql, "mysql");

        if(sqlStatement instanceof SQLUpdateStatement) {
            Object result = realTarget(invocation.getArgs()[0]);

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

    // 如果参数是String，则添加单引号， 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    /**
     * 进行？的替换过的真正sql
     * @param configuration
     * @param boundSql
     * @return
     */
    public static String realSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // sql语句中多个空格都用一个空格代替
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (CollectionUtils.isEmpty(parameterMappings) || parameterObject == null) {
            return sql;
        }
        // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            return sql;
        }
        // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,
        // 主要支持对JavaBean、Collection、Map三种类型对象的操作
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        for (ParameterMapping parameterMapping : parameterMappings) {
            String propertyName = parameterMapping.getProperty();
            if (metaObject.hasGetter(propertyName)) {
                Object obj = metaObject.getValue(propertyName);
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                // 该分支是动态sql
                Object obj = boundSql.getAdditionalParameter(propertyName);
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
            } else {
                // 打印出缺失，提醒该参数缺失并防止错位
                sql = sql.replaceFirst("\\?", "缺失");
            }
        }
        return sql;
    }

    public String realSqlParam(Configuration configuration, BoundSql boundSql) {

        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");

        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return sql;
        }

        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value = null;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (value == null && parameterObject == null) {
                    value = null;
                } else if (value == null && typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }

                // value 转换
                if (value == null) {
                    sql = sql.replaceFirst("\\?", "null");
                } else if(value instanceof String) {
                    sql = sql.replaceFirst("\\?", "'" + value + "'");
                } else if(value instanceof Date) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sql = sql.replaceFirst("\\?", "'" + format.format((Date)value) + "'");
                } else if(value instanceof LocalDate) {
                    String v = ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    sql = sql.replaceFirst("\\?", "'" + v + "'");
                } else {
                    sql = sql.replaceFirst("\\?", value.toString());
                }
            }
        }
        return sql;
    }
}