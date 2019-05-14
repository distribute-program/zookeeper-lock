package com.weixiaoyi.distribute.lock1.controller;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.KeeperException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 2019/5/7 20:43
 * @description：
 * @modified By：
 * @version: 1.0
 */
public class TestOne {

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZkClient zkClient = new ZkClient("127.0.0.1:2181", 5 * 1000);
        IZkDataListener iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String path, Object data) throws Exception {
                System.out.println("节点改变了" + path);
            }

            @Override
            public void handleDataDeleted(String path) throws Exception {
                System.out.println("节点被删除" + path);
            }
        };
        zkClient.subscribeDataChanges("/locks/zklock",iZkDataListener);

        //junit测试时，防止线程退出
        /*TimeUnit.SECONDS.sleep(15);*/
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

}
