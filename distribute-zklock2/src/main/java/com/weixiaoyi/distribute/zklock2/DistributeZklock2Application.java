package com.weixiaoyi.distribute.zklock2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * zookeeper 节点类型：
*   PERSISTENT ： 永久节点
 *  EPHEMERAL ： 临时节点
 *  PERSISTENT_SEQUENTIAL ： 永久有序节点
 *  EPHEMERAL_SEQUENTIAL ： 临时有序节点
 *
 * 如果只是防止业务并发访问  可以使用临时节点  每次删除节点后让其他等待线程一起去创建节点 创建节点成功的 即为拿到所
 * 如果类似秒杀活动的 要按照访问顺序进行顺序处理业务 这时 就要用临时有序节点 根据创建节点的序号去监听前一个节点的状态 前一个节点被删除 则可以拿到所
 * 临时节点是为了防止某个客户端挂掉或者zookeeper挂掉 出现死锁情况
 *
 * @return
 */
@SpringBootApplication
public class DistributeZklock2Application {

    public static void main(String[] args) {
        SpringApplication.run(DistributeZklock2Application.class, args);
    }

}
