package com.weixiaoyi.distribute.lock1.service;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 2019/5/7 15:18
 * @description：zklock服务层接口
 * @modified By：
 * @version: 1.0
 */
public interface DistributeService {

    /**
     * 业务处理
     * 会睡眠5S 模拟业务花费时间
     *
     * @return
     */
    String distributeServer() throws InterruptedException;

}
