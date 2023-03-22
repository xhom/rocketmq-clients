package com.vz.rocketmq.clients.transaction;

import org.apache.rocketmq.common.message.Message;

/**
 * @author visy.wang
 * @description: 本地事务处理器
 * @date 2023/3/22 13:22
 */
public interface LocalTransactionHandler {
    /**
     * 本地事务中要执行的业务逻辑
     * 相同MsgTag的消息会被分发到同一个处理器
     * @param message MQ消息内容
     * Tips: 在实现方法上请添加本地事务管理注解：@Transactional
     */
    void execute(Message message);
}
