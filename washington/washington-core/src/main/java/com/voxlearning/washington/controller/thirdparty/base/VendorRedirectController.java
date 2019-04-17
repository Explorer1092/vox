/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.thirdparty.base;

import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;
import com.voxlearning.washington.support.AbstractController;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 供应商接入
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-10-09
 */
public abstract class VendorRedirectController extends AbstractController {

    @Inject
    private ThirdPartyLoaderClient thirdPartyLoaderClient;
    @Inject
    private VendorAppsServiceClient vendorAppsServiceClient;

    /**
     * 懂你报告列表页
     *
     * @return
     */
    public String redirect(String url) {
        //签名
        Map<String, String> paramMap = params();
        if(paramMap == null){//异常case
            return "redirect:/";
        }

        return "redirect:" + UrlUtils.buildUrlQuery(url, paramMap);
    }

    /**
     * 构建参数，并签名
     *
     * @return
     */
    protected Map<String, String> params(){
        //当前用户
        Long userId = this.currentUserId();
        //未登陆
        if (ObjectUtils.anyBlank(userId) || userId <= 0) {
            logger.error("userId is null");
            return null;
        }
        String appKey = appKey();
        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(userId, appKey).stream().findFirst().orElse(null);
        //未映射
        if(landingSource == null){
            logger.error("userId:{}, appKey:{}, landingSource is null", userId, appKey);
            return null;
        }

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userId", landingSource.getSourceUid());
        paramMap.put("appKey", appKey);
        VendorApps vendorApps = getVendorApps();
        String sig = DigestSignUtils.signMd5(paramMap, vendorApps.getSecretKey());
        paramMap.put("sig", sig);
        return paramMap;
    }

    /**
     * 获取第三方app对接信息
     *
     * @return
     */
    protected VendorApps getVendorApps(){
        return vendorAppsServiceClient.getVendorAppsBuffer().loadByAk(appKey());
    }

    /**
     * 获取appKey
     *
     * @return
     */
    public abstract String appKey();
}
