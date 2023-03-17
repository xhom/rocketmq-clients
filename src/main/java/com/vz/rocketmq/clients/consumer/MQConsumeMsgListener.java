package com.vz.rocketmq.clients.consumer;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author visy.wang
 * @description: 消费者监听器
 * @date 2023/3/17 17:50
 */
@Component
public class MQConsumeMsgListener implements MessageListenerConcurrently {
    public static final Logger logger = LoggerFactory.getLogger(MQConsumeMsgListener.class);

    /**
     * 消费消息
     * @param list 消息列表，默认msg里只有一条消息，可以通过设置maxConsumeSize参数来批量接收消息
     * @param context 上下文
     * @return 消费结果，消费成功后返回CONSUME_SUCCESS，否则consumer会重新消费该消息，直到返回CONSUME_SUCCESS
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
        if (CollectionUtils.isEmpty(list)) {
            logger.info("MQ接收消息为空，直接返回成功");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        MessageExt msg = list.get(0);
        try {
            String topic = msg.getTopic();
            String tags = msg.getTags();
            String body = new String(msg.getBody(), StandardCharsets.UTF_8);

            logger.info("MQ消息：消息数：{}, topic={}, tags={}, body={}", list.size(), topic, tags, body);

            // TODO: 处理业务逻辑

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; //消费成功
        } catch (Exception e) {
            logger.error("获取MQ消息内容异常{}",e.getMessage(), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER; //消费失败，重新就回到队列，等待下次消费
        }
    }
}
