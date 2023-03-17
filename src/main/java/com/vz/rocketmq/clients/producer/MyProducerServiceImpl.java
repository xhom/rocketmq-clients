package com.vz.rocketmq.clients.producer;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
    public boolean sendMessage(String topic, String msgKey, String msgTag, Object body) {
        try{
            //构建消息
            Message message = buildMessage(topic, msgKey, msgTag, body);
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
    public boolean sendMessage(String topic, String msgTag, Object body) {
        return sendMessage(topic, null, msgTag, body);
    }

    @Override
    public boolean sendMessage(String topic, Object body) {
        return sendMessage(topic, null, null, body);
    }

    /**
     * 构建消息
     * @param topic 主题
     * @param msgKey 消息Key
     * @param msgTag 消息Tag
     * @param body 消息体
     * @return 消息
     */
    private Message buildMessage(String topic, String msgKey, String msgTag, Object body){
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
