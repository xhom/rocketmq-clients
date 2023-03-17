package com.vz.rocketmq.clients.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author visy.wang
 * @description: 生产者服务
 * @date 2023/3/17 13:52
 */
public interface MyProducerService {
    Logger logger = LoggerFactory.getLogger(MyProducerService.class);

    /**
     * 发送消息
     * @param topic 消息发送的目标Topic名称，需要提前创建
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param body 消息体，自动转JSON形式
     */
    boolean sendMessage(String topic, String msgKey, String msgTag, Object body);
    boolean sendMessage(String topic, String msgTag, Object body);
    boolean sendMessage(String topic, Object body);



}
