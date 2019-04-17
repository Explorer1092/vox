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

package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.constants.ProductType;
import com.voxlearning.utopia.agent.persist.*;
import com.voxlearning.utopia.agent.persist.entity.statistics.*;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by XiaoPeng.Yang on 15-3-10.
 */
@Named
public class AppStatisticsService extends AbstractAgentService {

    @Inject private AppStatisticsMonthPersistence appStatisticsMonthPersistence;
    @Inject private AppStatisticsDayPersistence appStatisticsDayPersistence;
    @Inject private AppStatisticsPeriodPersistence appStatisticsPeriodPersistence;
    @Inject private AppStickyPersistence appStickyPersistence;
    @Inject private VendorAppRefPersistence vendorAppRefPersistence;

    public List<AppStatisticsMonth> getAllAppStatisticsMonthByVendor(Long vendorId, String startMonth, String endMonth) {
        List<VendorAppRef> appRefs = vendorAppRefPersistence.loadAppsByVendor(vendorId);
        if (CollectionUtils.isEmpty(appRefs)) {
            return Collections.emptyList();
        }
        List<String> appKeys = new ArrayList<>();
        for (VendorAppRef ref : appRefs) {
            appKeys.add(ref.getAppKey());
        }
        if (appKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppStatisticsMonth> data = appStatisticsMonthPersistence.loadByMonthAndAppKeys(appKeys, ConversionUtils.toInt(startMonth), ConversionUtils.toInt(endMonth));
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        for (AppStatisticsMonth month : data) {
            month.setDeservedRevenue(ProductType.getSeparateRate(OrderProductServiceType.valueOf(month.getAppKey())) * month.getSharedRevenue());
            month.setAppKey(ProductType.getProductName(OrderProductServiceType.valueOf(month.getAppKey())));
            month.setPaidRate(month.getPaidRate() * 100);
        }
        return data;
    }

    public List<AppStatisticsPeriod> getAllAppStatisticsPeriodByVendor(Long vendorId, String startMonth, String endMonth) {
        List<VendorAppRef> appRefs = vendorAppRefPersistence.loadAppsByVendor(vendorId);
        if (CollectionUtils.isEmpty(appRefs)) {
            return Collections.emptyList();
        }
        List<String> appKeys = new ArrayList<>();
        for (VendorAppRef ref : appRefs) {
            appKeys.add(ref.getAppKey());
        }
        if (appKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppStatisticsPeriod> data = appStatisticsPeriodPersistence.loadByMonthAndAppKeys(appKeys, ConversionUtils.toInt(startMonth), ConversionUtils.toInt(endMonth));
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        for (AppStatisticsPeriod month : data) {
            month.setAppKey(ProductType.getProductName(OrderProductServiceType.valueOf(month.getAppKey())));
        }
        return data;
    }

    public List<Map<String, Object>> getAppObjsByVendorId(Long vendorId) {
        List<VendorAppRef> appRefs = vendorAppRefPersistence.loadAppsByVendor(vendorId);
        if (CollectionUtils.isEmpty(appRefs)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> appKeys = new ArrayList<>();
        for (VendorAppRef ref : appRefs) {
            Map<String, Object> appKey = new MapMessage();
            appKey.put("appKey", ref.getAppKey());
            appKey.put("appName", ProductType.getProductName(OrderProductServiceType.valueOf(ref.getAppKey())));
            appKeys.add(appKey);
        }
        return appKeys;
    }

    public List<AppStatisticsDay> getAppStatisticsDayByVendorAndAppKey(String startDate, String endDate, String appKey) {
        List<AppStatisticsDay> data = appStatisticsDayPersistence.loadByAppKeyAndDay(appKey, ConversionUtils.toInt(startDate), ConversionUtils.toInt(endDate));
        for (AppStatisticsDay day : data) {
            day.setAppKey(ProductType.getProductName(OrderProductServiceType.valueOf(day.getAppKey())));
            day.setPaidRate(day.getPaidRate() * 100);
        }
        return data;
    }

    public List<Map<String, Object>> getAppStickyListByAppKeyAndDay(String appKey, String startDate, String endDate) {
        int startDay = ConversionUtils.toInt(startDate);
        int endDay = ConversionUtils.toInt(endDate);
        List<AppSticky> stickyList = appStickyPersistence.loadByAppKeyAndDay(appKey, startDay, endDay);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<Integer, Map<Integer, AppSticky>> dataMap = new HashMap<>();
        for (AppSticky sticky : stickyList) {
            if (dataMap.containsKey(sticky.getBeginDay())) {
                Date monthDate = DateUtils.calculateDateDay(DateUtils.stringToDate(ConversionUtils.toString(sticky.getBeginDay()), "yyyyMMdd"), 30);
                if (sticky.getCurrentDay() <= ConversionUtils.toInt(DateUtils.dateToString(monthDate, "yyyyMMdd"))) {
                    dataMap.get(sticky.getBeginDay()).put(sticky.getCurrentDay(), sticky);
                }
            } else {
                Map<Integer, AppSticky> stickyMap = new HashMap<>();
                stickyMap.put(sticky.getCurrentDay(), sticky);
                dataMap.put(sticky.getBeginDay(), stickyMap);
            }
        }
        for (Map.Entry<Integer, Map<Integer, AppSticky>> entry : dataMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            AppSticky currentDaySticky = entry.getValue().get(entry.getKey());
            if (currentDaySticky == null) {
                continue;
            }
            int currentCount = currentDaySticky.getUserCount();
            data.put("day", entry.getKey());
            data.put("count", currentCount);
            data.put("sticky_1", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 1)), currentCount));
            data.put("sticky_2", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 2)), currentCount));
            data.put("sticky_3", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 3)), currentCount));
            data.put("sticky_4", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 4)), currentCount));
            data.put("sticky_5", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 5)), currentCount));
            data.put("sticky_6", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 6)), currentCount));
            data.put("sticky_7", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 7)), currentCount));
            data.put("sticky_14", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 14)), currentCount));
            data.put("sticky_30", getSticky(entry.getValue().get(getStickyDay(entry.getKey(), 30)), currentCount));
            dataList.add(data);
        }
        return dataList;
    }

    private Integer getStickyDay(Integer day, int delta) {
        Date stickyDay = DateUtils.calculateDateDay(DateUtils.stringToDate(ConversionUtils.toString(day), "yyyyMMdd"), delta);
        return ConversionUtils.toInt(DateUtils.dateToString(stickyDay, "yyyyMMdd"));
    }

    private String getSticky(AppSticky sticky, int currentCount) {
        if (sticky == null) {
            return "-";
        }
        Double stickyRate = new BigDecimal(sticky.getUserCount())
                .divide(new BigDecimal(currentCount), 3, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
        return stickyRate + "%";
    }
}
