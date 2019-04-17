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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.reward.constant.RewardProductPriceUnit;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardManagementClient;
import com.voxlearning.utopia.service.reward.entity.RewardOrderSummary;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/10/26.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动生成奖品中心订单统计任务",
        jobDescription = "每天4点50执行一次",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING},
        cronExpression = "0 50 4 * * ?"
)
@ProgressTotalWork(100)
public class AutoGenRewardOrderSummaryData extends ScheduledJobWithJournalSupport {

    @Inject
    private RewardManagementClient rewardManagementClient;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    @Inject
    private RewardLoaderClient rewardLoaderClient;

    private UtopiaSql utopiaSql;
    private UtopiaSql utopiaSqlOrder;
    private UtopiaSql utopiaSqlReward;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
        utopiaSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        //取当前时间
        Map<String, Object> params = new HashMap<>();
        Date endDate = DateUtils.calculateDateDay(new Date(), -1);
        Date beginDate = MonthRange.current().getStartDate();
        if (!MonthRange.current().contains(endDate)) {
            beginDate = MonthRange.current().previous().getStartDate();
        }
        String endDateStr = DateUtils.dateToString(DateUtils.calculateDateDay(new Date(), -1), "yyyy-MM-dd");
        params.put("beginDate", beginDate);
        params.put("endDate", DateUtils.stringToDate(endDateStr + " 23:59:59"));
        String whereStr = "SELECT o.PRODUCT_ID, o.PRODUCT_NAME, o.SKU_ID, o.SKU_NAME, SUM(o.QUANTITY) AS QUANTITY, " +
                "SUM(o.TOTAL_PRICE) AS TOTAL_PRICE, o.UNIT, COUNT(DISTINCT o.BUYER_ID) AS USER_COUNT FROM ( " +
                "SELECT PRODUCT_ID, PRODUCT_NAME, SKU_ID, SKU_NAME, QUANTITY, TOTAL_PRICE, UNIT, BUYER_ID FROM VOX_REWARD_ORDER WHERE DISABLED = FALSE " +
                "AND CREATE_DATETIME>=:beginDate " +
                "AND CREATE_DATETIME<=:endDate " +
                ") o " +
                "GROUP BY o.PRODUCT_ID, o.PRODUCT_NAME, o.SKU_NAME, o.SKU_ID, o.UNIT ORDER BY QUANTITY DESC";
        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();

        List<Map<String, Object>> data = utopiaSqlReward.withSql(whereStr).useParams(params).queryAll();
        Map<String, Object> results = new HashMap<>();
        for (Map<String, Object> map : data) {
            String key = map.get("PRODUCT_NAME").toString() + "_" + map.get("SKU_NAME").toString();

            if (results.containsKey(key)) {
                Map<String, Object> stat = (Map<String, Object>) results.get(key);
                if (map.get("UNIT").toString().equals(RewardProductPriceUnit.学豆.name())) {
                    stat.put("studentQuantity", map.get("QUANTITY").toString());
                    stat.put("studentTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("studentCount", map.get("USER_COUNT").toString());
                } else if (map.get("UNIT").toString().equals(RewardProductPriceUnit.园丁豆.name())) {
                    stat.put("teacherQuantity", map.get("QUANTITY").toString());
                    stat.put("teacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("teacherCount", map.get("USER_COUNT").toString());
                } else {
                    stat.put("juniorTeacherQuantity", map.get("QUANTITY").toString());
                    stat.put("juniorTeacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("juniorTeacherCount", map.get("USER_COUNT").toString());
                }

                results.put(key, stat);
            } else {
                Map<String, Object> stat = new HashMap<>();
                stat.put("productName", map.get("PRODUCT_NAME").toString());
                stat.put("productId", map.get("PRODUCT_ID").toString());
                stat.put("skuName", map.get("SKU_NAME").toString());
                stat.put("skuId", map.get("SKU_ID").toString());

                if (map.get("UNIT").toString().equals(RewardProductPriceUnit.学豆.name())) {
                    stat.put("studentQuantity", map.get("QUANTITY").toString());
                    stat.put("studentTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("studentCount", map.get("USER_COUNT").toString());

                } else if (map.get("UNIT").toString().equals(RewardProductPriceUnit.园丁豆.name())) {
                    stat.put("teacherQuantity", map.get("QUANTITY").toString());
                    stat.put("teacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("teacherCount", map.get("USER_COUNT").toString());

                } else {
                    stat.put("juniorTeacherQuantity", map.get("QUANTITY").toString());
                    stat.put("juniorTeacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("juniorTeacherCount", map.get("USER_COUNT").toString());

                }

                results.put(key, stat);
            }
        }

        List<RewardOrderSummary> summaries = rewardManagementClient
                .loadRewardOrderSummariesByMonth(SafeConverter.toInt(DateUtils.dateToString(beginDate, "yyyyMM")));
        for (Object map : results.values()) {
            Map<String, Object> dataMap = (Map<String, Object>) map;
            Long productId = SafeConverter.toLong(dataMap.get("productId"));
            Long skuId = SafeConverter.toLong(dataMap.get("skuId"));
            String productName = SafeConverter.toString(dataMap.get("productName"));

            List<RewardOrderSummary> collect = summaries.stream()
                    .filter(s -> Objects.equals(s.getProductId(), productId) && Objects.equals(s.getSkuId(), skuId))
                    // 这里也要校验商品名字，防止商品中间改名字的情况
                    .filter(s -> Objects.equals(s.getProductName(),productName))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                //修改
                RewardOrderSummary summary = MiscUtils.firstElement(collect);
                if (summary != null) {
                    summary.setStudentCount(SafeConverter.toInt(dataMap.get("studentQuantity")));
                    summary.setTeacherCount(SafeConverter.toInt(dataMap.get("teacherQuantity")));
                    summary.setJuniorTeacherCount(SafeConverter.toInt(dataMap.get("juniorTeacherQuantity")));
                    summary.setTeacherPrice(SafeConverter.toDouble(dataMap.get("teacherTotalPrice")));
                    summary.setStudentPrice(SafeConverter.toDouble(dataMap.get("studentTotalPrice")));
                    summary.setJuniorTeacherPrice(SafeConverter.toDouble(dataMap.get("juniorTeacherTotalPrice")));
                    summary.setUpdateDatetime(new Date());
                    rewardManagementClient.updateRewardOrderSummary(summary.getId(), summary);
                }
            } else {
                //添加
                RewardOrderSummary summary = new RewardOrderSummary();
                summary.setProductId(SafeConverter.toLong(dataMap.get("productId")));
                summary.setSkuId(SafeConverter.toLong(dataMap.get("skuId")));
                summary.setProductName(ConversionUtils.toString(dataMap.get("productName")));
                summary.setSkuName(ConversionUtils.toString(dataMap.get("skuName")));
                summary.setStudentCount(SafeConverter.toInt(dataMap.get("studentQuantity")));
                summary.setTeacherCount(SafeConverter.toInt(dataMap.get("teacherQuantity")));
                summary.setJuniorTeacherCount(SafeConverter.toInt(dataMap.get("juniorTeacherQuantity")));
                summary.setTeacherPrice(SafeConverter.toDouble(dataMap.get("teacherTotalPrice")));
                summary.setStudentPrice(SafeConverter.toDouble(dataMap.get("studentTotalPrice")));
                summary.setJuniorTeacherPrice(SafeConverter.toDouble(dataMap.get("juniorTeacherTotalPrice")));
                summary.setMonth(SafeConverter.toInt(DateUtils.dateToString(beginDate, "yyyyMM")));
                rewardManagementClient.persistRewardOrderSummary(summary);
            }
        }

        //获取用户数量统计 放入缓存
        /*Map<String, Object> paramMap = new HashMap<>();
        Date ucBeginDate = beginDate;
        // 是假期
        if (RewardRange.isVacation()) {
            final DateRange summerRange = RewardRange.getSummerRange();
            final DateRange winterRange = RewardRange.getWinterRange();
            if (summerRange.contains(new Date())) {
                ucBeginDate = summerRange.getStartDate();
            } else if (winterRange.contains(new Date())) {
                ucBeginDate = winterRange.getStartDate();
            }
        }
        paramMap.put("beginDate", ucBeginDate);
        paramMap.put("endDate", DateUtils.stringToDate(endDateStr + " 23:59:59"));
        String ucStr = "SELECT COUNT(DISTINCT o.BUYER_ID) AS COUNT, o.UNIT FROM ( " +
                "SELECT BUYER_ID, UNIT FROM VOX_REWARD_ORDER WHERE DISABLED = FALSE " +
                "AND CREATE_DATETIME>=:beginDate " +
                "AND CREATE_DATETIME<=:endDate " +
                ") o " +
                "GROUP BY o.UNIT";
        List<Map<String, Object>> userCountData = utopiaSqlOrder.withSql(ucStr).useParams(paramMap).queryAll();
        String cacheStr = "";
        for (Map<String, Object> map : userCountData) {
            if (RewardProductPriceUnit.学豆.name().equals(map.get("UNIT").toString())) {
                cacheStr = cacheStr + " 学生人数：" + map.get("COUNT");
            } else if (RewardProductPriceUnit.园丁豆.name().equals(map.get("UNIT").toString())) {
                cacheStr = cacheStr + " 小学老师人数：" + map.get("COUNT");
            } else {
                cacheStr = cacheStr + " 中学老师人数：" + map.get("COUNT");
            }
        }*/


        // 挪到了admin里面
        /*String queryStr = "SELECT " +
                "p.PRODUCT_TYPE AS P_TYPE, o.UNIT AS UNIT, COUNT(DISTINCT o.BUYER_ID) AS COUNT " +
                "FROM " +
                "VOX_REWARD_ORDER o," +
                "VOX_REWARD_PRODUCT p " +
                "WHERE " +
                "o.PRODUCT_ID = p.ID " +
                "AND o.DISABLED = FALSE "+
                "AND o.CREATE_DATETIME>=:beginDate " +
                "AND o.CREATE_DATETIME<=:endDate " +
                "GROUP BY p.PRODUCT_TYPE , o.UNIT";

        Date ucBeginDate = beginDate;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("beginDate", ucBeginDate);
        paramMap.put("endDate", DateUtils.stringToDate(endDateStr + " 23:59:59"));
        List<Map<String, Object>> userCountResult = utopiaSqlOrder.withSql(queryStr).useParams(params).queryAll();

        StringBuilder cacheStr = new StringBuilder();
        userCountResult.forEach(r -> {
            RewardProductType productType = RewardProductType.parse(r.get("P_TYPE").toString());
            String typeName = productType == null ? "" : productType.getDescription();
            String unit = r.get("UNIT").toString();
            Long count = SafeConverter.toLong(r.get("COUNT"));

            if (RewardProductPriceUnit.学豆.name().equals(unit)) {
                cacheStr.append(" 学生人数(").append(typeName).append("):").append(count);
            } else if (RewardProductPriceUnit.园丁豆.name().equals(unit)) {
                cacheStr.append(" 小学老师人数(").append(typeName).append(")：").append(count);
            } else {
                cacheStr.append(" 中学老师人数(").append(typeName).append(")：").append(count);
            }
        });

        String cacheKey = RewardOrderSummary.getUserCountSummaryKey();
        rewardCacheClient.getRewardCacheSystem().CBS.unflushable.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), cacheStr.toString());
*/
        progressMonitor.done();
    }
}
