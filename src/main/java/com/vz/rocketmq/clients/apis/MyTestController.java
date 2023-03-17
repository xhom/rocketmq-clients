package com.vz.rocketmq.clients.apis;

import com.vz.rocketmq.clients.config.MQConsumerConfigure;
import com.vz.rocketmq.clients.producer.MyProducerService;
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
public class MyTestController {
    public static final Logger logger = LoggerFactory.getLogger(MyTestController.class);

    @Autowired
    private MyProducerService myProducerService;

    @RequestMapping("/t")
    public Map<String,Object> test(){
        Map<String,Object> mp = new HashMap<>();
        mp.put("hello", "world");
        return mp;
    }

    @RequestMapping("/send/{msg}")
    public Boolean send(@PathVariable("msg") String msg){
        return myProducerService.sendMessage("TEST_TOPIC", msg);
    }

    @RequestMapping("/sendAsync/{msg}")
    public Boolean sendAsync(@PathVariable("msg") String msg){
        myProducerService.sendMessageAsync("TEST_TOPIC", msg, (isOK, message)->{
            logger.info("sendAsync:{}, message:{}", isOK, message);
        });
        return true;
    }

}
