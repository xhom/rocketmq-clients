package com.vz.rocketmq.clients.apis;

import com.vz.rocketmq.clients.enums.MQTopic;
import com.vz.rocketmq.clients.producer.MQProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

/**
 * @author visy.wang
 * @description: 测试接口
 * @date 2023/3/17 13:51
 */
@RestController
@RequestMapping("/t")
public class MQTestController {
    public static final Logger logger = LoggerFactory.getLogger(MQTestController.class);

    @Autowired
    private MQProducerService mqProducerService;

    @RequestMapping("/t")
    public Map<String,Object> test(){
        Map<String,Object> mp = new HashMap<>();
        mp.put("hello", "world");
        return mp;
    }

    @RequestMapping("/send/{msg}")
    public Boolean send(@PathVariable("msg") String msg){
        return mqProducerService.sendMessage(MQTopic.TEST_TOPIC, msg);
    }

    @RequestMapping("/sendAsync/{msg}")
    public Boolean sendAsync(@PathVariable("msg") String msg){
        mqProducerService.sendMessageAsync(MQTopic.TEST_TOPIC, msg, (isOK, message)->{
            logger.info("sendAsync:{}, message:{}", isOK, message);
        });
        return true;
    }

    @RequestMapping("/sendOrderly/{msg}")
    public Boolean sendOrderly(@PathVariable("msg") String msg){
        Integer buzId = Math.abs(msg.hashCode());
        logger.info("buzId={}", buzId);
        mqProducerService.sendMessageOrderly(MQTopic.TEST_TOPIC, msg, buzId, mqNum->{
            int index = buzId % mqNum;
            logger.info("队列索引：{}, 队列总数：{}", index, mqNum);
            return index;
        });
        return true;
    }
}
