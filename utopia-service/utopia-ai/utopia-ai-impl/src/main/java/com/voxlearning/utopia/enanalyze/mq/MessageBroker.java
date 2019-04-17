package com.voxlearning.utopia.enanalyze.mq;

/**
 * 消息分发器
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
public interface MessageBroker {

    /**
     * 投递消息
     *
     * @param message 消息
     */
    void send(Message message);
}
