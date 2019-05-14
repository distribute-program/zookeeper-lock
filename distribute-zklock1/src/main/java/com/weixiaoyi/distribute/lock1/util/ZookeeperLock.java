package com.weixiaoyi.distribute.lock1.util;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
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
public class ZookeeperLock implements Lock {

    /** zookeeper创建节点的根节点 */
    @Value("${zklock.rootpath}")
    private String LOCK_ROOT_PATH;

    /** 即将创建节点的名称 */
    private static final String createNodeName = "/zklock";

    /** 线程阻塞器 */
    private CountDownLatch countDownLatch = null;

    /** zk客户端连接对象 */
    @Resource
    private ZkClient zkClient;

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
     * 这里模拟无序请求访问服务的情况
     *
     * @return
     */
    @Override
    public boolean tryLock() {
        try {
            // 创建节点 创建成功得到锁则返回true  否则返回false
            zkClient.createEphemeral(LOCK_ROOT_PATH + createNodeName);
            log.info("创建临时节点 拿到锁，返回结果为：{}");
            return true;
        }catch (Exception e) {
            log.info("获取锁失败");
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
        zkClient.delete(LOCK_ROOT_PATH + createNodeName);
        log.info("释放锁------删除节点，节点名称：{}",LOCK_ROOT_PATH + createNodeName);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    public boolean waitLock(IZkDataListener iZkDataListener) throws InterruptedException {
        log.info("开始监听");
        // zk启动监听
        zkClient.subscribeDataChanges(LOCK_ROOT_PATH + createNodeName,iZkDataListener);

        countDownLatch = new CountDownLatch(1);
        countDownLatch.await();

        log.info("监听到节点被删除");
        if(!tryLock()) {
            log.info("没有获取到锁");
            waitLock(iZkDataListener);
        }
        return true;
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
                if((LOCK_ROOT_PATH + createNodeName).equals(path) && countDownLatch != null)
                    countDownLatch.countDown();
                else
                    log.info("没有进行计数减法");
            }
        };
        return iZkDataListener;
    }

}
