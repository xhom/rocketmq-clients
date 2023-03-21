package com.vz.rocketmq.clients.producer;

import com.vz.rocketmq.clients.enums.MQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author visy.wang
 * @description: 生产者服务
 * @date 2023/3/17 13:52
 */
public interface MQProducerService {
    Logger logger = LoggerFactory.getLogger(MQProducerService.class);

    /**
     * 发送消息（同步）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msg 消息内容
     * @return 是否发送成功
     * ====================================================================
     * 同步发送：
     * 同步发送是最常用的方式，是指消息发送方发出一条消息后，
     * 会在收到服务端同步响应之后才发下一条消息的通讯方式。
     * 可靠的同步传输被广泛应用于各种场景，如重要的通知消息、短消息通知等。
     */
    boolean sendMessage(MQTopic topic, String msgTag, String msgKey, Object msg);
    boolean sendMessage(MQTopic topic, String msgTag, Object msg);
    boolean sendMessage(MQTopic topic, Object msg);

    /**
     * 发送消息（顺序）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msg 消息内容
     * @param buzId 业务ID（用于确定队列索引的字段）
     * @param selector 队列选择器 <队列总数，选中的队列索引>
     * @return 是否发送成功
     * ====================================================================
     * 顺序消息：
     * RocketMQ通过生产者和服务端的协议保障单个生产者串行地发送消息，并按序存储和持久化。
     * 如需保证消息生产的顺序性，则必须满足以下条件：
     * 1.单一生产者：消息生产的顺序性仅支持单一生产者，不同生产者分布在不同的系统，
     * 即使设置相同的分区键，不同生产者之间产生的消息也无法判定其先后顺序。
     * 2.串行发送：生产者客户端支持多线程安全访问，
     * 但如果生产者使用多线程并行发送，则不同线程间产生的消息将无法判定其先后顺序。
     */
    boolean sendMessageOrderly(MQTopic topic, String msgTag, String msgKey, Object msg,
                               Object buzId, Function<Integer, Integer> selector);
    boolean sendMessageOrderly(MQTopic topic, String msgTag, Object msg,
                               Object buzId, Function<Integer, Integer> selector);
    boolean sendMessageOrderly(MQTopic topic, Object msg,
                               Object buzId, Function<Integer, Integer> selector);

    /**
     * 发送消息（批量）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msgList 消息内容列表
     * @return 是否发送成功
     * ====================================================================
     * 批量同步发送：
     * 在对吞吐率有一定要求的情况下，可以将一些消息聚成一批以后进行发送，
     * 可以增加吞吐率，并减少API和网络调用次数。
     * 要注意的是批量消息的大小不能超过 1MiB（否则需要自行分割），其次同一批 batch 中 topic 必须相同。
     */
    boolean sendMessageBatch(MQTopic topic, String msgTag, String msgKey, List<?> msgList);
    boolean sendMessageBatch(MQTopic topic, String msgTag, List<?> msgList);
    boolean sendMessageBatch(MQTopic topic, List<?> msgList);

    /**
     * 发送消息（延迟）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msg 消息内容
     * @param delayLevel 延迟等级： 1-18
     * @return 是否发送成功
     * ====================================================================
     * 延迟消息：
     * 延时消息的实现逻辑需要先经过定时存储等待触发，延时时间到达后才会被投递给消费者。
     * 因此，如果将大量延时消息的定时时间设置为同一时刻，
     * 则到达该时刻后会有大量消息同时需要被处理，会造成系统压力过大，导致消息分发延迟，影响定时精度。
     */
    boolean sendMessageWithDelay(MQTopic topic, String msgTag, String msgKey, Object msg, int delayLevel);
    boolean sendMessageWithDelay(MQTopic topic, String msgTag, Object msg, int delayLevel);
    boolean sendMessageWithDelay(MQTopic topic, Object msg, int delayLevel);

    /**
     * 发送消息（异步）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msg 消息内容
     * @param callback 异步回调 <是否发送成功，msgId（成功）或者错误信息（失败）>
     * ====================================================================
     * 异步发送：
     * 异步发送是指发送方发出一条消息后，不等服务端返回响应，接着发送下一条消息的通讯方式。
     * 异步发送一般用于链路耗时较长，对响应时间较为敏感的业务场景。
     * 例如，视频上传后通知启动转码服务，转码完成后通知推送转码结果等。
     */
    void sendMessageAsync(MQTopic topic, String msgTag, String msgKey,
                          Object msg, BiConsumer<Boolean,String> callback);
    void sendMessageAsync(MQTopic topic, String msgTag,
                          Object msg, BiConsumer<Boolean,String> callback);
    void sendMessageAsync(MQTopic topic, Object msg,
                          BiConsumer<Boolean,String> callback);

    /**
     * 发送消息（单向）
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msgKey 消息索引键，可根据关键字精确查找某条消息
     * @param msg 消息内容
     * =====================================================================
     * 单向模式发送：
     * 发送方只负责发送消息，不等待服务端返回响应且没有回调函数触发，即只发送请求不等待应答。
     * 此方式发送消息的过程耗时非常短，一般在微秒级别。
     * 适用于某些耗时非常短，但对可靠性要求并不高的场景，例如日志收集。
     */
    void sendMessageOneway(MQTopic topic, String msgTag, String msgKey, Object msg);
    void sendMessageOneway(MQTopic topic, String msgTag, Object msg);
    void sendMessageOneway(MQTopic topic, Object msg);

    /**
     * 事务消息
     * @param topic 消息发送的目标Topic名称
     * @param msgTag 消息Tag，用于消费端根据指定Tag过滤消息
     * @param msg 消息内容
     * @return 是否发送成功
     * 需配合事务监听器使用
     */
    boolean sendTransactionMessage(MQTopic topic, String msgTag, Object msg);
    boolean sendTransactionMessage(MQTopic topic, Object msg);
}
