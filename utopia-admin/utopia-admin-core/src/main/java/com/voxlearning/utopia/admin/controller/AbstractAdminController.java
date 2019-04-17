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

package com.voxlearning.utopia.admin.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.support.AlertMessage;
import com.voxlearning.alps.webmvc.support.AlertMessageManager;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext;
import com.voxlearning.utopia.admin.persist.AdminLogPersistence;
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.admin.util.EntityUtils;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.crm.client.AdminLogServiceClient;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;


abstract public class AbstractAdminController extends SpringContainerSupport {

    private static final String SessionKey_CurrentAuthLogin = "AdminGlobal_CurrentAuthLogin";
    private static final String SessionKey_AlertMessages = "AdminGlobal_AlertMessages";
    public static final String DEFAULT_REDMINE_APIKEY = "bf287580a6e677b1b6d0d671a1518ca16f6abd62";
    private static final String ADMIN_LOGIN_CACHE_KEY_PREFIX = "ADMIN_LOGIN_SESSION_";
//    private static final String ADMIN_SESSION_KEY_PREFIX = "ADMIN_SESSION_";

    protected static final String DATE_FORMAT = "yyyy-MM-dd";

    protected final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    @Inject private AdminCacheSystem adminCacheSystem;
    @Inject protected AdminLogPersistence adminLogPersistence;
    @Inject protected BadWordCheckerClient badWordCheckerClient;

    @Inject private AdminLogServiceClient adminLogServiceClient;

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

    protected double getRequestDouble(String name, double def) {
        return ConversionUtils.toDouble(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name) {
        return ConversionUtils.toLong(getRequest().getParameter(name));
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
        return StringUtils.isEmpty(value) ? null : Boolean.valueOf(value);
    }

    protected String requestString(String name) {
        String value = getRequest().getParameter(name);
        return StringUtils.isEmpty(value) ? null : value;
    }

    protected String requestString(String name, String iValue) {
        String value = getRequest().getParameter(name);
        return StringUtils.isEmpty(value) ? iValue : value;
    }

    protected String[] requestArray(String name) {
        return getRequest().getParameterValues(name);
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

    protected Date requestDate(String name) {
        return requestDate(name, DATE_FORMAT, null);
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

    protected <T> void requestFillEntity(T entity) {
        EntityUtils.getInstance().parse(getRequest().getParameterMap(), entity);
    }

    protected PageRequest buildPageRequest(int iSize) {
        return buildPageRequest("PAGE", "SIZE", iSize);
    }

    protected PageRequest buildPageRequest(String pageParam, String sizeParam, int iSize) {
        int page = getRequestInt(pageParam, 0);
        int size = getRequestInt(sizeParam, iSize);
        return new PageRequest(page, size);
    }

    protected PageRequest buildSortPageRequest(int iSize, String iSort) {
        return buildSortPageRequest("PAGE", "SIZE", iSize, "ORDER", Sort.Direction.DESC.name(), "SORT", iSort);
    }

    protected PageRequest buildSortPageRequest(String pageParam, String sizeParam, int iSize, String orderParam, String iOrder, String sortParam, String iSort) {
        int page = getRequestInt(pageParam, 0);
        int size = getRequestInt(sizeParam, iSize);
        Sort.Direction direction = Sort.Direction.fromStringOrNull(requestString(orderParam, iOrder));
        direction = direction != null ? direction : Sort.Direction.DESC;
        String property = requestString(sortParam, iSort);
        return new PageRequest(page, size, direction, property);
    }

    protected HttpServletResponse getResponse() {
        return HttpRequestContextUtils.currentRequestContext().getResponse();
    }

    protected boolean isRequestPost() {
        return getRequest().getMethod().equals("POST");
    }

    protected boolean isRequestGet() {
        return getRequest().getMethod().equals("GET");
    }

    protected void saveAuthToSession(AuthCurrentAdminUser adminUser) {
        adminCacheSystem.saveAuthUser(adminUser);

        // 记录LOGIN CACHE KEY，进行单点登录控制
        String cacheKey = generateAdminLoginKey(adminUser);
        Long cacheValue = System.currentTimeMillis();
        adminCacheSystem.CBS.storage.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), cacheValue);
    }

    public static String generateAdminLoginKey(AuthCurrentAdminUser adminUser) {
        return adminUser == null ? ADMIN_LOGIN_CACHE_KEY_PREFIX : ADMIN_LOGIN_CACHE_KEY_PREFIX + adminUser.getFakeUserId();
    }

    protected AuthCurrentAdminUser getCurrentAdminUser() {
        AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getCurrentAdminUser();
    }

    protected void addAdminLog(String operation) {
        addAdminLog(operation, null, null, null, null);
    }

    protected void addAdminLog(String operation, Long targetId) {
        addAdminLog(operation, targetId, null, null, null);
    }

    protected void addAdminLog(String operation, String targetStr) {
        addAdminLog(operation, null, targetStr, null, null);
    }

    protected void addAdminLog(String operation, Long targetId, String comment) {
        addAdminLog(operation, targetId, null, comment, null);
    }

    public void addAdminLog(String operation, String targetStr, String comment) {
        addAdminLog(operation, null, targetStr, comment, null);
    }

    protected void addAdminLog(String operation, Long targetId, String comment, Object targetData) {
        addAdminLog(operation, targetId, null, comment, targetData);
    }

    protected void addAdminLog(String operation, String targetStr, String comment, Object targetData) {
        addAdminLog(operation, null, targetStr, comment, targetData);
    }

    protected void addAdminLog(String operation, Long targetId, String targetStr, String comment, Object targetData) {

        AdminLog adminLog = new AdminLog();

        AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        String adminUserName = context.getCurrentAdminUser() == null ? "" : context.getCurrentAdminUser().getAdminUserName();
        adminLog.setAdminUserName(adminUserName);
        adminLog.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        adminLog.setOperation(operation);
        adminLog.setWebActionUrl(context.getRelativeUriPath());
        adminLog.setTargetId(targetId);
        adminLog.setTargetStr(targetStr);
        if (targetData != null)
            adminLog.setTargetData(JsonUtils.toJson(targetData));
        adminLog.setComment(StringUtils.defaultIfEmpty(comment, ""));

        adminLogServiceClient.getAdminLogService().persistAdminLog(adminLog).awaitUninterruptibly();
    }

    protected String redirect(String s) {
        if (s.charAt(0) == '/') {
            AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
            return "redirect:" + context.getWebAppContextPath() + s;
        }
        return "redirect:" + s;
    }

    public void initAlertMessageManager() {
        AlertMessageManager alertMessageManager = null;

        AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        if (context.getCurrentAdminUser() != null) {
            String cacheKey = StringUtils.join("admin_", context.getCurrentAdminUser().getFakeUserId(), "_AMM");
            List<AlertMessage> messages = adminCacheSystem.CBS.storage.load(cacheKey);
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
//
//    public void saveAlertMessagesToSession() {
//        AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
//        context.saveAlertMessagesToSession(SessionKey_AlertMessages);
//    }


    protected AlertMessageManager getAlertMessageManager() {
        AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getAlertMessageManager();
    }

    public ZipArchiveOutputStream getZipOutputStreamForDownloading(String filename) throws IOException {
        getResponse().reset();
        filename = attachmentFilenameEncoding(filename, getRequest());
        getResponse().addHeader("Content-Disposition", "attachment;filename=" + filename);
        getResponse().setContentType("application/x-zip-compressed");

        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(getResponse().getOutputStream());
        zos.setEncoding("GBK");
        return zos;
    }

    public static String attachmentFilenameEncoding(String filename, HttpServletRequest request) {
        try {
            if (StringUtils.contains(request.getHeader("User-Agent"), "MSIE")
                    || StringUtils.contains(request.getHeader("User-Agent"), "Trident")) {
                // IE browser
                return new String(filename.getBytes("gbk"), "iso8859-1");
            } else {
                // non-IE browser
                return new String(filename.getBytes("utf-8"), "iso8859-1");
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException();
        }
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

    public String getMarketingUrl() {
        switch (RuntimeMode.current()) {
            case PRODUCTION:
                return "http://marketing.oaloft.com";
            case STAGING:
                return "http://marketing.staging.17zuoye.net";
            case TEST:
                return "http://marketing.test.17zuoye.net";
//            case DEVELOPMENT:
//                return "http://localhost:8083";
            default:
                return "http://marketing.test.17zuoye.net";
        }
    }

    public void setUserAndSignToCookie(String userId, String sign) {
        Cookie cookieUser = new Cookie("userId", userId);
        Cookie cookieSign = new Cookie("sign", sign);
        if (RuntimeMode.ge(Mode.TEST)) {
            cookieUser.setDomain(getRequest().getServerName());
            cookieSign.setDomain(getRequest().getServerName());
        }
        cookieUser.setPath("/");
        cookieUser.setMaxAge(DateUtils.getCurrentToDayEndSecond());
        cookieSign.setPath("/");
        cookieSign.setMaxAge(DateUtils.getCurrentToDayEndSecond());
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

    protected String i7TinyUrl(String longUrl) {
        String responseStr = HttpRequestExecutor.defaultInstance()
                .post("http://www.17zyw.cn/crt")
                .addParameter("url", longUrl)
                .execute()
                .getResponseString();
        if (StringUtils.isNotBlank(responseStr)) {
            return "http://www.17zyw.cn/" + responseStr;
        } else {
            return longUrl;
        }
    }

}
