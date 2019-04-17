package com.voxlearning.utopia.service.push.impl.jiguang;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 极光常量类
 *
 * @author Wenlong Meng
 * @since Mar 13, 2019
 */
public class JGConstants {

    public static Map<String, JGHttpConfig> JG_URLS = Maps.newHashMap();
    static {
        JG_URLS.put("deleteUsers", new JGHttpConfig("https://device.jpush.cn/v3/aliases/", "DELETE"));
        JG_URLS.put("deleteTags", new JGHttpConfig("https://device.jpush.cn/v3/tags/", "DELETE"));
    }

    /**
     * 每次发消息人数
     */
    public static final int PUSH_COUNT = 1000;

}
