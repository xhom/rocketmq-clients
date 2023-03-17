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
    public boolean sendMessage(MQTopic topic, String msgTag, String msgKey, Object body) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, body);
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
    public boolean sendMessage(MQTopic topic, String msgTag, Object body) {
        return sendMessage(topic, msgTag, null, body);
    }

    @Override
    public boolean sendMessage(MQTopic topic, Object body) {
        return sendMessage(topic, null, null, body);
    }

    @Override
    public void sendMessageAsync(MQTopic topic, String msgTag, String msgKey,
                                 Object body, BiConsumer<Boolean,String> callback) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, body);
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
                                 Object body, BiConsumer<Boolean, String> callback) {
        sendMessageAsync(topic, msgTag, null, body, callback);
    }

    @Override
    public void sendMessageAsync(MQTopic topic, Object body,
                                 BiConsumer<Boolean, String> callback) {
        sendMessageAsync(topic, null, null, body, callback);
    }

    @Override
    public void sendMessageOneway(MQTopic topic, String msgTag, String msgKey, Object body) {
        try{
            //构建消息
            Message message = buildMessage(topic.getValue(), msgTag, msgKey, body);
            //发送消息(单向，即只发送请求不等待应答)
            defaultMQProducer.sendOneway(message);
        }catch(Exception e){
            logger.info("消息发送异常:{}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageOneway(MQTopic topic, String msgTag, Object body) {
        sendMessageOneway(topic, msgTag, null, body);
    }

    @Override
    public void sendMessageOneway(MQTopic topic, Object body) {
        sendMessageOneway(topic, null, null, body);
    }

    /**
     * 构建消息
     * @param topic 主题
     * @param msgTag 消息Tag
     * @param msgKey 消息Key
     * @param body 消息体
     * @return 消息
     */
    private Message buildMessage(String topic, String msgTag, String msgKey, Object body){
        byte[] bodyBytes = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        if(Objects.nonNull(msgTag)){
            if(Objects.nonNull(msgKey)){
                return new Message(topic, msgTag, msgKey, bodyBytes);
            }
            return new Message(topic, msgTag, bodyBytes);
        }
        return new Message(topic, bodyBytes);
    }
}
