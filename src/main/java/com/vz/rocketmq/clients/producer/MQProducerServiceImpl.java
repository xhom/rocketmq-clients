package com.vz.rocketmq.clients.producer;

import com.alibaba.fastjson.JSON;
import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.enums.MsgTag;
import com.vz.rocketmq.clients.transaction.LocalTransactionHandler;
import com.vz.rocketmq.clients.transaction.MQTransactionListener;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author visy.wang
 * @description: 生产者服务实现
 * @date 2023/3/17 13:52
 */
@Service
public class MQProducerServiceImpl implements MQProducerService {
    /**
     * 默认生产者
     * 一般来说，创建一个生产者就够了
     */
    @Autowired
    private DefaultMQProducer defaultMQProducer;
    /**
     * 事务生产者
     */
    @Autowired
    private TransactionMQProducer transactionMQProducer;
    /**
     * 事务消息监听器
     */
    @Autowired
    private MQTransactionListener mqTransactionListener;

    @Override
    public boolean sendMessage(MQTopic topic, MsgTag msgTag, String msgKey, Object msg) {
        try{
            //构建消息
            Message message = buildMessage(topic, msgTag, msgKey, msg);
            //发送消息
            SendResult sendResult = defaultMQProducer.send(message);
            logger.info("消息发送成功，msgId={}", sendResult.getMsgId());
            return true;
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean sendMessage(MQTopic topic, MsgTag msgTag, Object msg) {
        return sendMessage(topic, msgTag, null, msg);
    }

    @Override
    public boolean sendMessage(MQTopic topic, Object msg) {
        return sendMessage(topic, null, null, msg);
    }

    @Override
    public boolean sendMessageOrderly(MQTopic topic, MsgTag msgTag, String msgKey, Object msg,
                                      Object orderId, Function<Integer, Integer> selector) {
        try{
            Message message = buildMessage(topic, msgTag, msgKey, msg);
            SendResult sendResult = defaultMQProducer.send(message, (List<MessageQueue> mqs, Message _msg, Object arg) -> {
                int index = selector.apply(mqs.size());
                return mqs.get(index);
            }, orderId);
            logger.info("消息发送成功，msgId={}", sendResult.getMsgId());
            return true;
        }catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean sendMessageOrderly(MQTopic topic, MsgTag msgTag, Object msg, Object buzId, Function<Integer, Integer> selector) {
        return sendMessageOrderly(topic, msgTag, null, msg, buzId, selector);
    }

    @Override
    public boolean sendMessageOrderly(MQTopic topic, Object msg, Object buzId, Function<Integer, Integer> selector) {
        return sendMessageOrderly(topic, null, null, msg, buzId, selector);
    }

    @Override
    public boolean sendMessageBatch(MQTopic topic, MsgTag msgTag, String msgKey, List<?> msgList) {
        try{
            //构建消息
            List<Message> messageList = buildMessageList(topic, msgTag, msgKey, msgList);
            //发送消息
            SendResult sendResult = defaultMQProducer.send(messageList);
            logger.info("批量消息发送成功，msgId={}", sendResult.getMsgId());
            return true;
        }catch(Exception e){
            logger.info("批量消息发送异常:{}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean sendMessageBatch(MQTopic topic, MsgTag msgTag, List<?> msgList) {
        return sendMessageBatch(topic, msgTag, null, msgList);
    }

    @Override
    public boolean sendMessageBatch(MQTopic topic, List<?> msgList) {
        return sendMessageBatch(topic, null, null, msgList);
    }

    @Override
    public boolean sendMessageWithDelay(MQTopic topic, MsgTag msgTag, String msgKey, Object msg, int delayLevel) {
        try{
            //构建消息
            Message message = buildMessage(topic, msgTag, msgKey, msg);
            //设置延迟等级
            message.setDelayTimeLevel(delayLevel);
            //发送消息
            SendResult sendResult = defaultMQProducer.send(message);
            logger.info("消息发送成功，msgId={}", sendResult.getMsgId());
            return true;
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean sendMessageWithDelay(MQTopic topic, MsgTag msgTag, Object msg, int delayLevel) {
        return sendMessageWithDelay(topic, msgTag, null, msg, delayLevel);
    }

    @Override
    public boolean sendMessageWithDelay(MQTopic topic, Object msg, int delayLevel) {
        return sendMessageWithDelay(topic, null, null, msg, delayLevel);
    }

    @Override
    public void sendMessageAsync(MQTopic topic, MsgTag msgTag, String msgKey,
                                 Object msg, BiConsumer<Boolean,String> callback) {
        try{
            //构建消息
            Message message = buildMessage(topic, msgTag, msgKey, msg);
            //发送消息(异步)
            defaultMQProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    logger.info("异步消息发送成功，msgId={}", sendResult.getMsgId());
                    callback.accept(true, sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable throwable) {
                    logger.info("异步消息发送失败:{}", throwable.getMessage(), throwable);
                    callback.accept(false, throwable.getMessage());
                }
            });
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageAsync(MQTopic topic, MsgTag msgTag,
                                 Object msg, BiConsumer<Boolean, String> callback) {
        sendMessageAsync(topic, msgTag, null, msg, callback);
    }

    @Override
    public void sendMessageAsync(MQTopic topic, Object msg,
                                 BiConsumer<Boolean, String> callback) {
        sendMessageAsync(topic, null, null, msg, callback);
    }

    @Override
    public void sendMessageOneway(MQTopic topic, MsgTag msgTag, String msgKey, Object msg) {
        try{
            //构建消息
            Message message = buildMessage(topic, msgTag, msgKey, msg);
            //发送消息(单向，即只发送请求不等待应答)
            defaultMQProducer.sendOneway(message);
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageOneway(MQTopic topic, MsgTag msgTag, Object msg) {
        sendMessageOneway(topic, msgTag, null, msg);
    }

    @Override
    public void sendMessageOneway(MQTopic topic, Object msg) {
        sendMessageOneway(topic, null, null, msg);
    }

    @Override
    public boolean sendTransactionMessage(MQTopic topic, MsgTag msgTag, Object msg) {
        try{
            //构建消息
            Message message = buildMessage(topic, msgTag, null, msg);
            //发送事务消息
            TransactionSendResult sendResult = transactionMQProducer.sendMessageInTransaction(message, null);
            logger.info("事务消息发送成功，msgId={}", sendResult.getMsgId());
            return true;
        }catch (MQClientException e) {
            logger.info("事务消息发送失败:{}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean sendTransactionMessage(MQTopic topic, Object msg) {
        return sendTransactionMessage(topic, null, msg);
    }

    @Override
    public boolean sendTransactionMessage(MQTopic topic, MsgTag msgTag, Object msg, LocalTransactionHandler handler) {
        //先注册处理器
        mqTransactionListener.registry(msgTag, handler);
        //再发消息
        return sendTransactionMessage(topic, msgTag, msg);
    }

    /**
     * 构建消息
     * @param topic 主题
     * @param msgTag 消息Tag
     * @param msgKey 消息Key
     * @param msg 消息
     * @return 消息
     */
    private Message buildMessage(MQTopic topic, MsgTag msgTag, String msgKey, Object msg){
        String topicValue = topic.getValue();
        byte[] body = JSON.toJSONString(msg).getBytes(StandardCharsets.UTF_8);
        if(Objects.nonNull(msgTag)){
            String msgTagValue = msgTag.getValue();
            if(Objects.nonNull(msgKey)){
                return new Message(topicValue, msgTagValue, msgKey, body);
            }
            return new Message(topicValue, msgTagValue, body);
        }
        return new Message(topicValue, body);
    }

    /**
     * 构建批量消息
     * @param topic 主题
     * @param msgTag 消息Tag
     * @param msgKey 消息Key
     * @param msgList 消息列表
     * @return 消息列表
     */
    private List<Message> buildMessageList(MQTopic topic, MsgTag msgTag, String msgKey, List<?> msgList){
        return msgList.stream().map(msg -> {
            return buildMessage(topic, msgTag, msgKey, msg);
        }).collect(Collectors.toList());
    }
}
