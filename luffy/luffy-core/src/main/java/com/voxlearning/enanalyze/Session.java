package com.voxlearning.enanalyze;

/**
 * 会话
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
public class Session {
    private static ThreadLocal<String> _token = new ThreadLocal<>();
    private static ThreadLocal<String> _openId = new ThreadLocal<>();

    public static void setToken(String token) {
        _token.set(token);
    }

    public static String getToken() {
        return _token.get();
    }

    public static void setOpenId(String openId) {
        _openId.set(openId);
    }

    public static String getOpenId() {
        return _openId.get();
    }

    public static void purge() {
        _token.set(null);
        _openId.set(null);
    }
}
