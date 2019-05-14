package com.weixiaoyi.distribute.lock1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 所有请求到来后 都会去创建同一zk节点
 * 只有创建节点成功的线程获取到锁
 * 任务执行完成后会删除这一节点
 *
 * 其他程序节点进行监听 监听到这一节点被删除后，再去创建这一zk节点。。。
 *
 */
@SpringBootApplication
public class DistributeZklock1Application {

    public static void main(String[] args) {
        SpringApplication.run(DistributeZklock1Application.class, args);
    }

}
