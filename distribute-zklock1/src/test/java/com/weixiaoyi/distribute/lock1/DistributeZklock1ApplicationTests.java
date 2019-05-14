package com.weixiaoyi.distribute.lock1;

import com.weixiaoyi.distribute.lock1.service.DistributeService;
import com.weixiaoyi.distribute.lock1.util.ZookeeperLock;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributeZklock1ApplicationTests {

    @Resource
    private ZookeeperLock zookeeperLock;

    @Resource
    private DistributeService distributeService;

    @Test
    public void contextLoads() throws InterruptedException {
        distributeService.distributeServer();
    }

}
