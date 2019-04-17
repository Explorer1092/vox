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

package com.voxlearning.washington.controller.open;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MDCUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.CurrentRuntimeMode;
import com.voxlearning.galaxy.service.coin.api.DPCoinLoader;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserDeviceInfoServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.userlevel.api.UserLevelLoader;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.vendor.client.VendorAppsResgRefServiceClient;
import com.voxlearning.utopia.service.vendor.client.VendorResgContentServiceClient;
import com.voxlearning.washington.athena.SearchEngineServiceClient;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.controller.open.v1.content.ContentApiConstants;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.SessionUtils;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Abstract controller class for Open Platform API
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
public class AbstractApiController extends AbstractController {

    protected static final String APP_JXT_EASE_MOB_GROUP_ICON = "/public/skin/parentMobile/images/new_icon/banji_new.png";
    protected static final String APP_JXT_EASE_MOB_GROUP_ICON_UNABLE = "/public/skin/parentMobile/images/new_icon/banji_unable_new.png";
    protected static final String APP_JXT_GROUP_ICON_UNABLE = "/public/skin/parentMobile/images/new_icon/banji_unable_new.png";
    protected static final String APP_JXT_GROUP_ICON = "/public/skin/parentMobile/images/new_icon/banji_new.png";
    protected static final String CHANNEL_C_USER_WEB_SOURCE = "17Parent-c";
    protected static final MapMessage noUserResult = new MapMessage().add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE).add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
    private static final String OPEN_API_COLLECTION_NAME = "vendor_apps_logs";
    private static final List<String> VALID_APP_KEYS = new ArrayList<>();
    private static final List<String> VALID_MOBILE_APP_KEYS = Arrays.asList("17Student", "17Parent", "17Teacher",
            "17Yunketang",
            "17JuniorStu", "17JuniorTea", "17JuniorPar",
            "17Agent",
            "Daite",
            "StudyMates",
            "Shensz");
    protected static String bookImgUrlPrefix = "http://cdn-cnc.17zuoye.cn/resources/app/jzt/res/{0}.png";

    static {
        List<OrderProductServiceType> allValidTypes = OrderProductServiceType.getAllValidTypes();
        for (OrderProductServiceType serviceType : allValidTypes) {
            VALID_APP_KEYS.add(serviceType.name());
        }

        // VALID_APP_KEYS.addAll(VALID_MOBILE_APP_KEYS);
    }

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject protected AsyncFootprintServiceClient asyncFootprintServiceClient;
    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;
    @Inject private VendorResgContentServiceClient vendorResgContentServiceClient;
    @Inject private VendorAppsResgRefServiceClient vendorAppsResgRefServiceClient;
    @Inject private SearchEngineServiceClient searchEngineServiceClient;
    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @Inject private UserDeviceInfoServiceClient userDeviceInfoServiceClient;

    @ImportService(interfaceClass = UserLevelService.class)
    protected UserLevelService userLevelService;
    @ImportService(interfaceClass = UserLevelLoader.class)
    protected UserLevelLoader userLevelLoader;
    @ImportService(interfaceClass = DPCoinLoader.class)
    protected DPCoinLoader dpCoinLoader;

    protected static MapMessage convert2NativeMessage(MapMessage mapMessage) {
        return convert2NativeMessage(mapMessage, "");
    }

    protected static MapMessage convert2NativeMessage(MapMessage mapMessage, String defaultInfo) {
        Objects.requireNonNull(mapMessage);
        if (mapMessage.isSuccess())
            return successMessage();
        else {
            if (StringUtils.isBlank(mapMessage.getInfo()) && StringUtils.isNotBlank(defaultInfo)) {
                return failMessage(defaultInfo);
            } else
                return failMessage(mapMessage.getInfo());
        }
    }

    protected static MapMessage successMessage(String key, Object value) {
        MapMessage message = new MapMessage();
        message.add(RES_RESULT, RES_RESULT_SUCCESS);
        message.add(key, value);
        return message;
    }

    protected static MapMessage failMessage(Exception e) {
        if (e instanceof IllegalVendorUserException) {
            MapMessage message = new MapMessage();
            message.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
            message.add(RES_MESSAGE, e.getMessage());
            return message;
        }
        return failMessage(e.getMessage());
    }

    protected static MapMessage failMessage(String msg) {
        MapMessage message = new MapMessage();
        message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        message.add(RES_MESSAGE, msg);
        return message;
    }

    protected static MapMessage failMessage(String code, String msg) {
        MapMessage message = new MapMessage();
        message.add(RES_RESULT, code);
        message.add(RES_MESSAGE, msg);
        return message;
    }

    protected static MapMessage successMessage(String msg) {
        MapMessage message = new MapMessage();
        message.add(RES_RESULT, RES_RESULT_SUCCESS);
        message.add(RES_MESSAGE, msg);
        return message;
    }

    protected static MapMessage successMessage() {
        MapMessage message = new MapMessage();
        message.add(RES_RESULT, RES_RESULT_SUCCESS);
        return message;
    }

    public void validateRequestNoSessionKey(String... paramKeys) {
        if (!RuntimeMode.isDevelopment()) {
            VendorApps app = getApiRequestApp();
            if (app == null) {
                logValidateError("error_app");
                throw new IllegalArgumentException(RES_RESULT_APP_ERROR_MSG);
            }

            if (!isValidRequest(false, paramKeys)) {
                logValidateError("error_sig");
                throw new IllegalArgumentException(RES_RESULT_BAD_REQUEST_MSG);
            }
        }
    }

    public void validateRequest(String... paramKeys) {
        VendorApps app = getApiRequestApp();
        if (app == null) {
            logValidateError("error_app");
            throw new IllegalArgumentException(RES_RESULT_APP_ERROR_MSG);
        }

        if (!isValidRequest(true, paramKeys)) {
            logValidateError("error_sig");
            throw new IllegalArgumentException(RES_RESULT_BAD_REQUEST_MSG);
        }

        if (!isValidVendorUser()) {
            logValidateError("error_vendor_user");
            throw new IllegalVendorUserException(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_SESSION_KEY_EXPIRED_MSG);
        }

        User curUser = getApiRequestUser();
        if (curUser == null) {
            logValidateError("error_user");
            throw new IllegalArgumentException(RES_RESULT_USER_ERROR_MSG);
        }
    }

    protected void logValidateError(String reason) {
        try {
            User curUser = getApiRequestUser();
            com.voxlearning.alps.spi.bootstrap.LogCollector.info("app_validate_request_error_logs",
                    MiscUtils.map(
                            "app_key", getRequestString(REQ_APP_KEY),
                            "system", getRequestString(REQ_SYS),
                            "version", getRequestString(REQ_APP_NATIVE_VERSION),
                            "has_session_key", StringUtils.isNotBlank(getRequestString(REQ_SESSION_KEY)),
                            "reason", reason,
                            "user_id", (curUser != null ? curUser.getId() : 0),
                            "uri", getRequest().getRequestURI(),
                            "env", RuntimeMode.getCurrentStage(),
                            "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date()),
                            "params", getRequestAllParamsStr(),
                            "channel", getRequestString(REQ_CHANNEL)
                    ));
        } catch (Exception e) {
            // ignore it
        }
    }

    private String getRequestAllParamsStr() {
        Iterator iterator = getRequest().getParameterMap().entrySet().iterator();
        StringBuilder param = new StringBuilder();
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            Map.Entry entry = (Map.Entry) iterator.next();
            if (i == 1)
                param.append("?").append(entry.getKey()).append("=");
            else
                param.append("&").append(entry.getKey()).append("=");
            if (entry.getValue() instanceof String[]) {
                param.append(((String[]) entry.getValue())[0]);
            } else {
                param.append(entry.getValue());
            }
        }
        return param.toString();
    }

    // 验证客户端传过来的sessionkey是否跟服务端匹配
    // 有些特殊需求会更新sessionkey，导致前后端不一致，进而需要客户端重新登录
    // 方便以后用作sessionkey过期的检验
    public boolean isValidVendorUser() {
        String sessionKey = getRequestString(REQ_SESSION_KEY);
        OpenApiRequestContext ctx = (OpenApiRequestContext) getRequest().getAttribute(OpenApiRequestContext.class.getName());
        VendorAppsUserRef vendorAppsUserRef = ctx.getVendorAppsUserRef();
        if (vendorAppsUserRef == null) {
            Long userId = decodeUserIdFromSessionKey(commonConfiguration.getSessionEncryptKey(), sessionKey);
            vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef(getRequestString(REQ_APP_KEY), userId);
            ctx.setVendorAppsUserRef(vendorAppsUserRef);
        }
        return vendorAppsUserRef != null && StringUtils.equals(vendorAppsUserRef.getSessionKey(), sessionKey);
    }

    public boolean isValidRequest(boolean hasSessionKey, String... paramKeys) {
        // get the app key from request
        String appKey = getRequestString(ApiConstants.REQ_APP_KEY);
        if (StringUtils.isEmpty(appKey)) {
            return false;
        }

        // validate the sig
        String genRequestSig = generateRequestSig(hasSessionKey, getSecretKey(), paramKeys);
        String orgRequestSig = getRequestString(REQ_SIG);

        return genRequestSig.equals(orgRequestSig);
    }

    public String generateSessionkey(Long userId) {
        return SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), userId);
    }

    public User getApiRequestUser() {
        OpenApiRequestContext ctx = (OpenApiRequestContext) getRequest().getAttribute(OpenApiRequestContext.class.getName());

        User requestUser = ctx.getRequestUser();

        if (requestUser == null) {
            String appKey = getRequestString(REQ_APP_KEY);
            String sessionKey = getRequestString(REQ_SESSION_KEY);

            // get user info by user id
            Long userId = decodeUserIdFromSessionKey(commonConfiguration.getSessionEncryptKey(), sessionKey);
            if (userId == null || userId <= 0L) {
                return null;
            }

            VendorAppsUserRef appUserRef = ctx.getVendorAppsUserRef() == null ? vendorLoaderClient.loadVendorAppUserRef(appKey, userId) : ctx.getVendorAppsUserRef();
            if (appUserRef == null) {
                // return null;

                // FIXME 先临时处理一版, Shensz和17JuniorTea/17JuniorStu/17JuniorPar的关系
                if (!"Shensz".equals(appKey)) {
                    return null;
                }
                String userType = SafeConverter.toString(userId);
                String convertAppKey = "17JuniorStu";
                if (userType.startsWith("1")) {
                    convertAppKey = "17JuniorTea";
                } else if (userType.startsWith("2")) {
                    convertAppKey = "17JuniorPar";
                }

                appUserRef = vendorLoaderClient.loadVendorAppUserRef(convertAppKey, userId);
                if (appUserRef == null) {
                    return null;
                }
            }

            if (!sessionKey.equals(appUserRef.getSessionKey())) {
                // return null;

                // FIXME 先临时处理一版, Shensz和17JuniorTea/17JuniorStu/17JuniorPar的关系
                if (!"Shensz".equals(appKey)) {
                    return null;
                }

                String userType = SafeConverter.toString(userId);
                String convertAppKey = "17JuniorStu";
                if (userType.startsWith("1")) {
                    convertAppKey = "17JuniorTea";
                } else if (userType.startsWith("2")) {
                    convertAppKey = "17JuniorPar";
                }

                appUserRef = vendorLoaderClient.loadVendorAppUserRef(convertAppKey, userId);
                if (appUserRef == null || !sessionKey.equals(appUserRef.getSessionKey())) {
                    return null;
                }
            }

            requestUser = raikouSystem.loadUser(userId);
            ctx.setRequestUser(requestUser);
            ctx.setVendorAppsUserRef(appUserRef);
        }

        return requestUser;
    }

    public StudentDetail getApiRequestStudentDetail() {
        User user = getApiRequestUser();
        if (user != null && user.isStudent()) {
            return studentLoaderClient.loadStudentDetail(user.getId());
        }
        return null;
    }

    public TeacherDetail getApiRequestTeacherDetail() {
        User user = getApiRequestUser();
        if (user != null && user.isTeacher()) {
            return teacherLoaderClient.loadTeacherDetail(user.getId());
        }
        return null;
    }

    public void clearApiRequestUser() {
        OpenApiRequestContext ctx = (OpenApiRequestContext) getRequest().getAttribute(OpenApiRequestContext.class.getName());
        ctx.setRequestUser(null);
    }

    // 得到请求的Request App
    public VendorApps getApiRequestApp() {
        OpenApiRequestContext ctx = (OpenApiRequestContext) getRequest().getAttribute(OpenApiRequestContext.class.getName());

        VendorApps requestApp = ctx.getRequestVendorApp();
        if (requestApp == null) {
            String appKey = getRequestString(REQ_APP_KEY);
            if (StringUtils.isEmpty(appKey)) {
                return null;
            }

            requestApp = vendorLoaderClient.getExtension().loadVendorApp(appKey);
            ctx.setRequestVendorApp(requestApp);
        }

        return requestApp;
    }

    protected String getClientVersion() {
        return getRequestString(REQ_APP_NATIVE_VERSION);
    }

    protected String getClientSys() {
        return getRequestString(REQ_SYS);
    }

    protected Boolean clientIsAndroid() {
        return "android".equalsIgnoreCase(getClientSys());
    }

    protected Boolean clientIsIos() {
        return "ios".equalsIgnoreCase(getClientSys());
    }

    private String getSecretKey() {
        VendorApps apps = getApiRequestApp();
        if (apps == null) {
            return "";
        }

        return apps.getSecretKey();
    }

    private String generateRequestSig(boolean hasSessionKey, String secretKey, String... paramKeys) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(REQ_APP_KEY, getRequestString(REQ_APP_KEY));
        if (hasSessionKey) {
            paramMap.put(REQ_SESSION_KEY, getRequestString(REQ_SESSION_KEY));
        }

        for (String paramKey : paramKeys) {
            paramMap.put(paramKey, getRequestString(paramKey));
        }

        return DigestSignUtils.signMd5(paramMap, secretKey);
    }

    protected String attachUser2RequestApp(Long userId) {
        if (userId == null) {
            return null;
        }

        String appKey = getRequestString(REQ_APP_KEY);

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, userId)
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) {
            throw new IllegalStateException("No VendorAppsUserRef Found");
        }
        VendorAppsUserRef userRef = (VendorAppsUserRef) message.get("ref");
        return userRef.getSessionKey();
    }

    protected static <T> T validateNotNull(final T object, final String message, final Object... values) {
        if (object == null) {
            throw new IllegalArgumentException(String.format(message, values));
        }
        return object;
    }

    protected void validateRequired(String paramKey, Object... msgParams) {
        String paramValue = getRequestString(paramKey);
        if (StringUtils.isEmpty(paramValue)) {
            logValidateError("required_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_MSG, msgParams));
        }
    }

    // 字符串类型,必须+长度检查
    protected void validateRequiredLength(String paramKey, int minLen, int maxLen, Object... msgParams) {
        String paramValue = getRequestString(paramKey);
        if (StringUtils.isEmpty(paramValue)) {
            logValidateError("required_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_MSG, msgParams));
        }

        if (minLen > 0 && StringUtils.length(paramValue) < minLen) {
            logValidateError("required_length_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_LENGTH_MSG, msgParams));
        }

        if (maxLen > 0 && StringUtils.length(paramValue) > maxLen) {
            logValidateError("required_length_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_LENGTH_MSG, msgParams));
        }
    }

    protected void validateRequiredNumber(String paramKey, Object... msgParams) {
        String paramValue = getRequestString(paramKey);
        if (StringUtils.isEmpty(paramValue) || !NumberUtils.isNumber(paramValue)) {
            logValidateError("required_number_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_NUMBER_MSG, msgParams));
        }
    }

    protected void validateRequiredAny(String paramKey1, String paramKey2, Object... msgParams) {
        String paramValue1 = getRequestString(paramKey1);
        String paramValue2 = getRequestString(paramKey2);
        if (StringUtils.isEmpty(paramValue1) && StringUtils.isEmpty(paramValue2)) {
            logValidateError("required_any_" + paramKey1 + "_" + paramKey2);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_ANY_MSG, msgParams));
        }
    }

    protected void validateNumber(String paramKey, Object... msgParams) {
        String paramValue = getRequestString(paramKey);
        if (!StringUtils.isEmpty(paramValue) && !NumberUtils.isNumber(paramValue)) {
            logValidateError("validate_number_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_NUMBER_MSG, msgParams));
        }
    }

    protected void validateDigitNumber(String paramKey, Object... msgParams) {
        String paramValue = getRequestString(paramKey);
        if (!StringUtils.isEmpty(paramValue) && !NumberUtils.isDigits(paramValue)) {
            logValidateError("validate_digitNumber_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_NUMBER_MSG, msgParams));
        }
    }

    protected void validateMobileNumber(String paramKey) {
        String paramValue = getRequestString(paramKey);
        if (!StringUtils.isEmpty(paramValue) && !MobileRule.isMobile(paramValue)) {
            logValidateError("validate_mobileNumber_" + paramKey);
            throw new IllegalArgumentException(VALIDATE_ERROR_MOBILE_MSG);
        }
    }

    protected void validateEnum(String paramKey, String paramName, Object... enumValues) {
        String paramValue = getRequestString(paramKey);
        if (StringUtils.isEmpty(paramValue)) {
            return;
        }

        for (Object enumValue : enumValues) {
            if (paramValue.equals(enumValue)) {
                return;
            }
        }
        logValidateError("validate_enum_" + paramKey);
        throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_ENUM_MSG, paramName));
    }

    public void logApiCallInfo() {

        // don't log unknown app keys
        String appKey = getRequestString(REQ_APP_KEY);
        if (!VALID_APP_KEYS.contains(appKey)) {
            return;
        }

        Map<String, String> loggingInfo = new HashMap<>();
        loggingInfo.put(REQ_APP_KEY, appKey);
        loggingInfo.put(REQ_SESSION_KEY, getRequestString(REQ_SESSION_KEY));
        if (StringUtils.isNoneBlank(getRequestString("source_app"))) {
            loggingInfo.put("source_app", getRequestString("source_app"));
        }

        String userId = "";
        if (getApiRequestUser() != null) {
            userId = String.valueOf(getApiRequestUser().getId());
        }
        loggingInfo.put("user_id", userId);

        String apiName = getWebRequestContext().getRequest().getRequestURI();
        if (apiName.indexOf(".") > 0) {
            apiName = apiName.substring(0, apiName.indexOf("."));
        }
        loggingInfo.put("api_name", apiName);

        if ("/v1/clazz/share".equals(apiName)
                || "/v1/clazz/sysshare".equals(apiName)
                || "/v1/user/integral/add".equals(apiName)
                || apiName.startsWith("/v1/user/wechat/")
                || apiName.startsWith("/v1/appmessage/")) {
            loggingInfo.put("op", "batch");
        }

        loggingInfo.put("app_client_ip", getWebRequestContext().getRealRemoteAddress());

        LogCollector.info(OPEN_API_COLLECTION_NAME, loggingInfo);
    }

    // 记录登录次数
    public void logLoginCount() {
        User user = getApiRequestUser();
        VendorApps vendorApps = getApiRequestApp();
        // redmine 19054
        // 排除除了学生App端,家长App端,老师App端的所有第三方应用
        if (vendorApps == null || !VALID_MOBILE_APP_KEYS.contains(vendorApps.getAppKey())) {
            return;
        }
        if (user != null && user.getId() != null) {
            asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(),
                    getWebRequestContext().getRealRemoteAddress(),
                    UserRecordMode.LOGIN,// 这里应该是记错了，这块应该算VALIDATE
                    OperationSourceType.app,
                    true,
                    getAppType());
        }
    }

    // 将当前请求的用户id手动放入log中
    public void insertUserNameIntoMDC() {
        User user = getApiRequestUser();
        if (user != null) {
            MDCUtils.insertUserIdMDC(user.getId());
        }
    }

    protected MapMessage filterResponseContent(MapMessage responseData) {
        List<String> disallowResList = loadDisallowResList(getRequestString(REQ_APP_KEY));

        if (disallowResList != null && disallowResList.size() > 0) {
            for (String disallowRes : disallowResList) {
                if (responseData.get(disallowRes) != null) {
                    responseData.remove(disallowRes);
                }
            }
        }

        return responseData;
    }

    protected void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
        if (value == null) {
            dataMap.put(key, "");
        } else {
            dataMap.put(key, value);
        }
    }

    protected String getCrmBaseUrl() {
        String url = "http://admin.test.17zuoye.net/";
        if (RuntimeMode.isProduction()) {
            url = "http://admin.17zuoye.net/";
        } else if (RuntimeMode.isStaging()) {
            url = "http://admin.staging.17zuoye.net/";
        }

        return url;
    }

    protected static boolean specialSchool(Long schoolId) {
        // 353246：银座九号
        // 403069：一起作业培训学校
        // 431729：浦项小学
        List<Long> specialSchoolIds = Arrays.asList(353246L, 403069L, 431729L);
        if (schoolId == null) {
            return false;
        }
        return specialSchoolIds.contains(schoolId);
    }

    protected MapMessage internalUserLogin() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_USER_CODE, "用户名");
            validateRequired(REQ_PASSWD, "密码");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            if (StringUtils.isBlank(getRequestString(REQ_USER_TYPE))) {
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_PASSWD);
            } else {
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_PASSWD, REQ_USER_TYPE);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 验证用户名和密码
        String userAccount = getRequestString(REQ_USER_CODE);
        String userPassword = getRequestString(REQ_PASSWD);
        int reqUserType = 0;
        if (StringUtils.isNotBlank(getRequestString(REQ_USER_TYPE))) {
            reqUserType = getRequestInt(REQ_USER_TYPE);
        }
        String uuid = getRequestString(REQ_UUID);
        List<User> loginUserList = userLoaderClient.loadUsers(userAccount, null);

        List<Map<String, Object>> retUserList = new ArrayList<>();

        int userType = 0;
        String userPass = "";
        boolean useTmpPwd = false;
        for (User loginUser : loginUserList) {

            // 临时密码校验 xuesong.zhang 2015-11-19
            boolean tempMatch = false;
            if (StringUtils.isNotBlank(userPassword) && StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(loginUser.getId()), userPassword)) {
                tempMatch = true;
                useTmpPwd = true;
            }

            UserAuthentication ua = userLoaderClient.loadUserAuthentication(loginUser.getId());
            if (tempMatch || ua.fetchUserPassword().match(userPassword)) {
                // 如果指定了登陆身份的,需要按照登陆身份进行匹配
                if (reqUserType > 0 && !loginUser.getUserType().equals(reqUserType)) {
                    continue;
                }
                Map<String, Object> userItem = new HashMap<>();

                // 新设备校验 begin，先只对老师处理 xuesong.zhang 2019-3-20
                // 中学不校验，版本号小于1.9.3的不校验，对临时密码不做校验，未绑定手机的用户不做校验
                if (!loginVersionCheck("1.9.4.0", Ktwelve.PRIMARY_SCHOOL, getRequestString(REQ_USER_CODE), UserType.TEACHER)) {
                    TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(loginUser.getId());
                    // 特殊学校和test环境不做新设备校验
                    if (teacher != null && !specialSchool(teacher.getTeacherSchoolId()) && CurrentRuntimeMode.get() != Mode.TEST) {
                        boolean isJunior = teacher.isJuniorTeacher() || teacher.isSeniorTeacher();
                        if (StringUtils.isNotBlank(ua.getSensitiveMobile()) && !isJunior) {
                            if (!tempMatch && (reqUserType == 1 || loginUser.getUserType() == 1)) {
                                if (StringUtils.isEmpty(uuid)) {
                                    uuid = getRequestString(REQ_IMEI);
                                }

                                Set<String> deviceIds = userDeviceInfoServiceClient.loadDeviceIds(loginUser.getId());
                                if (CollectionUtils.isNotEmpty(deviceIds) && !deviceIds.contains(uuid)) {
                                    String am = sensitiveUserDataServiceClient.loadUserMobileObscured(loginUser.getId());
                                    // 密码登录的新设备
                                    userType = loginUser.getUserType();
                                    userItem.put(RES_USER_ID, loginUser.getId());
                                    userItem.put(RES_USER_MOBILE, am);
                                    userItem.put(RES_NEW_DEVICE, true);
                                    retUserList.add(userItem);
                                    continue;
                                }
                            }
                        }
                    }
                }
                // 新设备校验 end

                userItem.put(RES_USER_TYPE, loginUser.getUserType());
                userItem.put(RES_USER_ID, loginUser.getId());
                String sessionKey = attachUser2RequestApp(loginUser.getId());
                userItem.put(RES_SESSION_KEY, sessionKey);
                userItem.put(RES_REAL_NAME, loginUser.getProfile().getRealname());
                userItem.put(RES_AVATAR_URL, loginUser.getProfile().getImgUrl());
                userItem.put(RES_NEW_DEVICE, false);
                retUserList.add(userItem);

                userType = loginUser.getUserType();
                userPass = ua.getPassword();
            }
        }

        if (retUserList.size() == 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOW_USER_ACCOUNT_MSG);
//            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_PASSWORD_MSG);
            // 登录失败时记录信息
            recordUserLoginFailure(loginUserList);
            // redmine 26236
            Map<String, String> logInfo = new HashMap<>();
            logInfo.put("usertoken", userAccount);
            logInfo.put("usertype", SafeConverter.toString(reqUserType));
            logInfo.put("platform", getApiRequestApp().getAppKey());
            logInfo.put("version", getRequestString(REQ_APP_NATIVE_VERSION));
            logInfo.put("op", "app user login failed");
            logInfo.put("mod1", getRequestString(REQ_SYS));
            if (StringUtils.isEmpty(uuid)) {
                uuid = getRequestString(REQ_IMEI);
            }
            logInfo.put("mod3", uuid);
            if (CollectionUtils.isEmpty(loginUserList)) {
                logInfo.put("mod2", "account does not exist");
                LogCollector.info("backend-general", logInfo);
            } else {
                logInfo.put("mod2", "password validation failed");
                LogCollector.info("backend-general", logInfo);
            }
        } else {
            // 登录成功时记录信息
            if (retUserList.size() == 1) {
                RoleType roleType = RoleType.of(userType);
                if (("17ZuoyeMobile".equals(getApiRequestApp().getAppKey()) || "17ZYAdventure".equals(getApiRequestApp().getAppKey()) || "17Student".equals(getApiRequestApp().getAppKey())) && RoleType.ROLE_STUDENT != roleType) { //学生端app登录的角色不是学生，直接给出提示
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_ROLE_STUDENT);
                    return resultMap;
                } else if ("17Teacher".equals(getApiRequestApp().getAppKey()) && RoleType.ROLE_TEACHER != roleType) { //老师端app登录的角色不是老师，直接给出提示
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_ROLE_TEACHER);
                    return resultMap;
                } else if ("17Parent".equals(getApiRequestApp().getAppKey()) && RoleType.ROLE_PARENT != roleType) { //家长端app登录的角色不是家长，直接给出提示
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_ROLE_PARENT);
                    return resultMap;
                }
                Long userId = (Long) retUserList.get(0).get(RES_USER_ID);
                // 在这里做一下是否封禁的判断-- 老师
                TeacherExtAttribute teacherExtAttribute;
                if (RoleType.ROLE_TEACHER == roleType && (teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(userId)) != null) {
                    if (teacherExtAttribute.isForbidden() || teacherExtAttribute.isFreezing()) {
                        resultMap.add(RES_RESULT, RES_RESULT_TOAST_CODE);
                        resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
                        return resultMap;
                    }
                }

                // 是否为新设备
                if (Objects.equals(retUserList.get(0).get(RES_NEW_DEVICE), Boolean.TRUE)) {
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                    resultMap.add(RES_USER_ID, retUserList.get(0).get(RES_USER_ID));
                    resultMap.add(RES_USER_MOBILE, retUserList.get(0).get(RES_USER_MOBILE));
                    resultMap.add(RES_NEW_DEVICE, true);
                    return resultMap;
                }

                afterUserLoginSuccess(userId, userPass, roleType, useTmpPwd);
//                userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.app);
//                userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
//
//                getWebRequestContext().saveAuthenticationStates(-1, userId, userPass, roleType);
//
//                //如果是短信邀请进来的，默认绑定手机号
//                miscServiceClient.bindInvitedTeacherMobile(userId);
            } else {
                // 多用户的时候，删除SessionKey确保应用使用UserID再次登录
                for (Map<String, Object> retInfo : retUserList) {
                    retInfo.remove(RES_SESSION_KEY);
                }
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_USER_LIST, retUserList);
        }

        return resultMap;
    }

    /**
     * @param userId
     * @param password
     * @param type
     * @param useTmpPwd 是否为临时密码
     */
    protected void afterUserLoginSuccess(Long userId, String password, RoleType type, boolean useTmpPwd) {
        // 上报用户登录日志
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.app,
                false,
                getAppType());

        getWebRequestContext().saveAuthenticationStates(-1, userId, password, type);

        //如果是短信邀请进来的，默认绑定手机号
        miscServiceClient.bindInvitedTeacherMobile(userId);

        // 临时密码不用记录设备号
        if (!useTmpPwd) {
            // 记录设备信息
            String uuid = getRequestString(REQ_UUID);
            if (StringUtils.isEmpty(uuid)) {
                uuid = getRequestString(REQ_IMEI);
            }

            String sys = getRequestString(REQ_SYS);
            String model = getRequestString(REQ_MODEL);

            asyncFootprintServiceClient.getAsyncFootprintService().recordUserDeviceInfo(userId, uuid, sys, model);
        }
    }

    private void recordUserLoginFailure(List<User> userList) {
        if (userList == null || userList.size() == 0) {
            return;
        }
        for (User user : userList) {
//            userServiceClient.recordUserLoginFailure2(user.getId());
            asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(user.getId());
        }
    }

    protected String generateVersionUrl(String html5Url) {
        // 如果html5Url为空或者不以.html .vhapp结尾（静态地址）则直接返回
        if (StringUtils.isEmpty(html5Url) || !(html5Url.endsWith(".html") || html5Url.endsWith(".vhapp"))) {
            return html5Url;
        }
        // 加入-V20151001类似版本号
        String versionedUrlPath = cdnResourceVersionCollector.getVersionedUrlPath("/" + html5Url);
        if (StringUtils.isNotBlank(versionedUrlPath)) {
            if (versionedUrlPath.startsWith("/")) {
                versionedUrlPath = versionedUrlPath.substring(1);
            }
            return StringUtils.replace(versionedUrlPath, ".html", ".vhapp");
        } else {
            return StringUtils.replace(html5Url, ".html", ".vhapp");
        }
    }

    // 在open api里有多处调用。。暂时不封装在service里，其他地方不会有这种逻辑
    // 查询绑定的家长手机号：如果有关键家长并且有手机号，直接返回
    // 如果没有关键家长，返回第一个有手机号的家长手机号
    // 如果都没有手机号，则返回字段为空字符串
    // 注意：此处返回的不是obscuredMobile，有地方需要明文手机号，比如发验证码
    protected String getParentMobileByStudentId(Long studentId) {
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        List<String> parentMobileList = new ArrayList<>();
        for (StudentParent studentParent : studentParents) {
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(studentParent.getParentUser().getId());

            if (mobile != null) {
                parentMobileList.add(mobile);
                if (studentParent.isKeyParent()) {
                    return mobile;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(parentMobileList)) {
            return parentMobileList.get(0);
        }

        return StringUtils.EMPTY;
    }

    protected boolean hasSessionKey() {
        return StringUtils.isNotEmpty(getRequestString(REQ_SESSION_KEY));
    }

    protected Set<String> getUserMessageTagList(Long userId) {
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        Set<String> tagSet = new HashSet<>();
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return tagSet;
        }
        if (UserType.TEACHER == user.fetchUserType()) {
            //包班制老师 用主账号生成tag
            Long mainId = teacherLoaderClient.loadMainTeacherId(userId);
            if (mainId == null) {
                mainId = userId;
            }
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(mainId);
            generateTeacherTag(teacherDetail, tagSet);
            //YiQiXueTeacher的IM Tag
            Set<String> yiQiXueTag = asyncVendorCacheServiceClient.getAsyncVendorCacheService().loadYiQiXuePushTag(user.getId()).getUninterruptibly();
            if (CollectionUtils.isNotEmpty(yiQiXueTag)) {
                tagSet.addAll(yiQiXueTag);
            }
        } else if (UserType.STUDENT == user.fetchUserType()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            generateStudentTag(studentDetail, tagSet, false);
            //学生黑名单的恶心tag
            if (studentDetail.isInPaymentBlackListRegion()) {
                tagSet.add(JpushUserTag.PAYMENT_BLACK_LIST.tag);
            }
            if (!studentDetail.isInPaymentBlackListRegion()) {
                tagSet.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
            }

        } else if (UserType.PARENT == user.fetchUserType()) {
            //避免审核账号收到推送。直接不返回tag
            if (Objects.equals(20001L, user.getId())) {
                return Collections.emptySet();
            }
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(userId).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            studentDetails.stream().forEach(p -> generateStudentTag(p, tagSet, true));
            tagSet.add(JpushUserTag.USER_ALL_ONLY_FOR_PARENT.tag);
            //家长的黑名单取全部孩子的区域
            if (userBlacklistServiceClient.isInBlackListByParent(user, new ArrayList<>(studentDetails))) {
                tagSet.add(JpushUserTag.PAYMENT_BLACK_LIST.tag);
            }
            //家长的孩子全部不在任何黑名单时。打下面这个tab
            if (!tagSet.contains(JpushUserTag.PAYMENT_BLACK_LIST.tag)) {
                tagSet.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
            }
            // 家长关注的或者系统自动关注的公众号tag
//            List<OfficialAccounts> accountsList = officialAccountsServiceClient.loadUserOfficialAccounts(user.getId());
//            if (CollectionUtils.isNotEmpty(accountsList)) {
//                for (OfficialAccounts accounts : accountsList) {
//                    tagSet.add(JpushUserTag.OFFICIAL_ACCOUNT_FOLLOW.generateTag(accounts.getAccountsKey()));
//                }
//            }
            //YiQiXueTeacher的IM Tag
            //分别取每个孩子的。然后拼一起。
            if (VersionUtil.compareVersion(version, "2.1.0") >= 0 && CollectionUtils.isNotEmpty(studentIds)) {
                studentIds.forEach(studentId -> {
                    Set<String> yiQiXueTag = asyncVendorCacheServiceClient.getAsyncVendorCacheService().loadYiQiXuePushTag(studentId).getUninterruptibly();
                    if (CollectionUtils.isNotEmpty(yiQiXueTag)) {
                        tagSet.addAll(yiQiXueTag);
                    }
                });
            }
            if (VersionUtil.compareVersion(version, "2.2.0") >= 0) {
                tagSet.add(JpushUserTag.REFACTOR_PUSH_VERSION.tag);
            }
        }
        if (StringUtils.isNotEmpty(version)) {
            version = version.replaceAll("\\.", "_");
            String versionTag = JpushUserTag.VERSION.generateTag(version);
            tagSet.add(versionTag);
        }
        //大数据打的tag
        try {
            Set<String> labelSet = searchEngineServiceClient.getUserLabelSet(userId);
            if (CollectionUtils.isNotEmpty(labelSet)) {
                tagSet.addAll(new HashSet<>(labelSet));
            }
        } catch (Exception e) {
            logger.info("searchEngineServiceClient.getUserLabelSet error,userId={},ex={}", userId, e);
        }
        return tagSet;
    }

    //生成学生tag
    private Set<String> generateStudentTag(StudentDetail studentDetail, Set<String> tagSet, boolean forParent) {
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        if (studentDetail == null) {
            return tagSet;
        }
        if (studentDetail.getRootRegionCode() != null) {
            tagSet.add(JpushUserTag.PROVINCE.generateTag(studentDetail.getRootRegionCode().toString()));
        }
        if (studentDetail.getCityCode() != null) {
            tagSet.add(JpushUserTag.CITY.generateTag(studentDetail.getCityCode().toString()));
        }
        if (studentDetail.getStudentSchoolRegionCode() != null) {
            tagSet.add(JpushUserTag.COUNTY.generateTag(studentDetail.getStudentSchoolRegionCode().toString()));
            tagSet.add("region_" + studentDetail.getStudentSchoolRegionCode().toString());
        }
        if (studentDetail.getClazzLevelAsInteger() != null) {
            tagSet.add(JpushUserTag.CLAZZ_LEVEL.generateTag(studentDetail.getClazzLevelAsInteger().toString()));
        }
        if (studentDetail.getClazz() != null) {
            if (studentDetail.getClazz().getSchoolId() != null) {
                tagSet.add(JpushUserTag.SCHOOL.generateTag(studentDetail.getClazz().getSchoolId().toString()));
            }
            if (studentDetail.getClazz().getId() != null) {
                tagSet.add(JpushUserTag.CLAZZ.generateTag(studentDetail.getClazz().getId().toString()));
            }
        }
        List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        studentGroups.forEach(p -> {
            tagSet.add(JpushUserTag.ClAZZ_GROUP.generateTag(p.getId().toString()));
            tagSet.add(JpushUserTag.SUBJECT.generateTag(p.getSubject().name()));
            //去掉环信的版本的班组信息
            if (forParent && VersionUtil.compareVersion(version, "2.2.0") >= 0) {
                tagSet.add(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(p.getId().toString()));
            }
        });
        if (studentDetail.isJuniorStudent()) {
            tagSet.add(JpushUserTag.JUNIOR_SCHOOL.tag);
        } else if (studentDetail.isSeniorStudent()) {
            tagSet.add(JpushUserTag.SENIOR_SCHOOL.tag);
        } else if (studentDetail.isPrimaryStudent()) {
            tagSet.add(JpushUserTag.PRIMARY_SCHOOL.tag);
        } else if (studentDetail.isInfantStudent()) {
            tagSet.add(JpushUserTag.INFANT_SCHOOL.tag);
        }
        return tagSet;
    }

    //生成老师tag
    private Set<String> generateTeacherTag(TeacherDetail teacherDetail, Set<String> tagSet) {
        if (teacherDetail == null) {
            return tagSet;
        }
        if (teacherDetail.getRootRegionCode() != null) {
            tagSet.add(JpushUserTag.PROVINCE.generateTag(teacherDetail.getRootRegionCode().toString()));
        }
        if (teacherDetail.getCityCode() != null) {
            tagSet.add(JpushUserTag.CITY.generateTag(teacherDetail.getCityCode().toString()));
        }
        if (teacherDetail.getRegionCode() != null) {
            tagSet.add(JpushUserTag.COUNTY.generateTag(teacherDetail.getRegionCode().toString()));
        }
        if (teacherDetail.getTeacherSchoolId() != null) {
            tagSet.add(JpushUserTag.SCHOOL.generateTag(teacherDetail.getTeacherSchoolId().toString()));
        }
        tagSet.add(JpushUserTag.AUTH.generateTag(teacherDetail.fetchCertificationState().name()));
        List<GroupTeacherMapper> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(teacherDetail.getId(), false);
        Set<Long> clazzIds = new HashSet<>();
        teacherGroups.stream().forEach(p -> {
            if (p != null) {
                clazzIds.add(p.getClazzId());
                tagSet.add(JpushUserTag.CLAZZ.generateTag(p.getClazzId().toString()));
                tagSet.add(JpushUserTag.ClAZZ_GROUP.generateTag(p.getId().toString()));
                tagSet.add(JpushUserTag.SUBJECT.generateTag(p.getSubject().name()));
            }
        });
        raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .forEach(p -> tagSet.add(JpushUserTag.CLAZZ_LEVEL.generateTag(p.getClassLevel())));
        if (teacherDetail.isJuniorTeacher()) {
            tagSet.add(JpushUserTag.JUNIOR_SCHOOL.tag);
        } else if (teacherDetail.isSeniorTeacher()) {
            tagSet.add(JpushUserTag.SENIOR_SCHOOL.tag);
        } else if (teacherDetail.isPrimarySchool()) {
            tagSet.add(JpushUserTag.PRIMARY_SCHOOL.tag);
        } else if (teacherDetail.isInfantTeacher()) {
            tagSet.add(JpushUserTag.INFANT_SCHOOL.tag);
        }
        return tagSet;
    }


    protected Boolean checkStudentParentRef(Long studentId, Long parentId) {
        if (studentId == null || studentId == 0 || parentId == null || parentId == 0)
            return false;
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isEmpty(studentParents))
            return false;
        Set<User> parents = studentParents.stream().map(StudentParent::getParentUser).collect(Collectors.toSet());
        Set<Long> parentIds = parents.stream().map(User::getId).collect(Collectors.toSet());
        return parentIds.contains(parentId);
    }


    protected Map<String, Object> convert2BookMapForOld(NewBookProfile bookProfile, NewBookCatalog newBookCatalog) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, ContentApiConstants.RES_BOOK_ID, bookProfile.getId());
        addIntoMap(map, RES_BOOK_CNAME, bookProfile.getName());
        addIntoMap(map, RES_BOOK_ENAME, bookProfile.getAlias());
        addIntoMap(map, RES_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map, RES_CLAZZ_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(map, RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, RES_BOOK_TERM, bookProfile.getTermType());

        if (newBookCatalog != null) {
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), newBookCatalog.getName());
            if (bookPress != null) {
                addIntoMap(map, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                addIntoMap(map, RES_BOOK_COLOR, bookPress.getColor());
                addIntoMap(map, RES_BOOK_IMAGE, MessageFormat.format(bookImgUrlPrefix, bookPress.getColor()));
            }
        }
        return map;
    }

    protected <T> T getAlpsFutureResult(AlpsFuture<T> future, T defaultValue) {
        if (future == null)
            return defaultValue;
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.warn("get AlpsFuture InterruptedException", e);
            return defaultValue;
        }
    }

    /**
     * 获取系统请求
     *
     * @return
     */
    public AppSystemType getAppSystemType() {
        if (isAndroidRequest(getRequest())) {
            return AppSystemType.ANDROID;
        } else if (isIOSRequest(getRequest())) {
            return AppSystemType.IOS;
        }
        return null;
    }

    public static Long decodeUserIdFromSessionKey(String key, String sessionKey) {
        try {
            String data = AesUtils.decryptHexString(key, sessionKey);
            return Long.valueOf(data.split(",")[0]);
        } catch (RuntimeException e) {
            //e.printStackTrace();
            return -1L;
        }
    }

    private Collection<VendorAppsResgRef> loadVendorAppResgRefsByAppKey(final String appKey) {
        if (appKey == null) {
            return Collections.emptyList();
        }
        return vendorAppsResgRefServiceClient.getVendorAppsResgRefService()
                .findVendorAppsResgRefsByAppKeyFromBuffer(appKey)
                .getUninterruptibly();
    }

    private List<String> loadDisallowResList(String appKey) {
        List<VendorResg> resgList = vendorLoaderClient.loadVendorResgsIncludeDisabled().values()
                .stream()
                .filter(t -> !t.isDisabledTrue())
                .collect(Collectors.toList());
        if (resgList.size() == 0) {
            return Collections.emptyList();
        }

        List<String> retResList = new ArrayList<>();
        List<Long> allowResgIdList = new LinkedList<>();
        for (VendorAppsResgRef ref : loadVendorAppResgRefsByAppKey(appKey)) {
            allowResgIdList.add(ref.getResgId());
        }

        for (VendorResg resgItem : resgList) {
            if (!allowResgIdList.contains(resgItem.getId())) {
                List<VendorResgContent> resgContentList = vendorResgContentServiceClient.getVendorResgContentBuffer()
                        .loadAll()
                        .values()
                        .stream()
                        .filter(e -> Objects.equals(resgItem.getId(), e.getResgId()))
                        .collect(Collectors.toList());
                for (VendorResgContent resgContentItem : resgContentList) {
                    retResList.add(resgContentItem.getResName());
                }
            }
        }
        return retResList;
    }

    public boolean loginVersionCheck(String baseVersion, Ktwelve ktwelve, String userToken, UserType userType) {
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        if (VersionUtil.compareVersion(version, baseVersion) < 0) {
            if (!StringUtils.isBlank(userToken)) {
                List<User> userList = userLoaderClient.loadUsers(userToken, userType);
                List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
                if (UserType.TEACHER == userType) {
                    return teacherLoaderClient.loadTeachers(userIdList).values().stream().anyMatch(p -> ktwelve.equals(p.getKtwelve()));
                } else if (UserType.STUDENT == userType) {
                    return studentLoaderClient.loadStudentDetails(userIdList).values().stream().anyMatch(p -> p.matchKtwelve(ktwelve));
                }
            }
        }

        return false;
    }

    // fixme 上线时间暂定 2018-08-15
    protected boolean juniorTeacherAppOnlineTime() {
        if (RuntimeMode.isProduction()) {
            return DateUtils.stringToDate("2018-08-06 00:00:00").before(new Date());
        } else {
            return DateUtils.stringToDate("2018-07-08 00:00:00").before(new Date());
        }
    }

    protected boolean juniorStudentAppOnlineTime() {
        if (RuntimeMode.isProduction()) {
            return DateUtils.stringToDate("2018-08-20 00:00:00").before(new Date());
        } else {
            return DateUtils.stringToDate("2018-08-20 00:00:00").before(new Date());
        }
    }

    // 10月1号之前
    protected boolean juniorAppLimitTime() {
        if (RuntimeMode.isProduction()) {
            return DateUtils.stringToDate("2018-10-01 00:00:00").after(new Date());
        } else {
            return DateUtils.stringToDate("2018-10-01 00:00:00").after(new Date());
        }
    }

    public String getAppType() {
        String ua = getRequest().getHeader("User-Agent");
        if (StringUtils.isNoneBlank(ua) && ua.toLowerCase().contains("ios")) {
            return "iOS";
        }
        return "Android";
    }

}
