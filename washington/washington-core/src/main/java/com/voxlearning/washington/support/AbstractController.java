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

package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.cdn.CdnResourceVersionCollector;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.business.consumer.BusinessVendorServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.AccessDeniedRecord;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.reward.entity.RewardWishOrder;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.*;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import com.voxlearning.washington.service.*;
import com.voxlearning.washington.service.support.GameFlashLoaderConfigManager;
import com.voxlearning.washington.service.support.GameFlashLoaderTemplate;
import lombok.Getter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATETIME;
import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isNotBlank;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

abstract public class AbstractController extends AbstractUploadController {

    private static final String VERSION = "version";
    protected final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    protected CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

    @Getter
    @Inject private RaikouSystem raikouSystem;

    // Inject In Alphabetical order
    @Inject
    protected BadWordCheckerClient badWordCheckerClient;
    @Inject
    protected BusinessUserOrderServiceClient businessUserOrderServiceClient;
    @Inject
    protected BusinessVendorServiceClient businessVendorServiceClient;

    @Inject
    protected CdnResourceUrlGenerator cdnResourceUrlGenerator;
    @Inject
    protected CdnResourceVersionCollector cdnResourceVersionCollector;
    @Inject
    protected ChineseFlashGameLoader chineseFlashGameLoader;
    @Inject
    protected CrmSummaryLoaderClient crmSummaryLoaderClient;

    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    protected FairylandLoaderClient fairylandLoaderClient;
    @Inject
    protected GameFlashLoaderConfigManager gameFlashLoaderConfigManager;
    @Inject
    protected MathFlashGameLoader mathFlashGameLoader;

    @Inject
    protected OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject
    protected OpenApiAuth openApiAuth;
    @Inject
    protected PaymentGatewayManager paymentGatewayManager;

    @Inject
    protected UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject
    protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    protected UserOrderServiceClient userOrderServiceClient;
    @Inject
    protected WashingtonAuthenticationHandler washingtonAuthenticationHandler;
    @Inject
    protected AsyncFootprintServiceClient asyncFootprintServiceClient;

    private PageBlockContentGenerator pageBlockContentGenerator;
    @Inject
    private GlobalTagServiceClient globalTagServiceClient;
    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;

    @Inject
    private AppMessageServiceClient appMessageClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;


    public boolean onBeforeControllerMethod() {
        // 验证用户是否已经更改过密码，如已更改过需要重新登录
        // 如果是移动端过来的请求，不做这个验证
        // 针对21楼LiveCast的特殊处理，虽然不太好，暂时先这样 by xuesong.zhang 2017-01-05
        if (isMobileRequest(getRequest()) || getWebRequestContext().getRequest().getRequestURI().startsWith("/livecast")) {
            return true;
        }
        User user = currentUser();
        if (null != user) {
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            if (!ua.getPassword().equals(getWebRequestContext().getSaltedPassword())) {
                getWebRequestContext().cleanupAuthenticationStates();
                try {
                    getResponse().sendRedirect(ProductConfig.getMainSiteBaseUrl());
                    return false;
                } catch (IOException ignored) {
                }
            }
        }

        return true;
    }

    public WashingtonRequestContext getWebRequestContext() {
        return (WashingtonRequestContext) DefaultContext.get();
    }

    /* ======================================================================================
       以下代码负责 Request & Response
       ====================================================================================== */
    protected HttpServletRequest getRequest() {
        return getWebRequestContext().getRequest();
    }

    protected String getRequestParameter(String key, String def) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? def : v;
    }

    protected String getRequestString(String key) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? "" : v;
    }

    protected String getRequestStringCleanXss(String key) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? "" : StringUtils.cleanXSS(v);
    }

    protected boolean getRequestBool(String name) {
        return ConversionUtils.toBool(getRequest().getParameter(name));
    }

    protected boolean getRequestBool(String name, boolean def) {
        return SafeConverter.toBoolean(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name, long def) {
        return ConversionUtils.toLong(getRequest().getParameter(name), def);
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

    protected double getRequestDouble(String name) {
        return ConversionUtils.toDouble(getRequest().getParameter(name), 0);
    }

    protected HttpServletResponse getResponse() {
        return getWebRequestContext().getResponse();
    }

    protected boolean isRequestPost() {
        return getRequest().getMethod().equals("POST");
    }

    protected boolean isRequestGet() {
        //then URLEncodedUtils can be used to parse query-string ?
        return getRequest().getMethod().equals("GET");
    }

    protected boolean isRequestAjax() {
        return HttpRequestContextUtils.isRequestAjax(getRequest());
    }

    protected CookieManager getCookieManager() {
        return getWebRequestContext().getCookieManager();
    }

    /* ======================================================================================
       以下代码负责 captcha
       ====================================================================================== */
    protected void saveCaptchaCode(String token, String code) {
        washingtonCacheSystem.CBS.unflushable.set("Captcha:" + token, 600, code);
    }

    protected boolean consumeCaptchaCode(String token, String code) {
        if (StringUtils.isEmpty(code))
            return false;

        String cacheKey = "Captcha:" + token;
        CacheObject<String> cacheObject = washingtonCacheSystem.CBS.unflushable.get(cacheKey);
        if (cacheObject == null) {
            return false;
        }
        String except = cacheObject.getValue();
        boolean r = StringUtils.equals(StringUtils.trim(code), except);
        if (r) {
            washingtonCacheSystem.CBS.unflushable.delete(cacheKey);
        }
        return r;
    }

    /* ======================================================================================
       以下代码负责常用的数据，比如当前用户、地理信息等
       ====================================================================================== */

    protected Long currentUserId() {
        return getWebRequestContext().getUserId();
    }


    protected User currentUser() {
        return getWebRequestContext().getCurrentUser();
    }

    protected List<RoleType> currentUserRoleTypes() {
        return getWebRequestContext().getRoleTypes();
    }

    protected Teacher currentTeacher() {
        return getWebRequestContext().getCurrentTeacher();
    }

    protected TeacherDetail currentTeacherDetail() {
        return getWebRequestContext().getCurrentTeacherDetail();
    }

    protected User currentParent() {
        return getWebRequestContext().getCurrentUser();
    }

    protected User currentStudent() {
        return getWebRequestContext().getCurrentStudent();
    }

    protected StudentDetail currentStudentDetail() {
        return getWebRequestContext().getCurrentStudentDetail();
    }

    protected ResearchStaff currentResearchStaff() {
        return getWebRequestContext().getCurrentResearchStaff();
    }

    protected ResearchStaffDetail currentResearchStaffDetail() {
        return getWebRequestContext().getCurrentResearchStaffDetail();
    }

    /**
     * 触发一个HTTP错误码，返回给用户
     * 不要用这个函数！！！
     */
    @Deprecated
    protected String raiseErrorPage(HttpServletResponse response, int code, String message) {
        logger.error("raiseErrorPage " + code + " " + message);


        try {
            response.setStatus(code);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write(code + ":" + message);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    protected String getCdnBaseUrlWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlWithSep(getWebRequestContext().getRequest());
    }

    protected String getCdnBaseUrlStaticSharedWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlStaticSharedWithSep(getWebRequestContext().getRequest());
    }

    protected String getCdnBaseUrlAvatarWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlAvatarWithSep(getWebRequestContext().getRequest());
    }

    protected String getUserAvatarImgUrl(User user) {
        return getUserAvatarImgUrl(user.fetchImageUrl());
    }

    protected String getUserAvatarImgUrl(String imgFile) {
        String imgUrl;
        if (!StringUtils.isEmpty(imgFile)) {
            imgUrl = "gridfs/" + imgFile;
        } else {
            imgUrl = "upload/images/avatar/avatar_default.png";
        }

        return getCdnBaseUrlAvatarWithSep() + imgUrl;
    }

    protected List<Map<String, Object>> newBookPaintedSkin(List<NewBookProfile> books) {
        Set<String> bookIds = books.stream().map(NewBookProfile::getId).collect(Collectors.toSet());
        Map<String, NewBookCatalog> catalogs = newContentLoaderClient.loadBookCatalogByCatalogIds(bookIds);

        List<Map<String, Object>> bookMaps = new ArrayList<>();
        for (NewBookProfile book : books) {
            NewBookCatalog catalog = catalogs.get(book.getId());
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(book.getSubjectId()),
                    catalog == null ? "" : catalog.getName());
            Map<String, Object> bookMap = new HashMap<>();
            bookMap.put("id", book.getId());
            bookMap.put("cname", book.getName());
            bookMap.put("ename", book.getName());
            bookMap.put("bookType", book.getBookType());
            bookMap.put("status", book.getStatus());
            bookMap.put("latestVersion", book.getLatestVersion());
            if (bookPress != null) {
                bookMap.put("viewContent", bookPress.getViewContent());
                bookMap.put("color", bookPress.getColor());
            }
            bookMaps.add(bookMap);
            // book.setImgUrl(StringUtils.replace(book.getImgUrl(), "catalog", "catalog_new"));
        }
        return bookMaps;
    }

    protected void engPaintedSkin(List<Book> books) {
        for (Book book : books) {
            if (!StringUtils.contains(book.getImgUrl(), "catalog_new")) {
                BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, book.getPress());
                if (bookPress != null) {
                    book.setViewContent(bookPress.getViewContent());
                    book.setColor(bookPress.getColor());
                }
                // book.setImgUrl(StringUtils.replace(book.getImgUrl(), "catalog", "catalog_new"));
            }
        }
    }

    protected void mathPaintedSkin(List<MathBook> mathBooks) {
        for (MathBook book : mathBooks) {
            if (!StringUtils.contains(book.getImgUrl(), "math_new")) {
                BookPress bookPress = BookPress.getBySubjectAndPress(Subject.MATH, book.getPress());
                if (bookPress != null) {
                    book.setViewContent(bookPress.getViewContent());
                    book.setColor(bookPress.getColor());
                }
                book.setImgUrl(StringUtils.replace(book.getImgUrl(), "math", "math_new"));
            }
        }
    }

    protected void chinesePaintedSkin(List<BookDat> chineseBooks) {
        for (BookDat book : chineseBooks) {
            if (!StringUtils.contains(book.getImgUrl(), "chinese_new")) {
                BookPress bookPress = BookPress.getBySubjectAndPress(Subject.CHINESE, book.getPress());
                if (bookPress != null) {
                    book.setViewContent(bookPress.getViewContent());
                    book.setColor(bookPress.getColor());
                }
                book.setImgUrl(StringUtils.replace(book.getImgUrl(), "chinese", "chinese_new"));
            }
        }
    }

    protected Map<String, Object> loadEnglishGameFlash(LoadFlashGameContext context) {
        GameFlashLoaderTemplate template = GameFlashLoaderTemplate.getGameFlashLoaderTemplate(context.getStudyType());
        return template.load(getWebRequestContext(), context);
    }

    protected Map<String, Object> loadNewSelfstudyGameFlash(LoadFlashGameContext context) {
        GameFlashLoaderTemplate template = GameFlashLoaderTemplate.getGameFlashLoaderTemplate(context.getStudyType());
        return template.loadNewSelfStudy(getWebRequestContext(), context);
    }

    protected Map<String, Object> loadNewHomeworkGameFlash(LoadFlashGameContext context) {
        GameFlashLoaderTemplate template = GameFlashLoaderTemplate.getGameFlashLoaderTemplate(context.getStudyType());
        return template.loadNewHomework(getWebRequestContext(), context);
    }

    //============================错题订正需要的
    protected Map<String, Object> loadCorrectEnglishGameFlash(LoadFlashGameContext context, String homeworkCorrectId, Long unitId, Long lessonId, Long pointId, Long practiceId) {
        GameFlashLoaderTemplate template = GameFlashLoaderTemplate.getGameFlashLoaderTemplate(context.getStudyType());
        return template.loadEnglishHomeworkCorrect(getWebRequestContext(), context, homeworkCorrectId, unitId, lessonId, pointId, practiceId);
    }

    protected Map<String, Object> loadCorrectMathGameFlash(LoadMathFlashGameContext context, String homeworkCorrectId, Long unitId, Long lessonId, Long practiceId) {
        return mathFlashGameLoader.loadMathHomeworkCorrect(getWebRequestContext(), context, homeworkCorrectId, unitId, lessonId, practiceId);
    }


    protected void updateLupld() {
        // 重新设置7天倒计时
        int exp = 86400 * 7;
        getWebRequestContext().getCookieManager().setCookie("lupld", "1", exp);
    }

    // TODO: move method into utopia-reward-api module
    //奖品中心  获取用户愿望盒中奖品详细信息
    protected List<Map<String, Object>> getRewardUserWishProductDetail() {
        String key = CacheKeyGenerator.generateCacheKey("REWARD_USER_WISH_ORDER_LIST", null, new Object[]{currentUserId()});
        CacheObject<List<Map<String, Object>>> cacheObject = washingtonCacheSystem.CBS.flushable.get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            return cacheObject.getValue();
        }

        User user = currentUser();
        if (user.fetchUserType() == UserType.TEACHER) {
            user = currentTeacherDetail();
        } else if (user.fetchUserType() == UserType.STUDENT) {
            user = currentStudentDetail();
        }

        List<RewardWishOrder> wishOrders = rewardLoaderClient.getRewardWishOrderLoader()
                .loadUserRewardWishOrders(user.getId());
        List<Map<String, Object>> data = new ArrayList<>();

        for (RewardWishOrder wishOrder : wishOrders) {
            RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(user, wishOrder.getProductId());
            Map<String, Object> detailMap = new HashMap<>();
            if (detail != null) {
                detailMap.put("wishOrderId", wishOrder.getId());
                detailMap.put("image", detail.getImage());
                detailMap.put("productId", detail.getId());
                detailMap.put("skus", rewardLoaderClient.loadProductSku(detail.getId()));
                detailMap.put("price", detail.getDiscountPrice());
                detailMap.put("productName", detail.getProductName());
                detailMap.put("unit", detail.getUnit());
                detailMap.put("addTime", wishOrder.getCreateDatetime());
                detailMap.put("online", detail.getOnline());
                data.add(detailMap);
            }
        }

        // 按添加时间倒序排列
        data.sort((r1, r2) -> {
            Date addTime1 = (Date) r1.get("addTime");
            Date addTime2 = (Date) r2.get("addTime");

            return addTime2.compareTo(addTime1);
        });

        washingtonCacheSystem.CBS.flushable.add(key, DateUtils.getCurrentToDayEndSecond(), data);
        return data;
    }

    protected synchronized PageBlockContentGenerator getPageBlockContentGenerator() {
        if (pageBlockContentGenerator == null) {
            pageBlockContentGenerator = new PageBlockContentGenerator(pageBlockContentServiceClient);
        }
        return pageBlockContentGenerator;
    }

    protected boolean isMobileRequest(HttpServletRequest request) {
        return isIOSRequest(request) || isAndroidRequest(request);
    }

    protected boolean isIOSRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.containsIgnoreCase(userAgent, "iOS") ||
                StringUtils.containsIgnoreCase(userAgent, "iPhone") ||
                StringUtils.containsIgnoreCase(userAgent, "iPad") ||
                StringUtils.containsIgnoreCase(userAgent, "iPod");
    }

    protected boolean isAndroidRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.containsIgnoreCase(userAgent, "Android");
    }

    /**
     * 所长别开枪，是我！
     * 273997 : 银座九号小学(测试), 353246 : 银座九号小学(线上)
     *
     * @param schoolId
     * @return
     */
    protected boolean isYz(long schoolId) {
        return ((RuntimeMode.lt(Mode.STAGING) && schoolId == 273997L) ||
                (RuntimeMode.ge(Mode.STAGING) && schoolId == 353246L));
    }

    protected Map<String, String> passwordChangeTrackMap(Long userId, Long operatorId, String pos) {
        Map<String, String> map = new HashMap<>();
        map.put("user", SafeConverter.toString(userId));
        map.put("operator", SafeConverter.toString(operatorId));
        map.put("date", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
        map.put("pos", pos);
        map.put("env", RuntimeMode.current().name());
        return map;
    }


    /**
     * 判断是否是黑名单学校或者黑名单用户的学生
     * 因为API和H5都要调用这个。只能放在这着最上层的父类了。
     * 不然以后实在没法维护啊。
     *
     * @param users
     * @return
     */
    protected Map<Long, Boolean> isBlackSchoolOrBlackUser(Collection<User> users) {
        //查询黑名单相关信息
        Map<GlobalTagName, List<GlobalTag>> blacks = new LinkedHashMap<>();
        blacks.put(GlobalTagName.AfentiBlackListSchools, globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.AfentiBlackListSchools.name()));
        blacks.put(GlobalTagName.AfentiBlackListUsers, globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.AfentiBlackListUsers.name()));
        //黑名单学校
        Set<String> blackSchools = CollectionUtils.toLinkedList(blacks.get(GlobalTagName.AfentiBlackListSchools)).stream()
                .filter(t -> t != null)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        //黑名单用户
        Set<String> blackUsers = CollectionUtils.toLinkedList(blacks.get(GlobalTagName.AfentiBlackListUsers)).stream()
                .filter(t -> t != null)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        Set<Long> userIdSet = users.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazzs(userIdSet);
        Map<Long, Long> userSchoolIds = new HashMap<>();
        //学生的学校map
        if (MapUtils.isNotEmpty(clazzMap)) {
            clazzMap.keySet().stream().forEach(key -> {
                if (clazzMap.get(key) != null) {
                    userSchoolIds.put(key, clazzMap.get(key).getSchoolId());
                }
            });
        }
        Map<Long, Boolean> blackMap = new HashMap<>();
        for (User user : users) {
            boolean blackSchool = (!CollectionUtils.isEmpty(blackSchools)) && userSchoolIds.containsKey(user.getId()) && blackSchools.contains(userSchoolIds.get(user.getId()).toString());
            boolean blackUser = (!CollectionUtils.isEmpty(blackUsers)) && blackUsers.contains(user.getId().toString());
            if (blackSchool || blackUser) {
                blackMap.put(user.getId(), Boolean.TRUE);
            } else {
                blackMap.put(user.getId(), Boolean.FALSE);
            }
        }
        return blackMap;
    }

    protected String combineCdbUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }

        if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
            return url;
        } else {
            return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
        }
    }

    protected String generateAliYunImgUrl(String url) {
        return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_homework_image_host")) + url;
    }

    protected String generatePmcAliYunImgUrl(String url) {
        return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + url + "?x-oss-process=image/resize,m_lfit,h_210,w_315/format,png";
    }

    protected String fetchMainsiteUrlByCurrentSchema() {
        if (getWebRequestContext().isHttpsRequest()) {
            return "https://www." + TopLevelDomain.getTopLevelDomain();
        }
        return "http://www." + TopLevelDomain.getTopLevelDomain();
    }

    // 根据客户端的版本号获取h5版本号
    protected String generateBigVersion(String ver) {
        String val;
        try {
            String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "native_h5_version_mapping");
            regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> regMap = JsonUtils.fromJson(regStr);
            if (regMap != null && !regMap.isEmpty()) {
                Set<String> keys = regMap.keySet();
                for (String key : keys) {
                    if (ver.matches(key)) {
                        val = (String) regMap.get(key);
                        if (val != null) {
                            return val;
                        }
                    }
                }
            }
            // 怀疑线上某些壳的版本没有被配置到合适的h5 version里，需要找到这些nativeVersion
//            logger.warn("native version {} dose not match any h5 version", ver);
            return (String) regMap.get("default");
        } catch (Exception e) {
            logger.error("apiLoadVer - Excp : {}", e);
            return "V1_9_5";
        }
    }

    protected Boolean teacherHasSlave(Long teacherId) {
        Set<Long> allTeacherIdSet = teacherLoaderClient.loadRelTeacherIds(teacherId);
        return CollectionUtils.isNotEmpty(allTeacherIdSet) && allTeacherIdSet.size() > 1;
    }

    //如果学生姓名为空，返回学号
    protected String getStudentName(User user) {
        return isNotBlank(user.fetchRealname()) ? user.fetchRealname() : SafeConverter.toString(user.getId(), "");
    }


    /* ======================================================================================
       包班制支持
       一个老师多学科情况,拿到指定学科的老师
       ====================================================================================== */

    protected Long getSubjectSpecifiedTeacherId() {
        return getSubjectSpecifiedTeacherId(currentSubject());
    }

    protected Long getSubjectSpecifiedTeacherId(Subject subject) {
        Teacher teacher = currentTeacher();
        if (subject == null) {
            return teacher != null ? teacher.getId() : null;
        }

        // 多学科支持
        if (teacher != null) {
            // 此时需要根据学科切换当前老师的班级信息
            if (subject != teacher.getSubject()) {
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                if (id != null) {
                    return id;
                }
            }
        }
        return teacher != null ? teacher.getId() : null;
    }

    protected Teacher getSubjectSpecifiedTeacher() {
        return getSubjectSpecifiedTeacher(currentSubject());
    }

    protected Teacher getSubjectSpecifiedTeacher(Subject subject) {
        Teacher teacher = currentTeacher();
        if (subject == null) {
            return teacher;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                Teacher t = teacherLoaderClient.loadTeacher(id);
                if (t != null) {
                    teacher = t;
                }
            }
        }
        return teacher;
    }

    protected TeacherDetail getSubjectSpecifiedTeacherDetail() {
        return getSubjectSpecifiedTeacherDetail(currentSubject());
    }

    protected TeacherDetail getSubjectSpecifiedTeacherDetail(Subject subject) {
        TeacherDetail teacher = currentTeacherDetail();
        if (subject == null) {
            return teacher;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                TeacherDetail t = teacherLoaderClient.loadTeacherDetail(id);
                if (t != null) {
                    teacher = t;
                }
            }
        }
        return teacher;
    }

    protected Subject currentSubject() {
        String subjectStr = getRequestString("subject");
        return StringUtils.isNotEmpty(subjectStr) ? Subject.of(subjectStr) : null;
    }

    protected List<Subject> getSpecifiedSubjectsByTeacherIdAndClazzId(long teacherId, long clazzId) {
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> specifiedSubjects = new ArrayList<>();
        for (Long relTeacherId : relTeacherIds) {
            GroupMapper g = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(relTeacherId, clazzId, false);
            if (g != null) {
                specifiedSubjects.add(g.getSubject());
            }
        }
        return specifiedSubjects;
    }

    protected List<Map<String, Object>> toSubjectList(List<Subject> subjects, Boolean sort) {
        if (CollectionUtils.isEmpty(subjects))
            return Collections.emptyList();
        if (sort)
            subjects = subjects.stream().sorted((o1, o2) -> Integer.compare(o1.getKey(), o2.getKey())).collect(Collectors.toList());
        List<Map<String, Object>> list = new ArrayList<>();
        subjects.forEach(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_SUBJECT, s.name());
            map.put(RES_SUBJECT_NAME, s.getValue());
            list.add(map);
        });
        return list;
    }

    /**
     * 获取系统请求
     */
    public AppSystemType getAppSystemType() {
        if (isAndroidRequest(getRequest())) {
            return AppSystemType.ANDROID;
        } else if (isIOSRequest(getRequest())) {
            return AppSystemType.IOS;
        }
        return null;
    }

    // 检查用户对某个请求的访问权限，未登录用户不予许
    // 登陆用户当天内如果有超过maxNumber次/分钟的直接当天内禁止访问
    protected boolean validateAccessFreq(Long userId, String type, String req, int maxNumber) {
        // 未登录用户直接不予许访问
        if (userId == null) {
            return true;
        }

        Date curTime = new Date();

        // 当天内是否已经被禁止访问
        String todayLimitationKey = StringUtils.join(Arrays.asList(type + "_API_DAY_LIMIT_ACCESS_KEY", DateUtils.dateToString(curTime, "yyyyMMdd"), userId), ":");
        CacheObject<String> todayLimitationObject = washingtonCacheSystem.CBS.unflushable.get(todayLimitationKey);
        if (todayLimitationObject != null && todayLimitationObject.getValue() != null) {
            return true;
        }

        // 检查是否超过了maxNumber次/分钟
        String minuteLimitationKey = StringUtils.join(Arrays.asList(type + "_API_MINUTE_LIMIT_ACCESS_KEY", DateUtils.dateToString(curTime, "yyyyMMddHHmm"), userId), ":");
        CacheObject<Integer> minuteLimitationObject = washingtonCacheSystem.CBS.unflushable.get(minuteLimitationKey);
        if (minuteLimitationObject != null && minuteLimitationObject.getValue() != null) {
            if (SafeConverter.toInt(minuteLimitationObject.getValue()) > maxNumber) {
                // 超过maxNumber次/分钟，标记当日不可访问
                washingtonCacheSystem.CBS.unflushable.set(todayLimitationKey, DateUtils.getCurrentToDayEndSecond(), String.valueOf(userId));
                //如果超限发送邮件提醒
                Map<String, Object> content = new HashMap<>();
                content.put("userId", userId);
                content.put("info", req);
                emailServiceClient.createTemplateEmail(EmailTemplate.requestoverrunwarning)
                        .to("guohong.tan@17zuoye.com;xuesong.zhang@17zuoye.com;zhilong.hu@17zuoye.com;caijuan.gao@17zuoye.com;weiyi.shen@17zuoye.com")
                        .subject("用户请求超限邮件提醒")
                        .content(content)
                        .send();
                return true;
            }
        }

        // 访问次数++
        washingtonCacheSystem.CBS.unflushable.incr(minuteLimitationKey, 1, 1, 60);
        return false;
    }

    // 检查用户对某个请求的访问权限，未登录用户不予许
    // 登陆用户当天内如果有超过maxNumber次/天的直接当天内禁止访问
    protected boolean validateAccessFreqDays(Long userId, String type, int maxNumber) {
        // 未登录用户直接不予许访问
        if (userId == null) {
            return true;
        }

        // 检查是否超过了maxNumber次/天
        String dayLimitationKey = StringUtils.join(Arrays.asList(type + "_API_DAY_LIMIT_ACCESS_KEY", DateUtils.dateToString(new Date(), "yyyyMMdd"), userId), ":");
        CacheObject<Integer> dayLimitationObject = washingtonCacheSystem.CBS.unflushable.get(dayLimitationKey);
        if (dayLimitationObject != null && dayLimitationObject.getValue() != null) {
            if (SafeConverter.toInt(dayLimitationObject.getValue()) >= maxNumber) {
                return true;
            }
        }

        // 访问次数++
        washingtonCacheSystem.CBS.unflushable.incr(dayLimitationKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
        return false;
    }

    //预习者联盟活动用--2017.3.9
    public Map<Long, GroupMapper> loadActivitySameGroup(Collection<Long> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return null;
        }
        Map<Long, GroupMapper> groupMap = new HashMap<>();
        Map<Long, List<GroupMapper>> groupMapperList = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, true);
        if (MapUtils.isNotEmpty(groupMapperList)) {
            for (Map.Entry<Long, List<GroupMapper>> studentGroup : groupMapperList.entrySet()) {
                Set<Subject> subjects = studentGroup.getValue().stream().map(GroupMapper::getSubject).collect(Collectors.toSet());
                GroupMapper groupMapper = null;
                if (CollectionUtils.isNotEmpty(subjects)) {
                    if (subjects.contains(Subject.ENGLISH)) {
                        groupMapper = studentGroup.getValue().stream().filter(e -> e.getSubject() == Subject.ENGLISH).findFirst().orElse(null);
                    } else if (subjects.contains(Subject.MATH)) {
                        groupMapper = studentGroup.getValue().stream().filter(e -> e.getSubject() == Subject.MATH).findFirst().orElse(null);
                    } else if (subjects.contains(Subject.CHINESE)) {
                        groupMapper = studentGroup.getValue().stream().filter(e -> e.getSubject() == Subject.CHINESE).findFirst().orElse(null);
                    }
                }
                groupMap.put(studentGroup.getKey(), groupMapper);
            }

        }
        return groupMap;
    }

    // 获取产品tips
    protected Map<String, List<String>> fetchProductTips(OrderProductServiceType type) {
        if (type == null || type == OrderProductServiceType.Unknown) return Collections.emptyMap();

        try {
            String value = getPageBlockContentGenerator().getPageBlockContentHtml(type.name() + "_Products", type.name() + "_Tips")
                    .replace("\r", "").replace("\n", "").replace("\t", "");
            if (StringUtils.isEmpty(value)) return Collections.emptyMap();

            Map<String, List<String>> results = new HashMap<>();
            Map<String, Object> products = JsonUtils.fromJson(value);
            for (Map.Entry<String, Object> entry : products.entrySet()) {
                // noinspection unchecked
                List<Map<String, Object>> attributes = (List<Map<String, Object>>) entry.getValue();
                List<String> tips = new ArrayList<>();
                for (Map attribute : attributes) {
                    if (!attribute.containsKey("start") || !attribute.containsKey("end") ||
                            !attribute.containsKey("style") || !attribute.containsKey("content")) continue;

                    Date start = DateUtils.stringToDate(SafeConverter.toString(attribute.get("start")), FORMAT_SQL_DATETIME);
                    Date end = DateUtils.stringToDate(SafeConverter.toString(attribute.get("end")), FORMAT_SQL_DATETIME);
                    String style = SafeConverter.toString(attribute.get("style"));
                    String content = SafeConverter.toString(attribute.get("content"));

                    if (new DateRange(start, end).contains(new Date())) {
                        tips.add(MessageFormat.format(style, content));
                    }
                }
                results.put(entry.getKey(), tips);
            }
            return results;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return Collections.emptyMap();
        }
    }

    protected MapMessage validateStudentPay(Long userId, OrderProduct product) {
//        User user = userLoaderClient.loadUser(userId);
//        if (user == null) {
//            return MapMessage.errorMessage("请登录后重试");
//        }
//        // 如果用户是支付限额白名单， 直接过
//        if (userBlacklistServiceClient.isInPaymentLimitWhiteUserList(user)) {
//            return MapMessage.successMessage();
//        }
//        // 校验用户是否属于灰名单地区
//        if (userBlacklistServiceClient.isInGrayRegionList(user)) {
//            return MapMessage.errorMessage("您所在的区域暂时还未开放购买，如有疑问请联系客服：4001601717");
//        }
//        // 如果用户已经解除今日限制 直接过
//        String free = asyncOrderCacheServiceClient.getAsyncOrderCacheService().StudentPayFreeCacheManager_fetch(userId).take();
//        if (StringUtils.isNotBlank(free)) {
//            return MapMessage.successMessage();
//        }
//        OrderProductServiceType serviceType = OrderProductServiceType.safeParse(product.getProductType());
//        // 黄白金任务不限制
//        if (serviceType == MicroPaymentTask || serviceType == MicroPaymentPlatinumTask || serviceType == EagletSinologyClassRoom) {
//            return MapMessage.successMessage();
//        }
//        Date day7 = DateUtils.nextDay(new Date(), -7);
//        // 校验支付次数限制
//        List<UserOrder> paidOrders = userOrderLoaderClient.loadUserOrderList(userId);
//        if (CollectionUtils.isNotEmpty(paidOrders)) {
//            paidOrders = paidOrders.stream()
//                    .filter(p -> PaymentStatus.Paid == p.getPaymentStatus())
//                    .filter(p -> p.getOrderProductServiceType() != null
//                            && OrderProductServiceType.safeParse(p.getOrderProductServiceType()) != OrderProductServiceType.MicroPaymentPlatinumTask
//                            && OrderProductServiceType.safeParse(p.getOrderProductServiceType()) != OrderProductServiceType.MicroPaymentTask
//                            && OrderProductServiceType.safeParse(p.getOrderProductServiceType()) != EagletSinologyClassRoom)
//                    .collect(Collectors.toList());
//
//            List<UserOrder> dayPaidOrders = paidOrders.stream()
//                    .filter(p -> p.getUpdateDatetime() != null && DayRange.current().contains(p.getUpdateDatetime()))
//                    .collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(dayPaidOrders) && dayPaidOrders.size() >= 5) {
//                LogCollector.info("backend-general", MiscUtils.map(
//                        "env", RuntimeMode.getCurrentStage(),
//                        "usertoken", userId,
//                        "mod1", "The number of payments exceeded the limit 5",
//                        "op", "exceedOrderLimit"
//                ));
//                return MapMessage.errorMessage("你花钱有点多哦，暂时不能支付啦，请跟爸妈商量一下，或联系客服400-160-1717");
//            }
//            List<UserOrder> day7PaidOrders = paidOrders.stream()
//                    .filter(p -> p.getUpdateDatetime() != null && p.getUpdateDatetime().after(DayRange.newInstance(day7.getTime()).getStartDate()))
//                    .collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(day7PaidOrders) && day7PaidOrders.size() >= 10) {
//                LogCollector.info("backend-general", MiscUtils.map(
//                        "env", RuntimeMode.getCurrentStage(),
//                        "usertoken", userId,
//                        "mod1", "The number of payments exceeded the limit 10",
//                        "op", "exceedOrderLimit"
//                ));
//                return MapMessage.errorMessage("你花钱有点多哦，暂时不能支付啦，请跟爸妈商量一下，或联系客服400-160-1717");
//            }
//        }
//        // 校验支付限额
//        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId);
//        paymentHistories = paymentHistories.stream()
//                .filter(p -> PaymentStatus.Paid == p.getPaymentStatus())
//                .filter(p -> Double.compare(p.getPayAmount().doubleValue(), 1000.0) < 0) // 单笔金额超过1k的不记录限额
//                .collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(paymentHistories)) {
//            Double payPriceSum;
//            // 总订单每天不能超过600元
//            payPriceSum = paymentHistories
//                    .stream()
//                    .filter(p -> p.getPayDatetime() != null && DayRange.current().contains(p.getPayDatetime()))
//                    .mapToDouble(value -> value.getPayAmount().doubleValue())
//                    .sum();
//            if (Double.compare(payPriceSum + product.getPrice().doubleValue(), 600.0) >= 0) {
//                LogCollector.info("backend-general", MiscUtils.map(
//                        "env", RuntimeMode.getCurrentStage(),
//                        "usertoken", userId,
//                        "mod1", "The sum price of payments exceeded the limit 600",
//                        "op", "exceedOrderLimit"
//                ));
//                return MapMessage.errorMessage("你花钱有点多哦，暂时不能支付啦，请跟爸妈商量一下，或联系客服400-160-1717");
//            }
//
//            // 总订单 最近7天支付不能超过1000.0
//            payPriceSum = paymentHistories
//                    .stream()
//                    .filter(p -> p.getPayDatetime() != null && p.getPayDatetime().after(DayRange.newInstance(day7.getTime()).getStartDate()))
//                    .mapToDouble(value -> value.getPayAmount().doubleValue())
//                    .sum();
//            if (Double.compare(payPriceSum + product.getPrice().doubleValue(), 1000.0) >= 0) {
//                LogCollector.info("backend-general", MiscUtils.map(
//                        "env", RuntimeMode.getCurrentStage(),
//                        "usertoken", userId,
//                        "mod1", "The sum price of payments exceeded the limit 1000",
//                        "op", "exceedOrderLimit"
//                ));
//                return MapMessage.errorMessage("你花钱有点多哦，暂时不能支付啦，请跟爸妈商量一下，或联系客服400-160-1717");
//            }
//        }
        return MapMessage.successMessage();
    }

    protected boolean noAccessPermission(User user) {
        if (user == null || !user.isTeacher()) {
            return false;
        }
        Map<String, Object> limitConfigMap = loadTeacherAccessLimitConfig();
        if (MapUtils.isEmpty(limitConfigMap)) {
            return false;
        }
        String requestPath = getRequest().getRequestURI();
        if (requestPath.startsWith("//")) {
            requestPath = requestPath.substring(1, requestPath.length());
        }
        int index = requestPath.lastIndexOf(".");
        if (index != -1) {
            requestPath = requestPath.substring(0, index);
        }
        if (!limitConfigMap.containsKey(requestPath)) {
            return false;
        }
        Object config = limitConfigMap.get(requestPath);
        Map<String, Object> configMap = JsonUtils.fromJson(JsonUtils.toJson(config));
        if (MapUtils.isEmpty(configMap)) {
            return false;
        }

        Date curTime = new Date();
        Long userId = user.getId();
        // 当天内是否已经被禁止访问
        String todayPermissionKey = StringUtils.join(Arrays.asList("API_DAY_ACCESS_PERMISSION_" + requestPath, DateUtils.dateToString(curTime, "yyyyMMdd"), userId), ":");
        CacheObject<String> todayPermissionObject = washingtonCacheSystem.CBS.unflushable.get(todayPermissionKey);
        if (todayPermissionObject != null && todayPermissionObject.getValue() != null) {
            return true;
        }

        // 每分钟限制次数
        int minuteLimitCount = SafeConverter.toInt(configMap.get("minuteLimit"), -1);
        if (minuteLimitCount > 0) {
            String minuteLimitationKey = StringUtils.join(Arrays.asList("API_MINUTE_LIMIT_ACCESS_KEY_" + requestPath, DateUtils.dateToString(curTime, "yyyyMMddHHmm"), userId), ":");
            CacheObject<Integer> minuteLimitationObject = washingtonCacheSystem.CBS.unflushable.get(minuteLimitationKey);
            if (minuteLimitationObject != null && minuteLimitationObject.getValue() != null && SafeConverter.toInt(minuteLimitationObject.getValue()) >= minuteLimitCount) {
                // 超过minuteLimitCount次/分钟，标记当日不可访问
                washingtonCacheSystem.CBS.unflushable.set(todayPermissionKey, DateUtils.getCurrentToDayEndSecond(), String.valueOf(userId));
                saveAccessDeniedRecord(userId, requestPath, minuteLimitationKey, SafeConverter.toInt(minuteLimitationObject.getValue()));
                return true;
            } else {
                // 访问次数++
                washingtonCacheSystem.CBS.unflushable.incr(minuteLimitationKey, 1, 1, 60);
            }
        }

        // 每小时限制次数
        int hourLimitCount = SafeConverter.toInt(configMap.get("hourLimit"), -1);
        if (hourLimitCount > 0) {
            String hourLimitationKey = StringUtils.join(Arrays.asList("API_HOUR_LIMIT_ACCESS_KEY_" + requestPath, DateUtils.dateToString(curTime, "yyyyMMddHH"), userId), ":");
            CacheObject<Integer> hourLimitationObject = washingtonCacheSystem.CBS.unflushable.get(hourLimitationKey);
            if (hourLimitationObject != null && hourLimitationObject.getValue() != null && SafeConverter.toInt(hourLimitationObject.getValue()) >= hourLimitCount) {
                // 超过hourLimitCount次/小时，标记当日不可访问
                washingtonCacheSystem.CBS.unflushable.set(todayPermissionKey, DateUtils.getCurrentToDayEndSecond(), String.valueOf(userId));
                saveAccessDeniedRecord(userId, requestPath, hourLimitationKey, SafeConverter.toInt(hourLimitationObject.getValue()));
                return true;
            } else {
                // 访问次数++
                washingtonCacheSystem.CBS.unflushable.incr(hourLimitationKey, 1, 1, 3600);
            }
        }

        // 每天限制次数
        int dayLimitCount = SafeConverter.toInt(configMap.get("dayLimit"), -1);
        if (dayLimitCount > 0) {
            String dayLimitationKey = StringUtils.join(Arrays.asList("API_DAY_LIMIT_ACCESS_KEY_" + requestPath, DateUtils.dateToString(curTime, "yyyyMMdd"), userId), ":");
            CacheObject<Integer> dayLimitationObject = washingtonCacheSystem.CBS.unflushable.get(dayLimitationKey);
            if (dayLimitationObject != null && dayLimitationObject.getValue() != null && SafeConverter.toInt(dayLimitationObject.getValue()) >= dayLimitCount) {
                // 超过dayLimitCount次/天，标记当日不可访问
                washingtonCacheSystem.CBS.unflushable.set(todayPermissionKey, DateUtils.getCurrentToDayEndSecond(), String.valueOf(userId));
                saveAccessDeniedRecord(userId, requestPath, dayLimitationKey, SafeConverter.toInt(dayLimitationObject.getValue()));
                return true;
            } else {
                // 访问次数++
                washingtonCacheSystem.CBS.unflushable.incr(dayLimitationKey, 1, 1, 86400);
            }
        }
        return false;
    }

    protected void saveHomePageUrl(Long userId, String value) {
        String key = "HOME_PAGE_URL:" + userId;
        washingtonCacheSystem.CBS.persistence.set(key, 0, value);
    }

    protected String getHomePageUrl(Long userId) {
        String key = "HOME_PAGE_URL:" + userId;
        CacheObject<String> cacheHomepageUrl = washingtonCacheSystem.CBS.persistence.get(key);
        return cacheHomepageUrl.extractValue() != null ? String.valueOf(cacheHomepageUrl.extractValue()) : null;
    }

    private Map<String, Object> loadTeacherAccessLimitConfig() {
        List<PageBlockContent> accessLimitConfig = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("AccessLimit");
        if (CollectionUtils.isNotEmpty(accessLimitConfig)) {
            PageBlockContent configPageBlockContent = accessLimitConfig.stream()
                    .filter(p -> "TeacherConfig".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                return JsonUtils.convertJsonObjectToMap(configContent);
            }
        }
        return null;
    }

    private void saveAccessDeniedRecord(Long userId, String requestPath, String limitationKey, int limitationValue) {
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", userId,
                "mod1", requestPath,
                "mod2", limitationKey,
                "mod3", limitationValue,
                "ip", getWebRequestContext().getRealRemoteAddress(),
                "userAgent", getRequest().getHeader("user-agent"),
                "imei", getRequestString(REQ_IMEI),
                "uuid", getRequestString(REQ_UUID),
                "sys", getRequestString(REQ_SYS),
                "appVersion", getRequestString(REQ_APP_NATIVE_VERSION),
                "op", "access limitation"
        ));

        AccessDeniedRecord accessDeniedRecord = new AccessDeniedRecord();
        accessDeniedRecord.setUserId(userId);
        accessDeniedRecord.setRequestPath(requestPath);
        accessDeniedRecord.setLimitationKey(limitationKey);
        accessDeniedRecord.setLimitationValue(limitationValue);
        accessDeniedRecord.setIp(getWebRequestContext().getRealRemoteAddress());
        accessDeniedRecord.setUserAgent(getRequest().getHeader("user-agent"));
        accessDeniedRecord.setImei(getRequestString(REQ_IMEI));
        accessDeniedRecord.setUuid(getRequestString(REQ_UUID));
        accessDeniedRecord.setSys(getRequestString(REQ_SYS));
        accessDeniedRecord.setAppVersion(getRequestString(REQ_APP_NATIVE_VERSION));
        accessDeniedRecord.setMobileRequest(isMobileRequest(getRequest()));
        newHomeworkServiceClient.saveAccessDeniedRecord(accessDeniedRecord);
    }

    protected boolean isPreTerminalPeriod() {
        Date curTime = new Date();
        String year = DateUtils.dateToString(curTime, "yyyy");
        Date start = DateUtils.stringToDate(year + "-07-01 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        Date end = DateUtils.stringToDate(year + "-08-10 00:00:00", DateUtils.FORMAT_SQL_DATETIME);

        return curTime.after(start) && curTime.before(end);
    }

    /**
     * 是否是新模考试
     *
     * @param newExam
     * @return
     */
    protected boolean isNewModelExam(NewExam newExam) {
        /*Date newExamBeginData = DateUtils.stringToDate("2018-09-26 00:00:00"); // 一起测新模式考试开始时间
        Mode currentMode = RuntimeMode.current();
        if (currentMode.equals(Mode.TEST) || currentMode.equals(Mode.DEVELOPMENT)) {
            newExamBeginData = DateUtils.stringToDate("2018-09-01 00:00:00");
        } else if (currentMode.equals(Mode.STAGING)) {
            newExamBeginData = DateUtils.stringToDate("2018-09-25 00:00:00");
        }

        if (newExam.getSubject().equals(Subject.MATH) && newExam.getExamStartAt().after(newExamBeginData)) {
            return true;
        }
        return false;*/
        //为了方便测试，留下旧模考的入口
        return !"examv2".equals(getRequestString("examv2"));
    }

    protected void setCorsHeadersForParent() {
        WashingtonRequestContext context = getWebRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://*.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://*.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://*.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET,POST");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
    }

    /**
     * 只给 parent.17zuoye.com 前端支持跨域，别他么瞎用
     */
    protected void setCorsHeadersForParentOnlyForParent() {
        WashingtonRequestContext context = getWebRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://parent.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://parent.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://parent.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET,POST");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
    }

    protected void sendPushMsgAndSms(StudentDetail studentDetail, List<StudentParent> studentParentList, String productName) {
        //push消息,站内信发送给家长端
        List<Long> parentUserIds = new ArrayList<>();
        List<String> mobiles = new ArrayList<>();
        for (StudentParent parent : studentParentList) {
            if (parent.isKeyParent()) {
                parentUserIds.add(parent.getParentUser().getId());
                UserAuthentication authentication = userLoaderClient.loadUserAuthentication(parent.getParentUser().getId());
                if (isNotBlank(authentication.getSensitiveMobile())) {
                    mobiles.add(SensitiveLib.decodeMobile(authentication.getSensitiveMobile()));
                }
            }
        }
        String messageContent = "您的孩子" + studentDetail.fetchRealname() + "想使用［" + productName + "］，针对重难点进行精准提升，希望您确认并帮助开通。";

        String pushUrl = "/view/mobile/parent/17my_shell/order.vpage?useNewCore=wk&rel=3";
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("url", pushUrl);
        extInfo.put("t", "h5");
        extInfo.put("key", "j");
        appMessageClient.sendAppJpushMessageByIds(messageContent, AppMessageSource.PARENT, parentUserIds, extInfo, 0L);
        Map<String, Object> extInfo1 = new HashMap<>();
        extInfo1.put("tag", ParentMessageTag.订单.name());
        String messageUrl = "/view/mobile/parent/17my_shell/order.vpage?useNewCore=wk&rel=4";
        List<AppMessage> userMessageList = AppMessageUtils.generateAppUserMessage(parentUserIds, ParentMessageType.REMINDER.getType(), "提醒", messageContent, "", messageUrl, 1, JsonUtils.toJson(extInfo1));
        userMessageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        sendMessage(studentDetail.fetchRealname(), productName, mobiles);
    }


    public void sendMessage(String studentName, String productName, List<String> mobiles) {
        String smsContent = "您的孩子" + studentName + "想使用［" + productName + "］，针对重难点进行精准提升，希望您确认并帮助开通。请登录“家长通App”-在“个人中心”-选择“我的订单“-查看“待支付的订单”。获取家长通App：https://dwz.cn/66uFypxU";
        if (CollectionUtils.isNotEmpty(mobiles)) {
            for (String mobile : mobiles) {
                SmsMessage sms = new SmsMessage();
                sms.setMobile(mobile);
                sms.setSmsContent(smsContent);
                sms.setType(SmsType.AGENT_PAY.name());
                smsServiceClient.getSmsService().sendSms(sms);
            }
        }
    }

    public PaymentCallbackContext buildPaymentCallbackContext(PaymentRequest paymentRequest) {
        PaymentCallbackContext context = new PaymentCallbackContext("freepay", "notify");
        context.setVerifiedPaymentData(new PaymentVerifiedData());
        context.getVerifiedPaymentData().setExternalTradeNumber("");
        context.getVerifiedPaymentData().setExternalUserId(SafeConverter.toString(paymentRequest.getPayUser()));
        context.getVerifiedPaymentData().setPayAmount(paymentRequest.getPayAmount());
        context.getVerifiedPaymentData().setTradeNumber(paymentRequest.getTradeNumber());
        return context;
    }

    public WashingtonRequestContext getRequestContext() {
        return (WashingtonRequestContext) DefaultContext.get();
    }

    protected void setCorsHeaders() {
        WashingtonRequestContext context = getRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://www.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://www.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://www.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
    }

    /**
     * 从ExamFlashController拿过来的
     */
    protected <T> T getRequestObject(Class<T> cls) {
        String json = getRequestParameter("data", "");
        String param = getRequest().getQueryString();
        /**
         * 过滤XSS、CRLF攻击
         */
        // FIXME 现在采取措施：把所有 getRequestObject 里面的 cleanXSS 删了，其他的不要动，再跟踪一段时间。
        T t = JsonUtils.fromJson(json, cls);
        if (t == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", currentUser() != null ? currentUser().getId() : 0,
                    "mod1", param,
                    "mod2", json,
                    "op", "AbstractController getRequestObject"
            ));
        }
        return t;
    }

    /**
     * 获取作业用户（现在作业支持家长端，转换为根据sid拿到的user）
     */
    protected User getHomeworkUser() {
        User user = currentUser();
        if (user == null) {
            return null;
        }
        if (user.fetchUserType() == UserType.PARENT) {
            Long sid = getRequestLong("sid");
            user = raikouSystem.loadUser(sid);
        }
        return user;
    }

    /**
     * 判断是否是家长奖励开户人脸识别的新版本
     */
    protected boolean isParentRewardNewVersionForFaceDetect(String version) {
        //默认大于等于2.8.5才开启
        String ver = "2.8.5";

        try {
            String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_REWARD_FACE_DETECT_DOOR");
            if (isNotBlank(config)) {
                Map<String, Object> info = JsonUtils.fromJson(config);
                if (info.containsKey(VERSION)) {
                    ver = info.get(VERSION).toString();
                }
            }
        } catch (IllegalArgumentException ignore) {
        }

        return VersionUtil.compareVersion(version, ver) >= 0;
    }
}
