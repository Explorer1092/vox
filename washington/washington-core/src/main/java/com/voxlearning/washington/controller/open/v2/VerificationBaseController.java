package com.voxlearning.washington.controller.open.v2;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 发送验证码
 */
@Slf4j
public class VerificationBaseController extends AbstractApiController {

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    /**
     * 通过用户ID发送验证码
     *
     * @param smsType  消息类型
     * @param callback 回调校验
     * @return
     */
    public MapMessage sendVerifyCodeByUserId(SmsType smsType, UserValidateCallback callback) {

        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_USER_ID, "用户ID");
            validateRequiredNumber(REQ_USER_ID, "用户ID");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequired(REQ_CAPTCHA_CODE, "验证码");
            validateRequired(REQ_CAPTCHA_TOKEN, "验证码");
            validateVerifyCaptcha();
            if (hasSessionKey())
                validateRequest(REQ_USER_ID, REQ_IMEI, REQ_CAPTCHA_CODE, REQ_CAPTCHA_TOKEN);
            else
                validateRequestNoSessionKey(REQ_USER_ID, REQ_IMEI, REQ_CAPTCHA_CODE, REQ_CAPTCHA_TOKEN);
            if (callback != null) {
                callback.validate(getRequestLong(REQ_USER_ID));
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
        Long userId = getRequestLong(REQ_USER_ID);
        //根据userId取手机号
        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(userId);
        if (StringUtils.isBlank(authenticatedMobile)) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, "获取手机号码失败");
            return resultMap;
        }

        String userMobile = authenticatedMobile;

        MapMessage sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userId,
                userMobile,
                smsType.name());

        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 有图文验证码
     * 发送验证码
     *
     * @param smsType  消息类型
     * @param callback 回调校验
     * @return
     */
    public MapMessage sendVerifyCode(SmsType smsType, ValidateCallback callback) {

        MapMessage resultMap = new MapMessage();
        String userMobile;
        try {
            // FIXME temp log verification code request
            String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
            if (StringUtils.isNoneBlank(pickUpLog) && "1".equals(pickUpLog)) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", getRequestString(REQ_USER_CODE),
                        "appKey", getRequestString(REQ_APP_KEY),
                        "clientIp", getWebRequestContext().getRealRemoteAddress(),
                        "op", "sendRegisterVerifyCodeBefore"
                ));
            }

            validateRequired(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequired(REQ_CAPTCHA_CODE, "验证码");
            validateRequired(REQ_CAPTCHA_TOKEN, "验证码");
            validateVerifyCaptcha();
            if (hasSessionKey())
                validateRequest(REQ_USER_CODE, REQ_IMEI, REQ_CAPTCHA_CODE, REQ_CAPTCHA_TOKEN);
            else {
                if (smsType == SmsType.APP_TEACHER_VERIFY_MOBILE_LOGIN_MOBILE && getRequest().getParameter(REQ_USER_ID) != null) {
                    // 需求wiki: http://wiki.17zuoye.net/pages/viewpage.action?pageId=44845258
                    // 换设备验证的时候，传过来的是掩码手机号，所以需要传uid，此时将uid加入到签名校验中
                    validateRequestNoSessionKey(REQ_USER_CODE, REQ_IMEI, REQ_CAPTCHA_CODE, REQ_CAPTCHA_TOKEN, REQ_USER_ID);
                } else {
                    validateRequestNoSessionKey(REQ_USER_CODE, REQ_IMEI, REQ_CAPTCHA_CODE, REQ_CAPTCHA_TOKEN);
                }
            }

            userMobile = getRequestString(REQ_USER_CODE);
            if (smsType == SmsType.APP_TEACHER_VERIFY_MOBILE_LOGIN_MOBILE && StringUtils.contains(userMobile, "*")) {
                // 需求wiki: http://wiki.17zuoye.net/pages/viewpage.action?pageId=44845258
                // 换设备验证的时候，传过来的是掩码手机号，此时通过uid拿手机号
                userMobile = sensitiveUserDataServiceClient.loadUserMobile(getRequestLong(REQ_USER_ID));
            } else {
                userMobile = getRequestString(REQ_USER_CODE);
            }
            callback.validate(userMobile);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        Long userId = currentUserId();
        MapMessage sendVerifyCodeResult;

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        if (userId == null) {
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    smsType.name(),
                    false);
        } else {
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userId,
                    userMobile,
                    smsType.name());
        }

        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 没有图文验证码
     * 发送验证码
     *
     * @param smsType
     * @param callback 回调校验
     * @return
     */
    public MapMessage sendVerify(SmsType smsType, ValidateCallback callback) {

        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_IMEI);
            if (hasSessionKey())
                validateRequest(REQ_USER_CODE, REQ_IMEI);
            else
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
            // 其他验证
            callback.validate(getRequestString(REQ_USER_CODE));
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        String userMobile = getRequestString(REQ_USER_CODE);
        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userMobile,
                smsType.name(),
                false);

        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }

    public interface ValidateCallback {
        void validate(String monile) throws IllegalArgumentException;
    }

    public interface UserValidateCallback {
        void validate(Long userId) throws IllegalArgumentException;
    }

    private String getImeiSentMobileListKey(String imei) throws Exception {
        return MEMCACHE_KEY_PREFIX_IMEI_SENT_MOBILE + imei;
    }

    private String getMobileRecvImeiListKey(String mobile) throws Exception {
        return MEMCACHE_KEY_PREFIX_MOBILE_RECV_IMEI + mobile;
    }

    private String getMobileRecvCountKey(String imei, String mobile) throws Exception {
        return MEMCACHE_KEY_PREFIX_IMEI_SENT_MOBILE + imei + "_" + mobile;
    }

    private void validateVerifyCaptcha() {
        String captchaCode = getRequestString(REQ_CAPTCHA_CODE);
        String captchaToken = getRequestString(REQ_CAPTCHA_TOKEN);
        // 新增图文验证码校验
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            throw new IllegalArgumentException("请正确输入图中的验证码");
        }
    }

    private void validateVerifyCodeInfo() throws Exception {
        String userMobile = getRequestString(REQ_USER_CODE);
        baseValidateVerifyCodeInfo(userMobile);
    }

    private void validateVerifyCodeInfo(String userMobile) throws Exception {
        baseValidateVerifyCodeInfo(userMobile);
    }

    private void baseValidateVerifyCodeInfo(String userMobile) throws Exception {
        String imei = getRequestString(REQ_IMEI);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        // 防止骚扰用户，每个IMEI每天至多给3个不同的手机号码发验证
        // FIXME 他大爷的。。iOS1.9.9把IMEI写死了。。导致每天只能发送成功3个机器的验证码
        // FIXME 这个接口还有第三方在调用。。所以ver为空也得校验
        // FIXME 目前需要校验的是：
        // FIXME 1.所有来自第三方的请求（ver为空）
        // FIXME 2.来自客户端的请求，不包括iOS1.9.9.156
        String sentMobiles = washingtonCacheSystem.CBS.flushable.load(getImeiSentMobileListKey(imei));
        if ((StringUtils.isBlank(ver) ||
                (StringUtils.equalsIgnoreCase(sys, "ios") && StringUtils.isNotBlank(ver) && VersionUtil.compareVersion(ver, "1.9.9.156") != 0))
                && !StringUtils.isEmpty(sentMobiles) && !sentMobiles.contains(userMobile) && sentMobiles.split(",").length >= 4) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")请求接受的验证码超过上限!==单个imei发送的手机号码个数超过限制");
            throw new IllegalArgumentException("请求发送的验证码超过上限!");
        }

        // 防止骚扰用户，每个手机号至多接受3个IMEI的验证码
        String mobileRecvImeis = washingtonCacheSystem.CBS.flushable.load(getMobileRecvImeiListKey(userMobile));
        if (!StringUtils.isEmpty(mobileRecvImeis) && !mobileRecvImeis.contains(imei) && mobileRecvImeis.split(",").length >= 3) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")请求接受的验证码超过上限!==单个手机号使用的imei个数超过限制");
            throw new IllegalArgumentException("用户手机号码(" + userMobile + ")请求接受的验证码超过上限");
        }

        // 防止骚扰用户，每个IEMI给同一个手机号码至多发5次
        Integer sentCount = washingtonCacheSystem.CBS.flushable.load(getMobileRecvCountKey(imei, userMobile));
        if (sentCount != null && sentCount > 10) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")发送验证码的次数超过上限!===单个imei给单个手机号发送超过限制");
            throw new IllegalArgumentException("用户手机号码(" + userMobile + ")发送验证码的次数超过上限!");
        }
    }

    private void updateMemCacheInfo(String userMobile) {
        try {
//            String userMobile = getRequestString(REQ_USER_CODE);
            String imei = getRequestString(REQ_IMEI);
            Integer liveTime = DateUtils.getCurrentToDayEndSecond(); // 这里从24小时时长有效改成了当天有效

            final String sentMobilesKey = getImeiSentMobileListKey(imei);
            String sentMobiles = washingtonCacheSystem.CBS.flushable.load(sentMobilesKey);
            washingtonCacheSystem.CBS.flushable.set(sentMobilesKey, liveTime, addValue(sentMobiles, userMobile));

            final String mobileRecvImeiKey = getMobileRecvImeiListKey(userMobile);
            String mobileRecvImeis = washingtonCacheSystem.CBS.flushable.load(mobileRecvImeiKey);
            washingtonCacheSystem.CBS.flushable.set(mobileRecvImeiKey, liveTime, addValue(mobileRecvImeis, imei));

            final String sentCountKey = getMobileRecvCountKey(imei, userMobile);
            Integer sentCount = washingtonCacheSystem.CBS.flushable.load(sentCountKey);
            if (sentCount == null) {
                sentCount = 1;
            } else {
                sentCount++;
            }
            washingtonCacheSystem.CBS.flushable.set(sentCountKey, liveTime, sentCount);

        } catch (Exception e) {
            logger.warn("更新缓存时出现错误!", e);
        }
    }

    private static String addValue(String orgValue, String addedValue) {
        if (StringUtils.isEmpty(orgValue)) {
            return addedValue;
        }

        if (StringUtils.isEmpty(addedValue) || orgValue.contains(addedValue)) {
            return orgValue;
        }

        return orgValue + "," + addedValue;
    }

    private boolean validateLimitation(String appKey, String userMobile, String clientIp) {
        return true;
//        // FIXME IP白名单
//        String smsWhiteIps = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "sms_white_ip");
//        if (StringUtils.isNoneBlank(smsWhiteIps) && smsWhiteIps.contains(clientIp)) {
//            return true;
//        }
//
//        String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
//        if (StringUtils.isNoneBlank(pickUpLog) && "1".equals(pickUpLog)) {
//            LogCollector.info("backend-general", MiscUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userMobile,
//                    "appKey", appKey,
//                    "clientIp", clientIp,
//                    "op", "sendRegisterVerifyCode"
//            ));
//        }
//
//        // FIXME 有人盗刷验证码接口，临时按照IP进行计数，超过20就不发了
//        String cacheKey = "SMS_IP_COUNT:" + clientIp;
//        long count = washingtonCacheSystem.CBS.flushable.incr(cacheKey, 1, 1, 3600);
//        return count <= 10;
    }
}
