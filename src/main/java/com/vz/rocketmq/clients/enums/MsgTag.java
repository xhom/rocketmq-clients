package com.vz.rocketmq.clients.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author visy.wang
 * @description: MQ msgTag枚举
 * @date 2023/3/22 13:06
 */
@Getter
@AllArgsConstructor
public enum MsgTag {
    TEST_TRANSACTION_TAG1("TEST_TRANSACTION_TAG1", "测试事务消息标记1"),
    TEST_TRANSACTION_TAG2("TEST_TRANSACTION_TAG2", "测试事务消息标记2");

    /**
     * 标记值，必须唯一，不可重复
     */
    private final String value;
    /**
     * 标记描述
     */
    private final String desc;
}
