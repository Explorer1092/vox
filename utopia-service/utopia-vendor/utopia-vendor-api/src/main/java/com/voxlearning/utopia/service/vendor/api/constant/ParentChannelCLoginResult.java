package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author shiwe.liao
 * @since 2016-7-8
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
public enum ParentChannelCLoginResult {
    LOGIN_SUCCESS(0),   //手机号绑定了家长，直接登录
    @Deprecated
    LOAD_IDENTITY(1),   //手机号绑定了学生，需要去选择身份
    @Deprecated
    CLIENT_TELL_USER_CHOOSE_ADD_STUDENT(2),  //手机号未绑定家长/学生，客户端让用户去选择是否添加已有的平台学生
    @Deprecated
    VERIFY_IDENTITY_CHOOSE(3),//选择完身份后。不需要创建家长号。而是去验证已有的家长号
    ADD_STUDENT(4),//去添加孩子
    @Deprecated
    JXT_NEWS_TAB(5),//跳转到资讯tab
    @Deprecated
    REGISTER_CHANNEL_C(10), //调用专用接口注册为C端家长
    NONE(-1);   //不作任何处理


    private final int type;
}
