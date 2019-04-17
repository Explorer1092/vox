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

import com.google.common.collect.Maps;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.consumer.StudentAdvertisementInfoLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.consumer.OutsideReadingLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.core.util.MapUtils.isEmpty;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author malong
 * @since 2016/8/4
 */
@Controller
@Slf4j
@RequestMapping(value = "v1/student/activity/center")
public class StudentActivityCenterApiController extends AbstractStudentApiController {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject private StudentAdvertisementInfoLoaderClient studentAdvertisementInfoLoaderClient;
    @Inject private OutsideReadingLoaderClient outsideReadingLoaderClient;

    @RequestMapping(value = "/loadnewadmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadNewAdMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CREATE_TIME, "起始时间");
            validateRequest(REQ_CREATE_TIME);
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
        if (studentDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_LOAD_USER_ERROR);
            return resultMap;
        }

        boolean hadNew;
        int newCount;
        long createTime = getRequestLong(REQ_CREATE_TIME);
        try {
            List<NewAdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(studentDetail.getId(), "320501", getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
            if (createTime == 0) {
                hadNew = data.size() > 0;
                newCount = data.size();
            } else {
                newCount = SafeConverter.toInt(data.stream().filter(e -> Long.compare(e.getUpdateTime().getTime(), createTime) > 0).count());
                hadNew = newCount > 0;
            }
        } catch (Exception e) {
            logger.error("load advertisement data exception: {}", e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;

        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS)
                .add(RES_RESULT_HAD_NEW_MESSAGE, hadNew)
                .add(RES_RESULT_NEW_MESSAGE_COUNT, newCount);
        return resultMap;
    }

    @RequestMapping(value = "/getadvertisementinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getAdvertisementInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_AD_POSITION);
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
        String adPosition = getRequestString(REQ_AD_POSITION);
        if (StringUtils.isBlank(adPosition)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_SLOT_MSG);
            return resultMap;
        }
        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_LOAD_USER_ERROR);
            return resultMap;
        }

        boolean hasDoneHomework = asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                .StudentAppDoHomeworkRecordCacheManager_hasDoneHomework(studentDetail.getId())
                .take();

        //客户端传给后台的广告位对应实际的广告位，以后学生app加广告，只需在这个map里面配置添加客户端广告位和实际广告位的对应关系即可
        Map<String, String> adMap = MiscUtils.map(
                "student_top", "320103",
                //已完成未绑定家长通：320102；已完成已绑定家长通：320105；未完成：320104
                "student_middle", hasDoneHomework ? (hasUsedParentApp(studentDetail.getId()) ? "320105" : "320102") : "320104"
        );

        Map<String, Object> adInfos = new HashMap<>();
        String[] adPositions = adPosition.split(",");
        try {
            for (String position : adPositions) {
                position = StringUtils.trim(position);
                Map<String, Object> adInfo = new HashMap<>();
                String slotId = adMap.get(position);
                if (StringUtils.isNotBlank(slotId)) {
                    List<NewAdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                            .loadNewAdvertisementData(studentDetail.getId(), slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));

                    StudentAdvertisementInfo studentAdvertisementInfo = studentAdvertisementInfoLoaderClient
                            .loadByUserId(studentDetail.getId())
                            .stream()
                            .filter(p -> slotId.equals(p.getSlotId()))
                            .findFirst()
                            .orElse(null);

                    if (CollectionUtils.isNotEmpty(data) && (studentAdvertisementInfo == null || RandomUtils.nextInt(10) < 6)) {
                        generateMapInfo(data.get(0), adInfo);
                        adInfos.put(position, adInfo);
                        // 实时数据打点
                        for (int i = 0; i < data.size(); i++) {
                            if (Boolean.FALSE.equals(data.get(i).getLogCollected())) {
                                continue;
                            }
                            LogCollector.info("sys_new_ad_show_logs",
                                    MiscUtils.map(
                                            "user_id", studentDetail.getId(),
                                            "env", RuntimeMode.getCurrentStage(),
                                            "version", getRequestString("version"),
                                            "aid", data.get(i).getId(),
                                            "acode", data.get(i).getCode(),
                                            "index", i,
                                            "slotId", slotId,
                                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                            "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date()),
                                            "agent", getRequest().getHeader("User-Agent"),
                                            "uuid", UUID.randomUUID().toString(),
                                            "system", getRequestString("sys"),
                                            "system_version", getRequestString("sysVer")
                                    ));
                        }
                    } else {
                        if (studentAdvertisementInfo != null) {
                            adInfo.put("name", studentAdvertisementInfo.getMessageText());
                            adInfo.put("content", studentAdvertisementInfo.getMessageText());
                            adInfo.put("url", studentAdvertisementInfo.getClickUrl());
                            adInfo.put("imgUrl", combineMessageUrl(studentAdvertisementInfo.getImgUrl()));
                            adInfos.put(position, adInfo);

                            LogCollector.info("sys_new_ad_show_logs",
                                    MiscUtils.map(
                                            "user_id", studentDetail.getId(),
                                            "env", RuntimeMode.getCurrentStage(),
                                            "version", getRequestString("version"),
                                            "aid", studentAdvertisementInfo.getId(),
                                            "acode", studentAdvertisementInfo.getId(),
                                            "index", 1,
                                            "slotId", slotId,
                                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                            "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date()),
                                            "agent", getRequest().getHeader("User-Agent"),
                                            "uuid", UUID.randomUUID().toString(),
                                            "system", getRequestString("sys"),
                                            "system_version", getRequestString("sysVer")
                                    ));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("load advertisement data - excp: {}", ex);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("advertisementInfos", adInfos);
        return resultMap;
    }

    //判断学生是否有家长使用家长通App
    private boolean hasUsedParentApp(Long studentId) {
        if (studentId == null) {
            return false;
        }
        List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isEmpty(studentParentList)) {
            return false;
        }
        Set<Long> parentIdSet = studentParentList.stream()
                .map(studentParent -> studentParent.getParentUser().getId())
                .collect(Collectors.toSet());
        Map<Long, VendorAppsUserRef> vendorAppsUserRefMap = vendorLoaderClient.loadVendorAppUserRefs("17Parent", parentIdSet);
        if (isEmpty(vendorAppsUserRefMap)) {
            return false;
        }

        return true;
    }

    private String combineMessageUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
    }

    private void generateMapInfo(NewAdMapper adMapper, Map<String, Object> map) {
        map.put("imgUrl", combineMessageUrl(adMapper.getImg()));
        map.put("name", SafeConverter.toString(adMapper.getName(), ""));
        map.put("content", SafeConverter.toString(adMapper.getContent(), ""));
//        String linkUrl = "/be/london.vpage?aid=" + adMapper.getId() + "&index=" + 0 + "&v=" + getRequestString("sysVer") + "&s=" + getRequestString("sys");
        String linkUrl = AdvertiseRedirectUtils.redirectUrl(adMapper.getId(), 0, getRequestString("sysVer"), getRequestString("sys"), "", 0L);

        map.put("url", com.voxlearning.utopia.core.runtime.ProductConfig.getMainSiteBaseUrl() + linkUrl);
        map.put("adCode", SafeConverter.toString(adMapper.getCode(), ""));

//        Long startTime = SafeConverter.toLong(adMapper.getShowStartTime());
//        Long endTime = SafeConverter.toLong(adMapper.getShowEndTime());
//        String activityTime = "";
//        if (startTime != 0 && endTime != 0) {
//            activityTime = "活动时间：" + DateUtils.dateToString(new Date(startTime), "yyyy.MM.dd") + "-" + DateUtils.dateToString(new Date(endTime), "yyyy.MM.dd");
//        } else if (startTime != 0) {
//            activityTime = "开始时间：" + DateUtils.dateToString(new Date(startTime), "yyyy.MM.dd");
//        } else if (endTime != 0) {
//            activityTime = "截止时间：" + DateUtils.dateToString(new Date(endTime), "yyyy.MM.dd");
//        }
        map.put("activityTime", "");
    }

    /**
     * 学生端首页右上角显示书架接口
     */
    @RequestMapping(value = "bookshelf/message.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBookshelfMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CREATE_TIME, "起始时间");
            validateRequest(REQ_CREATE_TIME);
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
        if (studentDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_LOAD_USER_ERROR);
            return resultMap;
        }

        long createTime = getRequestLong(REQ_CREATE_TIME);
        try {
            Map<String, Boolean> outsideReadingStatus = outsideReadingLoaderClient.loadOutsideReadingStatus(studentDetail.getId());
            Map<String, Boolean> unFinishedReading = Maps.filterValues(outsideReadingStatus, finished -> finished == null || !finished);
            boolean showRedDot = MapUtils.isNotEmpty(unFinishedReading) && !DateUtils.isSameDay(new Date(), new Date(createTime));

            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS)
                    .add("show_icon", MapUtils.isNotEmpty(outsideReadingStatus))
                    .add(RES_RESULT_HAD_NEW_MESSAGE, showRedDot)
                    .add(RES_RESULT_NEW_MESSAGE_COUNT, unFinishedReading.size())
                    .add("bookshelf_url", NewHomeworkConstants.STUDENT_OUTSIDE_READING_BOOKSHELF_URL);
            return resultMap;
        } catch (Exception e) {
            logger.error("load outside reading bookshelf data exception: {}", e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
    }
}
