/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信账号类型
 * Created by Shuai Huan on 2014/11/13.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WechatType {

    @Deprecated
    PARENT(
            0,
            "wechat.appid",
            "wechat.appsecret",
            "WECHAT_CACHE_ACCESS_TOKEN",
            "WX_TICKET_EXCEED_QUOTA",
            "http://cdn.17zuoye.com/static/project/app/publiccode.jpg",
            "微信家长通"
    ),//家长端微信配置
    TEACHER(
            1,
            "wechat.teacher.appid",
            "wechat.teacher.appsecret",
            "WECHAT_TEACHER_CACHE_ACCESS_TOKEN",
            "WX_TEACHER_TICKET_EXCEED_QUOTA",
            "http://cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg",
            "微信老师端"
    ),//老师端微信配置
    AMBASSADOR(
            2,
            "wechat.ambassador.appid",
            "wechat.ambassador.appsecret",
            "WECHAT_AMBASSADOR_CACHE_ACCESS_TOKEN",
            "WX_AMBASSADOR_TICKET_EXCEED_QUOTA",
            "http://cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg",
            "微信校园大使"
    ),//校园大使微信号配置
    CHIPS(
            3,
            "wechat.chips.appid",
            "wechat.chips.appsecret",
            "WECHAT_CHIPS_CACHE_ACCESS_TOKEN",
            "WX_CHIPS_TICKET_EXCEED_QUOTA",
            "http://cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg",
            "微信薯条英语"
    ),
    @Deprecated
    STUDY_TOGETHER(
            4,
            "wechat.studytogether.appid",
            "wechat.studytogether.appsecret",
            "WECHAT_STUDY_TOGETHER_CACHE_ACCESS_TOKEN",
            "WX_17XUE_TICKET_EXCEED_QUOTA",
            "http://cdn.17zuoye.com/static/project/app/publiccode.jpg",
            "微信17学"
    ),
    PARENT_APP(
            5,
            "wechat.parentapp.appid",
            "wechat.parentapp.appsecret",
            "WECHAT_PARENT_APP_CACHE_ACCESS_TOKEN",
            "WX_PARENT_APP_TICKET_EXCEED_QUOTA",
            "http://cdn.17zuoye.com/static/project/app/publiccode.jpg",
            "一起学APP微信登录"
    );

    WechatType(int type, String appId, String appSecret, String accessTokenCacheKey,
               String ticketExeedQuotaCacheKey, String staticQrcode, String desc) {
        this.type = type;
        this.appId = appId;
        this.appSecret = appSecret;
        this.accessTokenCacheKey = accessTokenCacheKey;
        this.ticketExeedQuotaCacheKey = ticketExeedQuotaCacheKey;
        this.staticQrcode = staticQrcode;
        this.desc = desc;
    }

    private static Map<Integer, WechatType> types;

    static {
        types = new HashMap<>();
        for (WechatType type : values()) {
            types.put(type.getType(), type);
        }
    }

    public static WechatType of(Integer type) {
        return types.get(type);
    }

    @Getter
    private int type;
    @Getter
    private String appId;
    @Getter
    private String appSecret;
    @Getter
    private String accessTokenCacheKey;
    @Getter
    private String ticketExeedQuotaCacheKey;
    @Getter
    private String staticQrcode;
    @Getter
    private String desc;

}
