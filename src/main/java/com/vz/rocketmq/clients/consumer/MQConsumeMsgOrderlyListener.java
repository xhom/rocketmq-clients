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
        //设置是否自动提交： 默认自动提交，提交之后消息就不能够被再次消费。
        //关闭自动提交时,消息可能会被重复消费
        context.setAutoCommit(false);
        if (CollectionUtils.isEmpty(list)) {
            logger.info("MQ顺序消费接收消息为空");
            return ConsumeOrderlyStatus.SUCCESS;
        }
        MessageExt msg = list.get(0);
        try {
            String topic = msg.getTopic();
            String tags = msg.getTags();
            String body = new String(msg.getBody(), StandardCharsets.UTF_8);

            logger.info("MQ消息：消息数：{}, topic={}, tags={}, body={}", list.size(), topic, tags, body);

            // TODO: 处理业务逻辑

            return ConsumeOrderlyStatus.SUCCESS; //消费成功
        } catch (Exception e){
            logger.error("MQ顺序消费异常：{}",e.getMessage(), e);
            //表示等一会，再继续处理这批消息，不把这批消息放到重试队列里去处理其他消息，不会导致乱序。
            //顺序消息消费失败后，会自动进行消息重试，不进入重试队列。
            //会造成消费阻塞，因此使用顺序消息的时候要及时监控消费失败的情况，防止发生阻塞。
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
    }
}
