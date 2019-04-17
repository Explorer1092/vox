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

package com.voxlearning.utopia.mizar.service.settlement;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.mizar.athena.LoadSchoolSettlementServiceClient;
import com.voxlearning.utopia.mizar.service.AbstractMizarService;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import com.voxlearning.utopia.service.mizar.consumer.loader.SchoolSettlementLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.SchoolRefundAmortizeDataServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.SchoolSettlementServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UserSettlementService
 *
 * @author song.wang
 * @date 2017/6/23
 */

@Named
public class UserSettlementService extends AbstractMizarService {

    @Inject
    private SchoolSettlementLoaderClient schoolSettlementLoaderClient;
    @Inject
    private SchoolSettlementServiceClient schoolSettlementServiceClient;

    @Inject
    private SchoolRefundAmortizeDataServiceClient schoolRefundAmortizeDataServiceClient;
    @Inject
    private LoadSchoolSettlementServiceClient loadSchoolSettlementServiceClient;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    private static final Double PAYMENT_RATE = 0.5;

    public Collection<SchoolSettlement> loadSchoolSettlementData(List<Long> schoolIds, Integer month) {

        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }

        Map<Long, SchoolSettlement> schoolSettlementMap;  // 交易数据
        if (isCurrentMonth(month)) { // 当前月份， 调用远程接口
            schoolSettlementMap = loadSchoolSettlementServiceClient.loadSchoolSettlementtData(schoolIds, null);
            setOtherData(schoolSettlementMap);

            // 退款摊销数据
            Map<Long, List<SchoolRefundAmortizeData>> amortizeDataMap = loadSchoolSettlementServiceClient.loadRefundAmortizeData(schoolIds, null);
            calPayment(schoolSettlementMap, amortizeDataMap);
        } else { // 历史月份，直接从数据库获取数据
            schoolSettlementMap = schoolSettlementLoaderClient.loadSettlementBySchoolIds(schoolIds, month);
        }

        return schoolSettlementMap.values();
    }

    public void calAndSaveSettlementData(Set<Long> schoolIds, Integer day) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        // 交易数据
        Map<Long, SchoolSettlement> schoolSettlementMap = loadSchoolSettlementServiceClient.loadSchoolSettlementtData(schoolIds, day);

        // 判断返回数据是否是月末的数据， 如果不是则丢弃，如果是则保存到数据库
        SchoolSettlement item = schoolSettlementMap.get(schoolIds.iterator().next());
        if (!isLastDayOfMonth(item.getSettlementDay())) {
            return;
        }
        setOtherData(schoolSettlementMap);
        // 退款摊销数据
        Map<Long, List<SchoolRefundAmortizeData>> amortizeDataMap = loadSchoolSettlementServiceClient.loadRefundAmortizeData(schoolIds, day);
        calPayment(schoolSettlementMap, amortizeDataMap);

        Integer month = item.getMonth();

        // 将现有数据disable掉
        schoolSettlementServiceClient.disableByMonth(schoolIds, month);
        // 插入新的数据
        schoolSettlementServiceClient.saveSettlementData(schoolSettlementMap.values());

        schoolRefundAmortizeDataServiceClient.disableByMonth(schoolIds, month);
        List<SchoolRefundAmortizeData> refundAmortizeDataList = amortizeDataMap.values().stream().flatMap(List::stream).filter(p -> p != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(refundAmortizeDataList)) {
            schoolRefundAmortizeDataServiceClient.saveRefundAmortizeData(refundAmortizeDataList);
        }
    }

    private boolean isCurrentMonth(Integer month) {
        int currentMonth = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMM"));
        return currentMonth == month;
    }

    private boolean isLastDayOfMonth(Integer day) {
        if (day == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Integer lastDayOfMonth = SafeConverter.toInt(DateUtils.dateToString(calendar.getTime(), "yyyyMMdd"));
        return Objects.equals(day, lastDayOfMonth);
    }

    // 设置其他附属信息
    private void setOtherData(Map<Long, SchoolSettlement> schoolSettlementMap) {
        if (MapUtils.isEmpty(schoolSettlementMap)) {
            return;
        }
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolSettlementMap.keySet())
                .getUninterruptibly();
        schoolSettlementMap.forEach((k, v) -> {
            v.setPaymentRate(PAYMENT_RATE);
            School school = schoolMap.get(k);
            v.setSchoolName(school == null ? "" : school.getCname());
        });
    }

    private void calPayment(Map<Long, SchoolSettlement> settlementMap, Map<Long, List<SchoolRefundAmortizeData>> amortizeDataMap) {
        if (MapUtils.isEmpty(settlementMap)) {
            return;
        }
        settlementMap.forEach((k, v) -> {
            v.setPaymentRate(PAYMENT_RATE);
            List<SchoolRefundAmortizeData> refundAmortizeDataList = amortizeDataMap.get(k);
            if (CollectionUtils.isNotEmpty(refundAmortizeDataList)) {
                Map<Integer, SchoolRefundAmortizeData> monthAmortizeMap = refundAmortizeDataList.stream().collect(Collectors.toMap(SchoolRefundAmortizeData::getAmortizeMonth, Function.identity(), (o1, o2) -> o1));
                // 获取历史月份的结算数据
                Map<Integer, SchoolSettlement> monthSettlementMap = schoolSettlementLoaderClient.loadSettlementByMonths(k, monthAmortizeMap.keySet());
                // 加入当月数据
                monthSettlementMap.put(v.getMonth(), v);
                monthAmortizeMap.forEach((m, a) -> {
                    SchoolSettlement schoolSettlement = monthSettlementMap.get(m);
                    if (schoolSettlement != null && schoolSettlement.getPaymentRate() != null) {
                        v.setRefundAmortizeAmount(v.getRefundAmortizeAmount() + doubleMultiply(a.getAmortizeAmount(), schoolSettlement.getPaymentRate(), 2, BigDecimal.ROUND_CEILING));
                    }
                });

            }
            v.setPayment(doubleMultiply(v.getBasicSettlementAmount(), v.getPaymentRate(), 2, BigDecimal.ROUND_FLOOR) - v.getRefundAmortizeAmount());
        });
    }

    private double doubleMultiply(double d1, double d2, int newScale, int roundingMode) {
        if (newScale < 0) {
            newScale = 0;
        }
        return new BigDecimal(Double.toString(d1)).multiply(new BigDecimal(Double.toString(d2))).setScale(newScale, roundingMode).doubleValue();
    }

}
