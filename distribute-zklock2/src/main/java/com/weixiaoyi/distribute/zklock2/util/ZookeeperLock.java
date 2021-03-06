package com.weixiaoyi.distribute.zklock2.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 2019/5/7 15:22
 * @description：zookeeper锁对象
 * @modified By：
 * @version: 1.0
 */
@Component
@Slf4j
@Scope("prototype")
@Getter
public class ZookeeperLock implements Lock {

    /** zookeeper创建节点的根节点 */
    @Value("${zklock.rootpath}")
    private String LOCK_ROOT_PATH;

    /** zk客户端连接对象 */
    @Autowired
    private ZkClient zkClient;

    /** 即将创建节点的名称 */
    private static final String createNodeName = "/zklock-1";

    /** 记录自己创建节点的名称 以便删除释放锁 */
    private String ephemeralSequential = "";

    /** 记录上一个节点名称 以便进行监听 */
    private String lastNodeName = "";

    /** 线程阻塞器 */
    private CountDownLatch countDownLatch = null;

    /**
     * 获取锁
     *
     */
    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    /**
     *  获取锁 返回是否获取成功
     */
    @Override
    public boolean tryLock() {
        try {
            // 创建节点 创建成功得到锁则返回true  否则返回false
            ephemeralSequential = zkClient.createEphemeralSequential(LOCK_ROOT_PATH + createNodeName, "1");

            // 创建节点后 判断节点是否创建成功 成功-》继续。 不成功-》继续创建
            boolean exists = zkClient.exists(ephemeralSequential);
            if(!exists)
                tryLock();

            // 获取所有节点 判断自己是不是最小的节点 如果不是最小的节点则得到前一个节点 进行监听
            List<String> childrens = zkClient.getChildren(LOCK_ROOT_PATH);
            List<Long> childNum = new ArrayList<>();
            // 循环获取序号
            for(String childrec : childrens) {
                String[] split = childrec.split("-");
                if(!StringUtils.isEmpty(split))
                    childNum.add(Long.valueOf(split[1]));
            }
            Collections.sort(childNum);

            // 得到最小节点
            lastNodeName = LOCK_ROOT_PATH + createNodeName.substring(0,createNodeName.length()-1) + childNum.get(0);

            if(!ephemeralSequential.equals(lastNodeName)) {
                // 如何最小节点不是本节点 则根据序号-1得到前一个节点 返回没有拿到锁
                Long integer = Long.valueOf(ephemeralSequential.split("-")[1]);
                lastNodeName = LOCK_ROOT_PATH + createNodeName.substring(0,createNodeName.length()-1) + (integer - 1);
                return false;
            }

            log.info("创建临时有序节点 拿到锁，返回结果为：{}，前一个节点名称为：{}", ephemeralSequential, lastNodeName);
            return true;
        }catch (Exception e) {
            log.info("创建节点失败");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * 释放锁
     *
     */
    @Override
    public void unlock() {
        zkClient.delete(ephemeralSequential);
        log.info("释放锁------删除节点，节点名称：{}",ephemeralSequential);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    /**
     * 等待拿到锁
     * 进行zk客户端进行zk监听，使用CountDownLatch进行计数等待
     * 直到监听到前一节点被删除，计数减一到0后，取消堵塞
     *
     * @param iZkDataListener
     * @return
     * @throws InterruptedException
     */
    public void waitLock(IZkDataListener iZkDataListener) throws InterruptedException {
        log.info("开始监听，监听的节点为：{}",lastNodeName);
        // zk启动监听
        zkClient.subscribeDataChanges(lastNodeName,iZkDataListener);

        countDownLatch = new CountDownLatch(1);
        countDownLatch.await();

        log.info("监听到节点：{} 被删除",lastNodeName);
    }

    /**
     * 初始化zk监听器
     * zk有节点变化立即会得到通知
     * 监听节点删除事件 一旦目标节点被删除  则立即创建节点
     *
     * @return
     */
    public IZkDataListener configZkListener() {
        IZkDataListener iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String path, Object data) throws Exception {
                log.info("path == {}，data == {}", path, data);
            }

            @Override
            public void handleDataDeleted(String path) throws Exception {
                log.info("节点 {} 被删除", path);
                if(("".equals(lastNodeName) || lastNodeName.equals(path)) && countDownLatch != null)
                    countDownLatch.countDown();
                else
                    log.info("没有进行计数减法");
            }
        };
        return iZkDataListener;
    }

}

