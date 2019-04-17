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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.PasswordRule;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.consumer.StudentAdvertisementInfoLoaderClient;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserEmailServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.SessionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * User related API controller class.
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
@Controller
@RequestMapping(value = "/v1/user")
public class UserApiController extends AbstractApiController {

    private static final String ADMIN_USER_PRE = "admin.";
    private static final String RECORD_TITLE_PATTERN = "【{0}】用户{1}";
    private static final String REQ_USER_AUTH_INFOS = "user_auth_infos";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private UserEmailServiceClient userEmailServiceClient;

    @Inject private AdminUserServiceClient adminUserServiceClient;

    @Inject StudentAdvertisementInfoLoaderClient studentAdvertisementInfoLoaderClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;

    @Inject
    private RewardCenterClient rewardCenterClient;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    @RequestMapping(value = "/aktosk.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserSessionKeyByAppKey() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_APP_KEY_TARGET, "应用appKey");
            validateRequest(REQ_APP_KEY_TARGET);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        if (curUser == null || !curUser.isStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        }

        String ak = getRequestString(REQ_APP_KEY_TARGET);
        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(ak, curUser.getId())
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        }
        VendorAppsUserRef ref = (VendorAppsUserRef) message.get("ref");
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SESSION_KEY_TARGET, ref.getSessionKey());
        return resultMap;
    }

    // 云课堂专用方法
    @RequestMapping(value = "/yunktsk.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserSessionKey4Yunketang() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_APP_KEY, "应用appKey");
            validateRequired(REQ_USER_ID, "用户ID");
            validateDigitNumber(REQ_USER_ID, "用户ID");
            validateRequestNoSessionKey(REQ_APP_KEY, REQ_USER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String appKey = getRequestString(REQ_APP_KEY);
        if (!StringUtils.equals(appKey, "17Yunketang")) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APP_ERROR_MSG);
            return resultMap;
        }

        User curUser = raikouSystem.loadUser(getRequestLong(REQ_USER_ID));
        if (curUser == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        }

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, curUser.getId())
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        }
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(curUser.getId());
        getWebRequestContext().saveAuthenticationStates(-1, curUser.getId(), ua.getPassword(), RoleType.of(curUser.getUserType()));

        VendorAppsUserRef ref = (VendorAppsUserRef) message.get("ref");
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SESSION_KEY, ref.getSessionKey());
        return resultMap;
    }

    @RequestMapping(value = "/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserInfo() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // generate response message
        User curUser = getApiRequestUser();

        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(curUser.getId());

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ID, curUser.getId());
        resultMap.add(RES_USER_TYPE, curUser.getUserType());
        resultMap.add(RES_REAL_NAME, curUser.getProfile().getRealname());
        resultMap.add(RES_NICK_NAME, curUser.getProfile().getNickName());
        resultMap.add(RES_USER_MOBILE, mobile);
        resultMap.add(RES_USER_GENDER, curUser.getProfile().getGender());
        resultMap.add(RES_AVATAR_URL, getUserAvatarImgUrl(curUser));
        resultMap.add(RES_CDN, CdnConfig.getCdnDomainMap());

        if (UserType.STUDENT.getType() == curUser.getUserType()) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(curUser.getId());
            if (clazz != null) {

                KlxStudent klxStudent;
                if (clazz.isJuniorClazz() && (klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(curUser.getId())) != null) {
                    resultMap.add(RES_STUDENT_NUMBER, klxStudent.getStudentNumber());
                    resultMap.add(RES_STUDENT_SCAN_NUMBER, klxStudent.getScanNumber());
                }
                resultMap.add(RES_CLAZZ_ID, clazz.getId());
                resultMap.add(RES_CLAZZ_NAME, clazz.getClassName());
                resultMap.add(RES_CLAZZ_LEVEL, clazz.getClassLevel());

                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(clazz.getSchoolId())
                        .getUninterruptibly();
                if (school != null) {
                    resultMap.add(RES_SCHOOL_ID, school.getId());
                    resultMap.add(RES_SCHOOL_NAME, school.getCname());
                    resultMap.add(RES_SHORT_SCHOOL_NAME, school.getShortName());
                    Integer regionCode = school.getRegionCode();
                    ExRegion region = raikouSystem.loadRegion(regionCode);
                    if (region != null) {
                        resultMap.add(RES_PROVINCE_CODE, region.getProvinceCode());
                        resultMap.add(RES_PROVINCE_NAME, region.getProvinceName());
                        resultMap.add(RES_CITY_CODE, region.getCityCode());
                        resultMap.add(RES_CITY_NAME, region.getCityName());
                        resultMap.add(RES_COUNTRY_CODE, region.getCountyCode());
                        resultMap.add(RES_COUNTRY_NAME, region.getCountyName());
                    }
                }

                // ============================================================================================
                // 读取分组信息
                List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(curUser.getId(), false);
                if (CollectionUtils.isNotEmpty(groupMappers)) {
                    resultMap.put(RES_USER_GROUP_ID, GroupMapper.filter(groupMappers).idList());
                }
                // ============================================================================================
            }
        } else if (UserType.TEACHER.getType() == curUser.getUserType()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(curUser.getId());

            resultMap.add(RES_SCHOOL_ID, teacherDetail == null ? "" : teacherDetail.getTeacherSchoolId());
            resultMap.add(RES_SCHOOL_NAME, teacherDetail == null ? "" : teacherDetail.getTeacherSchoolName());
            resultMap.add(RES_CITY_CODE, teacherDetail == null ? "" : teacherDetail.getCityCode());
            resultMap.add(RES_CITY_NAME, teacherDetail == null ? "" : teacherDetail.getCityName());
            if (teacherDetail == null || teacherDetail.getSubject() == null) {
                resultMap.add(RES_TEACHER_SUBJECT, "");
            } else {
                resultMap.add(RES_TEACHER_SUBJECT, teacherDetail.getSubject().getKey());
            }
            if (teacherDetail == null || teacherDetail.getKtwelve() == null) {
                resultMap.add(RES_TEACHER_KTWELVE, "");
            } else {
                resultMap.add(RES_TEACHER_KTWELVE, teacherDetail.getKtwelve().getLevel());
            }
        }

        // 判断用户是否是VIP用户
        if (StringUtils.isNotBlank(getApiRequestApp().getPurchaseUrl())) {
            resultMap.add(RES_PURCHASE_URL, HttpRequestContextUtils.getWebAppBaseUrl() + getApiRequestApp().getPurchaseUrl());
        }

        AppPayMapper payInfo = userOrderLoaderClient.getUserAppPaidStatus(getRequestString(REQ_APP_KEY), curUser.getId());
        if (payInfo == null || payInfo.unpaid()) {
            resultMap.add(RES_PRODUCT_STATUS, PRODUCT_STATUS_UNPAID);   // 未购买
        } else if (payInfo.isExpire()) {
            resultMap.add(RES_PRODUCT_STATUS, PRODUCT_STATUS_EXPIRED);  // 已过期
            resultMap.add(RES_EXPIRE_TIME, payInfo.getExpireTime());
        } else {
            resultMap.add(RES_PRODUCT_STATUS, PRODUCT_STATUS_ACTIVE);
            resultMap.add(RES_EXPIRE_TIME, payInfo.getExpireTime());

            if (CollectionUtils.isEmpty(payInfo.getValidAppItems())) {
                resultMap.add(RES_VALID_ITEMS, payInfo.getValidAppItems());
            }
        }

        //使用天数累计，暂时只有走美使用到这个字段，由于其他产品不一定存在天数说法
        String appKey = getRequestString(REQ_APP_KEY);
        if (OrderProductServiceType.UsaAdventure.name().equals(appKey)) {
            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(getRequestString(REQ_APP_KEY), curUser.getId());
            Set<String> productIds = userOrders.stream()
                    .map(UserOrder::getProductId).collect(Collectors.toSet());
            Map<String, List<OrderProductItem>> items = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
            int totalPaymentDay = 0;
            for (String productId : items.keySet()) {
                List<OrderProductItem> itemList = items.get(productId);
                if (CollectionUtils.isEmpty(itemList)) continue;
                for (OrderProductItem item : itemList) {
                    totalPaymentDay += SafeConverter.toInt(item.getPeriod());
                }
            }

            resultMap.put(REQ_TOTAL_PAYMENT_DAY, totalPaymentDay);
        }
        return filterResponseContent(resultMap);
    }

    @RequestMapping(value = "/exitsInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage exitsInfo() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired("info_type", "信息类型");
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        // generate response message
        User curUser = getApiRequestUser();
        if (curUser == null || !curUser.isStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        }
        String infoType = getRequestString("info_type");
        if ("STUDENT_ADVERTISEMENT_INFO".equalsIgnoreCase(infoType)) {
            List<StudentAdvertisementInfo> list = studentAdvertisementInfoLoaderClient.loadByUserId(curUser.getId());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add("isExist", CollectionUtils.isNotEmpty(list));
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/usertype/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserType() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_USER_CODE, "用户名");
            validateRequestNoSessionKey(REQ_USER_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // generate response message
        List<User> userList = userLoaderClient.loadUsers(getRequestString(REQ_USER_CODE), null);
        if (userList == null || userList.size() == 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        }

        List<Integer> userTypeList = new ArrayList<>();
        for (User userInfo : userList) {
            userTypeList.add(userInfo.getUserType());
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_TYPE, userTypeList);

        return resultMap;
    }

    @RequestMapping(value = "/integral/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserIntegral() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // generate response message
        User curUser = getApiRequestUser();
        UserIntegral integral;
        if (curUser.isStudent()) {
            integral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(curUser.getId());
        } else if (curUser.isTeacher()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(curUser.getId());
            integral = integralLoaderClient.getIntegralLoader().loadTeacherIntegral(curUser.getId(), teacherDetail.getKtwelve());
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTEGRAL_MISMATCH_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ID, curUser.getId());
        resultMap.add(RES_USER_INTEGRAL, integral.getUsable());

        return resultMap;
    }

    @RequestMapping(value = "/integral/add.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addUserIntegral() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_INTEGRAL, "学豆数");
            if (StringUtils.isEmpty(getRequestString(REQ_INTEGRAL_REASON))) {
                validateRequest(REQ_INTEGRAL);
            } else {
                validateRequest(REQ_INTEGRAL, REQ_INTEGRAL_REASON);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 验证APP是否允许添加银币
        VendorApps vendorApps = getApiRequestApp();
        if (vendorApps.getIntegralType() < 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APP_NO_ADD_INTEGRAL);
            return resultMap;
        }

        // 判断一下对方的IP是否在IP白名单里面
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            String appClientIp = getWebRequestContext().getRealRemoteAddress();
            if (!StringUtils.startsWith(appClientIp, "10.")) {
                if (vendorApps.getServerIps() == null || !vendorApps.getServerIps().contains(appClientIp)) {
                    logger.error("forbidden access from {}, our white list {}", appClientIp, vendorApps.getServerIps());
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_APP_NO_ADD_INTEGRAL);
                    return resultMap;
                }
            }
        }

        // add integral
        Integer integral = getRequestInt(REQ_INTEGRAL);
        User curUser = getApiRequestUser();
        String integralReason = getRequestString(REQ_INTEGRAL_REASON);
        int todayTotalIntegral = integralHistoryLoaderClient.getIntegralHistoryLoader()
                .loadUserIntegralHistories(curUser.getId())
                .stream()
                .filter(h -> {
                    long createTime = h.getCreatetime() == null ? 0 : h.getCreatetime().getTime();
                    return createTime >= DayRange.current().getStartDate().getTime();
                })
                .filter(h -> Objects.equals(h.getIntegralType(), vendorApps.getIntegralType()))
                .mapToInt(IntegralHistory::getIntegral)
                .sum();

        if ((todayTotalIntegral + integral) > vendorApps.getDayMaxAddIntegral()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTEGRAL_OVER_LIMIT_MSG);
            return resultMap;
        }

        IntegralHistory integralHistory = new IntegralHistory();
        integralHistory.setUserId(curUser.getId());
        integralHistory.setIntegralType(vendorApps.getIntegralType());
        integralHistory.setIntegral(integral);
        if (StringUtils.isEmpty(integralReason)) {
            integralHistory.setComment(vendorApps.getCname() + ", 获取奖励" + integral + "学豆");
        } else {
            integralHistory.setComment(integralReason);
        }
        integralHistory.setAddIntegralUserId(curUser.getId());
        MapMessage message = userIntegralService.changeIntegral(integralHistory);
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * @see com.voxlearning.washington.controller.mobile.MobileUserAvatarController
     */
    @Deprecated
    @RequestMapping(value = "/check_avatar.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage checkAvatar() {
        // 检查是否上传过头像并返回头像列表
        Long uid = getRequestLong("uid");

        //应产品要求，新用户进入的时候不需要这个弹窗了
        Boolean flag = true; // 判断用户是否第一次进入头像页面，用来弹窗
//        long now = System.currentTimeMillis();
//        long threshold = (long)180 * 24 * 60 * 60 * 1000;
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2017, 12, 8,0,0,0);
//        if (now < calendar.getTimeInMillis() + threshold) { //在180天范围内，超过180天便不再判断是否第一次进入头像页面
//            CacheObject<Integer> cacheObject = washingtonCacheSystem.CBS.persistence.get(ConversionUtils.toString(uid));
//            if (cacheObject != null && cacheObject.getValue() != null && cacheObject.getValue() == 1) {
//                flag = true;
//            }else {
//                washingtonCacheSystem.CBS.persistence.set(ConversionUtils.toString(uid), 180 * 24 * 3600, 1);
//            }
//        }else {
//            flag = true;
//        }

        User curUser = raikouSystem.loadUser(uid);
        if (curUser == null) {
            return MapMessage.errorMessage();
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (StudentAvatar avatar : StudentAvatar.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("avatarCode", ConversionUtils.toString(avatar.getKey()));
            map.put("avatarName", avatar.getName());
            map.put("avatarUrl", avatar.getUrl());
            if (StringUtils.isNotBlank(curUser.fetchImageUrl()) && avatar.getUrl().equals(curUser.fetchImageUrl())) {
                map.put("isUsed", "true");
            } else {
                map.put("isUsed", "false");
            }
            result.add(map);
        }
        return MapMessage.successMessage()
                .add("result", result)
                .add("canUpload", userLevelLoader.hasPrivilegeForUploadAvatar(Collections.singleton(uid)))
                .add("hadAccessed", flag)
                .add("isTobyAvatar", rewardCenterClient.isTobyAvatarType(curUser.getId()));
    }

    @Deprecated // using MobileUserAvatarController.updateAvatar
    @RequestMapping(value = "/update_avatar.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateAvatar() {
        Long uid = getRequestLong("uid");
        // 判断用户是否第一次更改头像，点击更改头像后要有个弹窗，再次确认才更改
        Boolean checkFirstChangeAvatar = getRequestBool("checkFirstChangeAvatar");
        User curUser = raikouSystem.loadUser(uid);
        if (curUser == null) {
            return MapMessage.errorMessage();
        }
        // 查看用户当前头像是不是固定头像
        if (checkFirstChangeAvatar) {
            if (!StudentAvatar.imgUrlList().contains(curUser.getProfile().getImgUrl())) {
                return MapMessage.successMessage(); // 是第一次修改
            }
            return MapMessage.errorMessage(); // 不是第一次修改
        }
        int key = getRequestInt("avatarCode");
        StudentAvatar studentAvatar = StudentAvatar.parse(key);
        if (studentAvatar == null) {
            return MapMessage.errorMessage("请选取正确头像");
        }
        String imageGfsId = studentAvatar.getUrl().replace(".jpg", "");
        return userServiceClient.userImageUploaded(curUser.getId(), studentAvatar.getUrl(), imageGfsId);
    }

    @RequestMapping(value = "/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserInfo() {
        MapMessage resultMap = new MapMessage();

        try {
            validateEnum(REQ_USER_GENDER, "性别", "M", "F");
            validateRequest(REQ_NICK_NAME, REQ_REAL_NAME, REQ_USER_GENDER, REQ_AVATAR_DAT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        resultMap = new MapMessage();

        // Update the nick name and real name
        String realName = StringHelper.filterEmojiForMysql(getRequestString(REQ_REAL_NAME));

        if (StringUtils.isNoneBlank(realName) && StringRegexUtils.isNotRealName(realName)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_REALNAME_NOT_IS_CHINESS);
            return resultMap;
        }

        if (!StringUtils.isEmpty(realName)) {
            // Feature #54929
//            if (ForbidModifyNameAndPortrait.check()) {
//                return ForbidModifyNameAndPortrait.errorMessage;
//            }
            MapMessage changeResult = userServiceClient.changeName(curUser.getId(), realName);
            if (!changeResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_REALNAME_ERROR_MSG + changeResult.getInfo());
                return resultMap;
            }

            // update user name log
            LogCollector.info("backend-general", MiscUtils.map("usertoken", curUser.getId(),
                    "usertype", curUser.getUserType(),
                    "platform", getApiRequestApp().getAppKey(),
                    "version", getRequestString(REQ_APP_NATIVE_VERSION),
                    "op", "change user name",
                    "mod1", curUser.fetchRealname(),
                    "mod2", realName,
                    "mod3", curUser.getAuthenticationState()));
        }

        String nickName = StringHelper.filterEmojiForMysql(getRequestString(REQ_NICK_NAME));
        if (!StringUtils.isEmpty(nickName)) {
            MapMessage changeResult = userServiceClient.changeNickName(curUser.getId(), nickName);
            if (!changeResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_NICKNAME_ERROR_MSG + changeResult.getInfo());
                return resultMap;
            }
        }

        // Update User Gender
        String userGender = getRequestString(REQ_USER_GENDER);
        if (!StringUtils.isEmpty(userGender)) {
            MapMessage changeResult = userServiceClient.changeGender(curUser.getId(), userGender);
            if (!changeResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_GENDER_ERROR_MSG + changeResult.getInfo());
                return resultMap;
            }
        }

        // Update Avatar Image
        String avatarDat = getRequestString(REQ_AVATAR_DAT);
        if (!StringUtils.isEmpty(avatarDat)) {
            // FIXME 学生或家长需要判断家庭成长值是否达标
            if (curUser.isStudent()) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(curUser.getId());
                if (studentDetail.isInfantStudent()) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "“海老师为保护孩子们隐私，升级了头像功能，停止自主上传。升级新版本后会有更多头像让大家选择。");
                    return resultMap;
                } else if (studentDetail.isPrimaryStudent() && !userLevelLoader.hasPrivilegeForUploadAvatar(Collections.singleton(curUser.getId()))) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "家庭等级达到3级及以上时可自主修改头像哦");
                    return resultMap;
                } else if (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent()) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "暂时无法修改头像哦");
                    return resultMap;
                }
            } else if (curUser.isParent()) {
                // FIXME todo later
            } else if (curUser.isTeacher()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, "暂时无法修改头像，请升级到最新版");
                return resultMap;
            }

//            MapMessage changeResult = updateUserAvatar(curUser, avatarDat);
//            if (!changeResult.isSuccess()) {
//                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_AVATAR_ERROR_MSG + changeResult.getInfo());
//                return resultMap;
//            } else {
//                // update cache info to reload user avatar
//                clearApiRequestUser();
//                resultMap.add(RES_AVATAR_URL, getUserAvatarImgUrl(getApiRequestUser()));
//            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/integralhistory/get.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getIntegralHistory() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_PAGE_NUMBER);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Integer pageNumber = getRequestInt(REQ_PAGE_NUMBER);
        pageNumber = pageNumber < 1 ? 1 : pageNumber;
        // 获取金币前三个月的历史数据
        User curUser = getApiRequestUser();
        UserIntegralHistoryPagination pagination = userLoaderClient
                .loadUserIntegralHistories(curUser, 3, pageNumber - 1, 10);
        List<IntegralHistory> histories = pagination.getContent();
        List<Map<String, Object>> integralHistories = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(histories)) {
            for (IntegralHistory integralHistory : histories) {
                Map<String, Object> integralMap = new HashMap<>();
                integralMap.put(RES_CREATE_TIME, integralHistory.getCreatetime());
                integralMap.put(RES_USER_INTEGRAL, integralHistory.getIntegral());
                integralMap.put(RES_USER_INTEGRAL_COMMENT, integralHistory.getComment());
                integralHistories.add(integralMap);
            }
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(curUser.getId());
        resultMap.add(RES_USER_INTEGRAL, teacherDetail.getUserIntegral().getUsable());
        resultMap.add(RES_USER_INTEGRAL_HISTORY, integralHistories);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/password/change.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changePassword() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PASSWD, "原密码");
            validateRequired(REQ_NEW_PASSWD, "新密码");
            validateRequest(REQ_PASSWD, REQ_NEW_PASSWD);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String password = getRequestString(REQ_PASSWD);
        String newPassword = getRequestString(REQ_NEW_PASSWD);

        try {
            PasswordRule.validatePassword(newPassword);
        } catch (Exception ex) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PASSWORD_INVALID);
            return resultMap;
        }

        User curUser = getApiRequestUser();

        try {
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(curUser.getId());
            if (!ua.verifyPassword(password)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_PASSWORD);
                return resultMap;
            }

            MapMessage message = userServiceClient.changePassword(curUser, password, newPassword);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                getWebRequestContext().saveAuthenticationStates(-1, curUser.getId(), password, RoleType.of(curUser.getUserType()));

                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(curUser.getId());
                userServiceRecord.setOperatorId(curUser.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("修改密码");
                userServiceRecord.setComments("用户修改自己密码，操作端[app]");
                userServiceRecord.setAdditions("refer:UserApiController.changePassword");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }

            //若是教师，修改密码之后发送短信
            if (curUser.fetchUserType() == UserType.TEACHER) {
                Map<String, Object> content = new LinkedHashMap<>();
                content.put("name", curUser.getProfile().getRealname());
                content.put("userId", curUser.getId());
                content.put("password", newPassword);   // <- put new password here
                content.put("hotline", Constants.HOTLINE_SPACED);
                content.put("date", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
                content.put("time", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 HH点mm分"));

                // 短信内容不能含有 “操” --> "...如非本人操作请与我们联系..."
                userEmailServiceClient.buildEmail(EmailTemplate.teachermodifypassword)
                        .to(ua)
                        .subject("您已更改在一起作业的个人资料")
                        .content(content)
                        .send();
            }

            String appKey = getRequestString(REQ_APP_KEY);
            // 如果学生/老师/家长修改密码，更新sessionkey,强制登出
            if ("17Student".equals(appKey) || "17JuniorStu".equals(appKey)
                    || "17Teacher".equals(appKey) || "17JuniorTea".equals(appKey) || "Shensz".equals(appKey)
                    || "17Parent".equals(appKey) || "17JuniorPar".equals(appKey)
                    || "17Agent".equals(appKey)) {
                String sessionKeyNew = generateSessionkey(curUser.getId());
                MapMessage mapMessage = vendorServiceClient.expireSessionKey(appKey, curUser.getId(), sessionKeyNew);
                if (mapMessage.isSuccess()) {
                    resultMap.add(RES_SESSION_KEY, sessionKeyNew);
                    // 中学老师端还需要额外处理一下Shensz
                    if ("17JuniorTea".equals(appKey)) {
                        sessionKeyNew = generateSessionkey(curUser.getId());
                        vendorServiceClient.expireSessionKey("Shensz", curUser.getId(), sessionKeyNew);
                    }

                    if ("Shensz".equals(appKey)) {
                        sessionKeyNew = generateSessionkey(curUser.getId());
                        vendorServiceClient.expireSessionKey("17JuniorTea", curUser.getId(), sessionKeyNew);
                    }

                } else {
                    resultMap.add(RES_SESSION_KEY, "");
                }
            }
        } catch (Exception ex) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
        }

        return resultMap;
    }

    @RequestMapping(value = "/mobile/change.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changeMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequest(REQ_USER_TYPE, REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String verifyCode = getRequestString(REQ_VERIFY_CODE);
        User curUser = getApiRequestUser();
        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        MapMessage mapMessage;
        if (userType == UserType.STUDENT) {
            mapMessage = verificationService.verifyMobile(
                    curUser.getId(),
                    verifyCode,
                    SmsType.APP_STUDENT_VERIFY_MOBILE_CENTER.name());
        } else if (userType == UserType.TEACHER) {
            mapMessage = verificationService.verifyMobile(
                    curUser.getId(),
                    verifyCode,
                    SmsType.APP_TEACHER_VERIFY_MOBILE_CENTER.name());
        } else {
            mapMessage = verificationService.verifyMobile(
                    curUser.getId(),
                    verifyCode,
                    SmsType.APP_PARENT_VERIFY_MOBILE_CENTER.name());
        }

        if (mapMessage.isSuccess()) {
            //绑定手机验证成功时，如果用户是冻结状态，需要解除冻结
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(curUser.getId());
            if (studentExtAttribute != null && studentExtAttribute.isFreezing()) {
                studentServiceClient.freezeStudent(curUser.getId(), false);
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return resultMap;
    }

    // 为客服系统提供的用户信息获取接口
    // 传入参数: 用户ID或手机号码
    @RequestMapping(value = "/info.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserInfo4Crm() {
        MapMessage resultMap = new MapMessage();

        try {
            validateNumber(REQ_USER_ID, "用户ID");
            validateMobileNumber(REQ_CONTACT_MOBILE);
            validateRequiredAny(REQ_USER_ID, REQ_CONTACT_MOBILE, "用户ID", "手机号码");
            validateRequestNoSessionKey(REQ_USER_ID, REQ_CONTACT_MOBILE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        VendorApps vendorApps = getApiRequestApp();
        if (!"UserService".equals(vendorApps.getAppKey())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return resultMap;
        }

        // 判断一下对方的IP是否在IP白名单里面
        // FIXME 最近客服IP老变,暂时去掉IP白名单
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            String appClientIp = getWebRequestContext().getRealRemoteAddr();
            if (vendorApps.getServerIps() == null || !vendorApps.getServerIps().contains(appClientIp)) {
                final String CACHE_KEY = "USER_SERVICE_UNKNOWN_IPS";
                List<String> unknownIps = washingtonCacheSystem.CBS.flushable.load(CACHE_KEY);
                if (CollectionUtils.isEmpty(unknownIps)) {
                    unknownIps = new ArrayList<>();
                }

                if (!unknownIps.contains(appClientIp)) {
                    logger.error("forbidden access from {}, our white list {}", appClientIp, vendorApps.getServerIps());
                    unknownIps.add(appClientIp);
                    washingtonCacheSystem.CBS.flushable.set(CACHE_KEY, DateUtils.getCurrentToDayEndSecond(), unknownIps);
                }

//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
//                return resultMap;
            }
        }


        List<User> userList = new ArrayList<>();
        if (StringUtils.isNotBlank(getRequestString(REQ_USER_ID))) {
            Long userId = getRequestLong(REQ_USER_ID);
            User user = raikouSystem.loadUser(userId);
            if (user != null) {
                userList.add(user);
            }
        } else {
            String userMobile = getRequestString(REQ_CONTACT_MOBILE);
            List<User> users = userLoaderClient.loadUsers(userMobile, null);
            if (users != null && users.size() > 0) {
                userList.addAll(users);
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        List<Map<String, Object>> userInfoList = new ArrayList<>();
        for (User user : userList) {
            Map<String, Object> userInfo = new LinkedHashMap<>();
            // 用户类型（学生/老师）
            userInfo.put(RES_USER_TYPE, user.getUserType());
            userInfo.put(RES_EXT_USER_TYPE, user.getUserType()); // 扩展类型

            // 账号
            userInfo.put(RES_USER_ID, user.getId());
            // 学豆/金币数量
            UserIntegral userIntegral = null;
            // CRM链接
            if (user.fetchUserType() == UserType.STUDENT) {
                userIntegral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(user.getId());
                userInfo.put(RES_USER_CRM_LINK, getCrmBaseUrl() + "crm/student/studenthomepage.vpage?studentId=" + user.getId());
            } else if (user.fetchUserType() == UserType.TEACHER) {
                userIntegral = teacherLoaderClient.loadMainSubTeacherUserIntegral(user.getId(), null);
                userInfo.put(RES_USER_CRM_LINK, getCrmBaseUrl() + "crm/teacher/teacherhomepage.vpage?teacherId=" + user.getId());
            } else if (user.fetchUserType() == UserType.PARENT) {
                userInfo.put(RES_USER_CRM_LINK, getCrmBaseUrl() + "crm/parent/parenthomepage.vpage?parentId=" + user.getId());
            }

            // 学豆/金币数量
            if (userIntegral != null && userIntegral.getIntegral() != null) {
                userInfo.put(RES_USER_INTEGRAL, userIntegral.getIntegral().getUsableIntegral());
            } else {
                userInfo.put(RES_USER_INTEGRAL, 0);
            }

            userInfo.put(RES_USER_INTEGRAL_CRM_LINK, getCrmBaseUrl() + "crm/integral/integraldetail.vpage?userId=" + user.getId());

            // 是否绑定微信
            userInfo.put(RES_WECHAT_BINDING, false);

            if (user.fetchUserType() == UserType.STUDENT) {
                List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(user.getId());
                if (studentParents != null && studentParents.size() > 0) {
                    for (StudentParent parent : studentParents) {
                        if (wechatLoaderClient.isBinding(parent.getParentUser().getId(), WechatType.PARENT.getType())) {
                            userInfo.put(RES_WECHAT_BINDING, true);
                            break;
                        }
                    }
                }

                // 学校级别
                userInfo.put(RES_SCHOOL_LEVEL, SchoolLevel.JUNIOR.name());
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                if (studentDetail != null && studentDetail.isJuniorStudent()) {
                    userInfo.put(RES_SCHOOL_LEVEL, SchoolLevel.MIDDLE.name());
                } else if (studentDetail != null && studentDetail.isInfantStudent()) {
                    userInfo.put(RES_SCHOOL_LEVEL, SchoolLevel.INFANT.name());
                }
            } else if (user.fetchUserType() == UserType.TEACHER) {
                if (wechatLoaderClient.isBinding(user.getId(), WechatType.TEACHER.getType())) {
                    userInfo.put(RES_WECHAT_BINDING, true);
                }

                // IVR要单独给校园大使播放欢迎声音，所以独立出RES_EXT_USER_TYPE，1：老师，2：家长，3：学生 4：校园大使
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                if (teacherDetail != null && teacherDetail.isSchoolAmbassador()) {
                    userInfo.put(RES_EXT_USER_TYPE, 4);
                }

                // 学校级别
                userInfo.put(RES_SCHOOL_LEVEL, SchoolLevel.JUNIOR.name());
                if (teacherDetail != null && (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher())) {
                    userInfo.put(RES_SCHOOL_LEVEL, SchoolLevel.MIDDLE.name());
                } else if (teacherDetail != null && teacherDetail.isInfantTeacher()) {
                    userInfo.put(RES_SCHOOL_LEVEL, SchoolLevel.INFANT.name());
                }

            } else if (user.fetchUserType() == UserType.PARENT) {
                if (wechatLoaderClient.isBinding(user.getId(), WechatType.PARENT.getType())) {
                    userInfo.put(RES_WECHAT_BINDING, true);
                }
            }

            // 姓名
            userInfo.put(RES_REAL_NAME, user.getProfile().getRealname());
            // 学校
            School school = asyncUserServiceClient.getAsyncUserService()
                    .loadUserSchool(user)
                    .getUninterruptibly();
            if (school != null) {
                userInfo.put(RES_SCHOOL_ID, school.getId());
                userInfo.put(RES_SCHOOL_NAME, school.getCname());
            }

            // QQ号
            String qq = sensitiveUserDataServiceClient.loadUserQq(user.getId(), "/v1/info");
            if (StringUtils.isNotBlank(qq)) {
                userInfo.put(RES_CONTACT_QQ, qq);
            } else {
                userInfo.put(RES_CONTACT_QQ, "");
            }

            // 认证状态
            if (user.fetchUserType() == UserType.TEACHER || user.fetchUserType() == UserType.STUDENT) {
                userInfo.put(RES_USER_STATE, user.fetchCertificationState().getDescription());
            }

            userInfoList.add(userInfo);
        }

        // 排序，按照老师、学生、家长排序
        if (userInfoList.size() > 1) {
            Collections.sort(userInfoList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer userType1 = (Integer) o1.get(RES_USER_TYPE);
                    if (userType1.equals(UserType.PARENT.getType())) {
                        userType1 = userType1 + 10;
                    }
                    Integer userType2 = (Integer) o2.get(RES_USER_TYPE);
                    if (userType2.equals(UserType.PARENT.getType())) {
                        userType2 = userType2 + 10;
                    }
                    return userType1.compareTo(userType2);
                }
            });

        }

        resultMap.add(RES_USER_LIST, userInfoList);

        return resultMap;
    }

    // 为客服系统提供的工作记录接入CRM接口
    @RequestMapping(value = "/task_record/add.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage workRecordAdd() {
        MapMessage message = new MapMessage();
        try {
            validateRequired(REQ_CS_ID, "客服用户ID");
            validateRequiredNumber(REQ_CALL_TYPE, "呼叫类型");
            validateRequired(REQ_RECORD_TYPE, "记录类型");
            validateRequired(REQ_RECORD_STATUS, "记录状态");
            validateRequired(REQ_RECORD_CONTENT, "记录内容");
            validateRequiredNumber(REQ_TEACHER_ID, "用户ID");
            validateRequestNoSessionKey(REQ_CS_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            logger.error("Validate Excp : {}", e.getMessage());
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, e.getMessage());
            return message;
        }

        VendorApps vendorApps = getApiRequestApp();
        if (!"UserService".equals(vendorApps.getAppKey())) {
            logger.error("Error : {}", RES_RESULT_NO_ACCESS_RIGHT_MSG);
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return message;
        }

        // IP白名单
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            String appClientIp = getWebRequestContext().getRealRemoteAddr();
            if (vendorApps.getServerIps() == null || !vendorApps.getServerIps().contains(appClientIp)) {
                logger.error("Error : {}", RES_RESULT_NO_ACCESS_RIGHT_MSG);
                message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                message.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
                return message;
            }
        }

        final String callerId = getRequestString(REQ_CS_ID);    //客服编号

        AlpsFuture<AdminUser> adminUserFuture = adminUserServiceClient.getAdminUserService()
                .loadAdminUserByAgentId(callerId);

        final long userId = getRequestLong(REQ_TEACHER_ID);

        final int callType = getRequestInt(REQ_CALL_TYPE);
        CrmTaskRecordCategory recordCategory = CrmTaskRecordCategory.nameOf(getRequestString(REQ_RECORD_TYPE));
        recordCategory = recordCategory == null ? CrmTaskRecordCategory.其余_其余问题 : recordCategory;
        CrmContactType contactType = callType == 1 ? CrmContactType.电话呼入 : CrmContactType.电话呼出;
        String title = MessageFormat.format(RECORD_TITLE_PATTERN, String.valueOf(userId), (callType == 1 ? "呼入" : "呼出"));
        String content = getRequestString(REQ_RECORD_CONTENT);
        int callTime = getRequestInt(REQ_CALL_DURATION);
        String audioUrl = getRequestString(REQ_RECORDING_URL);

        String adminUsername = "";
        String adminRealName = "";
        AdminUser adminUser = adminUserFuture.getUninterruptibly();
        if (adminUser != null && StringUtils.isNotBlank(adminUser.getAdminUserName())) {
            adminUsername = adminUser.getAdminUserName();
            adminRealName = adminUser.getRealName();
        }


        MapMessage msg = crmSummaryServiceClient.addUserTaskRecord(userId, adminUsername, adminRealName, recordCategory, contactType, title, content, callTime, audioUrl, callerId);
        if (msg.isSuccess()) {
            message.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            logger.error("Error : {}", msg.getInfo());
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, msg.getInfo());
        }
        return message;
    }

    // 获取img_domain，不需要用户sessionkey
    @RequestMapping(value = "/imgdomain/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getImgDomain() {
        MapMessage result = new MapMessage();
        try {
            validateRequestNoSessionKey();
        } catch (IllegalArgumentException e) {
            logger.error("Validate Excp : {}", e.getMessage());
            result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            result.add(RES_MESSAGE, e.getMessage());
            return result;
        }

        result.add(RES_RESULT, RES_RESULT_SUCCESS);
        result.add(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        // 返回所有cdn列表
        if (getWebRequestContext().isHttpsRequest()) {
            List<String> cdnList = new ArrayList<>();
            CdnConfig.getCdnDomainMap().values()
                    .stream()
                    .forEach(e -> cdnList.add(e.replaceAll("^http://", "https://")));
            result.add(RES_CDN, cdnList);
        } else {
            result.add(RES_CDN, CdnConfig.getCdnDomainMap().values());
        }
        return result;
    }

    @RequestMapping(value = "/getmobile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_ID);
            validateRequestNoSessionKey(REQ_USER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long userId = getRequestLong(REQ_USER_ID);
        User user = raikouSystem.loadUser(userId);
        resultMap.add("is_user_exists", user != null);
        if (user == null) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_USER_ID_NOT_EXISTS_MSG);
            return resultMap;
        }
        String mobile = SafeConverter.toString(sensitiveUserDataServiceClient.loadUserMobileObscured(userId), "");
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (StringUtils.isBlank(mobile)) {
            resultMap.add(RES_MESSAGE, RES_RESULT_USER_NOT_BIND_MOBILE_MSG);
        }
        resultMap.add("mobile", mobile);

        return resultMap;
    }

    @RequestMapping(value = "/getUserId.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserId() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_MOBILE);
            validateRequired(REQ_USER_TYPE);
            validateRequestNoSessionKey(REQ_MOBILE, REQ_USER_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String mobile = getRequestString(REQ_MOBILE);
        String userType = getRequestString(REQ_USER_TYPE);
        UserAuthentication user = userLoaderClient.loadMobileAuthentication(mobile, UserType.valueOf(userType));
        if (user == null) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_USER_ID_NOT_EXISTS_MSG);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_USER_ID, user.getId());
        }
        return resultMap;
    }

    @RequestMapping(value = "/getschoollist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getSchoolList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_REGION_PCODE);
            //validateRequired(REQ_KTWELVE);
            validateRequestNoSessionKey(REQ_REGION_PCODE, REQ_KTWELVE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        int code = getRequestInt(REQ_REGION_PCODE);
        if (code == 0) {
            resultMap.add(RES_SCHOOL_LIST, new ArrayList<>());
            resultMap.add(RES_MESSAGE, RES_RESULT_SUCCESS);
            return resultMap;
        }

        String level = getRequestString(REQ_KTWELVE);

        List<School> schools;

        if (StringUtils.isEmpty(level)) {
            schools = loadAreaSchools(Collections.singleton(code));
        } else {
            Ktwelve ktwelve = Ktwelve.of(level);
            if (ktwelve != Ktwelve.PRIMARY_SCHOOL && ktwelve != Ktwelve.JUNIOR_SCHOOL && ktwelve != Ktwelve.INFANT) {
                ktwelve = Ktwelve.PRIMARY_SCHOOL;
            }
            schools = loadAreaSchools(Collections.singleton(code), SchoolLevel.safeParse(ktwelve.getLevel()));
        }

        List<Map<String, Object>> schoolList = new ArrayList<>();
        schools.stream().forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_SCHOOL_ID, e.getId());
            map.put(RES_SCHOOL_LEVEL, e.getLevel());
            map.put(RES_SCHOOL_NAME, e.getShortName());
            schoolList.add(map);
        });
        resultMap.put(RES_SCHOOL_LIST, schoolList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/clazzlevel/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzLevelList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_SCHOOL_ID, "学校ID");
            validateRequired(REQ_KTWELVE);
            validateRequestNoSessionKey(REQ_SCHOOL_ID, REQ_KTWELVE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        Ktwelve ktwelve = Ktwelve.of(getRequestString(REQ_KTWELVE));
        List<Long> clazzIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .nature()
                .originalLocationsAsList()
                .stream()
                .map(Clazz.Location::getId)
                .collect(Collectors.toList());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Set<String> clazzLevelSet = clazzMap.values()
                .stream()
                .filter(e -> (ktwelve == Ktwelve.PRIMARY_SCHOOL && e.isPrimaryClazz()) || (ktwelve == Ktwelve.JUNIOR_SCHOOL && e.isJuniorClazz()) || (ktwelve == Ktwelve.INFANT && e.isInfantClazz()))
                .map(Clazz::getClassLevel)
                .collect(Collectors.toSet());
        List<Map<String, Object>> clazzLevelList = new ArrayList<>();
        for (ClazzLevel clazzLevel : ClazzLevel.values()) {
            if (clazzLevelSet.contains(String.valueOf(clazzLevel.getLevel())) && !clazzLevel.getGraduatedClazzLevels().contains(clazzLevel)) {
                Map<String, Object> clazzLevelMap = new HashMap<>();
                clazzLevelMap.put("level", clazzLevel.getLevel());
                clazzLevelMap.put("description", clazzLevel.getDescription());
                clazzLevelList.add(clazzLevelMap);
            }
        }
        resultMap.add("clazz_level_list", clazzLevelList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/birthday/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserBirthday() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_BIRTHDAY, "生日");
            validateRequest(REQ_BIRTHDAY);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        resultMap = new MapMessage();

        Date userBirthday = DateUtils.stringToDate(getRequestString(REQ_BIRTHDAY), DateUtils.FORMAT_SQL_DATE);
        if (userBirthday != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(userBirthday);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            MapMessage result = userServiceClient.changeUserBirthday(curUser.getId(), year, month, day);
            if (!result.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, result.getInfo());
                return resultMap;
            }

            // update user birthday
            LogCollector.info("backend-general", MiscUtils.map("usertoken", curUser.getId(),
                    "usertype", curUser.getUserType(),
                    "platform", getApiRequestApp().getAppKey(),
                    "version", getRequestString(REQ_APP_NATIVE_VERSION),
                    "op", "change user name",
                    "mod1", curUser.fetchRealname(),
                    "mod2", getRequestString(REQ_BIRTHDAY),
                    "mod3", curUser.getAuthenticationState()));
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }

    private List<School> loadAreaSchools(Collection<Integer> regionCodes, SchoolLevel schoolLevel) {
        return raikouSystem.querySchoolLocations(regionCodes)
                .enabled()
                .waitingSuccess()
                .level(schoolLevel)
                .filter(s -> s.getType() != SchoolType.TRAINING.getType() && s.getType() != SchoolType.CONFIDENTIAL.getType())
                .transform()
                .asList();
    }

    private List<School> loadAreaSchools(Collection<Integer> regionCodes) {
        return raikouSystem.querySchoolLocations(regionCodes)
                .enabled()
                .waitingSuccess()
                .filter(s -> s.getType() != SchoolType.TRAINING.getType() && s.getType() != SchoolType.CONFIDENTIAL.getType())
                .transform()
                .asList();
    }

    // 获取客户端访问的schema
    @RequestMapping(value = "fetchschema.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchSchema() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_SCHEMA, "https");
        resultMap.add(RES_MAA_FLAG, true);  //已经全量
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    @RequestMapping(value = "updateUserAuthenticationState.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserAuthenticationState() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_AUTH_INFOS);
            if (hasSessionKey())
                validateRequest(REQ_USER_AUTH_INFOS);
            else
                validateRequestNoSessionKey(REQ_USER_AUTH_INFOS);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        List<Map> userAuthInfos = JsonUtils.fromJsonToList(getRequestString(REQ_USER_AUTH_INFOS), Map.class);
        if (userAuthInfos == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "参数校验失败");
            return resultMap;
        }
        if (!(Objects.equals(getRequestString(REQ_APP_KEY), "Shensz") || Objects.equals(getRequestString(REQ_APP_KEY), "17JuniorTea"))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APP_ERROR_MSG);
            return resultMap;
        }
        try {
            userAuthInfos.forEach(userAuthInfo -> {
                Long userId = SafeConverter.toLong(userAuthInfo.get("user_id"));
                AuthenticationState authenticationState = AuthenticationState.valueOf(SafeConverter.toString(userAuthInfo.get("authenticationState")));
                userServiceClient.updateAuthenticationState(userId, authenticationState.getState());
            });
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    @RequestMapping(value = "delete/verifycode/check.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteUserVerifyCodeCheck() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequest(REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        if (curUser == null) {
            return MapMessage.errorMessage();
        }

        // check验证码
        String code = getRequestString(REQ_VERIFY_CODE);

        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(curUser.getId());

        SmsType smsType = SmsType.NO_CATEGORY;
        if (curUser.isStudent()) {
            smsType = SmsType.APP_STUDENT_VERIFY_MOBILE_DELETE_ACCOUNT;
        } else if (curUser.isParent()) {
            smsType = SmsType.APP_PARENT_VERIFY_MOBILE_DELETE_ACCOUNT;
        } else if (curUser.isTeacher()) {
            smsType = SmsType.APP_TEACHER_VERIFY_MOBILE_DELETE_ACCOUNT;
        }
        if (smsType == SmsType.NO_CATEGORY) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_RELOGIN);
            return resultMap;
        }

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, smsType.name(), false);
        if (!validateResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        } else {
            validateResult.set(RES_RESULT, RES_RESULT_SUCCESS);
            return validateResult;
        }
    }

    @RequestMapping(value = "delete.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteUser() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequest(REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User user = getApiRequestUser();
        if (user == null) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_RELOGIN);
            return resultMap;
        }

        // check验证码
        String code = getRequestString(REQ_VERIFY_CODE);

        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(user.getId());

        SmsType smsType = SmsType.NO_CATEGORY;
        if (user.isStudent()) {
            smsType = SmsType.APP_STUDENT_VERIFY_MOBILE_DELETE_ACCOUNT;
        } else if (user.isParent()) {
            smsType = SmsType.APP_PARENT_VERIFY_MOBILE_DELETE_ACCOUNT;
        } else if (user.isTeacher()) {
            smsType = SmsType.APP_TEACHER_VERIFY_MOBILE_DELETE_ACCOUNT;
        }
        if (smsType == SmsType.NO_CATEGORY) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_RELOGIN);
            return resultMap;
        }

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, smsType.name(), true);
        if (!validateResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }
        String lock = "UserApiController_deleteUser";
        AtomicLockManager.instance().acquireLock(lock);
        MapMessage mapMessage = MapMessage.errorMessage();
        try {
            UserType userType = user.fetchUserType();
            switch (userType) {
                case STUDENT:
                    kickoutUser(user.getId(), "17Student", "17JuniorStu");
                    mapMessage = studentServiceClient.deleteStudent(user.getId());
                    break;
                case PARENT:
                    kickoutUser(user.getId(), "17Parent", "17JuniorPar");
                    mapMessage = parentServiceClient.deleteParent(user.getId());
                    unbindWechat(user.getId(), userType);
                    break;
                case TEACHER:
                    kickoutUser(user.getId(), "17Teacher", "17JuniorTea");
                    mapMessage = teacherServiceClient.deleteTeacher(user.getId());
                    unbindWechat(user.getId(), userType);
                    break;
                default:
                    break;
            }
            if (mapMessage.isSuccess()) {
                mapMessage = userServiceClient.disableUser(user.getId());
                if (mapMessage.isSuccess()) {
                    // 记录 UserServiceRecord
                    String mobileEncrypted = com.voxlearning.alps.extension.sensitive.codec.SensitiveLib.encodeMobile(userMobile);
                    String operation = "删除/注销用户" + user.getId() + "," + mobileEncrypted;
                    UserServiceRecord userServiceRecord = new UserServiceRecord();
                    userServiceRecord.setUserId(user.getId());
                    userServiceRecord.setOperatorId(user.fetchRealname());
                    userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                    userServiceRecord.setOperationContent("删除用户");
                    userServiceRecord.setComments(operation + "；说明[用户自主删除]");
                    userServiceClient.saveUserServiceRecord(userServiceRecord);
                    resultMap.setSuccess(true);
                }
            }
            if (!mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } catch (CannotAcquireLockException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_DEAL);
            return resultMap;
        } finally {
            AtomicLockManager.getInstance().releaseLock(lock);
        }
    }

    private void kickoutUser(Long userId, String... appKeys) {
        for (String appKey : appKeys) {
            VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef(appKey, userId);
            if (vendorAppsUserRef != null) {
                vendorServiceClient.expireSessionKey(
                        appKey,
                        userId,
                        SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), userId));
            }
        }
    }

    private void unbindWechat(Long userId, UserType userType) {
        int wechatTypeId = WechatType.PARENT.getType();
        if (userType.equals(UserType.TEACHER))
            wechatTypeId = WechatType.TEACHER.getType();
        UserWechatRef userWechatRef = wechatLoaderClient.loadUserWechatRefByUserIdAndWechatType(userId,
                wechatTypeId);
        if (userWechatRef != null) {
            wechatServiceClient.unbindUserAndWechat(userWechatRef.getOpenId());
        }
        //todo unbind wechat mini programme
    }
}
