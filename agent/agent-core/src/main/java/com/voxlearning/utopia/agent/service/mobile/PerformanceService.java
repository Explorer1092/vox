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

package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * @author Jia HuanYin
 * @since 2016/2/18
 */
@Named
public class PerformanceService extends AbstractAgentService {

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd");


    @Inject private ReportStatusServiceClient reportStatusServiceClient;



    // 地区表每日新增数据表北京地区最新数据的时间
    public Integer lastSuccessDataDay() {
        Integer lastDay = getLastDay();
        return lastDay == null || lastDay == 0 ? ConversionUtils.toInt(DATE_FORMAT.format(DateUtils.calculateDateDay(new Date(), -1))) : lastDay;
    }

    // 地区表每日新增数据表北京地区最新数据的时间
    public Date lastSuccessDataDate() {
        Integer lastDay = getLastDay();
        return lastDay == null || lastDay == 0 ? DateUtils.calculateDateDay(new Date(), -1) : DateUtils.stringToDate(String.valueOf(lastDay), "yyyyMMdd");
    }

    private Integer getLastDay() {
        ReportStatus reportStatus = reportStatusServiceClient.getReportStatusService()
                .loadReportStatus()
                .getUninterruptibly();
        if (reportStatus == null) {
            return null;
        }
        Map<String, Object> latestCollection = reportStatus.getLatestCollection();
        if (latestCollection == null) {
            return null;
        }
        Map map = (Map) latestCollection.get("vox_performance");
        if (map == null) {
            return null;
        }

        return SafeConverter.toInt(map.get("collection_name"));
    }

}
