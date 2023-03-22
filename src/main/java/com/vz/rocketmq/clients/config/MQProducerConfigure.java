package com.vz.rocketmq.clients.config;

import com.vz.rocketmq.clients.transaction.MQTransactionListener;
import lombok.Data;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

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

    /**
     * 事务生产者分组名
     */
    private String transGroupName;

    @Autowired
    private MQTransactionListener transactionListener;

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

    @Bean
    @ConditionalOnProperty(prefix = "rocketmq.producer", value = "isOn", havingValue = "true")
    public TransactionMQProducer transactionMQProducer() throws MQClientException{
        logger.info("MQ生产者（事务消息）正在创建...");
        TransactionMQProducer producer = new TransactionMQProducer(transGroupName);
        producer.setNamesrvAddr(nameSrvAddr);
        //创建事务监听器
        //MQTransactionListener transactionListener = new MQTransactionListener();
        //创建回查的线程池
        int corePoolSize = 2, maxPoolSize = 5;
        long keepAliveTime = 100;
        ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(2000);
        ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, blockingQueue, r -> {
            Thread thread = new Thread(r);
            thread.setName("RMQ-事务回查线程");
            return thread;
        });
        //设置事务回查的线程池,如果不设置也会默认生成一个
        producer.setExecutorService(executorService);
        //设置事务监听器
        producer.setTransactionListener(transactionListener);

        producer.start();

        logger.info("MQ生产者（事务消息）创建成功!");
        return producer;
    }
}
