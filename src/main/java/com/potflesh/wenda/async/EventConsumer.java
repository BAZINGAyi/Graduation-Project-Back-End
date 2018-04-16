package com.potflesh.wenda.async;
import com.alibaba.fastjson.JSON;
import com.potflesh.wenda.service.RedisService;
import com.potflesh.wenda.utils.RedisKeyUtil;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/17.
 */
@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware {

    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    // 处理事件的类型 和 对应处理事件的一些方法 （这里使用 list 表示 一个事件 可能被多个多个 handler 处理）
    private Map<EventType,List<EventHandler>> config = new HashedMap();

    private ApplicationContext applicationContext;

    @Autowired
    RedisService redisService;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 将系统实现所有的 EventHandler 的实现类 找出来
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);

        // 将每个事件和其对应的处理方法注册，在系统开启时注册
        if (beans != null) {
            // 遍历 map 集合的方法 还可以使用 迭代器
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {

                // 得到 handler 关注的事件
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                // 将关注的事件注册
                // 一个类型的事件可能会有多个
                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    // 将每个事件对应的事件处理方法( handler )加入
                    config.get(type).add(entry.getValue());
                }
            }
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    String key = RedisKeyUtil.getEventQueueKey();
                    // brpop 对应的是同步队列
                    // 如果没有取到值就一直阻塞
                    List<String> events = redisService.brpop(0, key);

                    for (String message : events) {
                        // events 的第一个返回值是 查找的 key 所以需要过滤掉。
                        if (message.equals(key)) {
                            continue;
                        }

                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }

                        // 处理每一个事件相应的一些处理方法
                        for (EventHandler handler : config.get(eventModel.getType())) {
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
