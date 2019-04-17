/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.helpers;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author changyuan
 * @author xinxin
 * @since 2015/12/28.
 */
@Named
public class SsoHelper {

    @Inject private VendorLoaderClient vendorLoaderClient;

    /**
     * 生成sso登录跳转用的url
     *
     * @param context
     * @param servletPath
     * @param queryString
     * @return
     * @throws UnsupportedEncodingException
     * @author xinxin
     */
    public String generateSsoReturnUrl(UtopiaHttpRequestContext context, String servletPath, String queryString) throws UnsupportedEncodingException {
        String baseUrl = context.getWebAppBaseUrl();

        //设置returnURL
        String returnUrl = baseUrl + servletPath;
        //加入参数
        if (StringUtils.isNotEmpty(queryString))
            returnUrl += "?" + queryString;

        returnUrl = baseUrl + "/ssojump.vpage?returnURL=" + URLEncoder.encode(returnUrl, "UTF-8");

        //app数据量比较小,只有几十条,未来也不会爆增,全部放到缓存里了 2015-12-23
        VendorApps app = vendorLoaderClient.loadVendorAppsIncludeDisabled().values()
                .stream()
                .filter(t -> t.isVisible(RuntimeMode.current().getLevel()) && t.getAppKey().equals(ProductConfig.get("sso.wsd.app_key", "17Platform")))
                .findFirst()
                .orElse(null);

        if (app == null) {
            return null;
        } else {
//            return buildSsoUrl(app, URLEncoder.encode(returnUrl, "UTF-8"));
            return buildSsoUrl(context, app, returnUrl);
        }
    }

    private String buildSsoUrl(UtopiaHttpRequestContext context, VendorApps app, String returnUrl) {
        Map<String, String> param = new HashMap<>();
        param.put("appKey", app.getAppKey());
        param.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        param.put("target", returnUrl);
        param.put("sign", DigestSignUtils.signMd5(param, app.getSecretKey()));

        return UrlUtils.buildUrlQuery(ProductConfig.getUcenterUrl() + "/sso/ticket.vpage",
                MiscUtils.m("data", JsonUtils.toJson(param)));
    }
}
