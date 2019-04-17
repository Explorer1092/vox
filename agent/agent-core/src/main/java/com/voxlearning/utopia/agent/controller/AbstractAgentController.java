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

package com.voxlearning.utopia.agent.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.alps.webmvc.support.AlertMessage;
import com.voxlearning.alps.webmvc.support.AlertMessageManager;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.persist.internal.InternalAuthDataLoader;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.utils.AgentOssManageUtils;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Slf4j
public class AbstractAgentController extends SpringContainerSupport {

    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    protected static final String MONTH_FORMAT = "yyyy-MM";
    protected static final String SCHOOL_LEVEL_COOKIE_NAME = "SCHOOL_LEVEL";
    protected static final String SCHOOL_LEVEL_TYPE_COOKIE_NAME = "SCHOOL_LEVEL_TYPE";

    @Inject private RaikouSystem raikouSystem;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected InternalAuthDataLoader internalAuthDataLoader;
    @Inject protected BaseUserService baseUserService;
    @Inject protected AsyncLogService asyncLogService;
    @Inject protected AgentRegionService agentRegionService;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected UserServiceClient userServiceClient;
    @Inject protected NewExamLoaderClient newExamLoaderClient;
    @Inject protected AgentCacheSystem agentCacheSystem;
    @Inject protected AgentApiAuth agentApiAuth;
    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject protected BaseOrgService baseOrgService;

    @Inject protected SchoolLoaderClient schoolLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;

    protected HttpServletRequest getRequest() {
        return HttpRequestContextUtils.currentRequestContext().getRequest();
    }

    protected String getRequestParameter(String key, String def) {
        String v = getRequest().getParameter(key);
        return v == null ? def : v;
    }

    protected String getRequestString(String key) {
        String v = getRequest().getParameter(key);
        return v == null ? "" : v;
    }

    protected long getRequestLong(String name, long def) {
        return ConversionUtils.toLong(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name) {
        return ConversionUtils.toLong(getRequest().getParameter(name));
    }

    protected Double getRequestDouble(String name) {
        return ConversionUtils.toDouble(getRequest().getParameter(name), 0D);
    }

    protected int getRequestInt(String name, int def) {
        return ConversionUtils.toInt(getRequest().getParameter(name), def);
    }

    protected int getRequestInt(String name) {
        return ConversionUtils.toInt(getRequest().getParameter(name));
    }

    protected boolean getRequestBool(String name) {
        return ConversionUtils.toBool(getRequest().getParameter(name));
    }

    protected Date getRequestDate(String name) {
        return getRequestDate(name, DATE_FORMAT, null);
    }

    protected Date getRequestMonth(String name) {
        return getRequestDate(name, MONTH_FORMAT, null);
    }

    protected Date getRequestDate(String name, String pattern) {
        return getRequestDate(name, pattern, null);
    }

    protected Date getRequestDate(String name, Date def) {
        return getRequestDate(name, DATE_FORMAT, def);
    }

    protected Date getRequestDate(String name, String pattern, Date def) {
        Date date = null;
        String value = getRequestString(name);
        if (StringUtils.isNotBlank(value)) {
            date = DateUtils.stringToDate(value, pattern);
        }
        return date != null ? date : def;
    }

    protected HttpServletResponse getResponse() {
        return HttpRequestContextUtils.currentRequestContext().getResponse();
    }

    protected boolean isRequestPost() {
        return getRequest().getMethod().equals("POST");
    }

    protected boolean isRequestGet() {
        //then URLEncodedUtils can be used to parse query-string ?
        return getRequest().getMethod().equals("GET");
    }

    protected Integer requestInteger(String name) {
        String value = getRequest().getParameter(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    protected Long requestLong(String name) {
        String value = getRequest().getParameter(name);
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return null;
        }
    }

    protected Boolean requestBoolean(String name) {
        String value = getRequest().getParameter(name);
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception ex) {
            return null;
        }
    }

    protected String requestString(String name) {
        String value = getRequest().getParameter(name);
        return StringUtils.isEmpty(value) ? null : value;
    }

    protected String requestString(String name, String iValue) {
        String value = getRequest().getParameter(name);
        return StringUtils.isEmpty(value) ? iValue : value;
    }

    protected Set<Integer> requestIntegerSet(String name) {
        return requestIntegerSet(name, ",");
    }

    protected Set<Integer> requestIntegerSet(String name, String sep) {
        Set<Integer> values = new HashSet<>();
        String[] array = getRequestString(name).split(sep);
        for (String e : array) {
            Integer value = SafeConverter.toInt(e);
            if (value > 0) {
                values.add(value);
            }
        }
        return values;
    }

    protected Set<Long> requestLongSet(String name) {
        return requestLongSet(name, ",");
    }

    protected Set<Long> requestLongSet(String name, String sep) {
        Set<Long> values = new HashSet<>();
        String[] array = getRequestString(name).split(sep);
        for (String e : array) {
            long value = SafeConverter.toLong(e);
            if (value > 0) {
                values.add(value);
            }
        }
        return values;
    }

    protected List<String> requestStringList(String name) {
        return requestStringList(name, ",");
    }

    protected List<String> requestStringList(String name, String sep) {
        Set<String> values = new HashSet<>();
        String[] array = getRequestString(name).split(sep);
        for (String e : array) {
            String value = StringUtils.trim(e);
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
        return new ArrayList<>(values);
    }

    // FIXME 这几个DATE方法似乎跟上面的 getRequestDate 系列重了
    protected Date requestDate(String name) {
        return requestDate(name, DATE_FORMAT, null);
    }

    protected Date requestDate(String name, String pattern) {
        return requestDate(name, pattern, null);
    }

    protected Date requestDate(String name, Date iDate) {
        return requestDate(name, DATE_FORMAT, iDate);
    }

    protected Date requestDate(String name, String pattern, Date iDate) {
        Date date = null;
        String value = getRequestString(name);
        if (StringUtils.isNotBlank(value)) {
            date = DateUtils.stringToDate(value, pattern);
        }
        return date != null ? date : iDate;
    }

    protected PageRequest buildPageRequest() {
        return buildPageRequest("page", "size");
    }

    protected PageRequest buildPageRequest(String pageParam, String sizeParam) {
        int page = getRequestInt(pageParam, 0);
        int size = getRequestInt(sizeParam, 6);
        return new PageRequest(page, size);
    }

//    public static void saveAuthToSession(AuthCurrentUser currentUser, HttpSession session) {
//        if (currentUser == null)
//            session.removeAttribute(SessionKey_CurrentAuthLogin);
//        else
//            session.setAttribute(SessionKey_CurrentAuthLogin, currentUser);
//
//    }

//    protected void saveAuthToSession(AuthCurrentUser currentUser) {
//        saveAuthToSession(currentUser, getSession());
//    }
//
//    public AuthCurrentUser loadAuthUSerFromCache(String userId) {
//        String cacheKey = agentApiAuth.getPreCacheKey_AuthCurrentUser() + userId;
//        return (AuthCurrentUser) agentCacheSystem.CBS.unflushable.load(cacheKey);
//    }

    public void setUserAndSignToCookie(String userId, String sign) {
        Cookie cookieUser = new Cookie("userId", userId);
        Cookie cookieSign = new Cookie("sign", sign);
        if (RuntimeMode.ge(Mode.TEST)) {
            cookieUser.setDomain(getRequest().getServerName());
            cookieSign.setDomain(getRequest().getServerName());
        }
        cookieUser.setPath("/");
        cookieUser.setMaxAge(30 * 24 * 60 * 60);
        cookieSign.setPath("/");
        cookieSign.setMaxAge(30 * 24 * 60 * 60);
        getResponse().addCookie(cookieUser);
        getResponse().addCookie(cookieSign);
    }

    public void removeUserAndSignFromCookie() {
        Cookie cookieUser = new Cookie("userId", "");
        Cookie cookieSign = new Cookie("sign", "");
        if (RuntimeMode.ge(Mode.TEST)) {
            cookieUser.setDomain(getRequest().getServerName());
            cookieSign.setDomain(getRequest().getServerName());
        }
        cookieUser.setPath("/");
        cookieUser.setMaxAge(0);
        cookieSign.setPath("/");
        cookieSign.setMaxAge(0);
        getResponse().addCookie(cookieUser);
        getResponse().addCookie(cookieSign);
    }

    public void removeSchoolLevelCookie() {
        Cookie cookieLevel = new Cookie(SCHOOL_LEVEL_COOKIE_NAME, "");
        cookieLevel.setPath("/");
        cookieLevel.setMaxAge(0);
        getResponse().addCookie(cookieLevel);
        Cookie cookieType = new Cookie(SCHOOL_LEVEL_TYPE_COOKIE_NAME, "");
        cookieType.setPath("/");
        cookieType.setMaxAge(0);
        getResponse().addCookie(cookieType);
    }

    protected AuthCurrentUser getCurrentUser() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getCurrentUser();
    }

    protected Long getCurrentUserId() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getCurrentUser().getUserId();
    }

    protected Set<Integer> getCurrentUserCities() {
        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(getCurrentUserId());
        Set<Integer> regionSet = baseOrgService.getGroupRegionsByGroupSet(groupIdList).stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionSet);
        return exRegionMap.values().stream().map(ExRegion::getCityCode).collect(Collectors.toSet());
    }

    protected String redirect(String s) {
        if (s.charAt(0) == '/') {
            AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
            return "redirect:" + context.getWebAppContextPath() + s;
        }
        return "redirect:" + s;
    }

    public void initAlertMessageManager() {
        AlertMessageManager alertMessageManager = null;

        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        if (context.getCurrentUser() != null) {
            List<AlertMessage> messages = agentCacheSystem.getAlertMessageCache().load(context.getCurrentUser().getUserId());
            if (CollectionUtils.isNotEmpty(messages)) {
                alertMessageManager = new AlertMessageManager();
                final AlertMessageManager AMM = alertMessageManager;
                messages.forEach(t -> AMM.addMessage(t.getCategory(), t.getContent(), t.getReference(), t.getData()));
            }
        }
        if (alertMessageManager == null) {
            alertMessageManager = new AlertMessageManager();
        }
        context.initAlertMessageManager(alertMessageManager);
    }

    public void saveAlertMessageToCache() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        if (context.getCurrentUser() != null && context.getAlertMessageManager() != null) {
            agentCacheSystem.getAlertMessageCache().set(
                    context.getCurrentUser().getUserId(),
                    context.getAlertMessageManager().getMessages());
        }
    }

    public String getWebRootPath() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getWebAppContextPath();
    }

    protected AlertMessageManager getAlertMessageManager() {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getAlertMessageManager();
    }

    protected Page paging(List source, Pageable pageable) {
        if (source == null) {
            return new PageImpl(new ArrayList<>(), pageable, 0);
        }
        int total = source.size();
        int size = pageable.getPageSize();
        int begin = size * pageable.getPageNumber();
        int end = begin + size < total ? begin + size : total;
        List content = source.subList(begin, end);
        return new PageImpl(content, pageable, total);
    }

    public void setCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(DateUtils.getCurrentToMonthEndSecond());
        getResponse().addCookie(cookie);
    }

    protected String upload(String file, String pathPrefix) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            return "";
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile(file);
            if (!inputFile.isEmpty()) {
                // 获取文件类型
                String originalFileName = inputFile.getOriginalFilename();
                String ext = StringUtils.substringAfterLast(originalFileName, ".");
                ext = StringUtils.defaultString(ext).trim().toLowerCase();

                SupportedFileType fileType;
                try {
                    fileType = SupportedFileType.valueOf(ext);
                } catch (Exception ex) {
                    throw new RuntimeException("不支持此格式文件");
                }

                String fileId = RandomUtils.nextObjectId();
                String fileName = pathPrefix + "-" + fileId + "." + ext;
                String contentType = fileType.getContentType();

                // FIXME: =====================================================
                // FIXME: Use StorageClient instead
                // FIXME: =====================================================
                GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
                GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

                @Cleanup InputStream inStream = inputFile.getInputStream();
                bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, inStream);

                String prePath = RuntimeMode.isUsingProductionData() ? "http://cdn-portrait.17zuoye.cn" : "http://cdn-portrait.test.17zuoye.net";
                return prePath + "/gridfs/" + fileName;
            }
        } catch (Exception ex) {
            log.error("上传失败,msg:{}", ex.getMessage(), ex);
        }
        return "";
    }

    private String upload(String content, String pathPrefix, String userId) {
        try {
            if (StringUtils.isNotBlank(content)) {
                String fileId = RandomUtils.nextObjectId();
                String fileName = pathPrefix + "-" + userId + "_" + fileId + ".txt";
                String contentType = SupportedFileType.xls.getContentType();

                // FIXME: =====================================================
                // FIXME: Use StorageClient instead
                // FIXME: =====================================================
                GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
                GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

                @Cleanup InputStream inStream = new ByteArrayInputStream(content.getBytes());
                bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, inStream);

                String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
                return prePath + "/gridfs/" + fileName;
            }
        } catch (Exception ex) {
            log.error("上传失败,msg:{}", ex.getMessage(), ex);
        }
        return "";
    }

    // Use OSS
    protected String uploadSchoolPhoto(String file) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            return "";
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile(file);
            if (inputFile != null && !inputFile.isEmpty()) {
                return AgentOssManageUtils.upload(inputFile);
            }
        } catch (Exception ex) {
            log.error("上传失败,msg:{}", ex.getMessage(), ex);
        }
        return "";
    }

    protected String buildUserRegionJsonTree() {
        AuthCurrentUser user = getCurrentUser();
        Map<Object, Object> userRegion = agentRegionService.buildUserRegionMapTree(user);
        return JsonUtils.toJson(userRegion);
    }

    protected String buildAllRegionJsonTree() {
        Map<Object, Object> userRegion = agentRegionService.buildAllTopRegionTree();
        return JsonUtils.toJson(userRegion);
    }

    protected String formatDate(Date date) {
        return formatDate(date, DATE_FORMAT);
    }

    protected String formatDate(Date date, String pattern) {
        try {
            FastDateFormat format = FastDateFormat.getInstance(pattern, Locale.CHINA);
            return format.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    protected void saveCaptchaCode(String token, String code) {
        agentCacheSystem.CBS.unflushable.set("Captcha:" + token, 60, code);
    }

    protected boolean consumeCaptchaCode(String token, String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        String cacheKey = "Captcha:" + token;
        CacheObject<String> cacheObject = agentCacheSystem.CBS.unflushable.get(cacheKey);
        if (cacheObject == null) {
            return false;
        }
        String except = cacheObject.getValue();
        boolean r = StringUtils.equals(StringUtils.trim(code), except);
        if (r) {
            agentCacheSystem.CBS.unflushable.delete(cacheKey);
        }
        return r;
    }

    // 统一的错误提示页面
    protected String errorInfoPage(AgentErrorCode code, String errorMessage, Model model) {
        model.addAttribute("info", code.getDesc());
        model.addAttribute("code", code.getCode());
        model.addAttribute("errorMessage", errorMessage);
        if (!model.containsAttribute("url") && StringUtils.isNotBlank(code.getReturnUrl())) {
            model.addAttribute("url", code.getReturnUrl());
        }
        return "/mobile/error/app_error";
    }

    // 统一的错误提示页面
    protected String errorInfoPage(AgentErrorCode code, Model model) {
        model.addAttribute("info", code.getDesc());
        model.addAttribute("code", code.getCode());
        if (!model.containsAttribute("url") && StringUtils.isNotBlank(code.getReturnUrl())) {
            model.addAttribute("url", code.getReturnUrl());
        }
        return "/mobile/error/app_error";
    }


    public MapMessage verifySmsCode(String mobile, String code, SmsType smsType) {

        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效手机号");
        }

        return smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
    }

}
