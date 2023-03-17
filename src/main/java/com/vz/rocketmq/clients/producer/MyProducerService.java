package com.vz.rocketmq.clients.producer;

import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author visy.wang
 * @description: 生产者服务
 * @date 2023/3/17 13:52
 */
public interface MyProducerService {
    Logger logger = LoggerFactory.getLogger(MyProducerService.class);

    /**
     * 发送消息
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param body 消息体，自动转JSON形式
     */
    boolean sendMessage(String topic, String msgTag, String msgKey, Object body);
    boolean sendMessage(String topic, String msgTag, Object body);
    boolean sendMessage(String topic, Object body);

    /**
     * 发送消息（异步）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param body 消息体，自动转JSON形式
     * @param callback 异步回调 <是否发送成功，msgId（成功）或者错误信息（失败）>
     */
    void sendMessageAsync(String topic, String msgTag, String msgKey,
                          Object body, BiConsumer<Boolean,String> callback);
    void sendMessageAsync(String topic, String msgTag,
                          Object body, BiConsumer<Boolean,String> callback);
    void sendMessageAsync(String topic, Object body,
                          BiConsumer<Boolean,String> callback);



}
