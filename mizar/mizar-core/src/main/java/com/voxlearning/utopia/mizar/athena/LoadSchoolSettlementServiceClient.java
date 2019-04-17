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

package com.voxlearning.utopia.mizar.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.LoadSchoolSettlementService;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;

import javax.inject.Named;
import java.util.*;

/**
 * LoadSchoolSettlementServiceClient
 *
 * @author song.wang
 * @date 2017/6/29
 */
@Named("com.voxlearning.utopia.mizar.athena.LoadSchoolSettlementServiceClient")
public class LoadSchoolSettlementServiceClient {

    @ImportService(interfaceClass = LoadSchoolSettlementService.class)
    private LoadSchoolSettlementService loadSchoolSettlementService;

    public Map<Long, SchoolSettlement> loadSchoolSettlementtData(Collection<Long> schoolIds, Integer day) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        Map<Long, SchoolSettlement> resultMap = new HashMap<>();

        Date defaultDate = DateUtils.addDays(new Date(), -1);
        if (day != null) {
            defaultDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        }
        Integer defaultSettlementDay = SafeConverter.toInt(DateUtils.dateToString(defaultDate, "yyyyMMdd"));
        Integer defaultMonth = SafeConverter.toInt(DateUtils.dateToString(defaultDate, "yyyyMM"));
        schoolIds.forEach(p -> {
            SchoolSettlement schoolSettlement = new SchoolSettlement();
            schoolSettlement.initData();
            schoolSettlement.setSchoolId(p);
            schoolSettlement.setSettlementDay(defaultSettlementDay);
            schoolSettlement.setMonth(defaultMonth);
            resultMap.put(p, schoolSettlement);
        });

        MapMessage message = loadSchoolSettlementService.loadSchoolSettlementtData(schoolIds, day);
        if (!message.isSuccess()) {
            return resultMap;
        }

        Integer dataDay = SafeConverter.toInt(message.get("date"), defaultSettlementDay);
        Map<String, Object> dataMap = (Map<String, Object>) message.get("dataMap");
        if (MapUtils.isEmpty(dataMap)) {
            return resultMap;
        }
        Integer month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(dataDay), "yyyyMMdd"), "yyyyMM"));

        schoolIds.forEach(p -> {
            Map<String, Object> itemMap = (Map<String, Object>) dataMap.get(String.valueOf(p));
            SchoolSettlement schoolSettlement = resultMap.get(p);
            schoolSettlement.setSettlementDay(dataDay);
            if (itemMap == null) {
                return;
            }
            schoolSettlement.setMonth(month);
            schoolSettlement.setTotalAmount(SafeConverter.toDouble(itemMap.get("totalAmount")));
            schoolSettlement.setRefundAmount(SafeConverter.toDouble(itemMap.get("refundAmount")));
            schoolSettlement.setOrderCount(SafeConverter.toInt(itemMap.get("orderCount")));
            schoolSettlement.setRefundOrderCount(SafeConverter.toInt(itemMap.get("refundOrderCount")));
            schoolSettlement.setBasicSettlementAmount(SafeConverter.toDouble(itemMap.get("basicSettlementAmount")));
            schoolSettlement.setTmOrderAmortizeAmount(SafeConverter.toDouble(itemMap.get("tmOrderAmortizeAmount")));
            schoolSettlement.setBtmOrderAmortizeAmount(SafeConverter.toDouble(itemMap.get("btmOrderAmortizeAmount")));
            schoolSettlement.setTmOneTimeOrderExpenditure(SafeConverter.toDouble(itemMap.get("tmOneTimeOrderExpenditure")));

        });

        return resultMap;
    }


    public Map<Long, List<SchoolRefundAmortizeData>> loadRefundAmortizeData(Collection<Long> schoolIds, Integer day) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<SchoolRefundAmortizeData>> resultMap = new HashMap<>();
        schoolIds.forEach(p -> resultMap.put(p, new ArrayList<>()));

        MapMessage message = loadSchoolSettlementService.loadRefundAmortizeData(schoolIds, day);
        if (!message.isSuccess()) {
            return resultMap;
        }
        Integer dataDay = SafeConverter.toInt(message.get("date"), SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMMdd")));
        Map<Integer, List<Object>> dataMap = (Map<Integer, List<Object>>) message.get("dataMap");
        if (MapUtils.isEmpty(dataMap)) {
            return resultMap;
        }
        Integer month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(dataDay), "yyyyMMdd"), "yyyyMM"));

        schoolIds.forEach(p -> {
            List<Object> amortizeList = dataMap.get(p.intValue());
            if (CollectionUtils.isEmpty(amortizeList)) {
                return;
            }
            amortizeList.forEach(k -> {
                Map<String, Object> dataItem = (Map<String, Object>) k;
                if (dataItem == null) {
                    return;
                }
                SchoolRefundAmortizeData amortizeData = new SchoolRefundAmortizeData();
                amortizeData.setSchoolId(p);
                amortizeData.setMonth(month);
                amortizeData.setAmortizeMonth(SafeConverter.toInt(dataItem.get("amortizeMonth")));
                amortizeData.setAmortizeAmount(SafeConverter.toDouble(dataItem.get("amortizeAmount")));
                amortizeData.setSettlementDay(dataDay);
                resultMap.get(p).add(amortizeData);
            });

        });

        return resultMap;
    }
}
