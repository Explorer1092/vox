package com.voxlearning.wechat.constants;

import lombok.Getter;

/**
 * @author Xin Xin
 * @since 10/16/15
 */
public enum MessageType {
    TEXT("text"),           //文本消息
    IMAGE("image"),          //图片消息
    VOICE("voice"),          //音频消息
    VIDEO("video"),          //视频消息
    SHORT_VIDEO("shortvideo"),    //小视频消息
    LOCATION("location"),       //地理位置消息
    LINK("link"),            //链接消息
    NEWS("news"),          //图文消息(回复消息)
    EVENT("event"),         //事件消息
    MUSIC("music"),         //音乐消息（客服消息使用)
    TRANSFER_CUSTOMER_SERVICE("transfer_customer_service");  //转至多客服系统消息

    @Getter
    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public static MessageType of(String type) {
        for (MessageType t : values()) {
            if (t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

}
