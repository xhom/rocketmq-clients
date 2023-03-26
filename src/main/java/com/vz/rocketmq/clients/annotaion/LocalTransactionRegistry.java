package com.vz.rocketmq.clients.annotaion;

import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.enums.MsgTag;

import java.lang.annotation.*;

/**
 * @author visy.wang
 * @description: 本地事务注册的注解
 * @date 2023/3/23 10:23
 */
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalTransactionRegistry {
    MQTopic topic(); //消息主题
    MsgTag tag() default MsgTag.NULL; //消息标记
}
