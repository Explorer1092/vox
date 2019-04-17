package com.voxlearning.wechat.constants;

import lombok.Getter;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
public enum EventType {
    SUBSCRIBE("subscribe"),  //用户关注或用户未关注时扫二维码事件
    UNSUBSCRIBE("unsubscribe"), //取消关注
    SCAN("SCAN"),           //用户已关注时扫二维码事件
    LOCATION("LOCATION"),   //上报地理位置事件
    CLICK("CLICK"),         //点击菜单接取事件
    VIEW("VIEW"),          //点击菜单跳转事件
    TEMPLATESENDJOBFINISH("TEMPLATESENDJOBFINISH"), //模板消息结果通知消息
    KF_CREATE_SESSION("kf_create_session"), //客服接入会话
    KF_CLOSE_SESSION("kf_close_session"), //客服关闭会话
    KF_SWITCH_SESSION("kf_switch_session"); //客服转接会话

    @Getter
    private String type;

    EventType(String type) {
        this.type = type;
    }

    public static EventType of(String type) {
        for (EventType t : values()) {
            if (t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }
}
