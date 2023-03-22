package com.vz.rocketmq.clients.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vz.rocketmq.clients.transaction.LocalTransactionHandler;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author visy.wang
 * @description: 订单服务（测试事务消息）
 * @date 2023/3/21 10:49
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService, LocalTransactionHandler {
    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

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
    @Transactional
    public void execute(Message message) {
        String transactionId = message.getTransactionId();
        String content = new String(message.getBody(), StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(content);

        //分发到业务方法
        addOrder(json.getString("orderId"));

        //记录事务状态
        TestCache.commit(transactionId);
    }
}
