package com.weixiaoyi.distribute.lock1.service.impl;

import com.weixiaoyi.distribute.lock1.service.DistributeService;
import com.weixiaoyi.distribute.lock1.util.ZookeeperLock;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
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
        if(zookeeperLock.tryLock()) {
            servering();
        }else {
            log.info("调用lock1服务");
            IZkDataListener iZkDataListener = zookeeperLock.configZkListener();
            boolean lockResult = zookeeperLock.waitLock(iZkDataListener);
            if(lockResult) {
                // 得到了锁 进行业务处理
                servering();
            }
        }
        return "lock1";
    }

    /**
     * 模拟业务处理 这里进行延时等待操作
     *
     * @throws InterruptedException
     */
    private void servering() throws InterruptedException {
        log.info("业务处理中 》》》》》》》》》》》》》》");
        Thread.sleep(5 * 1000);
        zookeeperLock.unlock();
    }

}
