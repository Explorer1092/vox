package com.voxlearning.wechat.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
public class WechatConfig {

    public static String getBaseSiteUrl() {
        String url = ProductConfig.get("wechat.base_site_url");

        String scheme = HttpRequestUtils.getRealRequestSchema(HttpRequestContextUtils.getRequestContextRequest());
        if (StringUtils.equals(scheme, "http")) {
            scheme = HttpRequestUtils.getRealRequestSchema2(HttpRequestContextUtils.getRequestContextRequest());
        }

        if (StringUtils.isBlank(scheme)) scheme = "https";

        if (url.startsWith(scheme + "://")) return url;

        if (url.startsWith("http://")) return scheme + url.substring(4);
        if (url.startsWith("https://")) return scheme + url.substring(5);

        return scheme + "://" + url;
    }
}
