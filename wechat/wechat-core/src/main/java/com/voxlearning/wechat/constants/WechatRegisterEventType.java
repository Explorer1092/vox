package com.voxlearning.wechat.constants;


public enum WechatRegisterEventType {
    SUBSCRIBE, LOGIN, BE;

    public static WechatRegisterEventType safeOf(String name) {
        try {
            return WechatRegisterEventType.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
