package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 七鱼事件类型定义
 *
 * @author Wenlong Meng
 * @version 1.0
 * @date 2018-08-28
 */
@AllArgsConstructor
public enum QiYuEventType {
    USER_INFO(1, "获取用户信息"),
    IVR_CHECK(2, "IVR校验"),
    IVR_CUSTOM(3, "自定义IVR接口"),
    PLAY_CONTENT(4, "播放内容接口"),
    SYNC_CALL_RECORD(5, "同步通话记录");

    @Getter
    private int code;//编码
    @Getter
    private String desc;//描述

    /**
     * 查询编码对应的用户类型
     *
     * @param code
     * @return
     */
    public static QiYuEventType of(int code){
        return Arrays.stream(QiYuEventType.values()).filter(t -> t.code == code).findFirst().orElse(USER_INFO);
    }

}
