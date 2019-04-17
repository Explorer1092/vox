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

package com.voxlearning.washington.controller.open.timeep;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.support.AbstractController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author changyuan
 * @since 2016/3/14
 */
public class AbstractTimeepController extends AbstractController {

    private final static String PARAM_APP_KEY = "appKey";
    private final static String PARAM_TIMESTAMP = "timestamp";
    private final static String PARAM_TOKEN = "token";
    private final static int TOKEN_EXPIRE_TIMESTAMP = 60 * 60 * 1000;

    /**
     * 验证api是否合法
     * @return
     */
    public boolean apiValidation() {
        try {
            VendorApps vendorApps = loadApp();
            validateToken(vendorApps);
            validateTimestamp();
            return true;
        } catch (UtopiaRuntimeException e) {
            return false;
        }
    }

    /**
     * 生成错误返回
     * @return
     */
    public MapMessage generateErrorMsg(ErrorCode errorCode) {
        return MapMessage.errorMessage().add("code", errorCode.getCode()).add("message", errorCode.getMsg());
    }

    private VendorApps loadApp() {
        String appKey = getRequestString(PARAM_APP_KEY);
        if (StringUtils.isEmpty(appKey)) {
            logger.warn("no appKey in the param: {}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new UtopiaRuntimeException("no appKey in the param.");
        }
        VendorApps vendorApps = vendorLoaderClient.loadVendorAppsIncludeDisabled().values()
                .stream()
                .filter(t -> t.isVisible(RuntimeMode.current().getLevel()))
                .filter(t -> StringUtils.equals(t.getAppKey(), appKey))
                .findFirst()
                .orElse(null);
        if (vendorApps == null) {
            logger.warn("no app found for this appKey: {}", appKey);
            throw new UtopiaRuntimeException("no app found.");
        }
        return vendorApps;
    }

    private void validateToken(VendorApps vendorApps) {
        String token = getRequestString(PARAM_TOKEN);
        if (StringUtils.isEmpty(token)) {
            logger.warn("no token in the param: {}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new UtopiaRuntimeException("no token in the param.");
        }

        Map<String, String> m = new HashMap<>();
        getRequest().getParameterMap().entrySet()
                .stream()
                .filter(e -> !StringUtils.equals(e.getKey(), PARAM_TOKEN))
                .forEach(e -> m.put(e.getKey(), e.getValue()[0]));

        String sign = DigestSignUtils.signMd5(m, vendorApps.getSecretKey());
        if (!StringUtils.equals(sign, token)) {
            logger.warn("validate token failed, incorrect token: {}", token);
            throw new UtopiaRuntimeException("validate token failed");
        }
    }

    private void validateTimestamp() {
        String timestamp = getRequestString(PARAM_TIMESTAMP);
        if (StringUtils.isEmpty(timestamp)) {
            logger.warn("no timestamp in the param: {}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new UtopiaRuntimeException("no timestamp in the param.");
        }

        if (Instant.now().minusMillis(TOKEN_EXPIRE_TIMESTAMP).isAfter(Instant.ofEpochMilli(SafeConverter.toLong(timestamp)))) {
            throw new UtopiaRuntimeException("timestamp expired");
        }
    }
}
