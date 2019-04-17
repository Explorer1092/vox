package com.voxlearning.wechat.payment;

import com.voxlearning.wechat.payment.handler.NotifyHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinxin on 2/2/2016.
 */
public class NotifyHandlerFactory {
    private static final Map<String, NotifyHandler> handlers = new HashMap<>();

    public static void register(NotifyHandler handler) {
        handlers.put(handler.getType(), handler);
    }

    public static NotifyHandler getHandler(String type) {
        NotifyHandler handler = handlers.get(type);

        if (null == handler) throw new IllegalArgumentException("未知的支付类型");

        return handler;
    }
}
