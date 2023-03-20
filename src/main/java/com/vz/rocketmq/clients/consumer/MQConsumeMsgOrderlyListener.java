package com.vz.rocketmq.clients.consumer;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author visy.wang
 * @description: 消费者监听器(顺序消息)
 * @date 2023/3/20 14:58
 */
@Component
public class MQConsumeMsgOrderlyListener implements MessageListenerOrderly {
    public static final Logger logger = LoggerFactory.getLogger(MQConsumeMsgOrderlyListener.class);

    /**
     * 消费消息
     * @param list 消息列表
     * @param context 上下文
     * @return 消费结果
     * ========================================
     * 与实现”MessageListenerConcurrently“接口的区别：
     * MessageListenerConcurrently：一个队列可能会被多个线程消费
     * MessageListenerOrderly：一个队列只会被一个线程消费
     */
    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext context) {
        context.setAutoCommit(false); //关闭自动提交

        if (CollectionUtils.isEmpty(list)) {
            logger.info("MQ接收消息为空，直接返回成功");
            return ConsumeOrderlyStatus.SUCCESS;
        }
        MessageExt msg = list.get(0);

        String topic = msg.getTopic();
        String tags = msg.getTags();
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);

        logger.info("MQ消息：消息数：{}, topic={}, tags={}, body={}", list.size(), topic, tags, body);

        // TODO: 处理业务逻辑

        return ConsumeOrderlyStatus.SUCCESS; //消费成功
    }
}
