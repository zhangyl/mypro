package com.zyl.mypro.job;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author lujianqing
 * 创建时间 2023/2/22
 */
@Component
public class SnowflakeWorkerIdAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeWorkerIdAlgorithm.class);


    private String realNodePath;

    private final Random random = new Random();

    private CuratorFramework client;



    @PostConstruct
    public void init() {

        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .connectionTimeoutMs(30000)
                .retryPolicy(new ExponentialBackoffRetry(2000, 4))
                .sessionTimeoutMs(30000)
                .canBeReadOnly(false)
                .defaultData(null)
                .build();
        client.start();

        try {
            if (client.checkExists().forPath("/zyl") == null) {
                client.create().creatingParentsIfNeeded().forPath("/zyl", null);
            }
            //        String digest = "artisan:Xe7+HMYId2eNV48821ZrcFwIqIE=:cdrwa";
            //        String digest = "zyl:GvvxoMGdcvZIFr+yQpIJ4iSB/mc=:cdrwa";
            String digest = DigestAuthenticationProvider.generateDigest("mic:mic");
            Id id = new Id("digest", digest);
            List<ACL> aclList = new ArrayList<>();
            aclList.add(new ACL(ZooDefs.Perms.ALL,id));
            String node = client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .withACL(aclList,false)
                    .forPath("/zyl", "111".getBytes("UTF-8"));
            logger.info("创建成功node={}", node);

        }catch (Exception e) {
            logger.error("初始化失败：", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(realNodePath);
        } catch (Exception ignore) {
            logger.warn("zookeeper client close error message:{}", ignore.getMessage());
        } finally {
            client.close();
        }
    }


    public int allocate(String localHost, int workerQuantity) {

        Set<String> activatedNodeSet = new HashSet<>();
        Collection<String> activatedNode;
        try {
            activatedNode = client.getChildren().forPath("");
        } catch (Exception e) {

        }
        return random.nextInt(workerQuantity);
    }



}
