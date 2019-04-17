package com.voxlearning.wechat.service;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
public interface MessageService {

    /**
     * 刷新用户最后操作时间，便于发送模板消息
     *
     * @param openId
     * @param opTime
     */
    public void updateActiveTime(String openId, Long opTime);
}
