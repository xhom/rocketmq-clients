package com.vz.rocketmq.clients.transaction;

import com.vz.rocketmq.clients.annotaion.LocalTransactionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @author visy.wang
 * @description: 本地事务处理器发现者
 * @date 2023/3/27 0:40
 */
@Component
public class LocalTransactionDiscoverer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private MQTransactionListener mqTransactionListener;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if(Objects.isNull(applicationContext.getParent())){
            Map<String, Object> beans = applicationContext.getBeansWithAnnotation(LocalTransactionRegistry.class);
            //自动将这些标注了注解的bean注册到MQ的事务监听器
            for(Object bean: beans.values()){
                //不为null且实现了LocalTransactionHandler接口的Bean才注册
                if(Objects.nonNull(bean) && (bean instanceof LocalTransactionHandler)){
                    mqTransactionListener.registry((LocalTransactionHandler) bean);
                }
            }
        }
    }
}
