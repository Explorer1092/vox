package com.voxlearning.wechat.context;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.support.utils.WechatSignUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinxin on 5/1/2016.
 * Wechat pay config
 */

public class PayConfig {
    private final String PAY_CONFIG_PREPAY_ID = "prepay_id";
    private final String PAY_CONFIG_APP_ID = "appId";
    private final String PAY_CONFIG_TIMESTAMP = "timeStamp";
    private final String PAY_CONFIG_NONCE = "nonceStr";
    private final String PAY_CONFIG_PACKAGE = "package";
    private final String PAY_CONFIG_SIGNTYPE = "signType";
    private final String PAY_CONFIG_SIGN_MD5 = "MD5";

    @Getter
    @Setter
    private WxConfig wxConfig;
    @Getter
    @Setter
    private String payPackage;
    @Getter
    @Setter
    private String signType;
    @Getter
    @Setter
    private String backUrl;

    public PayConfig(WxConfig wxConfig, String prepayId, String backUrl) {
        this.wxConfig = wxConfig;
        this.payPackage = PAY_CONFIG_PREPAY_ID + "=" + prepayId;
        this.backUrl = backUrl;
        this.signType = PAY_CONFIG_SIGN_MD5;
    }

    public String md5Sign(WechatType wechatType) {
        Map<String, Object> payMap = new HashMap<>();
        payMap.put(PAY_CONFIG_APP_ID, ProductConfig.get(wechatType.getAppId()));
        payMap.put(PAY_CONFIG_TIMESTAMP, wxConfig.getTimestamp());
        payMap.put(PAY_CONFIG_NONCE, wxConfig.getNonce());
        payMap.put(PAY_CONFIG_PACKAGE, payPackage);
        payMap.put(PAY_CONFIG_SIGNTYPE, this.signType);

        return WechatSignUtils.md5Sign(payMap);
    }
}
