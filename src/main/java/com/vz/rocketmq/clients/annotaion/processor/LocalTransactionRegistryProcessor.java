package com.vz.rocketmq.clients.annotaion.processor;

import com.vz.rocketmq.clients.annotaion.LocalTransactionRegistry;
import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.enums.MsgTag;
import com.vz.rocketmq.clients.transaction.LocalTransactionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author visy.wang
 * @description: LocalTransactionRegistry注解处理器
 * @date 2023/3/23 10:34
 */
public class LocalTransactionRegistryProcessor {
    private static final Map<String,MQTopic> topicsCache = new HashMap<>();
    private static final Map<String,MsgTag> tagsCache = new HashMap<>();

    public static MQTopic getTopic(LocalTransactionHandler handler){
        Class<?> clazz = handler.getClass();
        MQTopic topic = topicsCache.get(clazz.getName());
        if(Objects.nonNull(topic)){
            return topic;
        }

        if(!clazz.isAnnotationPresent(LocalTransactionRegistry.class)){
            return MQTopic.NULL;
        }

        LocalTransactionRegistry annotation = clazz.getAnnotation(LocalTransactionRegistry.class);
        topic = annotation.topic();
        topicsCache.put(clazz.getName(), topic);

        return topic;
    }

    public static MsgTag getTag(LocalTransactionHandler handler){
        Class<?> clazz = handler.getClass();
        MsgTag tag = tagsCache.get(clazz.getName());
        if(Objects.nonNull(tag)){
            return tag;
        }

        if(!clazz.isAnnotationPresent(LocalTransactionRegistry.class)){
            return MsgTag.NULL;
        }

        LocalTransactionRegistry annotation = clazz.getAnnotation(LocalTransactionRegistry.class);
        tag = annotation.tag();
        tagsCache.put(clazz.getName(), tag);

        return tag;
    }
}
