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

package com.voxlearning.washington.controller.mobile.student.fairyland;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.message.client.AppMessageServiceClient;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AbtestMapper;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductStatus;
import com.voxlearning.utopia.service.message.api.constant.MessageSource;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.support.UserAbtestLoaderClientHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.Stem101;
import static com.voxlearning.utopia.api.constant.OrderProductServiceType.Walker;
import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.STUDENT_APP;
import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.STUDENT_PC;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.APPS;

/**
 * @author xinqiang.wang
 * @serial 2014/8/8
 */

@Controller
@RequestMapping("/student/fairyland")
public class StudentFairylandController extends AbstractApiController {

    @Inject private AppMessageServiceClient appMessageServiceClient;

    @Inject private VendorLoaderClient vendorLoaderClient;
    @Inject private FairylandLoaderClient fairylandLoaderClient;
    @Inject private UserAbtestLoaderClientHelper userAbtestLoaderClientHelper;

    // 2014暑期改版 -- 课外乐园首页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "studentv3/fairyland/index";
    }

    // 移动版课外乐园首页 (2016-07改版下线)
    //　2.7.0之前的版本，如果页面加载失败，读取壳中写死的默认页面才会调用这个请求
    @Deprecated
    @RequestMapping(value = "indexmobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index_mobile() {
        String version = getRequestString("version"); // 壳的版本号
        return MapMessage.successMessage().add("appsInfo", Collections.EMPTY_LIST);
    }
    
    @RequestMapping(value = "viewMessage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage viewMessage() {
        String id = getRequestString("id");
        String msgType = getRequestString("msgType");
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请登录");
        }
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(msgType)) {
            return MapMessage.errorMessage("传入参数不能为空");
        }
        try {
            MessageSource messageSource = MessageSource.valueOf(msgType);
            appMessageServiceClient.getAppMessageService().updateMessageViewed(id, studentDetail.getId(), messageSource.name());
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage("没有当前类型,msgType=" + msgType);
        }
    }

    // 移动版课外乐园首页 2016-07-11改版
    @RequestMapping(value = "pc/applist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage pcApplist() {
        StudentDetail studentDetail = currentStudentDetail();

        List<Map<String, Object>> appsInfo = new ArrayList<>();
        Map<String, AppPayMapper> appPaidStatus = new HashMap<>();
        Map<String, VendorApps> appsMap = new HashMap<>();
        List<String> availableServiceTypes;
        List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(STUDENT_PC, APPS);
        if (CollectionUtils.isNotEmpty(fairylandProducts)) {
            List<VendorApps> apps = businessVendorServiceClient.getStudentPcAvailableApps(studentDetail.getId());
            Set<String> appKeys = apps.stream().map(VendorApps::getAppKey).collect(Collectors.toSet());
            apps.stream().forEach(p -> appsMap.put(p.getAppKey(), p));

            fairylandProducts = fairylandProducts.stream()
                    .filter((p) -> (FairylandProductStatus.ONLINE.name().equals(p.getStatus())))
                    .filter((p) -> (!p.getProductType().equals(APPS.name()) || appKeys.contains(p.getAppKey())))
                    .collect(Collectors.toList());

            //获取有效的appKey列表
            availableServiceTypes = fairylandProducts.stream()
                    .filter(p -> p.getProductType().equals(APPS.name()))
                    .map(FairylandProduct::getAppKey)
                    .collect(Collectors.toList());

            //获取用户使用产品状态
            appPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(availableServiceTypes, studentDetail.getId(), false);
        }
        for (FairylandProduct fairylandProduct : fairylandProducts) {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("appKey", fairylandProduct.getAppKey());
            appInfo.put("productName", fairylandProduct.getProductName());
            appInfo.put("productDesc", fairylandProduct.getProductDesc());
            appInfo.put("operationMessage", fairylandProduct.getOperationMessage());
            appInfo.put("productIcon", fairylandProduct.getProductIcon());

            //app类型需要增加额外信息
            if (fairylandProduct.getProductType().equals(APPS.name())) {
                appInfo.put("launchUrl", fairylandProduct.fetchRedirectUrl(RuntimeMode.current()));
                //购买状态及购买：　0　未购买,1 购买过期,2正在使用
                int appStatus = 0;
                if (MapUtils.isNotEmpty(appPaidStatus) && appPaidStatus.containsKey(fairylandProduct.getAppKey())) {
                    appStatus = appPaidStatus.get(fairylandProduct.getAppKey()).getAppStatus();
                }
                appInfo.put("appStatus", appStatus);
            }

            // FIXME 沃克大冒险 and 趣味数学下线，非付费 or 有效期外用户不可见
            if (Walker.name().equals(fairylandProduct.getAppKey()) || Stem101.name().equals(fairylandProduct.getAppKey())) {
                AppPayMapper payMapper = appPaidStatus.get(fairylandProduct.getAppKey());
                if (payMapper == null || !payMapper.isActive()) {
                    continue;
                }
            }

            appsInfo.add(appInfo);
        }

        return MapMessage.successMessage().add("appInfo", appsInfo);
    }

    // 移动版课外乐园首页 2016-07-11改版
    @RequestMapping(value = "applist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage applist() {

        String version = getRequestString("version"); // 壳的版本号
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生账号错误");
        }
        // todo:abtest压力测试完成以后下线下面这段代码
        ///// abtest start///
        String experimentId = "583be5bd6cdb8a312cb0354a";
        try {
            AbtestMapper abtestMapper = userAbtestLoaderClientHelper.generateUserAbtestInfo(studentDetail.getId(), experimentId);
            Map<String, String> logInfo = new HashMap<>();
            logInfo.put("module", "abtest");
            logInfo.put("op", "abtest_at_wechat_ucenter_page");
            logInfo.put("experimentId", abtestMapper.getExperimentId());
            logInfo.put("groupId", abtestMapper.getGroupId());
            logInfo.put("groupName", abtestMapper.getGroupName());
            logInfo.put("planId", abtestMapper.getPlanId());
            logInfo.put("planName", abtestMapper.getPlanName());
            logInfo.put("userId", SafeConverter.toString(studentDetail.getId()));
            LogCollector.info("backend-general", logInfo);
        } catch (Exception ex) {
            logger.info("generate abtest user info failed,{}/{}", experimentId, studentDetail.getId(), ex);
        }

        ///// abtest end///

        FlightRecorder.dot("beforeLoadFairylandProducts");
        List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(STUDENT_APP, null);
        FlightRecorder.dot("afterLoadFairylandProducts");
        List<Map<String, Object>> appsInfo = new ArrayList<>();
        Map<String, AppPayMapper> appPaidStatus = null;
        Map<String, VendorApps> appsMap = new HashMap<>();
        Map<String, String> useAppNumDesc;
        List<String> availableServiceTypes = new ArrayList<>();

        //过滤apps
        if (CollectionUtils.isNotEmpty(fairylandProducts)) {
            FlightRecorder.dot("beforeGetStudentMobileAvailableApps");
            List<VendorApps> apps = businessVendorServiceClient.getStudentMobileAvailableApps(studentDetail, version, getAppSystemType());
            FlightRecorder.dot("afterGetStudentMobileAvailableApps");
            Set<String> appKeys = apps.stream().map(VendorApps::getAppKey).collect(Collectors.toSet());
            apps.stream().forEach(p -> appsMap.put(p.getAppKey(), p));

            fairylandProducts = fairylandProducts.stream()
                    .filter((p) -> (FairylandProductStatus.ONLINE.name().equals(p.getStatus())))
                    .filter((p) -> appKeys.contains(p.getAppKey()))
                    .collect(Collectors.toList());

            //获取有效的appKey列表
            availableServiceTypes = fairylandProducts.stream()
                    .map(FairylandProduct::getAppKey)
                    .collect(Collectors.toList());

            //获取用户使用产品状态
            FlightRecorder.dot("beforeGetUserAppPaidStatus");
            appPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(availableServiceTypes, studentDetail.getId(), false);
            FlightRecorder.dot("afterGetUserAppPaidStatus");

        }
        FlightRecorder.dot("beforeFetchUserUseNumDesc");
        useAppNumDesc = businessVendorServiceClient.fetchUserUseNumDesc(availableServiceTypes, studentDetail);
        FlightRecorder.dot("afterFetchUserUseNumDesc");
        for (FairylandProduct fairylandProduct : fairylandProducts) {
            //杭州市显示点读机#41875
            if (studentDetail.getCityCode() != 330100 && OrderProductServiceType.PicListenBook.name().equals(fairylandProduct.getAppKey())) {
                continue;
            }
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("appKey", fairylandProduct.getAppKey());
            appInfo.put("productName", fairylandProduct.getProductName());
            appInfo.put("productDesc", fairylandProduct.getProductDesc());
            appInfo.put("backgroundImage", fairylandProduct.getBackgroundImage());
            appInfo.put("productIcon", fairylandProduct.getProductIcon());
            appInfo.put("operationMessage", useAppNumDesc.getOrDefault(fairylandProduct.getAppKey(), null));
            appInfo.put("hotFlag", fairylandProduct.getHotFlag());
            appInfo.put("newFlag", fairylandProduct.getNewFlag());
            appInfo.put("recommendFlag", fairylandProduct.getRecommendFlag());
            appInfo.put("catalogDesc", fairylandProduct.getCatalogDesc());
            //app类型需要增加额外信息
            if (fairylandProduct.getProductType().equals(APPS.name())) {

                appInfo.put("launchUrl", fairylandProduct.fetchRedirectUrl(RuntimeMode.current()));
                //浏览器内核与支持屏幕转向
                if (appsMap.containsKey(fairylandProduct.getAppKey())) {
                    appInfo.put("orientation", appsMap.get(fairylandProduct.getAppKey()).getOrientation());
                    appInfo.put("browser", appsMap.get(fairylandProduct.getAppKey()).getBrowser());
                }
            }
            //购买状态及购买开通人数：　0　未购买,1 购买过期,2正在使用
            int appStatus = 0;
            if (MapUtils.isNotEmpty(appPaidStatus)
                    && appPaidStatus.containsKey(fairylandProduct.getAppKey())) {
                appStatus = appPaidStatus.get(fairylandProduct.getAppKey()).getAppStatus();
            }
            appInfo.put("appStatus", appStatus);
            appsInfo.add(appInfo);
        }

        //获取顶部消息列表
        MapMessage result = MapMessage.successMessage();
        result.set("appsInfo", appsInfo);

        try {
            result.set("appUserMessages", new ArrayList<>());
        } catch (Exception e) {
            logger.error("loadMessage error");
        }

        return result;
    }

    // 2014暑期改版 -- 课外乐园 -- 获取三个同班，同校，全国的购买了某个应用的用户头像以及学豆
    @RequestMapping(value = "appdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage appDetail() {
        StudentDetail student = currentStudentDetail();

        OrderProductServiceType type;
        try {
            type = OrderProductServiceType.valueOf(getRequestString("appType"));
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }

        List<Long> studentIds = washingtonCacheSystem.CBS.flushable
                .wrapCache(userOrderServiceClient)
                .keys(student.getId(), type.name())
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .buyInfo(student, type);

        List<Map<String, Object>> studentList = new ArrayList<>();
        Map<String, Object> self = new HashMap<>();
        self.put("studentId", student.getId());
        self.put("studentName", student.fetchRealname());
        self.put("studentImg", student.fetchImageUrl());
        studentList.add(0, self);

        if (CollectionUtils.isNotEmpty(studentIds)) {
            Map<Long, User> users = userLoaderClient.loadUsers(studentIds);
            for (Long studentId : studentIds) {
                User user = users.get(studentId);
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", studentId);
                map.put("studentName", user == null ? studentId : user.fetchRealnameIfBlankId());
                map.put("studentImg", user == null ? "" : user.fetchImageUrl());
                studentList.add(map);
            }
        }

        return MapMessage.successMessage().add("studentList", studentList);
    }

    @RequestMapping(value = "userappinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserAppOpenInfo() {
        String appKey = getRequestString("appKey");
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请登录");
        }
        if (StringUtils.isBlank(appKey)) {
            return MapMessage.errorMessage("无效的APP KEY!");
        }

        VendorApps app = vendorLoaderClient.getExtension().loadVendorApp(appKey);

        Set<String> availableApps = businessVendorServiceClient.getStudentMobileAvailableApps(studentDetail, null, null)
                .stream()
                .map(VendorApps::getAppKey)
                .collect(Collectors.toSet());
        if (!availableApps.contains(app.getAppKey())) {
            return MapMessage.errorMessage("无效的APP KEY!");
        }

        Map<String, AppPayMapper> userAppPaidInfo = userOrderLoaderClient.getUserAppPaidStatus(Collections.singletonList(appKey), currentUserId(), false);
        AppPayMapper appStatus = userAppPaidInfo.get(appKey);
        Map<String, Object> appPayInfo = new HashMap<>();
        if (appStatus == null) {
            appPayInfo.put("appStatus", 0);//从未购买
        } else {
            appPayInfo.put("appStatus", appStatus.getAppStatus());
        }

        // load App Hwcoin Paid Status
        Integer orderCount = vendorLoaderClient.getUserPaidHwcoinOrderCount(app.getAppKey(), currentUserId());
        if (orderCount != null && orderCount > 0) {
            appPayInfo.put("inAppPurchase", true);
        } else {
            appPayInfo.put("inAppPurchase", false);
        }

        appPayInfo.put("appKey", app.getAppKey());
        appPayInfo.put("appName", app.getCname());
        appPayInfo.put("appIcon", app.getAppIcon());
        appPayInfo.put("appUrl", app.getAppUrl());

        return MapMessage.successMessage().add("appInfo", appPayInfo);
    }

    /**
     * 小鹰直播入口
     */
    @RequestMapping(value = "/live/index.vpage", method = RequestMethod.GET)
    public String live(Model model) {
        return "studentv3/fairyland/live/index";
    }
}
