package com.vz.rocketmq.clients.config;

import lombok.Data;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author visy.wang
 * @description: RocketMQ生产者配置
 * @date 2023/3/17 15:30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq.producer")
public class MQProducerConfigure {
    public static final Logger logger = LoggerFactory.getLogger(MQProducerConfigure.class);

    /**
     * 分组组名称
     */
    private String groupName;
    /**
     * NameServer地址
     */
    private String nameSrvAddr;
    /**
     * 消息最大值
     */
    private Integer maxMsgSize;
    /**
     * 消息发送超时时间
     */
    private Integer timeOut;
    /**
     * 发送失败重试次数
     */
    private Integer retryTimes;

    @Bean
    @ConditionalOnProperty(prefix = "rocketmq.producer", value = "isOn", havingValue = "true")
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        logger.info("MQ生产者正在创建...");
        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(nameSrvAddr);
        producer.setVipChannelEnabled(false);
        producer.setMaxMessageSize(maxMsgSize);
        producer.setSendMsgTimeout(timeOut);
        producer.setRetryTimesWhenSendAsyncFailed(retryTimes);
        producer.start();
        logger.info("MQ生产者创建成功!");
        return producer;
    }
}
