package com.vz.rocketmq.clients.producer;

import com.alibaba.fastjson.JSON;
import com.vz.rocketmq.clients.enums.MQTopic;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

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
    public boolean sendMessageBatch(MQTopic topic, String msgTag, String msgKey, List<?> bodyList) {
        try{
            //构建消息
            List<Message> messageList = buildMessageList(topic.getValue(), msgTag, msgKey, bodyList);
            //发送消息
            SendResult sendResult = defaultMQProducer.send(messageList);
            logger.info("消息发送成功，msgId={}", sendResult.getMsgId());
            return true;
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
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
    public void sendMessageAsync(MQTopic topic, String msgTag, String msgKey,
                                 Object msg, BiConsumer<Boolean,String> callback) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, msg);
            //发送消息(异步)
            defaultMQProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    logger.info("消息异步发送成功，msgId={}", sendResult.getMsgId());
                    callback.accept(true, sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable throwable) {
                    logger.info("消息异步发送失败:{}", throwable.getMessage(), throwable);
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
     * @param msg 消息体
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
    private List<Message> buildMessageList(String topic, String msgTag, String msgKey, List<?> msgList){
        List<Message> messageList = new ArrayList<>();
        for(Object msg: msgList){
            messageList.add(buildMessage(topic, msgTag, msgKey, msg));
        }
        return messageList;
    }
}
