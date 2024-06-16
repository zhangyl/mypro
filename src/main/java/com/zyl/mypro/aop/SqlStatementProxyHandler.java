package com.zyl.mypro.aop;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuqichun
 * @date 2022-11-24
 */
@Slf4j
@Component
public class SqlStatementProxyHandler {

    private static final Logger log = LoggerFactory.getLogger(SqlStatementProxyHandler.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final HashMap<String, String> PRIMARY_COLUMN_MAP = new HashMap<>();

    @Value("#{'${log.table.names:t_user}'.split(',')}")
    private List<String> logTableNames;

    private static final String SELECT_PREFIX = "select * from ";

    @PostConstruct
    public void initPrimaryKeyMap() {
        Connection coo = null;
        try {
            coo = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
        } catch (SQLException e) {
            log.error("initPrimaryKeyMap jdbcTemplate connection error", e);
            return;
        }
        for (String tableName : logTableNames) {
            if (StringUtils.isBlank(tableName)){
                continue;
            }
            try {
                String primaryKey = fetchSchema(coo, tableName);
                PRIMARY_COLUMN_MAP.put(tableName, primaryKey);
            } catch (SQLException e) {
                log.error("initPrimaryKeyMap tableName={}", tableName, e);
            }
        }
        log.info("SqlStatementProxyHandler logTableNames={} initPrimaryKeyMap={}", JSON.toJSONString(logTableNames), JSON.toJSONString(PRIMARY_COLUMN_MAP));
    }

    public List<Map<String, Object>> beforeImage(SQLUpdateStatement sqlUpdateStatement) {

        String tableName = sqlUpdateStatement.getTableName().toString();

        if (StringUtils.isBlank(PRIMARY_COLUMN_MAP.get(tableName))){
            return Collections.emptyList();
        }

        String beforeWhere = sqlUpdateStatement.getWhere().toString();
        if (StringUtils.isBlank(beforeWhere)) {
            return Collections.emptyList();
        }

        String beforeSql = SELECT_PREFIX + tableName;
        if (!"".equals(beforeWhere.trim())) {
            beforeSql += " where " + beforeWhere.trim();
        }
        List<Map<String, Object>> beforeImageList = jdbcTemplate.queryForList(beforeSql);
        // 字符串转json
        strToJson(beforeImageList);
        return beforeImageList;
    }

    public List<Map<String, Object>> beforeImage(MySqlDeleteStatement sqlDeleteStatement) {
        String tableName = sqlDeleteStatement.getTableName().toString();
        if (StringUtils.isBlank(PRIMARY_COLUMN_MAP.get(tableName))){
            return Collections.emptyList();
        }

        boolean isRecordLoggerTable = recordLoggerTable(tableName);
        if (!isRecordLoggerTable) {
            return Collections.emptyList();
        }
        String beforeWhere = sqlDeleteStatement.getWhere().toString();
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(beforeWhere)) {
            return Collections.emptyList();
        }
        String beforeSql = SELECT_PREFIX + tableName;
        if (!"".equals(beforeWhere.trim())) {
            beforeSql += " where " + beforeWhere.trim();
        }
        List<Map<String, Object>> beforeImageList = jdbcTemplate.queryForList(beforeSql);

        // 字符串转json
        strToJson(beforeImageList);
        return beforeImageList;

    }

    public List<Map<String, Object>> beforeImage(MySqlInsertStatement sqlInsertStatement) {
        return Collections.emptyList();
    }

    public List<Map<String, Object>> afterImage(MySqlDeleteStatement sqlDeleteStatement, PreparedStatement statement,
        List<Map<String, Object>> beforeImageList) {
        String tableName = sqlDeleteStatement.getTableName().toString();
        if (StringUtils.isBlank(PRIMARY_COLUMN_MAP.get(tableName))){
            return Collections.emptyList();
        }

        boolean isRecordLoggerTable = recordLoggerTable(tableName);
        if (!isRecordLoggerTable) {
            return Collections.emptyList();
        }
        String primaryColumn = PRIMARY_COLUMN_MAP.get(tableName);

        if (CollectionUtils.isEmpty(beforeImageList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> afterImageList = beforeImageList.stream().map(imageMap -> {
            Map<String, Object> afterImageMap = new HashMap<>();
            afterImageMap.put(primaryColumn, imageMap.get(primaryColumn));
            return afterImageMap;
        }).collect(Collectors.toList());

        // 字符串转json
        strToJson(afterImageList);
        return afterImageList;
    }

    public List<Map<String, Object>> afterImage(SQLUpdateStatement sqlUpdateStatement, PreparedStatement statement,
        List<Map<String, Object>> beforeImageList) {

        String tableName = sqlUpdateStatement.getTableName().toString();
        if (StringUtils.isBlank(PRIMARY_COLUMN_MAP.get(tableName))){
            return Collections.emptyList();
        }

        if (CollectionUtils.isEmpty(beforeImageList)) {
            return Collections.emptyList();
        }

        String primaryColumn = PRIMARY_COLUMN_MAP.get(tableName);
        if (StringUtils.isBlank(primaryColumn)) {
            throw new RuntimeException("not primary key error");
        }

        if (CollectionUtils.isEmpty(beforeImageList)) {
            return Collections.emptyList();
        }

        List<Object> primaryKeyList =
            beforeImageList.stream().map(map -> map.get(primaryColumn)).collect(Collectors.toList());

        StringBuilder primaryKeyStr = new StringBuilder();
        for (Object clo : primaryKeyList) {
            primaryKeyStr.append("'").append(clo).append("'").append(",");
        }

        String primaryKeyValues = primaryKeyStr.substring(0, primaryKeyStr.length() - 1);
        List<Map<String, Object>> afterImageList = Lists.newLinkedList();
        if (StringUtils.isNotBlank(primaryKeyValues)) {
            String afterSql =
                SELECT_PREFIX + tableName + " where " + primaryColumn + " in " + "(" + primaryKeyValues + ")";
            afterImageList = jdbcTemplate.queryForList(afterSql);
        }

        if (CollectionUtils.isEmpty(afterImageList)) {
            return Collections.emptyList();
        }

        for (Map<String, Object> imageMap : afterImageList) {
            Object value = imageMap.get(primaryColumn);
            imageMap.put(primaryColumn, value);
        }
        // 字符串转json
        strToJson(afterImageList);
        return afterImageList;
    }

    public List<Map<String, Object>> afterImage(MySqlInsertStatement sqlInsertStatement, PreparedStatement statement,
        List<Map<String, Object>> beforeImageList) {
        String tableName = sqlInsertStatement.getTableName().toString();
        if (StringUtils.isBlank(PRIMARY_COLUMN_MAP.get(tableName))){
            return Collections.emptyList();
        }

        boolean isRecordLoggerTable = recordLoggerTable(sqlInsertStatement.getTableName().toString());
        if (!isRecordLoggerTable) {
            return Collections.emptyList();
        }
        List<String> columns =
            sqlInsertStatement.getColumns().stream().map(Object::toString).collect(Collectors.toList());

        List<List<SQLExpr>> sqlExprList =
            sqlInsertStatement.getValuesList().stream().map(SQLInsertStatement.ValuesClause::getValues)
                .collect(Collectors.toList());

        List<Map<String, Object>> afterImageList = sqlExprList.stream().map(sqlExprs -> {
            HashMap<String, Object> beforeImage = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                String param = sqlExprs.get(i).toString();
                String value = param.trim();

                boolean isStringFormat = (param.trim().startsWith("'") && param.trim().endsWith("'"))
                    || param.trim().startsWith("\"") && param.trim().endsWith("\"");
                if (isStringFormat) {
                    value = param.trim();
                    value = value.substring(1, value.length() - 1);
                }

                beforeImage.put(columns.get(i), value);
            }
            return beforeImage;
        }).collect(Collectors.toList());
        // 字符串转json
        strToJson(afterImageList);

        fillPkValues(sqlInsertStatement, statement, afterImageList);
        return afterImageList;
    }

    private String fetchSchema(Connection connection, String tableName) throws SQLException {
        String sql = SELECT_PREFIX + tableName + " LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return resultSetMetaToSchema(rs.getMetaData(), connection.getMetaData());
        } catch (SQLException sqlEx) {
            throw sqlEx;
        } catch (Exception e) {
            throw new SQLException(String.format("Failed to fetch schema of %s", tableName), e);
        }
    }

    private String resultSetMetaToSchema(ResultSetMetaData rsmd, DatabaseMetaData dbmd) throws SQLException {
        String colName = "";
        String tableName = rsmd.getTableName(1);
        try (ResultSet rsIndex = dbmd.getIndexInfo(rsmd.getCatalogName(1), rsmd.getSchemaName(1), tableName, false,
            true)) {
            while (rsIndex.next()) {
                String indexName = rsIndex.getString("INDEX_NAME");
                if (!"PRIMARY".equalsIgnoreCase(indexName)) {
                    continue;
                }
                colName = rsIndex.getString("COLUMN_NAME");
                PRIMARY_COLUMN_MAP.put(tableName, colName);
                return colName;
            }
        }
        if (StringUtils.isBlank(colName)) {
            throw new RuntimeException("not exist primary key");
        }
        return colName;
    }

    private void fillPkValues(MySqlInsertStatement sqlInsertStatement, PreparedStatement rawStatement,
        List<Map<String, Object>> afterImageList) {
        String tableName = sqlInsertStatement.getTableName().toString();
        String primaryColumn = PRIMARY_COLUMN_MAP.get(tableName);
        if (containsPrimaryKey(sqlInsertStatement)) {
            fillPkValuesByColumnValue(afterImageList, primaryColumn);
        } else {
            fillPkValuesByAuto(rawStatement, afterImageList, primaryColumn);
        }
    }

    private void fillPkValuesByColumnValue(List<Map<String, Object>> afterImageList, String primaryColumn) {
        for (Map<String, Object> imageMap : afterImageList) {
            Object primaryColumnValue = imageMap.get(primaryColumn);
            if (primaryColumnValue == null) {
                continue;
            }
            imageMap.put(primaryColumn, primaryColumnValue);
            break;
        }
    }

    private void fillPkValuesByAuto(PreparedStatement statement, List<Map<String, Object>> afterImageList,
        String primaryColumn) {

        List<Object> pkValues = Lists.newLinkedList();
        try {
            ResultSet generatedKeys = statement.getGeneratedKeys();
            while (generatedKeys.next()) {
                pkValues.add(generatedKeys.getObject(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < afterImageList.size(); i++) {
            Map<String, Object> imageMap = afterImageList.get(i);
            imageMap.put(primaryColumn, pkValues.get(i));
        }
    }

    private boolean containsPrimaryKey(MySqlInsertStatement sqlInsertStatement) {
        List<String> insertColumns =
            sqlInsertStatement.getColumns().stream().map(Object::toString).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(insertColumns)) {
            return false;
        }
        String tableName = sqlInsertStatement.getTableName().toString();
        String pkColumn = SqlStatementProxyHandler.PRIMARY_COLUMN_MAP.get(tableName);
        return insertColumns.stream().anyMatch(column -> StringUtils.equalsIgnoreCase(column, pkColumn));
    }

    private boolean recordLoggerTable(String tableName) {
        return StringUtils.isNotBlank(PRIMARY_COLUMN_MAP.get(tableName));
    }

    /**
     *   JSONValidator.from().validate()存在bug，不能用来判断是否是json，如果只包含数字的话，则误判为json
     *   通过JSON.parseObject转换判断，但是存在空字符的时候也会转换成null
     */
    private void strToJson(List<Map<String, Object>> imageList){
        if (CollectionUtils.isEmpty(imageList)){
            return;
        }
        for (Map<String, Object> imageMap : imageList) {
            for (Map.Entry<String, Object> entry : imageMap.entrySet()) {
                if (!(entry.getValue() instanceof String)){
                    continue;
                }
                String valueStr = (String)entry.getValue();
                if ("".equals(valueStr.trim())){
                    continue;
                }
                valueStr = StringEscapeUtils.unescapeJava(valueStr);
                log.debug("strToJson valueStr={}", valueStr);
                try {
                    JSONObject valueJson = JSON.parseObject(valueStr);
                    entry.setValue(valueJson);
                } catch (Exception ignore) {
                    try {
                        List<Object> valueList = JSON.parseArray(valueStr);
                        entry.setValue(valueList);
                    }catch (Exception ignore1) {
                    }
                }
            }
        }
    }

}
