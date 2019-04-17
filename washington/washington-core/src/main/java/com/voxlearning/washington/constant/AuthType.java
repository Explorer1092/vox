package com.voxlearning.washington.constant;

import lombok.Getter;

@Getter
public enum AuthType {
    /**
     * 第三方跳转
     */
    THIRD_PARTY(false),
    /**
     * 团购
     */
    GROUPON(true),
    /**
     * 强制身份认证
     */
    LG(true),
    /**
     * 非强制身份认证
     */
    NLG(false);

    /**
     * 是否需要在微信授权认证流程强制登录
     */
    private Boolean needLogin;

    AuthType(Boolean needLogin) {
        this.needLogin = needLogin;
    }

    public static AuthType of(String type) {
        try {
            return valueOf(type);
        } catch (Exception ex) {
            return null;
        }
    }

}
