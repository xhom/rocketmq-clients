package com.vz.rocketmq.clients.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author visy.wang
 * @description: 事务消息测试服务
 * @date 2023/3/21 10:49
 */
@Service
public class TransactionTestServiceImpl implements TransactionTestService {
    @Override
    @Transactional
    public void addUser(String userName) {

    }
}
