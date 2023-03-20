package com.vz.rocketmq.clients.producer;

import com.alibaba.fastjson.JSON;
import com.vz.rocketmq.clients.enums.MQTopic;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
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
public class MyProducerServiceImpl implements MyProducerService {
    /**
     * 一般来说，创建一个生产者就够了
     */
    @Autowired
    private DefaultMQProducer defaultMQProducer;

    @Override
    public boolean sendMessage(MQTopic topic, String msgTag, String msgKey, Object msg) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, msg);
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
    public boolean sendMessage(MQTopic topic, String msgTag, Object msg) {
        return sendMessage(topic, msgTag, null, msg);
    }

    @Override
    public boolean sendMessage(MQTopic topic, Object msg) {
        return sendMessage(topic, null, null, msg);
    }

    @Override
    public boolean sendMessageOrderly(MQTopic topic, String msgTag, String msgKey, Object msg,
                                      Object orderId, Function<Integer, Integer> selector) {
        try{
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, msg);
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
    public boolean sendMessageOrderly(MQTopic topic, String msgTag, Object msg, Object buzId, Function<Integer, Integer> selector) {
        return sendMessageOrderly(topic, msgTag, null, msg, buzId, selector);
    }

    @Override
    public boolean sendMessageOrderly(MQTopic topic, Object msg, Object buzId, Function<Integer, Integer> selector) {
        return sendMessageOrderly(topic, null, null, msg, buzId, selector);
    }

    @Override
    public boolean sendMessageBatch(MQTopic topic, String msgTag, String msgKey, List<?> msgList) {
        try{
            //构建消息
            List<Message> messageList = buildMessageList(topic.getValue(), msgTag, msgKey, msgList);
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
    public boolean sendMessageBatch(MQTopic topic, String msgTag, List<?> msgList) {
        return sendMessageBatch(topic, msgTag, null, msgList);
    }

    @Override
    public boolean sendMessageBatch(MQTopic topic, List<?> msgList) {
        return sendMessageBatch(topic, null, null, msgList);
    }

    @Override
    public boolean sendMessageWithDelay(MQTopic topic, String msgTag, String msgKey, Object msg, int delayLevel) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, msg);
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
    public boolean sendMessageWithDelay(MQTopic topic, String msgTag, Object msg, int delayLevel) {
        return sendMessageWithDelay(topic, msgTag, null, msg, delayLevel);
    }

    @Override
    public boolean sendMessageWithDelay(MQTopic topic, Object msg, int delayLevel) {
        return sendMessageWithDelay(topic, null, null, msg, delayLevel);
    }

    @Override
    public void sendMessageAsync(MQTopic topic, String msgTag, String msgKey,
                                 Object msg, BiConsumer<Boolean,String> callback) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, msg);
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
    public void sendMessageAsync(MQTopic topic, String msgTag,
                                 Object msg, BiConsumer<Boolean, String> callback) {
        sendMessageAsync(topic, msgTag, null, msg, callback);
    }

    @Override
    public void sendMessageAsync(MQTopic topic, Object msg,
                                 BiConsumer<Boolean, String> callback) {
        sendMessageAsync(topic, null, null, msg, callback);
    }

    @Override
    public void sendMessageOneway(MQTopic topic, String msgTag, String msgKey, Object msg) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, msg);
            //发送消息(单向，即只发送请求不等待应答)
            defaultMQProducer.sendOneway(message);
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageOneway(MQTopic topic, String msgTag, Object msg) {
        sendMessageOneway(topic, msgTag, null, msg);
    }

    @Override
    public void sendMessageOneway(MQTopic topic, Object msg) {
        sendMessageOneway(topic, null, null, msg);
    }

    /**
     * 构建消息
     * @param topic 主题
     * @param msgTag 消息Tag
     * @param msgKey 消息Key
     * @param msg 消息
     * @return 消息
     */
    private Message buildMessage(String topic, String msgTag, String msgKey, Object msg){
        byte[] body = JSON.toJSONString(msg).getBytes(StandardCharsets.UTF_8);
        if(Objects.nonNull(msgTag)){
            if(Objects.nonNull(msgKey)){
                return new Message(topic, msgTag, msgKey, body);
            }
            return new Message(topic, msgTag, body);
        }
        return new Message(topic, body);
    }

    /**
     * 构建批量消息
     * @param topic 主题
     * @param msgTag 消息Tag
     * @param msgKey 消息Key
     * @param msgList 消息列表
     * @return 消息列表
     */
    private List<Message> buildMessageList(String topic, String msgTag, String msgKey, List<?> msgList){
        return msgList.stream().map(msg -> {
            return buildMessage(topic, msgTag, msgKey, msg);
        }).collect(Collectors.toList());
    }
}
