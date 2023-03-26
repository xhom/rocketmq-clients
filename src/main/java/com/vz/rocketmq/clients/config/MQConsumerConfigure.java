package com.vz.rocketmq.clients.config;

import com.vz.rocketmq.clients.consumer.MQConsumeMsgListener;
import lombok.Data;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author visy.wang
 * @description: RocketMQ消费者配置
 * @date 2023/3/17 17:46
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq.consumer")
public class MQConsumerConfigure {
    public static final Logger logger = LoggerFactory.getLogger(MQConsumerConfigure.class);

    /**
     * 分组组名称
     */
    private String groupName;
    /**
     * NameServer地址
     */
    private String nameSrvAddr;
    /**
     * 订阅的主题
     */
    private String topics;
    /**
     * 消费者最小线程数
     */
    private Integer minThreads;
    /**
     * 消费者最大线程数
     */
    private Integer maxThreads;
    /**
     * 一次消费消息的条数
     */
    private Integer maxConsumeSize;
    /**
     * 消费监听器
     */
    @Autowired
    private MQConsumeMsgListener consumeMsgListener;

    /**
     * mq 消费者配置
     * @return 默认消费者
     */
    @Bean //通过容器实例化调用此方法完成消费者的创建，实际使用中并不会注入：defaultMQPushConsumer
    public DefaultMQPushConsumer defaultConsumer(){
        logger.info("MQ消费者正在创建...");
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(nameSrvAddr);
        consumer.setConsumeThreadMin(minThreads);
        consumer.setConsumeThreadMax(maxThreads);
        consumer.setConsumeMessageBatchMaxSize(maxConsumeSize);

        // 设置监听，编写具体消费的逻辑
        consumer.registerMessageListener(consumeMsgListener);

        /*
         * 设置consumer第一次启动是从队列头部开始还是队列尾部开始
         * 如果不是第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        /*
         * 设置消费模型，集群还是广播(broadcasting)，默认为集群(clustering)
         */
        //consumer.setMessageModel(MessageModel.BROADCASTING);

        try {
            // 设置该消费者订阅的主题和tag，如果订阅该主题下的所有tag，则使用*,
            String[] topicArr = topics.split(";");
            for (String topic : topicArr) {
                String[] tagArr = topic.split("~");
                //可以订阅多个 topic+tag 组合
                consumer.subscribe(tagArr[0], tagArr[1]);
            }
            //启动消费者
            consumer.start();
            logger.info("MQ消费者创建成功，消费组：{}, 订阅主题：{}, NameServer地址：{}",
                    groupName, topics, nameSrvAddr);
        } catch (MQClientException e) {
            logger.info("MQ消费者创建失败，错误信息：{}", e.getMessage(), e);
        }
        return consumer;
    }
}
