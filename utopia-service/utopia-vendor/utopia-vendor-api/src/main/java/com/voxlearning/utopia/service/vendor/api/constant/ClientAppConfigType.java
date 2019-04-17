package com.voxlearning.utopia.service.vendor.api.constant;

/**
 * @author malong
 * 客户端获取配置的类型
 * @since 2016/9/5
 */
public enum ClientAppConfigType {
    JSPATCH,        //ios热更新
    RECORD,         //app录音
    APP_DOWNLOAD,   //app静默下载
    APP_RESOURCE, // 资源更新 2016-12-28
    DYNAMIC_APP_RESOURCE, // 动态资源 2019-01-16
    ;

    public static ClientAppConfigType parse(String type) {
        try {
            return valueOf(type);
        } catch (Exception ignored) {
            return null;
        }
    }
}
