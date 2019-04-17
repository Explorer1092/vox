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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.DPVendorService;
import com.voxlearning.utopia.service.vendor.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.entity.AppUserMessageDynamic;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.cache.manager.YiQiXuePushTagCacheSystem;
import com.voxlearning.utopia.service.vendor.impl.loader.VendorLoaderImpl;
import com.voxlearning.utopia.service.vendor.impl.support.OpenApiUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = DPVendorService.class)
@ExposeService(interfaceClass = DPVendorService.class)
public class DPVendorServiceImpl implements DPVendorService {

    private CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncVendorCacheServiceImpl asyncVendorCacheService;
    @Inject private AppMessageServiceImpl appMessageService;
    @Inject private VendorLoaderImpl vendorLoader;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private YiQiXuePushTagCacheSystem yiQiXuePushTagCacheSystem;
    @Inject private AsyncVendorServiceImpl asyncVendorService;
    @Inject private VendorServiceImpl vendorService;

    @Override
    public void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo) {
        appMessageService.sendAppJpushMessageByIds(content, source, userIds, extInfo);
    }

    @Override
    public void sendAppJpushMessageByIds(String content, String source, List<Long> userIds, Map<String, Object> extInfo) {
        AppMessageSource appMessageSource = null;
        try {
            appMessageSource = AppMessageSource.valueOf(source);
        } catch (Exception ignore) {

        }
        appMessageService.sendAppJpushMessageByIds(content, appMessageSource, userIds, extInfo);
    }

    @Override
    public void sendAppJpushMessageByIds(String content, AppMessageSource source, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        appMessageService.sendAppJpushMessageByIds(content, source, userIds, extInfo, sendTimeEpochMilli);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo) {
        appMessageService.sendAppJpushMessageByTags(content, source, tags, tagsAnd, extInfo);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, AppMessageSource source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        appMessageService.sendAppJpushMessageByTags(content, source, tags, tagsAnd, extInfo, durationTime);
    }

    @Override
    public void sendAppJpushMessageByTags(String content, String source, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        appMessageService.sendAppJpushMessageByTags(content, AppMessageSource.of(source), tags, tagsAnd, extInfo, durationTime);
    }

    @Override
    public MapMessage saveAppUserDynamicMessage(Collection<AppUserMessageDynamic> messages) {
        return appMessageService.saveAppUserDynamicMessage(messages);
    }

    @Override
    public void sendClassmatesUsageToParentFairyland(Long clazzId, Long studentId, String ak, String content) {
        if (clazzId == null || studentId == null || StringUtils.isEmpty(content)) return;

        OrderProductServiceType appKey = OrderProductServiceType.safeParse(ak);
        if (appKey == OrderProductServiceType.Unknown) return;

        asyncVendorCacheService.ParentFairylandClassmatesUsageCacheManager_record(clazzId, appKey, studentId, content)
                .awaitUninterruptibly();
    }

    @Override
    public Map<String, Object> getUserInfo(String appKey, String sessionKey) {
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(sessionKey)) {
            return MapUtils.m("success", false, "info", "参数错误");
        }
        // 校验appkey
        VendorApps requestApp = vendorLoader.loadVendor(appKey);
        if (requestApp == null) {
            return MapUtils.m("success", false, "info", "错误的appKey");
        }
        // 校验user
        Long userId = decodeUserIdFromSessionKey(commonConfiguration.getSessionEncryptKey(), sessionKey);
        if (userId == null || userId <= 0L) {
            return MapUtils.m("success", false, "info", "错误的sessionKey");
        }
        VendorAppsUserRef appUserRef = vendorLoader.loadVendorAppUserRef(appKey, userId);
        if (appUserRef == null) {
            return MapUtils.m("success", false, "info", "错误的sessionKey");
        }

        if (!sessionKey.equals(appUserRef.getSessionKey())) {
            return MapUtils.m("success", false, "info", "错误的sessionKey");
        }
        User curUser = raikouSystem.loadUser(userId);
        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(curUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user_id", curUser.getId());
        result.put("user_type", curUser.getUserType());
        result.put("real_name", curUser.getProfile().getRealname());
        result.put("nick_name", curUser.getProfile().getNickName());
        result.put("user_mobile", mobile);
        result.put("gender", curUser.getProfile().getGender());
        result.put("avatar_url", curUser.fetchImageUrl());

        if (UserType.STUDENT.getType() == curUser.getUserType()) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(curUser.getId());
            if (clazz != null) {
                result.put("clazz_id", clazz.getId());
                result.put("clazz_name", clazz.getClassName());
                result.put("clazz_level", clazz.getClassLevel());

                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(clazz.getSchoolId())
                        .getUninterruptibly();
                if (school != null) {
                    result.put("school_id", school.getId());
                    result.put("school_name", school.getCname());
                    result.put("school_short_name", school.getShortName());
                    Integer regionCode = school.getRegionCode();
                    ExRegion region = raikouSystem.loadRegion(regionCode);
                    if (region != null) {
                        result.put("province_code", region.getProvinceCode());
                        result.put("province_name", region.getProvinceName());
                        result.put("city_code", region.getCityCode());
                        result.put("city_name", region.getCityName());
                        result.put("country_code", region.getCountyCode());
                        result.put("country_name", region.getCountyName());
                    }
                }
                // 读取分组信息
                List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(curUser.getId(), false);
                if (CollectionUtils.isNotEmpty(groupMappers)) {
                    result.put("group_ids", GroupMapper.filter(groupMappers).idList());
                }
            }
        }
        // 判断用户是否是VIP用户
        if (StringUtils.isNotBlank(requestApp.getPurchaseUrl())) {
            result.put("purchase_url", HttpRequestContextUtils.getWebAppBaseUrl() + requestApp.getPurchaseUrl());
        }

        AppPayMapper payInfo = userOrderLoaderClient.getUserAppPaidStatus(appKey, curUser.getId());
        if (payInfo == null || payInfo.unpaid()) {
            result.put("product_status", "unpaid");   // 未购买
        } else if (payInfo.isExpire()) {
            result.put("product_status", "expired");  // 已过期
            result.put("expire_time", payInfo.getExpireTime());
        } else {
            result.put("product_status", "active");
            result.put("expire_time", payInfo.getExpireTime());

            if (CollectionUtils.isEmpty(payInfo.getValidAppItems())) {
                result.put("valid_items", payInfo.getValidAppItems());
            }
        }

        //使用天数累计，暂时只有走美使用到这个字段，由于其他产品不一定存在天数说法
        if (OrderProductServiceType.UsaAdventure.name().equals(appKey)) {
            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(appKey, curUser.getId());
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
            result.put("total_payment_day", totalPaymentDay);
        }
        return result;
    }

    @Override
    public Boolean setUserYiQiXuePushTag(Long userId, Set<String> tags) {
        if (userId == null) {
            return Boolean.FALSE;
        }
        return yiQiXuePushTagCacheSystem.getYiQiXuePushTagCache().setUserYiQiXuePushTag(userId, tags);
    }

    @Override
    public Set<String> loadUserYiQiXuePushTag(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        return yiQiXuePushTagCacheSystem.getYiQiXuePushTagCache().loadUserYiQiXuePushTag(userId);
    }

    private Long decodeUserIdFromSessionKey(String key, String sessionKey) {
        try {
            String data = AesUtils.decryptHexString(key, sessionKey);
            return Long.valueOf(data.split(",")[0]);
        } catch (RuntimeException e) {
            return -1L;
        }
    }

    @Override
    public MapMessage isValidRequest(String appKey, String sessionKey, Map<String, String> request, String sig) {
        if (StringUtils.isBlank(appKey) || request == null) {
            return MapMessage.errorMessage("app key and request can not be null");
        }

        VendorApps requestApp = vendorLoader.loadVendor(appKey);
        if (requestApp == null) {
            return MapMessage.errorMessage("Unknown app key " + appKey);
        }

        Map<String, String> params = new HashMap<>();
        params.put("app_key", appKey);

        if (StringUtils.isNoneBlank(sessionKey)) {
            params.put("session_key", appKey);
        }

        for (String reqKey : request.keySet()) {
            params.put(reqKey, request.get(reqKey));
        }

        String reqSig = DigestSignUtils.signMd5(params, requestApp.getSecretKey());

        MapMessage retObj = new MapMessage();
        retObj.setSuccess(Objects.equals(reqSig, sig));

        return retObj;
    }

    @Override
    public MapMessage loadVendorApps(String appKey) {
        VendorApps vendorApps = vendorLoader.loadVendor(appKey);
        if (vendorApps != null) {
            return MapMessage.successMessage().add("secretKey", vendorApps.getSecretKey());
        }

        return MapMessage.errorMessage("Unknown app key " + appKey);
    }

    @Override
    public MapMessage updateUserAppSessionKey(String appKey, Long userId) {
        MapMessage message = vendorService.expireSessionKey(appKey, userId,
                OpenApiUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), userId));
        if (message.isSuccess()) {
            VendorAppsUserRef vendorAppsUserRef = vendorLoader.loadVendorAppUserRef(appKey, userId);
            if (vendorAppsUserRef != null) {
                return MapMessage.successMessage().add("sessionKey", vendorAppsUserRef.getSessionKey());
            }
        }
        return message;
    }

    /**
     * 用户有sessionKye的话直接获取，没有就重新生成返回
     *
     * @param appKey
     * @param userId
     * @return
     */
    @Override
    public MapMessage loadSessionKey(String appKey, Long userId) {
        MapMessage message = asyncVendorService.registerVendorAppUserRef(appKey, userId).getUninterruptibly();

        if (message.isSuccess()) {
            VendorAppsUserRef ref = (VendorAppsUserRef) message.get("ref");
            if (ref != null) {
                return MapMessage.successMessage().add("sessionKey", ref.getSessionKey());
            }
        }
        return message;
    }


}
