package com.vz.rocketmq.clients.annotaion.processor;

import com.vz.rocketmq.clients.annotaion.LocalTransactionRegistry;
import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.enums.MsgTag;
import com.vz.rocketmq.clients.transaction.LocalTransactionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author visy.wang
 * @description: LocalTransactionRegistry注解处理器
 * @date 2023/3/23 10:34
 */
public class LocalTransactionRegistryProcessor {
    private static final Map<String,MQTopic> topicsCache = new HashMap<>();
    private static final Map<String,MsgTag> tagsCache = new HashMap<>();

    public static MQTopic getTopic(LocalTransactionHandler handler){
        return getPropValue(handler, topicsCache, LocalTransactionRegistry::topic);
    }

    public static MsgTag getTag(LocalTransactionHandler handler){
        return getPropValue(handler, tagsCache, LocalTransactionRegistry::tag);
    }

    public static boolean isAnnotationPresent(LocalTransactionHandler handler){
        return isAnnotationPresent(handler.getClass());
    }

    private static boolean isAnnotationPresent(Class<?> clazz){
        return clazz.isAnnotationPresent(LocalTransactionRegistry.class);
    }

    private static <T> T getPropValue(LocalTransactionHandler handler,
                                      Map<String,T> cacheMap, Function<LocalTransactionRegistry,T> valueGetter){
        Class<?> clazz = handler.getClass();
        String clazzName = clazz.getName();

        //已缓存则直接返回
        T value = cacheMap.get(clazzName);
        if(Objects.nonNull(value)){
            return value;
        }

        //判断是否存在注解
        if(!isAnnotationPresent(clazz)){
            return null;
        }

        //获取值并缓存
        value = valueGetter.apply(clazz.getAnnotation(LocalTransactionRegistry.class));
        cacheMap.put(clazzName, value);

        return value;
    }
}
