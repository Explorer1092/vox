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
package com.voxlearning.utopia.service.psr.impl.athena;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.psr.entity.PsrBookPersistenceNew;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamItem;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Named
@Data
public class PsrGoalBalance implements Serializable {

    @Inject private PsrExamEnData psrExamEnData;
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrGoalLoaderClient psrGoalLoaderClient;

    public PsrExamContent balanceWithGoalPsr(PsrExamContext psrExamContext) {
        PsrExamContent retExamContent = new PsrExamContent();
        if (psrExamContext == null)
            return retExamContent;

        int level = getLevel(psrExamContext);
        switch (level) {
            case 1:  // 上线
                retExamContent = dealWithGoalPsr(psrExamContext);
                break;
            case 2:  // 导流量测试
                dealWithGoalPsr(psrExamContext);
                break;
            default:
                break;
        }
        return retExamContent;
    }

    private int getLevel(PsrExamContext psrExamContext) {
        int retLevel = 0;
        if (psrExamContext == null)
            return retLevel;

        // GoalPsr暂时不支持旧教材推题,仅支持小英、小数新教材推题
        PsrBookPersistenceNew bookInfo = psrBooksSentencesNew.getBookPersistenceByBookId(psrExamContext.getBookId());
        if (bookInfo == null || bookInfo.getLatestVersion() == 0)
            return retLevel;

        int balanceConfig = SafeConverter.toInt(ekCouchbaseDao.getCouchbaseDataByKey("goalpsr_balance"), 0);
        switch (balanceConfig) {
            case 1:  // 全量导流
                retLevel = 1;
                break;
            case 2:  // 导流测试
                retLevel = 2;
                break;
            case 3:  // 灰度测试,查看是否符合灰度条件
                retLevel = checkCondition(psrExamContext, bookInfo) ? 1 : 0;
                break;
            default:
                break;
        }

        return retLevel;
    }

    // 灰度的条件,目前可按照 年级+出版社 灰度
    // [1|0] \t [1|2|3] \t1;2;3;4 | sid1;sid2;sid3 | sid1:1:2:3;sid2:3:4:5
    private boolean checkCondition(PsrExamContext psrExamContext, PsrBookPersistenceNew bookInfo) {
        if (psrExamContext == null || bookInfo == null) return false;

        String key = "goalpsr_condition_" + psrExamContext.getSubject().name();
        String value = ekCouchbaseDao.getCouchbaseDataByKey(key);
        if (StringUtils.isBlank(value)) return false;

        int transform = 0;
        int type = 0;
        String[] conditions = value.split("\t");
        if (conditions.length < 1) return false;
        transform = SafeConverter.toInt(conditions[0], 0);
        if (transform != 1) return false;
        if (conditions.length < 2) return false;
        type = SafeConverter.toInt(conditions[1], 0);
        if (type!=1 && type!=2 && type!=3 && type!=4) return false;
        if (conditions.length < 3 || StringUtils.isBlank(conditions[2])) return false;
        String[] tmpArr = conditions[2].split(";");
        if (tmpArr.length <= 0) return false;
        boolean ret = false;
        switch (type) {
            case 1:  // 仅按照年级 灰度
                ret = Arrays.asList(tmpArr).contains(bookInfo.getClazzLevel().toString());
                break;
            case 2:  // 仅按照出版社 灰度
                ret = Arrays.asList(tmpArr).contains(bookInfo.getSeriesId());
                break;
            case 3:  // 仅按照教材Id 灰度
                ret = Arrays.asList(tmpArr).contains(bookInfo.getBookId());
                break;
            case 4:  // 年级+出版社 灰度
                Map<String, List<Integer>> validSeriesGrades = new HashMap<>();
                for (String str : tmpArr) {
                    if (StringUtils.isBlank(str)) continue;
                    String[] sidArr = str.split(":");
                    if (sidArr.length < 2) continue;
                    String seriesId = sidArr[0];
                    List<Integer> grades = new ArrayList<>();
                    for (int i=1; i<sidArr.length; i++)
                        grades.add(SafeConverter.toInt(sidArr[i], 0));
                    validSeriesGrades.put(seriesId, grades);
                }
                ret = validSeriesGrades.containsKey(bookInfo.getSeriesId())
                        && validSeriesGrades.get(bookInfo.getSeriesId()).contains(bookInfo.getClazzLevel());
                break;
            default:
                ret = false;
                break;
        }

        return ret;
    }

    private PsrExamContent dealWithGoalPsr(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrExamContent retExamContent = new PsrExamContent();
        Map<String, String> recomQids = psrGoalLoaderClient.getPsrLoader().getPsrRecommendation(psrExamContext.getUserId(), psrExamContext.getBookId(), psrExamContext.getUnitId(), psrExamContext.getECount(), psrExamContext.getSubject(), psrExamContext.getGrade());
        if (MapUtils.isNotEmpty(recomQids)) {
            for (String qid : recomQids.keySet()) {
                PsrExamItem item = new PsrExamItem();
                item.setEid(qid);
                item.setEk(recomQids.get(qid));
                item.setEt("");
                item.setWeight(0.0D);
                item.setAlogv("goalpsr");
                item.setPsrExamType("psrexammath_goalpsr");
                retExamContent.getExamList().add(item);
                retExamContent.getEids().add(qid);
            }
        }
        return logContent(retExamContent, psrExamContext, "success", dtB, "info");
    }

    private PsrExamContent logContent(PsrExamContent retExamContent,
                                      PsrExamContext psrExamContext, String errorMsg,
                                      Date dtB, String logLevel) {
        if (retExamContent == null)
            retExamContent = new PsrExamContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retExamContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retExamContent.getExamList().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(retExamContent, psrExamContext, uTAll);
            switch (logLevel) {
                case "info":
                    log.info(strLog);
                    break;
                case "error":
                    log.error(strLog);
                    break;
                case "warn":
                    log.warn(strLog);
                    break;
                default:
                    log.info(strLog);
            }
        }

        return retExamContent;
    }

    private String formatReturnLog(PsrExamContent retExamContent, PsrExamContext psrExamContext, Long totalTime) {
        String strLog = retExamContent.formatList("ExamGoal");
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString();
        strLog += " grade:" + Integer.valueOf(psrExamContext.getGrade()).toString() + "]";
        strLog += "[TotalTime:" + totalTime.toString() + "]";

        return strLog;
    }
}
