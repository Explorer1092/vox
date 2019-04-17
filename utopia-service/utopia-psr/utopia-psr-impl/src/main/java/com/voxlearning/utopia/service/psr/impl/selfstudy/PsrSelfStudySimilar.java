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

package com.voxlearning.utopia.service.psr.impl.selfstudy;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.psr.entity.PsrBookPersistenceNew;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
/*
 * lcl
 * 2017.2.3
 * 自学产品使用
 * 根据题目id查询其类题,仅用于小学英语
 * 主要逻辑:添加不超纲
 */

@Slf4j
@Named
@Data
public class PsrSelfStudySimilar implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;

    public Map<String, List<String>> deal(String bookId, Map<String, List<String>> qids, int reqCount) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                "selfstudy", "", 0L, 0, bookId, "0", reqCount, 0.0f, 0.0f);

        PsrBookPersistenceNew bookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(bookId);
        Integer classLevel = (bookPersistenceNew == null ? 1 : bookPersistenceNew.getClazzLevel());
        Subject subject = (bookPersistenceNew == null ? Subject.ENGLISH :Subject.fromSubjectId(bookPersistenceNew.getSubjectId()));
        psrExamContext.setSubject(subject);
        psrExamContext.setGrade(classLevel);

        return dealCore(psrExamContext, qids);
    }

    public Map<String/*requiredQid*/, List<String/*similarQid*/>> dealCore(PsrExamContext psrExamContext, Map<String, List<String>> qids) {
        Date dtB = new Date();
        Map<String, List<String>> retMap = new HashMap<>();
        if (psrExamContext == null || MapUtils.isEmpty(qids))
            return logContent(psrExamContext, retMap, "PsrSelfStudySimilar para err", dtB, "info");

        // 初始化不超纲数据
        psrExamEnFilter.initBookEids(psrExamContext, psrExamContext.getSubject());
        for (String unitId : qids.keySet()) {
            List<String> requiredQids = qids.get(unitId);
            if (CollectionUtils.isEmpty(requiredQids))
                continue;

            // 计算单元不超纲使用
            psrExamContext.setUnitId(unitId);
            Map<String/*requiredQid*/, List<String/*similarQid*/>> unitQids = getUnitSimilarQids(psrExamContext, requiredQids);
            if (MapUtils.isEmpty(unitQids))
                continue;

            unitQids.keySet().stream().filter(p -> {return !retMap.containsKey(p);}).forEach(p -> retMap.put(p, unitQids.get(p)));
        }

        return logContent(psrExamContext, retMap, "success", dtB, "info");
    }

    // 根据单元获取请求题目的类题数据
    // 加入不超纲逻辑
    private Map<String/*requiredQid*/, List<String/*similarQid*/>> getUnitSimilarQids(PsrExamContext psrExamContext, List<String> requiredQids) {
        Map<String, List<String>> retMap = new HashMap<>();
        if (psrExamContext == null || CollectionUtils.isEmpty(requiredQids))
            return retMap;

        requiredQids.stream().forEach(p -> {
            retMap.put(p, getSimilarQidsByQid(psrExamContext, p));
        });

        return retMap;
    }

    private List<String> getSimilarQidsByQid(PsrExamContext psrExamContext, String requiredQid) {
        List<String> retList = new ArrayList<>();
        Map<String, Double> similarQids = getSimilarQidsFromCouchbase(requiredQid);
        if (MapUtils.isEmpty(similarQids))
            return Arrays.asList(requiredQid);

        // 获取合适的类题
        List<Map.Entry<String, Double>> sortList = new LinkedList<>(similarQids.entrySet());
        // 按相似度排序,默认降序
        final boolean finalDesc = true; // 默认降序
        Collections.sort(sortList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                int n = 0;
                if (o2.getValue() - o1.getValue() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getValue() - o1.getValue() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });

        // 优先推荐相似度高的
        for (Map.Entry<String, Double> entry : sortList) {
            // 不重复推题
            if (psrExamContext.getRecommendEids().contains(entry.getKey()))
                continue;

            // 超纲的不推荐,测试环境数据有问题,不适用超纲逻辑
            if (!RuntimeMode.isTest() && psrExamEnFilter.isAboveLevelEid(psrExamContext, entry.getKey(), psrExamContext.getSubject()))
                continue;

            // 不符合要求的不推荐
            if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, entry.getKey(), psrExamContext.getSubject())) {
                //log.error("FindAnBadEid:" + entry.getKey());
                continue;
            }

            psrExamContext.getRecommendEids().add(entry.getKey());
            retList.add(entry.getKey());
            if (retList.size() >= psrExamContext.getECount())
                break;
        }

        if (CollectionUtils.isEmpty(retList))
            return Arrays.asList(requiredQid);

        return retList;
    }

    private Map<String, Double> getSimilarQidsFromCouchbase(String qid) {
        if (StringUtils.isEmpty(qid))
            return Collections.emptyMap();

        String strKey = "similar_" + qid;
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);

        return PsrTools.decodeSimilarEids(strValue);
    }

    private Map<String, List<String>> logContent(PsrExamContext psrExamContext, Map<String, List<String>> retMap, String errorMsg,
                                              Date dtB, String logLevel) {
        if (retMap == null)
            retMap = new HashMap<>();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(psrExamContext, errorMsg, retMap, uTAll);
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

        return retMap;
    }

    private String formatReturnLog(PsrExamContext psrExamContext, String errorMsg, Map<String, List<String>> retMap, Long spendTime) {
        if (psrExamContext == null || MapUtils.isEmpty(retMap) || spendTime == null) {
            return "psrExamContext or retExamContent or spendTime is null.";
        }
        String strLog = "[PsrSelfStudySimilar:return code:"+ errorMsg +"][retcount:" + retMap.size() + "]";
        strLog += retMap.toString();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString() + " bookId:" + psrExamContext.getBookId();
        strLog += " qids:" + retMap.keySet().toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }

}

