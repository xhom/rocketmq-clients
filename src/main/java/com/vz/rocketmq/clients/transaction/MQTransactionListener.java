package com.vz.rocketmq.clients.transaction;

import com.vz.rocketmq.clients.annotaion.processor.LocalTransactionRegistryProcessor;
import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.enums.MsgTag;
import com.vz.rocketmq.clients.service.TransactionLogCache;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author visy.wang
 * @description: MQ事务监听器
 * @date 2023/3/20 16:26
 */
@Component
public class MQTransactionListener implements TransactionListener {
    public static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);
    /**
     * 使用本事务监听器的约定：
     * Topic+msgTag 对应唯一的一个处理器
     */
    //保存每个Topic-MsgTag 对应的业务处理器
    private static final Map<String, LocalTransactionHandler> localTransactionHandlers = new ConcurrentHashMap<>();

    //本地事务处理器注册
    public void registry(LocalTransactionHandler handler){
        if(!LocalTransactionRegistryProcessor.isAnnotationPresent(handler)){
            //没有注解不能注册
            return;
        }
        MQTopic topic = LocalTransactionRegistryProcessor.getTopic(handler);
        MsgTag tag = LocalTransactionRegistryProcessor.getTag(handler);
        String handlerKey = topic.getValue() + "_" + tag.getValue();
        if(!localTransactionHandlers.containsKey(handlerKey)){
            //一个 Topic+MsgTag 只注册一次
            localTransactionHandlers.put(handlerKey, handler);
            logger.info("本地事务处理器注册成功：{} -> {}", handlerKey, handler.getClass().getName());
        }
    }

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
        String transactionId = message.getTransactionId();
        logger.info("开始执行本地事务，transactionId={}", transactionId);
        logger.info("已注册的本地事务处理器:");
        logger.info("----------------------------------------------");
        localTransactionHandlers.forEach((k,v) -> {
            logger.info("{}：{}", k, v.getClass().getName());
        });
        logger.info("----------------------------------------------");

        //将消息分发到 Topic+MsgTag 对应的处理器
        String handlerKey = message.getTopic() + "_" + message.getTags();
        LocalTransactionHandler handler = localTransactionHandlers.get(handlerKey);
        if(Objects.isNull(handler)){
            logger.info("未找到已注册的本地事务处理器...");
            //按提交成功处理
            return LocalTransactionState.COMMIT_MESSAGE;
        }

        try{
            //执行具体的业务逻辑
            handler.execute(message);
            logger.info("执行本地事务成功，transactionId={}", transactionId);
            return LocalTransactionState.COMMIT_MESSAGE;
        }catch (Exception e){
            logger.info("执行本地事务异常：transactionId={}, message={}", transactionId, e.getMessage());
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    /**
     * 回查本地事务状态
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
        String transactionId = messageExt.getTransactionId();
        logger.info("本地事务回查：transactionId={}", transactionId);
        Boolean state = TransactionLogCache.get(transactionId);

        if(Boolean.TRUE.equals(state)){
            logger.info("本地事务回查结果：已提交成功");
            return LocalTransactionState.COMMIT_MESSAGE;
        }else{
            logger.info("本地事务回查结果：未提交成功");
            return LocalTransactionState.UNKNOW; //继续回查
        }
    }
}
