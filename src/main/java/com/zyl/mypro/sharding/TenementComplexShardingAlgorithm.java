package com.zyl.mypro.sharding;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.zip.CRC32;

public final class TenementComplexShardingAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {
    private static final Logger log = LoggerFactory.getLogger(TenementComplexShardingAlgorithm.class);
    private Properties props;
    private Set<String> specialTenementSet;

    private int modNumber = 1;


    private final String columnEntCode = "ent_code";
    private final String columnCreateTime = "create_time";



    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void init(Properties properties) {

        this.props = properties;

        String specialTenement = props.getProperty("special-tenement");
        List<String> tenementList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(specialTenement);
        specialTenementSet = new HashSet<>(tenementList);

        String modNum = props.getProperty("mod-number");
        if(StringUtils.isNotBlank(modNum)) {
            modNumber = Integer.parseInt(modNum.trim());
        }

    }

    @Override
    public String getType() {
        return "TenementComplex";
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<Comparable<?>> shardingValue) {


        String value = "";

        Map<String, Collection<Comparable<?>>> columnValuesMap = shardingValue.getColumnNameAndShardingValuesMap();
        Collection<Comparable<?>> columnValueList = columnValuesMap.get(columnEntCode);
        if(CollectionUtils.isNotEmpty(columnValueList)) {
            value = (String)columnValueList.iterator().next();
        }

        String actuallyTableName;
        //大租户独占表
        if(specialTenementSet.contains(value)) {
            Map<String, Range<Comparable<?>>> rangeValueMap = shardingValue.getColumnNameAndRangeValuesMap();
            Range<Comparable<?>> createTimeRange = rangeValueMap.get(columnCreateTime);
            if(createTimeRange != null) {
                Comparable<?> upperEndpoint = createTimeRange.upperEndpoint();
                if(upperEndpoint instanceof Date) {
                    Calendar c = Calendar.getInstance();
                    c.set(2022, 1, 1);
                    Date date = c.getTime();
                    Date p = (Date)upperEndpoint;
                    // 模拟2022年之前的查询，添加表后缀_before_2022
                    if(p.before(date)) {
                        actuallyTableName = shardingValue.getLogicTableName() + "_" + value + "_before_2022";
                        return Lists.newArrayList(actuallyTableName);
                    }
                }
                log.info(upperEndpoint.toString());
            }
            shardingValue.getColumnNameAndRangeValuesMap();
            actuallyTableName = shardingValue.getLogicTableName() + "_" + value;
            return Lists.newArrayList(actuallyTableName);
        }
        //普通租户分表
        CRC32 crc32 = new CRC32();
        crc32.update(value.getBytes());
        //抽配置
        long suffix = crc32.getValue() % modNumber;
        actuallyTableName = shardingValue.getLogicTableName() + "_" + suffix;
        if (availableTargetNames.contains(actuallyTableName)) {
            return Lists.newArrayList(actuallyTableName);
        }

        return null;
    }
}
