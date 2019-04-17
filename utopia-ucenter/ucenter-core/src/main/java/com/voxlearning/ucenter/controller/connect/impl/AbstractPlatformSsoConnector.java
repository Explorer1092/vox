package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lcy
 * @since 17/2/26
 */
abstract class AbstractPlatformSsoConnector extends AbstractSsoConnector {

    private final static String DEFAULT_USER_ID_PARAM_KEY = "uid";
    private final static String DEFAULT_SIGN_PARAM_KEY = "sign";
    private final static String DEFAULT_TIMESTAMP_PARAM_KEY = "timestamp";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // {"ticket":"xxxxxx", "uid":123, "sign":"", "time":// 时间戳} time用于对时，如果时间差大于一个时间，校验失败
        // sign=md5(uid + ticket + time +secretKey)
        Map<String, String> dataMap = JsonUtils.fromJsonToMapStringString(token);
        Long uid = SafeConverter.toLong(dataMap.get(getUserIdParamKey()));
        String sign = SafeConverter.toString(dataMap.get(getSignParamKey()));
        Long time = SafeConverter.toLong(dataMap.get(getTimestampParamKey()));

        Date serverDate = new Date();

        int timeLimitInSeconds = getTimeLimitInSeconds();
        if (time == 0
                || time > DateUtils.addSeconds(serverDate, timeLimitInSeconds).getTime()
                || time < DateUtils.addMinutes(serverDate, -timeLimitInSeconds).getTime()) {
            return MapMessage.errorMessage("validate time failed");
        }
        if (uid == 0 || StringUtils.isBlank(sign)) {
            return MapMessage.errorMessage("uid or sign is null");
        }

        Map<String, String> singMap = new HashMap<>();
        dataMap.entrySet().stream()
                .filter(e -> !StringUtils.equals(e.getKey(), getSignParamKey()))
                .forEach(e -> singMap.put(e.getKey(), e.getValue()));
        String serverSign = DigestSignUtils.signMd5(singMap, getSecretKey());
        if (!StringUtils.equalsIgnoreCase(sign, serverSign)) {
            return MapMessage.errorMessage("validate sign failed");
        }
        return MapMessage.successMessage().add("userId", uid);
    }

    @Override
    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validateResult, CookieManager cookieManager) {
        return super.processUserBinding(landingSource, sourceName, validateResult, cookieManager);
    }

    abstract String getSecretKey();

    abstract int getTimeLimitInSeconds();

    protected String getUserIdParamKey() {
        return DEFAULT_USER_ID_PARAM_KEY;
    }

    protected String getSignParamKey() {
        return DEFAULT_SIGN_PARAM_KEY;
    }

    protected String getTimestampParamKey() {
        return DEFAULT_TIMESTAMP_PARAM_KEY;
    }
}
