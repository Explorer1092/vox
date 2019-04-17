package com.voxlearning.utopia.enanalyze.mq.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.enanalyze.exception.BusinessException;
import com.voxlearning.utopia.enanalyze.mq.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 消息分发器本地实现，可重构为kafka
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
@Slf4j
@Service
public class MessageBrokerLocalImpl implements MessageBroker, InitializingBean {

    /**
     * 消费者列表
     */
    @Resource
    List<MessageConsumer> consumers;

    /**
     * 注册表
     */
    Map<Topic, MessageConsumer> registry;

    /**
     * 阻塞队列
     */
    private BlockingQueue<Message> queue;

    /**
     * 执行线程
     */
    ExecutorService executorService;


    @Override
    public void send(Message message) {
        if (null != message && null != message.getTopic() && StringUtils.isNotBlank(message.getBody())) {
            MessageConsumer consumer = registry.get(message.getTopic());
            if (null != consumer) {
                queue.add(message);
            }
        }
    }

    void consume(Message message) {
        try {
            MessageConsumer consumer = registry.get(message.getTopic());
            if (null != consumer) {
                executorService.submit(() -> consumer.handle(message.getBody()));
            }
        } catch (BusinessException e) {
            log.error("消息分发器|业务异常|{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("消息分发器|未知异常|{}", e.getMessage(), e);
        }
    }

    class Bootstrap implements Runnable {
        @Override
        public void run() {
            Message message;
            while (true) {
                try {
                    message = queue.take();
                    consume(message);
                } catch (InterruptedException e) {
                    log.error("消息分发器|队列异常|{}", e.getMessage(), e);
                } catch (BusinessException e) {
                    log.error("消息分发器|业务异常|{}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("消息分发器|未知异常|{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() {

        registry = Collections.synchronizedMap(new HashMap());
        // 通过spring的DI构建注册表
        consumers.stream().forEach(i -> {
            ConsumerLabel annotation = i.getClass().getAnnotation(ConsumerLabel.class);
            if (null != annotation) {
                registry.put(annotation.topic(), i);
            }
        });

        executorService = Executors.newCachedThreadPool();
        queue = new LinkedBlockingQueue();

        Thread thread = new Thread(new Bootstrap());
        thread.start();
    }
}
