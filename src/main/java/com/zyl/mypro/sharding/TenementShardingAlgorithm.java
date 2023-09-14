package com.zyl.mypro.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;
import java.util.zip.CRC32;

public final class TenementShardingAlgorithm implements StandardShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String value = shardingValue.getValue();
        if(value.equals("T001")) {
            String actuallyTableName = shardingValue.getLogicTableName() + "_" + "zyl";
            return actuallyTableName;
        }
        CRC32 crc32 = new CRC32();
        crc32.update(value.getBytes());
        long suffix = crc32.getValue() % 2;
        String actuallyTableName = shardingValue.getLogicTableName() + "_" + suffix;
        if (availableTargetNames.contains(actuallyTableName)) {
            return actuallyTableName;
        }
        return null;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
        return null;
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }

    @Override
    public String getType() {
        return "Tenement";
    }
}
