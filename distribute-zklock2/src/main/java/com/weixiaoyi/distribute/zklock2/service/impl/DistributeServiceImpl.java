package com.weixiaoyi.distribute.zklock2.service.impl;

import com.weixiaoyi.distribute.zklock2.service.DistributeService;
import com.weixiaoyi.distribute.zklock2.util.ZookeeperLock;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 2019/5/7 15:19
 * @description：zklick服务层实现类‘
 * @modified By：
 * @version: 1.0
 */
@Service
@Slf4j
@Scope("prototype")
public class DistributeServiceImpl implements DistributeService {

    /** 自定义锁对象 */
    @Resource
    private ZookeeperLock zookeeperLock;

    /**
     * @see DistributeService#distributeServer()
     *
     * @return
     */
    @Override
    public String distributeServer() throws InterruptedException {
        log.info("zookeeperlock为：{}",zookeeperLock);

        if(zookeeperLock.tryLock()) {
            servering();
        }else {
            // 没有拿到锁 进行监听
            IZkDataListener iZkDataListener = zookeeperLock.configZkListener();
            zookeeperLock.waitLock(iZkDataListener);
            servering();
        }
        return zookeeperLock.getEphemeralSequential();
    }

    /**
     * 模拟业务处理 这里进行延时等待操作
     *
     * @throws InterruptedException
     */
    private void servering() throws InterruptedException {
        log.info("业务处理中 》》》》》》》》》》》》》》");
        Thread.sleep(100);
        zookeeperLock.unlock();
    }

}
