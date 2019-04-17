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

package com.voxlearning.ucenter.controller.sso;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.ucenter.support.constants.SsoConstants;
import com.voxlearning.ucenter.support.constants.SsoError;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.ucenter.support.exception.SsoValidateException;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 23/12/2015.
 */
public class AbstractSsoController extends AbstractWebController {

    @Override
    public boolean onBeforeControllerMethod() {
        if (!super.onBeforeControllerMethod()) {
            return false;
        }

        try {
            String dataJson = getRequestParameter(SsoConstants.DATA, null);
            Map<String, Object> data = JsonUtils.fromJson(dataJson);

            VendorApps app = loadApp(data);
            validateSign(app, data);
            validateTimestamp(data);

            return true;
        } catch (SsoValidateException ex) {
            try {
                // 发生签名验证错误，直接跳转回用户中心首页
                getResponse().sendRedirect("redirect:/login.vpage");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 检查是否是合法的第三方
     * 验证失败打日志,一旦被搞日志刷屏
     */
    private VendorApps loadApp(Map<String, Object> data) throws SsoValidateException {
        commonParaterRequired();


        String appKey = SafeConverter.toString(data.get(SsoConstants.APP_KEY));
        if (StringUtils.isBlank(appKey)) {
            logger.warn("illegal request,appKey not found:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_APP);
        }

        //app数据量比较小,只有几十条,未来也不会爆增,全部放到缓存里了 2015-12-23
        List<VendorApps> apps = vendorLoaderClient.loadVendorAppsIncludeDisabled().values()
                .stream()
                .filter(t -> t.isVisible(RuntimeMode.current().getLevel()))
                .collect(Collectors.toList());

        if (!apps.stream().map(VendorApps::getAppKey).collect(Collectors.toSet()).contains(appKey)) {
            logger.warn("illegal request,appKey not exist:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_APP);
        }

        Optional<VendorApps> app = apps.stream().filter(a -> a.getAppKey().equals(appKey)).findFirst();
        if (!app.isPresent()) {
            logger.warn("illegal request,app not exist:{}", JsonUtils.toJson(getRequest().getParameterMap()));
        }

        return app.get();
    }

    /**
     * 验证签名
     * 验证失败打日志,一旦被搞日志刷屏
     */
    private void validateSign(VendorApps app, Map<String, Object> data) throws SsoValidateException {
        commonParaterRequired();
        String signature = SafeConverter.toString(data.get(SsoConstants.SIGNATURE));

        Map<String, String> params = new HashMap<>();
        data.keySet().stream()
                .filter(name -> !SsoConstants.SIGNATURE.equals(name) && null != data.get(name))
                .forEach(name -> params.put(name, SafeConverter.toString(data.get(name))));

        String sign = DigestSignUtils.signMd5(params, app.getSecretKey());
        if (!sign.equals(signature)) {
            logger.warn("validate signature failed,incorrect sign:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_SIGN);
        }
    }

    /**
     * 超时较验
     * 只较验明文timestamp,不保证sign正确
     */
    private void validateTimestamp(Map<String, Object> data) {
        String timestamp = SafeConverter.toString(data.get(SsoConstants.TIMESTAMP));
        if (StringUtils.isBlank(timestamp) || !NumberUtils.isNumber(timestamp)) {
            logger.warn("validate timestamp failed:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_PARAMTER_LIST);
        }

        if (Instant.now().minusMillis(SsoConstants.SIGN_EXPIRE_TIMESTAMP).isAfter(Instant.ofEpochMilli(Long.parseLong(timestamp)))) {
//            logger.warn("validate timestamp failed,timestamp expired:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_TIMESTAMP_EXPIRED);
        }
    }

    /**
     * 检查一下公用的参数是否齐全
     */
    private void commonParaterRequired() {
        String dataJson = getRequestParameter(SsoConstants.DATA, null);
        Map<String, Object> data = JsonUtils.fromJson(dataJson);
        if (MapUtils.isEmpty(data)) {
            logger.warn("sso validate failed,incorrect parameter list:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_PARAMTER_LIST);
        }
        String appKey = SafeConverter.toString(data.get(SsoConstants.APP_KEY));
        String timestamp = SafeConverter.toString(data.get(SsoConstants.TIMESTAMP));
        String signature = SafeConverter.toString(data.get(SsoConstants.SIGNATURE));

        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(timestamp) || StringUtils.isBlank(signature)) {
            logger.warn("sso validate failed,incorrect parameter list:{}", JsonUtils.toJson(getRequest().getParameterMap()));
            throw new SsoValidateException(SsoError.REQ_VALIDATE_INVALID_PARAMTER_LIST);
        }
    }
}
