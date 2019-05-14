package com.weixiaoyi.distribute.zklock3.util;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 2019/5/7 18:13
 * @description：管理自创建bean对象
 * @modified By：
 * @version: 1.0
 */
@Component
@Configuration
@Slf4j
public class Beans {

    @Value(value = "${zklock.cliPath}")
    private String zklockPath;

    /**
     * 实例化zookeeper客户端连接对象
     *
     * @return
     */
    @Bean
    public ZkClient configZkLock() {
        log.info("创建zk连接");
        ZkClient zkClient = new ZkClient(zklockPath, 5 * 1000);
        return zkClient;
    }

}
