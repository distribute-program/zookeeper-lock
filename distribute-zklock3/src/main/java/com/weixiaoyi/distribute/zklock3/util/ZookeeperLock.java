package com.weixiaoyi.distribute.zklock3.util;

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
 * @date ：Created in 22019-5-14 14:13:20
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

    /** 即将创建节点的名称前缀 */
    private static final String createNodeName = "/zklock-1";

    /** 记录自己创建节点的名称 以便删除释放锁 */
    private String ephemeralSequential = "";

    /** 记录上一个节点名称 以便进行监听 */
    private String lastNodeName = "";

    private Boolean isLast = true;

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
     * 获取锁 返回是否获取成功
     * zookeeper 节点类型：
     * PERSISTENT ： 永久节点
     * EPHEMERAL ： 临时节点
     * PERSISTENT_SEQUENTIAL ： 永久有序节点
     * EPHEMERAL_SEQUENTIAL ： 临时有序节点
     *
     * 如果只是防止业务并发访问  可以使用临时节点  每次删除节点后让其他等待线程一起去创建节点 创建节点成功的 即为拿到所
     * 如果类似秒杀活动的 要按照访问顺序进行顺序处理业务 这时 就要用临时有序节点 根据创建节点的序号去监听前一个节点的状态 前一个节点被删除 则可以拿到所
     * 临时节点是为了防止某个客户端挂掉或者zookeeper挂掉 出现死锁情况
     *
     * 这里模拟秒杀情况 进行有序临时节点的观测
     *
     * @return
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

            lastNodeName = getSmallNodeName();

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
        isLast = true;
        log.info("开始监听，监听的节点为：{}",lastNodeName);
        // zk启动监听
        zkClient.subscribeDataChanges(lastNodeName,iZkDataListener);

        countDownLatch = new CountDownLatch(1);
        countDownLatch.await();

        if(!isLast) {
            // 监听到前面的节点被删除 但自己还不是序号最小的节点 重新进行监听节点
            waitLock(configZkListener());
        }

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
                if(("".equals(ZookeeperLock.this.lastNodeName) || ZookeeperLock.this.lastNodeName.equals(path)) && countDownLatch != null) {
                    // 监听到前一个节点被删除 如果自己节点是最小节点 则获得到锁 否则重新获取前一个节点 重新进行监听
                    String lastNodeName = getLastNodeName();
                    if(StringUtils.isEmpty(lastNodeName)) {
                        // 前面没有节点了 获取到锁 进行堵塞释放
                        log.info("被删除的监听节点是最小的节点");
                    }else {
                        // 前一个节点被意外删除 则更换监听节点
                        isLast = false;
                        ZookeeperLock.this.lastNodeName = lastNodeName;
                        log.info("被删除的监听节点不是最小的节点，再次获取前一个节点为：{}",ZookeeperLock.this.lastNodeName);
                    }
                    countDownLatch.countDown();
                }else {
                    log.info("没有进行计数减法");
                }
            }
        };
        return iZkDataListener;
    }

    /**
     * 获取前一个序号节点 如果自己是序号最小的节点 返回null
     *
     */
    private String getLastNodeName() {
        log.info("本节点为：{}",ephemeralSequential);
        long myNodeNum = Long.valueOf(ephemeralSequential.split("-")[1]);
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

        int i = childNum.indexOf(myNodeNum);
        log.info("数组内容为：{}，查询的数据为：{}，得到的查询结果为：{}",childNum.toString(),myNodeNum,i);
        if(i > 0) {
            return LOCK_ROOT_PATH + createNodeName.substring(0,createNodeName.length()-1) + childNum.get(i-1);
        }

        // 得到最小节点
        return null;
    }

    /**
     * 获取序号最小的节点
     *
     */
    private String getSmallNodeName() {
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
        return LOCK_ROOT_PATH + createNodeName.substring(0,createNodeName.length()-1) + childNum.get(0);
    }

}

