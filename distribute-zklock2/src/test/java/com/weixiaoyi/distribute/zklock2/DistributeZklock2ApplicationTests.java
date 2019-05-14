package com.weixiaoyi.distribute.zklock2;

import com.weixiaoyi.distribute.zklock2.controller.DistributeController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributeZklock2ApplicationTests {

    @Resource
    private DistributeController distributeController;

    @Test
    public void contextLoads() {

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                distributeController.findServer();
            }).start();
        }

    }

}
