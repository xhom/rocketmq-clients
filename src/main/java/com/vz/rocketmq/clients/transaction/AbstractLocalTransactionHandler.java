package com.vz.rocketmq.clients.transaction;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author visy.wang
 * @description: 本地事务处理器（抽象类）
 * @date 2023/3/23 10:15
 */
public abstract class AbstractLocalTransactionHandler implements LocalTransactionHandler {
    @Autowired
    private MQTransactionListener mqTransactionListener;

    /**
     * 类实例化时自动把自己注册到MQ的事务监听器
     */
    @PostConstruct
    public void selfRegistry() {
        mqTransactionListener.registry(this);
    }
}
