package com.voxlearning.wechat.handler;

import com.voxlearning.wechat.context.MessageContext;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author Xin Xin
 * @since 10/16/15
 */
public interface IMessageHandler {
    /**
     * 生成handler的识别标识
     */
    String getFingerprint();

    /**
     * 处理消息
     *
     * @param context
     * @return
     */
    String handle(MessageContext context);

    void setApplicationContext(ApplicationContext context);

    void setExtInstances(Map<String, Object> extInstances);
}
