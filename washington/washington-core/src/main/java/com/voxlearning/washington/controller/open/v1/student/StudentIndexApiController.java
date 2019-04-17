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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.WarmHeartPlanConstant;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryLoaderClient;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardStatus;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardLogCacheWrapper;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCache;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardHelper;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.PopupTitle;
import com.voxlearning.utopia.service.vendor.api.constant.StudentTabNoticeType;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandServiceClient;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.controller.open.v1.util.AppHomeworkCardFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;

/**
 * Student index controller
 * Created by Shuai Huan on 2015/9/17
 */
@Controller
@RequestMapping(value = "/v1/student")
@Slf4j
public class StudentIndexApiController extends AbstractStudentApiController {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private FairylandServiceClient fairylandServiceClient;
    @Inject private AncientPoetryLoaderClient ancientPoetryLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = WarmHeartPlanService.class)
    private WarmHeartPlanService warmHeartPlanService;

    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {

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

        StudentDetail studentDetail = getCurrentStudentDetail();

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isFreezing() || studentExtAttribute.isForbidden()) {
                //账号冻结的话会要求验证身份，所以这里把手机号返回
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
                return resultMap;
            }
        }

        if (studentDetail.getClazz() == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            } else {
                resultMap.add(RES_CLAZZ_ID, 0);
                resultMap.add(RES_HOMEWORK_CARD_LIST, Collections.emptyList());
                resultMap.add(RES_TAB_LIST, getAppTabList());
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            }
            return resultMap;
        }
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        String ua = getRequest().getHeader("User-Agent");

        Map<String, Object> studentAppIndexData = businessStudentServiceClient.loadStudentAppIndexData(getCurrentStudentDetail(), ver, sys);
        Map<String, Object> configMap = AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator());
        List<Map<String, Object>> homeworkCards = buildHomeworkCardList(studentAppIndexData, configMap);
        if (CollectionUtils.isEmpty(homeworkCards) && VersionUtil.compareVersion(ver, "1.9.5.0") >= 0 && VersionUtil.compareVersion(ver, "2.7.0") < 0) {
            if (asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                    .StudentAppDoHomeworkRecordCacheManager_hasDoneHomework(studentDetail.getId())
                    .take()) {
                CollectionUtils.addNonNullElement(homeworkCards, allFinishedHomeworkCard(studentDetail.getId()));
            }
        }
        resultMap.add(RES_INTEGRAL, studentDetail.getUserIntegral().getUsable());
        resultMap.add(RES_CLAZZ_ID, studentDetail.getClazz().getId());
        resultMap.add(RES_TAB_LIST, getAppTabList());
        resultMap.add(RES_HOMEWORK_CARD_LIST, homeworkCards);
        resultMap.add(RES_ONLINE_CSM, "onlinecsm.17zuoye.com");
        String notSupportUrl = "resources/apps/hwh5/homework/" + generateBigVersion(ver, studentDetail) + "/LinkToDownload/index.html";
        resultMap.add(RES_NOT_SUPPORT_HOMEWORK_LINK, generateVersionUrl(notSupportUrl));

        Map<String, Object> ocrMentalConfig = new LinkedHashMap<>();
        String imageWidthStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "OCR_MENTAL_IMAGE_WIDTH");
        String imageQualityStr = newHomeworkContentServiceClient.loadImageQualityStr(studentDetail);
        String imageBrightThStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "OCR_MENTAL_IMAGE_BRIGHT_TH");
        String imageDarkThStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "OCR_MENTAL_IMAGE_DARK_TH");
        String imageClearThStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "OCR_MENTAL_IMAGE_CLEAR_TH");
        int imageWidth = SafeConverter.toInt(imageWidthStr, 1080);
        float imageQuality = SafeConverter.toFloat(imageQualityStr, 0.8f);
        int imageBrightTh = SafeConverter.toInt(imageBrightThStr, 110);
        int imageDarkTh = SafeConverter.toInt(imageDarkThStr, -110);
        float imageClearTh = SafeConverter.toFloat(imageClearThStr, 3.5f);
        ocrMentalConfig.put(RES_OCR_MENTAL_IMAGE_WIDTH, imageWidth);
        ocrMentalConfig.put(RES_OCR_MENTAL_IMAGE_QUALITY, imageQuality);
        ocrMentalConfig.put(RES_OCR_MENTAL_IMAGE_BRIGHT_TH, imageBrightTh);
        ocrMentalConfig.put(RES_OCR_MENTAL_IMAGE_DARK_TH, imageDarkTh);
        ocrMentalConfig.put(RES_OCR_MENTAL_IMAGE_CLEAR_TH, imageClearTh);

        String slotId = "321201";
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(studentDetail.getId(), slotId, getRequestString(REQ_SYS), ver);
        List<Map<String, Object>> adMapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(newAdMappers)) {
            int index = 0;
            for (NewAdMapper p : newAdMappers) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("img", combineCdbUrl(p.getImg()));
                item.put("url", StringUtils.isEmpty(p.getUrl()) ? "" : AdvertiseRedirectUtils.redirectUrl(p.getId(), index, ver, sys, "", studentDetail.getId()));
                adMapperList.add(item);
                //曝光打点
                if (Boolean.TRUE.equals(p.getLogCollected())) {
                    LogCollector.info("sys_new_ad_show_logs",
                            MapUtils.map(
                                    "user_id", studentDetail.getId(),
                                    "env", RuntimeMode.getCurrentStage(),
                                    "version", ver,
                                    "aid", p.getId(),
                                    "acode", SafeConverter.toString(p.getCode()),
                                    "index", index,
                                    "slotId", slotId,
                                    "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                    "time", DateUtils.dateToString(new Date()),
                                    "agent", ua,
                                    "system", sys
                            ));
                }
                index++;
            }
        }
        ocrMentalConfig.put(RES_OCR_MENTAL_AD_LIST, adMapperList);
        ocrMentalConfig.put(RES_OCR_MENTAL_SHOW_PARENT_LINK, grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "OcrMental", "ShowParentLink"));
        ocrMentalConfig.put(RES_OCR_MENTAL_PARENT_LINK_TEXT, "打开\"一起学（原家长通）\"APP，也能拍照检查！还能打卡拿奖励！GO！");
        ocrMentalConfig.put(RES_OCR_MENTAL_PARENT_LINK_URL, "https://www.17zuoye.com/view/mobile/common/download?app_type=17parent");

        resultMap.add(RES_OCR_MENTAL_CONFIG, ocrMentalConfig);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildHomeworkCardList(Map<String, Object> studentAppIndexData, Map<String, Object> configMap) {
        if (MapUtils.isEmpty(studentAppIndexData)) return Collections.emptyList();
        List<Map<String, Object>> homeworkCardList = new LinkedList<>();
        List<Map<String, Object>> enterableNewExamCards = (List<Map<String, Object>>) studentAppIndexData.get("enterableNewExamCards");
        if (CollectionUtils.isNotEmpty(enterableNewExamCards)) {
            enterableNewExamCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, false)));
        }
        List<Map<String, Object>> homeworkCards = (List<Map<String, Object>>) studentAppIndexData.get("homeworkCards");
        if (CollectionUtils.isNotEmpty(homeworkCards)) {
            homeworkCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, false)));
        }
        List<Map<String, Object>> correctionHomeworkCards = (List<Map<String, Object>>) studentAppIndexData.get("correctionHomeworkCards");
        if (CollectionUtils.isNotEmpty(correctionHomeworkCards)) {
            correctionHomeworkCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, false)));
        }
        List<Map<String, Object>> makeUpHomeworkCards = (List<Map<String, Object>>) studentAppIndexData.get("makeUpHomeworkCards");
        if (CollectionUtils.isNotEmpty(makeUpHomeworkCards)) {
            makeUpHomeworkCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, true)));
        }
        List<Map<String, Object>> basicReviewHomeworkCards = (List<Map<String, Object>>) studentAppIndexData.get("basicReviewHomeworkCards");
        if (CollectionUtils.isNotEmpty(basicReviewHomeworkCards)) {
            basicReviewHomeworkCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, false)));
        }
        List<Map<String, Object>> enterableUnitTestCards = (List<Map<String, Object>>) studentAppIndexData.get("enterableUnitTestCards");
        if (CollectionUtils.isNotEmpty(enterableUnitTestCards)) {
            enterableUnitTestCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, false)));
        }
        List<Map<String, Object>> ancientPoetryActivityCards = (List<Map<String, Object>>) studentAppIndexData.get("ancientPoetryActivityCards");
        if (CollectionUtils.isNotEmpty(ancientPoetryActivityCards)) {
            ancientPoetryActivityCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, true)));
        }
        List<Map<String, Object>> outsideReadingCards = (List<Map<String, Object>>) studentAppIndexData.get("outsideReadingCards");
        if (CollectionUtils.isNotEmpty(outsideReadingCards)) {
            outsideReadingCards.forEach(e -> homeworkCardList.add(convertData(e, configMap, true)));
        }
        return homeworkCardList;
    }

    private Map<String, Object> convertData(Map<String, Object> data, Map<String, Object> configMap, boolean makeUpFlag) {
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        String homeworkType = SafeConverter.toString(data.get("homeworkType"));
        List<String> types = JsonUtils.fromJsonToList(JsonUtils.toJson(data.get("types")), String.class);
        String desc = SafeConverter.toString(data.get("desc"));
        Map<String, Object> homeworkCardMap = new HashMap<>();
        homeworkCardMap.put(RES_HOMEWORK_CARD_TYPE, homeworkType);
        homeworkCardMap.put(RES_HOMEWORK_CARD_DESC, desc);
        homeworkCardMap.put(RES_HOMEWORK_MAKE_UP_FLAG, makeUpFlag);
        homeworkCardMap.put(RES_HOMEWORK_ID, data.get("homeworkId"));
        homeworkCardMap.put(RES_HOMEWORK_END_DATE, data.get("endDate"));
        homeworkCardMap.put(RES_HOMEWORK_START_COMMENT, data.get("startComment"));
        if (data.get("finishCount") != null) {
            homeworkCardMap.put(RES_HOMEWORK_FINISH_COUNT, data.get("finishCount"));
        }
        if (data.get("homeworkCount") != null) {
            homeworkCardMap.put(RES_HOMEWORK_COUNT, data.get("homeworkCount"));
        }
        if (data.get("url") != null) {
            homeworkCardMap.put(RES_URL, data.get("url"));
        }
        if (data.get("params") != null) {
            homeworkCardMap.put(RES_PARAMS, data.get("params"));
        }
        if (data.get("initParams") != null) {
            homeworkCardMap.put(RES_INIT_PARAMS, data.get("initParams"));
        }
        if (data.get("endDateStr") != null) {
            homeworkCardMap.put(RES_HOMEWORK_END_DATE_STR, data.get("endDateStr"));
        }
        // 配置中命中了：此app的版本支持当前作业类型
        AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(configMap, homeworkType, types, ver, sys);
        boolean supported = info.getSupportType() == AppHomeworkCardFilter.HomeworkCardSupportType.SUPPORTED;
        homeworkCardMap.put(RES_HOMEWORK_CARD_SUPPORT_FLAG, supported);
        if (!supported && info.getSupportType() != null) {
            homeworkCardMap.put(RES_NOT_SUPPORT_HOMEWORK_LINK_PARAM, buildNotSupportParams(SafeConverter.toString(data.get("homeworkId")), homeworkType, desc, info.getSupportType().name(), info.getNoSupportObjectiveConfigType()));
        }
        homeworkCardMap.put(RES_HOMEWORK_CARD_SOURCE, info.getSourceType());
        homeworkCardMap.put(RES_HOMEWORK_CARD_VARIETY, info.getHomeworkOrQuiz());

        return homeworkCardMap;
    }

    @RequestMapping(value = "/homeworkcard.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkCard() {

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

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            resultMap.add(RES_CLAZZ_ID, 0);
            resultMap.add(RES_HOMEWORK_CARD_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        Map<String, Object> studentAppIndexData = businessStudentServiceClient.loadStudentIndexDataForSpg(getCurrentStudentDetail());
        List<Map<String, Object>> homeworkCards = buildHomeworkCardList(studentAppIndexData, Collections.emptyMap());

        resultMap.add(RES_HOMEWORK_CARD_LIST, homeworkCards);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    // 是否提示下载家长通（17Parent）
    @RequestMapping(value = "/checkjzt.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage checkJzt() {

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

        StudentDetail studentDetail = getCurrentStudentDetail();
        // 如果中灰度区域并且没绑定家长,演示账户3921029不能出现提示下载家长通
        boolean isHit = false;
        if (studentDetail.getId() != 3921029L && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "17Parent", "spread")) {
            // 灰度范围之内，孩子没有绑定家长的用户并且7*24小时之内没看过此卡片者，才可以看到强绑家长通的卡片
            if (CollectionUtils.isEmpty(studentLoaderClient.loadStudentParentRefs(studentDetail.getId()))) {
                if (!asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                        .StudentCheckJztCacheManager_hasRecord(studentDetail.getId())
                        .take()) {
                    isHit = true;
                    // 返回卡片显示文案和跳转链接
                    resultMap.add(RES_HTML5_URL, "/view/mobile/student/primary/open_parentApp_guide.vpage");
                    resultMap.add(RES_TITLE, "致新同学的信");
                    // 记录已经看到卡片了
                    asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                            .StudentCheckJztCacheManager_record(studentDetail.getId())
                            .awaitUninterruptibly();
                }
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SHOW_JZT, isHit);
        return resultMap;
    }

    @RequestMapping(value = "/gettablist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getTabList() {
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
        StudentDetail studentDetail = getCurrentStudentDetail();
        List<Map<String, Object>> function_list = generateFunctionList(studentDetail);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap.add("function_list", function_list);
    }

    // 获取扩展tab接口
    @RequestMapping(value = "/getexttablist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getExtTabList() {
        MapMessage resultMap = new MapMessage();
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        AppSystemType appSystemType = getAppSystemType();

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

        List<Map<String, Object>> tabList = new ArrayList<>();
        StudentDetail studentDetail = getCurrentStudentDetail();
        // 自学tab显示条件， 白名单  or  购买过产品
        boolean hasPaid = false;
        Set<String> availableApps = businessVendorServiceClient.getStudentMobileAvailableApps(studentDetail, version, appSystemType)
                .stream()
                .map(VendorApps::getAppKey)
                .collect(Collectors.toSet());
        Map<String, AppPayMapper> userAppPaidInfo = userOrderLoaderClient.getUserAppPaidStatus(new ArrayList<>(availableApps), currentUserId(), false);
        for (AppPayMapper mapper : userAppPaidInfo.values()) {
            if (mapper.hasPaid()) {
                hasPaid = true;
                break;
            }
        }

        // 苹果审核账号不显示任务
        if (studentDetail != null && studentDetail.getId() != 3921029) {
            if (VersionUtil.compareVersion(version, "2.8.6.0") >= 0) {
                Map<String, Object> zxTab = new HashMap<>();
                zxTab.put("tab_key", "task");
                zxTab.put("tab_name", "任务");
                zxTab.put("tab_url", "/view/mobile/student/wonderland/index_new");
                zxTab.put("tab_img", "");
                zxTab.put("tab_focus_img", "");
                // zxTab.put("tab_focus", fairylandServiceClient.isExistRedDot(studentDetail)); // 是否有红点
                tabList.add(zxTab);

            } else if (!studentDetail.isInPaymentBlackListRegion() || hasPaid) {
                // tab_key tab_name tab_url, tab_img, tab_focus
                Map<String, Object> zxTab = new HashMap<>();
                zxTab.put("tab_key", "self_study");
                zxTab.put("tab_name", "成长");
                zxTab.put("tab_url", "/view/mobile/student/wonderland/index");
                zxTab.put("tab_img", "");
                zxTab.put("tab_focus_img", "");
                zxTab.put("tab_focus", fairylandServiceClient.isExistRedDot(studentDetail)); // 是否有红点
                tabList.add(zxTab);
            }
        }

        //地区灰度
        boolean isWhiteList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                "IndependentOcrRecognition", "WhiteList");
        //独立拍照检查入口( version>=3.1.5.0 && 地区灰度)
        if (VersionUtil.compareVersion(version, "3.1.5.0") >= 0 && isWhiteList) {
            boolean openTabBook = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                    "IndependentOcrRecognition", "TabBook");
            Map<String, Object> zxTab = new HashMap<>();
            zxTab.put("tab_key", openTabBook  && VersionUtil.compareVersion(version, "3.1.8.0") >= 0 ? "tab_book" : "tab_paper"); //拍照检查=（tab_paper/tab_book）tab_book教辅， tab_paper口算，AI识别做业务区分，对客户端来说只是展示的气泡不同
            zxTab.put("tab_name", "拍照检查");
            zxTab.put("tab_url", "");
            zxTab.put("tab_img", "");
            zxTab.put("tab_focus_img", "");
            tabList.add(zxTab);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("ext_tab_list", tabList);
        return resultMap;
    }

    // 获取红点接口
    @RequestMapping(value = "/getreddot.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getRedDot() {
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
        Map<String, Object> redDotMap = new HashMap<>();
        StudentDetail studentDetail = getCurrentStudentDetail();
        // 自学tab红点
        redDotMap.put("self_study", fairylandServiceClient.isExistRedDot(studentDetail));
        // 我的任务红点
        redDotMap.put("task", false);//todo 等待阿包提供接口
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("red_dot_map", redDotMap);
        return resultMap;
    }

    private List<Map<String, Object>> generateFunctionList(StudentDetail studentDetail) {
        String nativeVer = getRequestString(REQ_APP_NATIVE_VERSION);

        List<Map<String, Object>> function_list = new ArrayList<>();
        //获取配置信息
        Map<String, Object> configMap = getNewTabListConfig(studentDetail);
        if (MapUtils.isEmpty(configMap)) {
            return function_list;
        }

        for (Map.Entry<String, Object> tabEntrySet : configMap.entrySet()) {
            String tabName = tabEntrySet.getKey();
            if (tabEntrySet.getValue() != null) {
                Map<String, Object> detailMap = (Map<String, Object>) tabEntrySet.getValue();
                String startVer = SafeConverter.toString(detailMap.get("startVer"));
                String endVer = SafeConverter.toString(detailMap.get("endVer"));
                //如果没有配置startVer，或者当前版本小于配置的startVer， 就跳过这个tab
                if (StringUtils.isBlank(startVer) || VersionUtil.compareVersion(nativeVer, startVer) < 0) {
                    continue;
                }
                //如果配置了endVer，并且当前版本大于配置的endVer，跳过这个tab
                if (StringUtils.isNotBlank(endVer) && VersionUtil.compareVersion(nativeVer, endVer) > 0) {
                    continue;
                }

                Map<String, Object> tabMap = new HashMap<>();
                //tab名（exp：自学乐园）
                tabMap.put("function_name", SafeConverter.toString(detailMap.get("tabName"), ""));
                //tab type （0-自学乐园， 1-作业记录， 2-速算）
                tabMap.put("function_special", SafeConverter.toInt(detailMap.get("tabType")));
                //功能类型：(Native\Web)
                tabMap.put("function_type", SafeConverter.toString(detailMap.get("functionType"), ""));
                //跳转url
                tabMap.put("function_url", SafeConverter.toString(detailMap.get("functionUrl"), ""));
                //图片地址
                tabMap.put("function_image", getCdnBaseUrlStaticSharedWithSep() + SafeConverter.toString(detailMap.get("imgUrl"), ""));

                Map<String, Object> function_target = new HashMap<>();
                //自学乐园从接口返回数据，其他tab走配置
                if ("FAIRYLAND".equals(tabName)) {
                    MapMessage mapMessage = fairylandServiceClient.isExistNewMessageAndPopupTitle(studentDetail);
                    generateNoticeInfoMap(mapMessage, function_target);
                    tabMap.put("function_target", function_target);
                    function_list.add(tabMap);
                } else {
                    generatePromotionInfo(detailMap, tabMap);
                    function_list.add(tabMap);
                }

            }
        }

        // 争取不影响原有逻辑
        try {
            if (VersionUtil.compareVersion(nativeVer, "2.8.6.0") > 0) {
                boolean available = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                        "Student", "warm_heart");

                // 灰度没开的话再去查老师布置状态
                Boolean assignStatus = false;
                if (!available) {
                    assignStatus = warmHeartPlanService.loadTeacherAssignStatus(studentDetail.getId());
                }

                if (available || assignStatus) {
                    Map<String, Object> tabMap = new HashMap<>();
                    tabMap.put("function_special", 6);
                    tabMap.put("function_name", WarmHeartPlanConstant.TEACHER_APP_ICON_NAME);
                    tabMap.put("function_image", WarmHeartPlanConstant.TEACHER_APP_ICON_IMAGE);
                    tabMap.put("function_url", WarmHeartPlanConstant.STUDENT_INDEX_PAGE);
                    tabMap.put("function_type", "Web");
                    tabMap.put("function_target", new Object());
                    function_list.add(tabMap);
                }
            }

            // 古诗活动增加小岛
            boolean poetryActivityGray = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                    "ancient_poetry", "activity");
            if (poetryActivityGray) {
                GroupMapper groupMapper = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false)
                        .stream()
                        .filter(group -> group.getSubject().equals(Subject.CHINESE))
                        .findFirst()
                        .orElse(null);
                if (groupMapper != null) {
                    MapMessage message = ancientPoetryLoaderClient.fetchGroupActivityList(groupMapper.getId(), Boolean.TRUE, studentDetail.getId());
                    if (message.isSuccess()) {
                        List<Map<String, Object>> result = (List<Map<String, Object>>) message.get("result");
                        if (CollectionUtils.isNotEmpty(result)) {
                            String activityId = null;
                            for (Map<String, Object> resultMap : result) {
                                boolean passed = resultMap.get("passed") instanceof Boolean && (boolean) resultMap.get("passed");
                                if (!passed) {
                                    activityId = SafeConverter.toString(resultMap.get("activityId"));
                                    break;
                                }
                            }
                            if (activityId == null) {
                                activityId = SafeConverter.toString(result.get(0).get("activityId"));
                            }

                            Map<String, Object> tabMap = new HashMap<>();
                            tabMap.put("function_special", 7);
                            tabMap.put("function_image", "https://oss-image.17zuoye.com/gushi/2019/03/20/20190320143559426094.png");
                            tabMap.put("function_url", UrlUtils.buildUrlQuery("/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/middle-page/transit.vhtml", MapUtils.m("redirectUrl", UrlUtils.buildUrlQuery(NewHomeworkConstants.STUDENT_ANCIENT_POETRY_ACTIVITY_URL, MapUtils.m("activityId", activityId, "fullscreen", true)))));
                            tabMap.put("function_name", "诗词大会");
                            tabMap.put("function_type", "Web");
                            tabMap.put("function_target", new Object());
                            function_list.add(tabMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (RuntimeMode.isUsingTestData()) {
                log.error(e.getMessage(), e);
            }
        }
        return function_list;
    }

    private void generatePromotionInfo(Map<String, Object> detailMap, Map<String, Object> tabMap) {
        Map<String, Object> function_target = new HashMap<>();
        if (detailMap == null) {
            return;
        }
        generateNoticeInfoMap(detailMap, function_target);
        tabMap.put("function_target", function_target);
    }

    //生成提醒信息
    private void generateNoticeInfoMap(Map<String, Object> messageMap, Map<String, Object> function_target) {
        StudentTabNoticeType noticeType = StudentTabNoticeType.parse(SafeConverter.toString(messageMap.get("noticeType")));
        switch (noticeType) {
            case RED_DOT:
                function_target.put("function_alert_type", noticeType.getType());
                function_target.put("function_alert_type_red_dot", SafeConverter.toBoolean(messageMap.get("showRedDot")));
                break;
            case TAG:
                //标签需要配置noticeType(TAG)、tagType(NEW、HOT、RECOMMEND)
                PopupTitle tagType = PopupTitle.parse(SafeConverter.toString(messageMap.get("tagType")));
                if (tagType != PopupTitle.UNKNOWN) {
                    Map<String, Object> function_alert_type_target = new HashMap<>();
                    if (tagType.getTagInfo()) {
                        function_alert_type_target.put("target_info", tagType.getDesc());
                    }
                    if (tagType == PopupTitle.PARENT_REWARD) {
                        String img = generateParentRewardImg();
                        if (StringUtils.isNotBlank(img))
                            function_alert_type_target.put("target_image", img);
                    } else {
                        function_alert_type_target.put("target_image", getCdnBaseUrlStaticSharedWithSep() + tagType.getImgUrl());
                    }
                    if (MapUtils.isNotEmpty(function_alert_type_target)) {
                        function_target.put("function_alert_type", noticeType.getType());
                        function_target.put("function_alert_type_target", function_alert_type_target);
                    }
                }
                break;
            case NUMBER:
                function_target.put("function_alert_type", noticeType.getType());
                Map<String, Object> function_alert_type_number = new HashMap<>();
                function_alert_type_number.put("number_info", SafeConverter.toString(messageMap.get("numberInfo"), ""));
                function_alert_type_number.put("number_image", getCdnBaseUrlStaticSharedWithSep() + noticeType.getImgUrl());
                function_target.put("function_alert_type_number", function_alert_type_number);
                break;
        }
    }

    private String generateParentRewardImg() {
        String rewardNeedSend = "/public/skin/parentMobile/images/new_icon/reward_need_send.png";
        String rewardNeedReceive = "/public/skin/parentMobile/images/new_icon/reward_need_receive.png";
        String letterUnread = "/public/skin/parentMobile/images/new_icon/letter_unread.png";
        Long studentId = getCurrentStudent().getId();

        int sendCount;
        String sendCacheKey = ParentRewardHelper.sendCacheKey(studentId);
        CacheObject<Object> sendList = ParentRewardCache.getPersistenceCache().get(sendCacheKey);
        if (sendList == null || sendList.getValue() == null) {
            sendCount = initCache(studentId, ParentRewardStatus.SEND, sendCacheKey);
        } else {
            List<ParentRewardLogCacheWrapper> wrappers = (List<ParentRewardLogCacheWrapper>) sendList.getValue();
            sendCount = (int) wrappers.stream().filter(wrapper -> wrapper.getExpireDate().after(new Date())).count();
        }
        if (sendCount > 0) {
            return getCdnBaseUrlStaticSharedWithSep() + rewardNeedReceive;
        } else {
            int initCount;
            String initCacheKey = ParentRewardHelper.initCacheKey(studentId);
            CacheObject<Object> initList = ParentRewardCache.getPersistenceCache().get(initCacheKey);
            if (initList == null || initList.getValue() == null) {
                initCount = initCache(studentId, ParentRewardStatus.INIT, initCacheKey);
            } else {
                List<ParentRewardLogCacheWrapper> wrappers = (List<ParentRewardLogCacheWrapper>) initList.getValue();
                initCount = (int) wrappers.stream().filter(wrapper -> wrapper.getExpireDate().after(new Date())).count();
            }
            if (initCount > 0) {
                return getCdnBaseUrlStaticSharedWithSep() + rewardNeedSend;
            }

            if (parentRewardLoader.getUnreadLetterCount(studentId) > 0) {
                return getCdnBaseUrlStaticSharedWithSep() + letterUnread;
            }
        }
        return "";
    }

    private int initCache(Long studentId, ParentRewardStatus status, String cacheKey) {
        int expireTime = (int) ((ParentRewardHelper.currentTermDateRange().getEndTime() - Instant.now().toEpochMilli()) / 1000);
        List<ParentRewardLog> logList = parentRewardLoader.getParentRewardList(studentId, status.getType());
        List<ParentRewardLogCacheWrapper> wrappers = new ArrayList<>();
        logList.forEach(log -> {
            ParentRewardLogCacheWrapper wrapper = new ParentRewardLogCacheWrapper();
            wrapper.setId(log.getId());
            if (ParentRewardStatus.INIT == status) {
                wrapper.setExpireDate(log.getSendExpire());
            } else {
                wrapper.setExpireDate(log.getReceiveExpire());
            }
            wrappers.add(wrapper);
        });
        ParentRewardCache.getPersistenceCache().set(cacheKey, expireTime, wrappers);
        return logList.size();
    }

    private String buildNotSupportParams(String homeworkId, String homeworkType, String homeworkDesc, String cardSupportType, String noSupportObjectiveConfigType) {
        Map<String, Object> notSupportParam = new HashMap<>();
        notSupportParam.put(RES_HOMEWORK_ID, homeworkId);
        notSupportParam.put(RES_HOMEWORK_TYPE, homeworkType);
        notSupportParam.put(RES_HOMEWORK_CARD_DESC, homeworkDesc);
        notSupportParam.put(RES_HOMEWORK_CARD_SUPPORT_TYPE, cardSupportType);
        notSupportParam.put(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        notSupportParam.put(RES_NO_SUPPORT_OBJECTIVE_CONFIG_TYPE, noSupportObjectiveConfigType);
        return JsonUtils.toJson(notSupportParam);
    }

    // 所有作业都已完成的作业类型
    private Map<String, Object> allFinishedHomeworkCard(Long studentId) {
        Map<String, Object> map = new HashMap<>();
        map.put(RES_HOMEWORK_ID, 0);
        map.put(RES_HOMEWORK_CARD_TYPE, "ALL_FINISHED");
        map.put(RES_HOMEWORK_CARD_DESC, "作业已全部完成");
        map.put(RES_HOMEWORK_MAKE_UP_FLAG, false);
        map.put(RES_HOMEWORK_COUNT, 0);
        map.put(RES_HOMEWORK_FINISH_COUNT, 0);
        map.put(RES_HOMEWORK_END_DATE, new Date());
        map.put(RES_HOMEWORK_CARD_SUPPORT_FLAG, true);
        map.put(RES_SHOW_BUTTON, CollectionUtils.isEmpty(parentLoaderClient.loadStudentParents(studentId)));
        map.put(RES_LINK, "/studentMobile/center/homeworkreport.vpage");
        return map;
    }

    // 获取标签推广类型,包括点击几次之后消失
    protected Map<String, Object> getNewTabListConfig(StudentDetail studentDetail) {
        String pageConfigName = "index_tab_new";
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StuApp", "HomeTab")) {
            pageConfigName = "stu_app_home_tab";
        }

        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", pageConfigName);
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> regMap = JsonUtils.fromJson(regStr);
        if (regMap == null) {
            return Collections.emptyMap();
        }

        // 首页3个tab的第1个位置
        Map<String, Object> firstTab = new HashMap<>();
        if (MapUtils.isNotEmpty(regMap) && regMap.containsKey("FIRST_TAB")) {
            Map<String, Map<String, Object>> firstTabMap = (Map<String, Map<String, Object>>) regMap.get("FIRST_TAB");

            if (MapUtils.isNotEmpty(firstTabMap)) {
                // 把tab的key放进map
                List<Map<String, Object>> tempFirstTabList = new ArrayList<>();
                for (Map.Entry<String, Map<String, Object>> entry : firstTabMap.entrySet()) {
                    Map<String, Object> tempMap = entry.getValue();
                    tempMap.put("tabKey", entry.getKey());
                    tempFirstTabList.add(tempMap);
                }

                firstTab = tempFirstTabList.stream()
                        .filter(o -> (o.containsKey("functionGray") && matchHomeTab(studentDetail, SafeConverter.toString(o.get("functionGray"), ""))))
                        .min(Comparator.comparingInt(o2 -> SafeConverter.toInt(o2.get("order"), 1)))
                        .orElse(null);
            }

        }
        if (MapUtils.isNotEmpty(firstTab)) {
            // 特殊解析，直播按计划周期换图
            String tabKey = SafeConverter.toString(firstTab.get("tabKey"), "");
            if (StringUtils.equalsIgnoreCase(tabKey, "LIVE_BROADCAST")) {
                String imgUrlLive = SafeConverter.toString(firstTab.get("imgUrlLive"), "");
                List<String> weekDays = Arrays.asList(StringUtils.split(SafeConverter.toString(firstTab.get("weeks"), ""), ","));

                Calendar cal = Calendar.getInstance();
                // 周几，周日是0，周一是1以此类推
                int d = cal.get(Calendar.DAY_OF_WEEK) - 1;

                List<String> dayTimes = Arrays.asList(StringUtils.split(SafeConverter.toString(firstTab.get("dayTime"), ""), "-"));
                FastDateFormat fdf = FastDateFormat.getInstance("HHmmss");
                int now = SafeConverter.toInt(fdf.format(System.currentTimeMillis()), 0);

                if (CollectionUtils.isNotEmpty(weekDays) && weekDays.contains(String.valueOf(d)) && // 周几匹配
                        CollectionUtils.isNotEmpty(dayTimes) && now > SafeConverter.toInt(dayTimes.get(0), 0) && now < SafeConverter.toInt(dayTimes.get(1), 0) // 时间匹配
                ) {
                    if (StringUtils.isNotBlank(imgUrlLive)) {
                        firstTab.put("imgUrl", imgUrlLive);
                    }
                }
            }
            regMap.put("FIRST_TAB", firstTab);
        }

        return regMap;
    }


    protected static boolean matchHomeTab(StudentDetail student, String functionGray) {
        String grayExpress = "";
        if (student.getStudentSchoolRegionCode() == null) {
            grayExpress = StringUtils.join(grayExpress, "000000", "_");
        } else {
            grayExpress = StringUtils.join(grayExpress, student.getStudentSchoolRegionCode(), "_");
        }

        if (student.getClazz() == null) {
            grayExpress = StringUtils.join(grayExpress, "000000", "_");
        } else {
            grayExpress = StringUtils.join(grayExpress, student.getClazz().getSchoolId(), "_");
        }

        grayExpress = StringUtils.join(grayExpress, student.getId(), "_", DateUtils.dateToString(new Date(), "yyyyMMdd"));
        return grayExpress.matches(functionGray);
    }

    // 获取标签推广类型,包括点击几次之后消失
    protected Map<String, Object> getTabListConfig() {
        StudentDetail studentDetail = getCurrentStudentDetail();
        // 处于付费黑名单地区 或者 处于非教育产品黑名单地区 不出现推广标签
        if (studentDetail.isInPaymentBlackListRegion()) {
            return Collections.emptyMap();
        }
        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "index_tab");
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> regMap = JsonUtils.fromJson(regStr);
        if (regMap == null) {
            return Collections.emptyMap();
        }
        return regMap;
    }

    private void putPromotionType(Map<String, Object> configMap, String tabType, Map<String, Object> tabMap) {
        if (MapUtils.isEmpty(configMap)) {
            return;
        }
        try {
            Map detailMap = (Map) configMap.get(tabType);
            if (detailMap != null) {
                tabMap.put("promotion_type", detailMap.get("promotionType"));           // 推广类型:NEW,HOT,RECOMMENDED
            }
        } catch (Exception e) {
        }
    }

    // App的首页下端的tab列表[教辅，作业历史，随身听]等
    private List<Map<String, Object>> getAppTabList() {

        Map<String, Object> configMap = getTabListConfig();

        String nativeVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        List<Map<String, Object>> tabList = new LinkedList<>();

        for (TabType tabType : TabType.values()) {
            if (VersionUtil.compareVersion(nativeVersion, tabType.getStartVer()) < 0) {
                continue;
            }
            if (tabType.getEndVer() != null && VersionUtil.compareVersion(nativeVersion, tabType.getEndVer()) >= 0) {
                continue;
            }
            Map<String, Object> tabMap = new HashMap<>();
            tabMap.put("type", tabType.getType());
            putPromotionType(configMap, tabType.name(), tabMap);
            tabList.add(tabMap);
        }

        return tabList;
    }

    private enum TabType {
        // 教辅练习册入口已下线
//        WORKBOOK(0, "1.0.0.0", null),           // 教辅
        FAIRYLAND(3, "2.4.0.0", null),          // 课外乐园
        WALKMAN(1, "1.0.0.0", "2.4.0.0"),       // 随身听
        HOMEWORK_HISTORY(2, "1.0.0.0", null);   // 作业历史

        @Getter private int type;          // tab类型
        @Getter private String startVer;   // 从哪个客户端版本开始支持，>=
        @Getter private String endVer;     // 支持到哪个客户端版本截至, <

        TabType(int type, String startVer, String endVer) {
            this.type = type;
            this.startVer = startVer;
            this.endVer = endVer;
        }
    }
}
