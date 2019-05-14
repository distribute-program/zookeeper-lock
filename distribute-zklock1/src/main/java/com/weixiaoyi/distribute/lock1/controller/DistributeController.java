package com.weixiaoyi.distribute.lock1.controller;

import com.weixiaoyi.distribute.lock1.service.DistributeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * @author ：yuanLong Wei
 * @date ：Created in 2019/5/7 15:13
 * @description：zklock1前端控制器
 * @modified By：
 * @version: 1.0
 */
@Slf4j
@RestController
public class DistributeController {

    @Resource
    private DistributeService distributeService;

    @RequestMapping("findServer")
    public String findServer() {
        String result = null;
        try {
            result = distributeService.distributeServer();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "出现异常";
        }
    }

}
