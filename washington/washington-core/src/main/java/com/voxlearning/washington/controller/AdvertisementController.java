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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.advertisement.constants.AdConstants;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.consumer.StudentAdvertisementInfoLoaderClient;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementPositionType;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementPriority;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementDetail;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementSlot;
import com.voxlearning.utopia.service.config.client.AdvertisementServiceClient;
import com.voxlearning.utopia.service.config.client.AdvertisementSlotServiceClient;
import com.voxlearning.utopia.service.config.constant.AdvertisementConstants;
import com.voxlearning.utopia.service.config.consumer.AdvertisementLoaderClient;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.user.api.mappers.AdMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * 获取广告位Controller
 * RequestMapping之前叫"/ad",小区宽带可能会拦截此类请求
 * 遂字母各自+1，变成"/be"
 * Created by Shuai Huan on 2014/9/16.
 */
@Controller
@RequestMapping("/be")
@Slf4j
public class AdvertisementController extends AbstractController {

    @Inject private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;
    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject private AdvertisementSlotServiceClient advertisementSlotServiceClient;
    @Inject private AdvertisementLoaderClient advertisementLoaderClient;
    @Inject private AdvertisementServiceClient advertisementServiceClient;
    @Inject private StudentAdvertisementInfoLoaderClient studentAdvertisementInfoLoaderClient;

    @RequestMapping(value = "info.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getAds() {
        MapMessage mapMessage = new MapMessage();
        Integer position = getRequestInt("p");
        if (AdvertisementPositionType.parse(position) == null) {
            return MapMessage.errorMessage("没有此广告位。");
        }
        try {
            List<AdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadAdvertisementData(currentUserId(), position);
            mapMessage.setSuccess(true);
            mapMessage.add("data", data);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            mapMessage.setErrorCode("获取广告失败:" + ex.getMessage());
        }
        return mapMessage;
    }


    // 获取新的广告内容接口
    @RequestMapping(value = "newinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getNewAds() {
        MapMessage mapMessage = new MapMessage();
        String slotId = getRequestString("p");
        String version = getRequestString("v");
        String system = getRequestString("s");
        String systemVersion = getRequestString("sv");

        AdvertisementSlot slot = advertisementSlotServiceClient.getAdvertisementSlotBuffer().load(slotId);
        if (slot == null) {
            return MapMessage.errorMessage("no data!");
        }
        try {
            Long userId = currentUserId();
            List<NewAdMapper> data = new ArrayList<>();
            // 获取精细化运营广告， 针对特殊用户投放的 学生app活动中心
            if (slotId.equals("320501")) {
                StudentAdvertisementInfo info = studentAdvertisementInfoLoaderClient
                        .loadByUserId(userId)
                        .stream()
                        .filter(p -> slotId.equals(p.getSlotId()))
                        .findFirst()
                        .orElse(null);
                if (info != null && StringUtils.isNotBlank(info.getAdvertisementId())) {
                    AdvertisementDetail detail = advertisementLoaderClient.loadAdDetail(SafeConverter.toLong(info.getAdvertisementId()));
                    if (detail != null) {
                        NewAdMapper adMapper = convertMapper(detail);
                        if (adMapper != null) {
                            data.add(adMapper);
                        }
                    }
                }
            }
            List<NewAdMapper> commonAdList = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(userId, slotId, system, version);
            if (CollectionUtils.isNotEmpty(commonAdList)) {
                data.addAll(commonAdList);
            }
            mapMessage.setSuccess(true);
            mapMessage.add("data", data);
            mapMessage.add("imgDoMain", getCdnBaseUrlStaticSharedWithSep());
            mapMessage.add("width", slot.getWidth());
            mapMessage.add("height", slot.getHeight());

            // 每次一起展示的广告加入一个统一的UUID
            String uuid = UUID.randomUUID().toString();
            for (int i = 0; i < data.size(); i++) {
                if (Boolean.FALSE.equals(data.get(i).getLogCollected())) {
                    continue;
                }
                // log
                LogCollector.instance().info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", currentUserId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", version,
                                "aid", data.get(i).getId(),
                                "acode", data.get(i).getCode(),
                                "index", i,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "uuid", uuid,
                                "system", system,
                                "system_version", systemVersion
                        ));
            }
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            mapMessage.setErrorCode("load data fail:" + ex.getMessage());
        }

        return mapMessage;
    }

    private NewAdMapper convertMapper(AdvertisementDetail detail) {
        if (detail == null) {
            return null;
        }
        NewAdMapper data = new NewAdMapper();
        data.setId(detail.getId());
        data.setImg(detail.getImgUrl());
        data.setDescription(detail.getDescription());
        data.setName(detail.getName());
        data.setCode(SafeConverter.toString(detail.getAdCode()));
        data.setPriority(AdvertisementPriority.ofLevel(detail.getPriority()));
        data.setHasUrl(StringUtils.isNotBlank(detail.getResourceUrl()));
        data.setContent(detail.getAdContent());
        data.setBtnContent(detail.getBtnContent());
        data.setShowStartTime(detail.getShowTimeStart() == null ? 0 : detail.getShowTimeStart().getTime());
        data.setShowEndTime(detail.getShowTimeEnd() == null ? 0 : detail.getShowTimeEnd().getTime());
        data.setGif(detail.getGifUrl());
        data.setExt(detail.getExtUrl());
        data.setShowSeconds(detail.getDisplayDuration() == null ? "" : detail.getDisplayDuration().toString());
        data.setUrl(detail.getResourceUrl());
        data.setNeedLogin(true);
        data.setShowLogo(detail.getShowLogo());
        data.setShowLimit(detail.getShowLimit());
        data.setClickLimit(detail.getClickLimit());
        data.setCreateTime(detail.getCreateDatetime());
        data.setUpdateTime(detail.getUpdateDatetime());
        data.setDailyClickLimit(detail.getDailyClickLimit());
        data.setDisplayPeriod(detail.fetchPeriod());
        data.setLogCollected(detail.isLogCollected());
        return data;
    }


    // 广告统一跳转入口
    @RequestMapping(value = "london.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String london() {
        Long aid = getRequestLong("aid");
        Integer index = getRequestInt("index");
        String version = getRequestString("v");
        String system = getRequestString("s");
        String systemVersion = getRequestString("sv");
        Long sid = getRequestLong("sid");
        AdvertisementDetail detail = advertisementLoaderClient.loadAdDetail(aid);
        if (detail == null) {
            return "redirect:/";
        }
        LogCollector.instance().info("sys_new_ad_click_logs",
                MiscUtils.map(
                        "user_id", currentUserId(),
                        "env", RuntimeMode.getCurrentStage(),
                        "version", version,
                        "aid", detail.getId(),
                        "acode", SafeConverter.toString(detail.getAdCode()),
                        "index", index,
                        "slotId", detail.getAdSlotId(),
                        "client_ip", getWebRequestContext().getRealRemoteAddress(),
                        "time", DateUtils.dateToString(new Date()),
                        "agent", getRequest().getHeader("User-Agent"),
                        "system", system,
                        "system_version", systemVersion
                ));
        // 记录用户点击次数
        if (detail.getUserClickQuota() != null && detail.getUserClickQuota() > 0) {
            userAdvertisementServiceClient.incUserClickCount(currentUserId(), detail.getAdCode(), detail.getShowTimeEnd());
        }
        // 记录点击次数
        if (detail.getClickLimit() != null && detail.getClickLimit() > 0) {
            advertisementServiceClient.getAdvertisementService().incAdCacheCountByType(detail.getId(), AdvertisementConstants.CLICK_TYPE, detail.getShowTimeEnd().getTime()).awaitUninterruptibly();
        }
        // 记录每日点击次数
        if (detail.getDailyClickLimit() != null && detail.getDailyClickLimit() > 0) {
            advertisementServiceClient.getAdvertisementService()
                    .incAdCacheCountByType(detail.getId(), AdvertisementConstants.DAILY_CLICK_TYPE, DateUtils.getCurrentToDayEndSecond()).awaitUninterruptibly();
        }
        String url = detail.getResourceUrl();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("sid", SafeConverter.toString(sid));
        paramMap.put("_aid", SafeConverter.toString(aid));
        paramMap.put("app_version", version);
        if (detail.isRedirectWithUid()) {
            paramMap.put("uid", SafeConverter.toString(currentUserId()));
        }

        url = UrlUtils.buildUrlQuery(url, paramMap);

        try {
            //追踪订单效果，记录广告入口
            int splitIndex = url.indexOf("?");
            String midUrl = url.substring(splitIndex + 1, url.length());
            LinkedHashMap<String, String> reqParams = UrlUtils.parseQueryString(midUrl);
            if (MapUtils.isNotEmpty(reqParams) && reqParams.containsKey("entranceId")) {
                asyncOrderCacheServiceClient.getAsyncOrderCacheService()
                        .UserOrderReferCacheManager_setRecord(currentUserId(), reqParams.get("entranceId"))
                        .awaitUninterruptibly();
            } else if (MapUtils.isNotEmpty(reqParams) && reqParams.containsKey("refer")) {
                asyncOrderCacheServiceClient.getAsyncOrderCacheService()
                        .UserOrderReferCacheManager_setRecord(currentUserId(), reqParams.get("refer"))
                        .take();
            }
            // 记录用户最近一次点击增值广告的信息 只有URL填写了这个参数的记录
            if (MapUtils.isNotEmpty(reqParams) && reqParams.containsKey("activityId")) {
                Map<String, Object> adMap = new HashMap<>();
                adMap.put("advertisementId", aid);
                adMap.put("slotId", detail.getAdSlotId());
                adMap.put("activityId", reqParams.get("activityId"));
                asyncOrderCacheServiceClient.getAsyncOrderCacheService()
                        .UserOrderAdvertiseCacheManager_set(currentUserId(), adMap)
                        .awaitUninterruptibly();
            }
        } catch (Exception e) {
            logger.error("url parseQueryString error,aid={}", detail.getId());
        }

        return "redirect:" + url;
    }

    @RequestMapping(value = "incuserview.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage incUserViewCount() {
        Long userId = currentUserId();
        Long aid = getRequestLong("aid");
        Long count = getRequestLong("count");

        if (userId == null || aid == 0L || count == 0L) {
            return MapMessage.errorMessage("错误的参数");
        }

        AdvertisementDetail detail = advertisementLoaderClient.loadAdDetail(aid);
        if (detail == null) {
            return MapMessage.errorMessage("错误的参数");
        }

        userAdvertisementServiceClient.incUserViewCount(userId, detail.getAdCode(), count, detail.getShowTimeEnd());

        // 家长端闪屏特殊处理
        if (Objects.equals(AdConstants.PARENT_WELCOME_AD_SLOT_ID, detail.getAdSlotId())) {
            userAdvertisementServiceClient.incAdUserViewCount(userId, detail.getId());
        }

        return MapMessage.successMessage();
    }

}
