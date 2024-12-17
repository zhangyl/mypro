package com.zyl.mypro.job;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyElasticJob implements SimpleJob {

    private static final Logger log = LoggerFactory.getLogger(MyElasticJob.class);

    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("shardingCount={}, shardingItem={}", shardingContext.getShardingTotalCount(), shardingContext.getShardingItem());
    }
}
