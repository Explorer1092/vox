package com.voxlearning.utopia.enanalyze.mq;

/**
 * 事件消费者
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
public interface MessageConsumer {

    /**
     * 处理
     *
     * @param messageBody 消息体
     */
    void handle(String messageBody);
}
