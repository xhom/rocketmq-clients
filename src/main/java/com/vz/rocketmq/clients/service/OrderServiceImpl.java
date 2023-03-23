package com.vz.rocketmq.clients.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vz.rocketmq.clients.annotaion.LocalTransactionRegistry;
import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.enums.MsgTag;
import com.vz.rocketmq.clients.transaction.AbstractLocalTransactionHandler;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author visy.wang
 * @description: 订单服务（测试事务消息）
 * @date 2023/3/21 10:49
 */
@Service("orderService")
@LocalTransactionRegistry(topic = MQTopic.TEST_TOPIC_TRANSACTION, tag = MsgTag.TEST_TRANSACTION_TAG1)
public class OrderServiceImpl extends AbstractLocalTransactionHandler implements OrderService{
    //测试用
    private final AtomicInteger transactionIndex = new AtomicInteger(0);

    @Override
    public void others() {
        System.out.println("others...");
    }

    @Override
    public void addOrder(String orderId) {
        int t = transactionIndex.getAndIncrement();

        if(t%2 == 0){
            logger.info("订单添加成功：orderId={}", orderId);

        }else{
            int i = 3 / 0;
        }
    }

    @Override
    public void execute(Message message) {
        String transactionId = message.getTransactionId();
        String content = new String(message.getBody(), StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(content);

        //分发到业务方法
        addOrder(json.getString("orderId"));

        //记录事务状态
        TransactionLogCache.add(transactionId);
    }
}
