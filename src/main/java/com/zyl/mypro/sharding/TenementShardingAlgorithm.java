package com.zyl.mypro.sharding;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.*;
import java.util.zip.CRC32;

public final class TenementShardingAlgorithm implements StandardShardingAlgorithm<String> {

    private Properties props;
    private Set<String> specialTenementSet;

    private int modNumber = 1;

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {

        String value = shardingValue.getValue();

        String actuallyTableName;
        //大租户独占表
        if(specialTenementSet.contains(value)) {
            actuallyTableName = shardingValue.getLogicTableName() + "_" + value;
            return actuallyTableName;
        }
        //普通租户分表
        CRC32 crc32 = new CRC32();
        crc32.update(value.getBytes());
        //抽配置
        long suffix = crc32.getValue() % modNumber;
        actuallyTableName = shardingValue.getLogicTableName() + "_" + suffix;
        if (availableTargetNames.contains(actuallyTableName)) {
            return actuallyTableName;
        }

        return null;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
        return null;
    }


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
        return "Tenement";
    }
}
