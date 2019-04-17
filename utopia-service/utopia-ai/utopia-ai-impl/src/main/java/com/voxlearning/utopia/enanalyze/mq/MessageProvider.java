package com.voxlearning.utopia.enanalyze.mq;

/**
 * 消息生产者
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
public interface MessageProvider {


    /**
     * 发送消息
     *
     * @param message 消息
     */
    void send(Message message);

}
