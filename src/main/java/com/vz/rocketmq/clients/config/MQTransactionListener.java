package com.vz.rocketmq.clients.config;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author visy.wang
 * @description: MQ事务监听器
 * @date 2023/3/20 16:26
 */
public class MQTransactionListener implements TransactionListener {
    //测试用，递增的ID
    private final AtomicInteger transactionIndex = new AtomicInteger(0);

    private final ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

    /**
     * 执行本地事务
     * 此方法是半事务消息发送成功后，执行本地事务的方法，
     * 具体执行完本地事务后，可以在该方法中返回以下三种状态：
     * 1.LocalTransactionState.COMMIT_MESSAGE：提交事务，允许消费者消费该消息
     * 2.LocalTransactionState.ROLLBACK_MESSAGE：回滚事务，消息将被丢弃不允许消费
     * 3.LocalTransactionState.UNKNOW：暂时无法判断状态，等待固定时间以后Broker端根据回查规则向生产者进行消息回查
     * @param message 半事务消息
     * @param o 自定义业务参数
     * @return 事务提交状态
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        int value = transactionIndex.getAndIncrement();
        int status = value % 3;
        localTrans.put(message.getTransactionId(), status);
        return LocalTransactionState.UNKNOW;
    }

    /**
     * 检查本地事务
     * 此方法是由于二次确认消息没有收到，Broker端回查事务状态的方法
     * 回查规则：
     * 本地事务执行完成后，若Broker端收到的本地事务返回状态为LocalTransactionState.UNKNOW，
     * 或生产者应用退出导致本地事务未提交任何状态。
     * 则Broker端会向消息生产者发起事务回查，第一次回查后仍未获取到事务状态，则之后每隔一段时间会再次回查。
     * @param messageExt 检查消息
     * @return 事务状态
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        Integer status = localTrans.get(messageExt.getTransactionId());
        if (null != status) {
            switch (status) {
                case 0:
                    return LocalTransactionState.UNKNOW;
                case 1:
                    return LocalTransactionState.COMMIT_MESSAGE;
                case 2:
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                default:
                    return LocalTransactionState.COMMIT_MESSAGE;
            }
        }
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
