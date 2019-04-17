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

package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.persist.entity.statistics.PaymentDataSummary;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alex on 15-1-21.
 */
@Data
public class GroupPaymentSummaryInfoData implements Serializable {

    private String groupId;
    private String groupName;
    private String groupType;
    private Date startDate;
    private Date endDate;
    private boolean dailySplit;
    private boolean productSplit;

    private Map<String, DailyPaymentSummaryInfoData> dailySummaryData;

    public GroupPaymentSummaryInfoData(String groupType, String groupId, String groupName, Date startDate, Date endDate, Boolean dailySplit, Boolean productSplit) {
        this.groupType = groupType;
        this.groupId = groupId;
        this.groupName = groupName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailySplit = dailySplit;
        this.productSplit = productSplit;

        dailySummaryData = new LinkedHashMap<>();
    }

    public void append(AfentiOrder order) {
        if (order == null) {
            return;
        }

        String dateRange;
        if (dailySplit) {
            dateRange = DateUtils.dateToString(order.getPayDatetime(), DateUtils.FORMAT_SQL_DATE);
        } else {
            dateRange = DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE) + " - " + DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE);
        }

        DailyPaymentSummaryInfoData dailyData;
        if (dailySummaryData.containsKey(dateRange)) {
            dailyData = dailySummaryData.get(dateRange);
        } else {
            dailyData = new DailyPaymentSummaryInfoData(dateRange, productSplit);
            dailySummaryData.put(dateRange, dailyData);
        }

        dailyData.append(order);
    }

    public void append(PaymentDataSummary order) {
        if (order == null) {
            return;
        }

        String dateRange;
        if (dailySplit) {
            String yyyyMMddDate = String.valueOf(order.getDate());
            dateRange = yyyyMMddDate.substring(0, 4) + "-" + yyyyMMddDate.substring(4, 6) + "-" + yyyyMMddDate.substring(6, 8);
        } else {
            dateRange = DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE) + " - " + DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE);
        }

        DailyPaymentSummaryInfoData dailyData;
        if (dailySummaryData.containsKey(dateRange)) {
            dailyData = dailySummaryData.get(dateRange);
        } else {
            dailyData = new DailyPaymentSummaryInfoData(dateRange, productSplit);
            dailySummaryData.put(dateRange, dailyData);
        }

        dailyData.append(order);
    }

    public int getDeepSize() {
        int size = 0;
        Set<String> dailyKeys = dailySummaryData.keySet();
        for (String dailyKey : dailyKeys) {
            size += dailySummaryData.get(dailyKey).getDeepSize();
        }
        return size;
    }

}
