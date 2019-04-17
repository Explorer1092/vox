package com.voxlearning.utopia.service.wechat.impl.support;

import com.voxlearning.utopia.core.runtime.ProductConfig;

public class WechatConfig {

    public static String getBaseSiteUrl() {
        String url = ProductConfig.get("wechat.base_site_url");

        String scheme = "https";

        if (url.startsWith(scheme + "://")) return url;

        if (url.startsWith("http://")) return scheme + url.substring(4);

        return scheme + "://" + url;
    }
}
