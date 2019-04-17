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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.FairylandService;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductRedirectType;
import com.voxlearning.utopia.service.vendor.api.constant.PopupTitle;
import com.voxlearning.utopia.service.vendor.api.constant.StudentFairylandMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentTabNoticeType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.mapper.FairylandProductUrl;
import com.voxlearning.utopia.service.vendor.impl.loader.VendorLoaderImpl;
import com.voxlearning.utopia.service.vendor.impl.persistence.FairylandProductPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.STUDENT_APP;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.APPS;
import static com.voxlearning.utopia.service.vendor.api.constant.StudentFairylandMessageType.COMMON_MSG;
import static com.voxlearning.utopia.service.vendor.api.constant.StudentFairylandMessageType.OPEN_APP_MSG;

/**
 * @author peng.zhang.a
 * @since 16-8-26
 */
@Named("com.voxlearning.utopia.service.vendor.impl.service.FairylandServiceImpl")
@Service(interfaceClass = FairylandService.class)
@ExposeServices({
        @ExposeService(interfaceClass = FairylandService.class, version = @ServiceVersion(version = "20181015")),
        @ExposeService(interfaceClass = FairylandService.class, version = @ServiceVersion(version = "20170215"))
})
@Deprecated
public class FairylandServiceImpl extends SpringContainerSupport implements FairylandService {

    @Inject private FairylandProductServiceImpl fairylandProductService;
    @Inject private FairylandProductPersistence fairylandProductPersistence;
    @Inject private VendorLoaderImpl vendorLoader;
    @Inject private VendorAppsServiceImpl vendorAppsService;
    @Inject private AppMessageLoaderClient appMessageLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private MessageLoaderClient messageLoaderClient;

    @Override
    public MapMessage updateFairylandProduct(Long id, FairylandProduct fairylandProduct) {
        if (id == 0 || fairylandProduct == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        fairylandProduct.setId(id);
        FairylandProduct modified = fairylandProductPersistence.replace(fairylandProduct);
        if (modified != null) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("更新数据失败");
        }
    }

    @Override
    public MapMessage insertFairylandProduct(FairylandProduct fairylandProduct) {
        if (fairylandProduct == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        try {
            fairylandProductPersistence.insert(fairylandProduct);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage("插入失败");
        }
    }

    @Override
    public MapMessage deleteFairylandProduct(Long id) {
        if (id == 0) {
            return MapMessage.errorMessage("id不能为空");
        }
        int delNum = fairylandProductPersistence.disable(id);
        if (delNum > 0) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("删除失败,id=" + id);
        }
    }

    @Override
    public MapMessage isExistNewMessageAndPopupTitle(StudentDetail studentDetail) {
        if (studentDetail == null || studentDetail.isInPaymentBlackListRegion() || studentDetail.getClazz() == null) {
            return MapMessage.successMessage();
        }
        long nowTime = System.currentTimeMillis();

        Set<String> validApps = vendorLoader.loadVendorAppsIncludeDisabled()
                .values()
                .stream()
                .filter(p -> !p.isDisabledTrue())
                .filter(p -> p.validateClazzLevel(studentDetail.getClazzLevel().getLevel()))
                .map(VendorApps::getAppKey)
                .collect(Collectors.toSet());
        Set<String> userMessageIds = messageLoaderClient.getMessageLoader().loadAppMessageLocations(studentDetail.getId())
                .stream()
                .filter(p -> SafeConverter.toLong(p.getExpiredTime(), 0) > nowTime)
                .filter(p -> StudentFairylandMessageType.of(p.getMessageType()) != null)
                .map(AppMessage.Location::getId)
                .collect(Collectors.toSet());
        List<AppMessage> userMessageDynamics = messageLoaderClient.getMessageLoader().loadAppMessageByIds(userMessageIds)
                .values()
                .stream()
                .filter(p -> p.getMessageType() == COMMON_MSG.type || validApps.contains(p.getExtInfo().getOrDefault("appKey", "")))
                .collect(Collectors.toList());

        Set<String> globalTagMsgIds = userMessageDynamics.stream()
                .filter(p -> SafeConverter.toLong(p.getExpiredTime(), 0) > nowTime
                        && StringUtils.isNotEmpty(p.getAppTagMsgId()))
                .map(AppMessage::getAppTagMsgId)
                .collect(Collectors.toSet());

        List<AppGlobalMessage> globalMessages = appMessageLoaderClient.findByMessageSource(AppMessageSource.FAIRYLAND.name())
                .stream()
                .filter(p -> SafeConverter.toLong(p.getExpiredTime(), 0) > nowTime)
                .filter(p -> StudentFairylandMessageType.of(p.getMessageType()) != null)
                .filter(p -> p.getMessageType() == COMMON_MSG.type || validApps.contains(p.getExtInfo().get("appKey")))
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .collect(Collectors.toList());

        AppGlobalMessage globalMessage;

        //是否有未过期的标签
        globalMessage = globalMessages.stream()
                .filter(p -> p.getPopupTitle() != null)
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .findFirst()
                .orElse(null);
        String popupTitleStr = userMessageDynamics.stream()
                .filter(p -> p.getPopupTitle() != null)
                .filter(p -> globalMessage == null || globalMessage.getCreateTime() < p.getCreateTime())
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .map(AppMessage::getPopupTitle)
                .findFirst()
                .orElse(globalMessage == null ? null : globalMessage.getPopupTitle());
        if (StringUtils.isNotBlank(popupTitleStr)) {
            return MapMessage.successMessage()
                    .set("noticeType", StudentTabNoticeType.TAG)
                    .set("tagType", PopupTitle.parse(popupTitleStr));
        }

        //判断是否有小红点
        boolean globalShowRedDot = globalMessages
                .stream()
                .filter(p -> !globalTagMsgIds.contains(p.getId()))
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .map(p -> true)
                .findFirst()
                .orElse(false);
        boolean userShowRedDot = userMessageDynamics.stream()
                .filter(p -> !p.getViewed())
                .map(p -> true)
                .findFirst()
                .orElse(false);
        if (globalShowRedDot || userShowRedDot) {
            return MapMessage.successMessage()
                    .set("noticeType", StudentTabNoticeType.RED_DOT)
                    .set("showRedDot", true);
        }
        return MapMessage.successMessage();
    }

    //学生app 自学tab 是否显示红点
    @Override
    public boolean isExistRedDot(StudentDetail studentDetail) {
        // crm配置新运营消息时红点显示，消息查看后或消息过期红点消失
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return false;
        }
        long nowTime = System.currentTimeMillis();
        Set<String> validApps = vendorLoader.loadVendorAppsIncludeDisabled()
                .values()
                .stream()
                .filter(p -> !p.isDisabledTrue())
                .filter(p -> p.validateClazzLevel(studentDetail.getClazzLevel().getLevel()))
                .map(VendorApps::getAppKey)
                .collect(Collectors.toSet());
        Set<String> userMessageIds = messageLoaderClient.getMessageLoader().loadAppMessageLocations(studentDetail.getId())
                .stream()
                .filter(p -> SafeConverter.toLong(p.getExpiredTime(), 0) > nowTime)
                .filter(p -> StudentFairylandMessageType.of(p.getMessageType()) != null)
                .map(AppMessage.Location::getId)
                .collect(Collectors.toSet());
        List<AppMessage> userMessageDynamics = messageLoaderClient.getMessageLoader().loadAppMessageByIds(userMessageIds)
                .values()
                .stream()
                .filter(p -> p.getMessageType() == COMMON_MSG.type || validApps.contains(p.getExtInfo().getOrDefault("appKey", "")))
                .collect(Collectors.toList());

        Set<String> globalTagMsgIds = userMessageDynamics.stream()
                .filter(p -> SafeConverter.toLong(p.getExpiredTime(), 0) > nowTime
                        && StringUtils.isNotEmpty(p.getAppTagMsgId()))
                .map(AppMessage::getAppTagMsgId)
                .collect(Collectors.toSet());

        List<AppGlobalMessage> globalMessages = appMessageLoaderClient.findByMessageSource(AppMessageSource.FAIRYLAND.name())
                .stream()
                .filter(p -> SafeConverter.toLong(p.getExpiredTime(), 0) > nowTime)
                .filter(p -> StudentFairylandMessageType.of(p.getMessageType()) != null)
                .filter(p -> p.getMessageType() == COMMON_MSG.type || validApps.contains(p.getExtInfo().get("appKey")))
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .collect(Collectors.toList());

        //判断是否有小红点
        boolean globalShowRedDot = globalMessages
                .stream()
                .filter(p -> !globalTagMsgIds.contains(p.getId()))
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .map(p -> true)
                .findFirst()
                .orElse(false);
        boolean userShowRedDot = userMessageDynamics.stream()
                .filter(p -> !p.getViewed())
                .map(p -> true)
                .findFirst()
                .orElse(false);
        return globalShowRedDot || userShowRedDot;
    }

    @Override
    public MapMessage saveAppMessage(List<Long> userIds,
                                     String title, String linkUrl, Integer linkType, StudentFairylandMessageType studentFairylandMessageType,
                                     Long expiredTime, PopupTitle popupTitle, String appKey, String content, Map<String, Object> extInfo) {
        List<AppMessage> list = new ArrayList<>();
        if (StringUtils.isEmpty(title) || studentFairylandMessageType == null || expiredTime <= 0) {
            return MapMessage.errorMessage();
        }
        if (extInfo == null) {
            extInfo = new HashMap<>();
        }
        extInfo.put("appKey", appKey);
        for (Long userId : userIds) {
            AppMessage appUserMessageDynamic = new AppMessage();
            appUserMessageDynamic.setTitle(title);
            appUserMessageDynamic.setExpiredTime(expiredTime);
            appUserMessageDynamic.setUserId(userId);
            appUserMessageDynamic.setViewed(false);
            appUserMessageDynamic.setPopupTitle(popupTitle == null ? null : popupTitle.name());
            appUserMessageDynamic.setExtInfo(extInfo);
            appUserMessageDynamic.setLinkType(linkType);
            appUserMessageDynamic.setLinkUrl(linkUrl);
            appUserMessageDynamic.setMessageType(studentFairylandMessageType.type);
            appUserMessageDynamic.setContent(content);
            list.add(appUserMessageDynamic);
        }
        list.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        return MapMessage.successMessage();
    }

    public MapMessage saveOpenAppMessage(List<Long> userIds, String title, Long expiredTime, PopupTitle popupTitle,
                                         String appKey, String content, Map<String, Object> extInfo) {
        List<AppMessage> list = new ArrayList<>();
        if (StringUtils.isEmpty(title) || expiredTime <= 0) {
            return MapMessage.errorMessage();
        }
        VendorApps vendorApps = vendorLoader.loadVendor(appKey);
        FairylandProduct fairylandProduct = fairylandProductService.getFairylandProductBuffer()
                .loadFairylandProducts(STUDENT_APP, APPS)
                .stream().filter(p -> appKey.equals(p.getAppKey())).findFirst().orElse(null);

        if (fairylandProduct == null || vendorApps == null) {
            return MapMessage.errorMessage();
        }
        if (extInfo == null) {
            extInfo = new HashMap<>();
        }
        extInfo.put("appKey", appKey);
        for (Long userId : userIds) {
            AppMessage appUserMessageDynamic = new AppMessage();
            appUserMessageDynamic.setTitle(title);
            appUserMessageDynamic.setExpiredTime(expiredTime);
            appUserMessageDynamic.setUserId(userId);
            appUserMessageDynamic.setViewed(false);
            appUserMessageDynamic.setPopupTitle(popupTitle == null ? null : popupTitle.name());
            appUserMessageDynamic.setExtInfo(extInfo);
            appUserMessageDynamic.setLinkType(1);
            Map<String, Object> params = new HashMap<>();
            params.put("appKey", fairylandProduct.getAppKey());
            params.put("platform", fairylandProduct.getPlatform());
            params.put("productType", fairylandProduct.getProductType());
            String linkUrl = UrlUtils.buildUrlQuery(FairylandProductRedirectType.MID_PAGE_URL, params);
            appUserMessageDynamic.setLinkUrl(linkUrl);
            appUserMessageDynamic.setMessageType(OPEN_APP_MSG.type);
            appUserMessageDynamic.setContent(content);
            list.add(appUserMessageDynamic);
        }
        list.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        return MapMessage.successMessage();
    }

    @Override
    public AlpsFuture<List<FairylandProductUrl>> fetchStudentAppFairylandProductUrl() {
        // 获取学生app课外乐园所有应用
        List<FairylandProduct> products = fairylandProductService.getFairylandProductBuffer()
                .loadFairylandProducts(STUDENT_APP, APPS);
        // 获取VendorApps

        Map<String, VendorApps> apps = vendorAppsService.getVendorAppsBuffer()
                .dump()
                .getVendorAppsList()
                .stream()
                .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity()));

        List<FairylandProductUrl> urls = new ArrayList<>();
        for (FairylandProduct product : products) {
            VendorApps app = apps.get(product.getAppKey());
            if (app == null) continue;
            FairylandProductUrl url = new FairylandProductUrl();
            url.setLaunchUrl(product.fetchRedirectUrl(RuntimeMode.current()));
            url.setAppKey(product.getAppKey());
            url.setOrientation(app.getOrientation());
            url.setBrowser(app.getBrowser());
            urls.add(url);
        }
        return new ValueWrapperFuture<>(urls);
    }
}
