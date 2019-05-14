package com.weixiaoyi.distribute.zklock3.service;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 22019-5-14 14:13:20
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
