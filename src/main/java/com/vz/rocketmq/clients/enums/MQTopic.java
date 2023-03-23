package com.vz.rocketmq.clients.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author visy.wang
 * @description: MQ主题枚举
 * @date 2023/3/17 21:14
 */
@Getter
@AllArgsConstructor
public enum MQTopic {
    NULL("null", "空主题（仅用于容错，不建议使用）"),
    TEST_TOPIC("TEST_TOPIC", "测试主题"),
    TEST_TOPIC_TRANSACTION("TEST_TOPIC_TRANSACTION", "测试主题(事务消息)");

    /**
     * 主题值，必须唯一，不可重复
     */
    private final String value;
    /**
     * 主题描述
     */
    private final String desc;
}
