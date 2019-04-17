/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.statistics.AppStatisticsDay;
import com.voxlearning.utopia.agent.persist.entity.statistics.AppStatisticsMonth;
import com.voxlearning.utopia.agent.persist.entity.statistics.AppStatisticsPeriod;
import com.voxlearning.utopia.agent.service.workspace.AppStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-3-10.
 */
@Controller
@RequestMapping("/vendor")
@Slf4j
public class VendorStatisticsController extends AbstractAgentController {
    @Inject private AppStatisticsService appStatisticsService;

    //应用按月表
    @RequestMapping(value = "statistics/monthstatistics.vpage", method = RequestMethod.GET)
    public String monthStatistics(Model model) {
        String endDate = getRequestString("endDate");
        String startDate = getRequestString("startDate");
        if (StringUtils.isBlank(endDate)) {
            endDate = DateUtils.dateToString(new Date(), "yyyyMM");
        }

        if (StringUtils.isBlank(startDate)) {
            startDate = DateUtils.dateToString(new Date(), "yyyyMM");
        }
        List<AppStatisticsMonth> monthList = appStatisticsService.getAllAppStatisticsMonthByVendor(getCurrentUserId(), startDate, endDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("monthList", monthList);
        return "vendor/statistics/monthstatistics";
    }

    //应用按周期统计表
    @RequestMapping(value = "statistics/periodstatistics.vpage", method = RequestMethod.GET)
    public String periodStatistics(Model model) {
        String endDate = getRequestString("endDate");
        String startDate = getRequestString("startDate");
        if (StringUtils.isBlank(endDate)) {
            endDate = DateUtils.dateToString(new Date(), "yyyyMM");
        }

        if (StringUtils.isBlank(startDate)) {
            startDate = DateUtils.dateToString(new Date(), "yyyyMM");
        }
        List<AppStatisticsPeriod> monthList = appStatisticsService.getAllAppStatisticsPeriodByVendor(getCurrentUserId(), startDate, endDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("monthList", monthList);
        return "vendor/statistics/periodstatistics";
    }

    //应用按日统计表
    @RequestMapping(value = "statistics/daystatistics.vpage", method = RequestMethod.GET)
    public String dayStatistics(Model model) {
        String endDate = getRequestString("endDate");
        String startDate = getRequestString("startDate");
        String appKey = getRequestString("appKey");
        if (StringUtils.isBlank(endDate)) {
            endDate = DateUtils.dateToString(new Date(), "yyyyMMdd");
        }

        if (StringUtils.isBlank(startDate)) {
            startDate = DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMMdd");
        }
        List<Map<String, Object>> appObjs = appStatisticsService.getAppObjsByVendorId(getCurrentUserId());
        if (StringUtils.isBlank(appKey)) {
            if (CollectionUtils.isNotEmpty(appObjs)) {
                appKey = MiscUtils.firstElement(appObjs).get("appKey").toString();
            } else {
                appKey = "";
            }
        }
        List<AppStatisticsDay> dayList = appStatisticsService.getAppStatisticsDayByVendorAndAppKey(startDate, endDate, appKey);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("dayList", dayList);
        model.addAttribute("appObjs", appObjs);
        model.addAttribute("appKey", appKey);
        return "vendor/statistics/daystatistics";
    }

    //应用留存粘性
    @RequestMapping(value = "statistics/sticky.vpage", method = RequestMethod.GET)
    public String sticky(Model model) {
        String endDate = getRequestString("endDate");
        String startDate = getRequestString("startDate");
        String appKey = getRequestString("appKey");
        if (StringUtils.isBlank(endDate)) {
            endDate = DateUtils.dateToString(new Date(), "yyyyMMdd");
        }

        if (StringUtils.isBlank(startDate)) {
            startDate = DateUtils.dateToString(MonthRange.current().previous().getEndDate(), "yyyyMMdd");
        }
        List<Map<String, Object>> appObjs = appStatisticsService.getAppObjsByVendorId(getCurrentUserId());
        if (StringUtils.isBlank(appKey)) {
            if (CollectionUtils.isNotEmpty(appObjs)) {
                appKey = MiscUtils.firstElement(appObjs).get("appKey").toString();
            } else {
                appKey = "";
            }
        }
        List<Map<String, Object>> stickyList = appStatisticsService.getAppStickyListByAppKeyAndDay(appKey, startDate, endDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("stickyList", stickyList);
        model.addAttribute("appObjs", appObjs);
        model.addAttribute("appKey", appKey);
        return "vendor/statistics/sticky";
    }
}
