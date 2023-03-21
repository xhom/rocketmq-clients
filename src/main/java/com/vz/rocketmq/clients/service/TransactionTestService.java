package com.vz.rocketmq.clients.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author visy.wang
 * @description: 事务消息测试服务
 * @date 2023/3/21 10:48
 */
public interface TransactionTestService {
    Logger logger = LoggerFactory.getLogger(TransactionTestService.class);

    /**
     * 添加用户
     * @param userName 用户名
     */
    void addUser(String userName);

}
