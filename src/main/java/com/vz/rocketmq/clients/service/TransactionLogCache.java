package com.vz.rocketmq.clients.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author visy.wang
 * @description: 测试用，记录事务状态，实际应该保存在数据库中，并和业务SQL在同一个事务中
 * @date 2023/3/21 23:12
 */
public class TransactionLogCache {
    private static final Map<String, Boolean> transactionStates = new ConcurrentHashMap<>();

    public static void add(String transactionId){
        transactionStates.put(transactionId, Boolean.TRUE);
    }

    public static Boolean get(String transactionId){
        return transactionStates.get(transactionId);
    }
}
