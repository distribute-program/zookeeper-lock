package com.weixiaoyi.distribute.zklock3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 版本二可以进行分布式多节点公用同一把锁 而且是将请求排序的有序锁
 *
 * 但是 如果部署到多个环境的系统其中某一个节点挂掉， 则此节点zk客户端挂掉，引发zk中删除大量临时节点
 * 而其他系统节点监听到这些节点被删除，则都会得到锁 进行业务处理  此时变成并发处理
 *
 * 此版本避免了这种情况发生：
 *  1. 所监听的节点被删除后，判断本节点是否是序号最小的节点  如果是序号最小的节点  则获得锁 进行业务操作
 *  2. 本节点不是序号最小的节点，则获取前一个节点，重新进行监听。
 *
 *  缺点：
 *      此版本每次监听到节点删除后都会进行获取所有节点进行排序、遍历操作
 *      系统节点挂掉时 所有未进行处理的请求不会进行处理  此处可配合mq进行业务处理
 *  优点：
 *      可靠性高，即使系统中有某一节点挂掉后，也可进行正常运行
 *
 */
@SpringBootApplication
public class DistributeZklock3Application {

    public static void main(String[] args) {
        SpringApplication.run(DistributeZklock3Application.class, args);
    }

}
