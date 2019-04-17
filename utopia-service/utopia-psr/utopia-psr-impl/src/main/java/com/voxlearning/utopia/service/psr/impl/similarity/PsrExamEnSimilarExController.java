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

package com.voxlearning.utopia.service.psr.impl.similarity;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.psr.entity.PsrExamEnSimilarContentEx;
import com.voxlearning.utopia.service.psr.entity.PsrExamEnSimilarItemEx;
import com.voxlearning.utopia.service.psr.entity.PsrUserHistoryEid;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Named
@Data
public class PsrExamEnSimilarExController implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private QuestionLoaderClient questionLoaderClient;

    public PsrExamEnSimilarContentEx deal(String product, Long userId, List<String> qids, int eCount) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                product, "", userId, 0, "0", "0", eCount, 0.0f, 0.0f);
        psrExamContext.setRequiredEids(qids);

        return dealCore(psrExamContext);
    }

    public PsrExamEnSimilarContentEx dealCore(PsrExamContext psrExamContext) {
        Date dtB = new Date();

        PsrExamEnSimilarContentEx retExamContent = new PsrExamEnSimilarContentEx();
        if (psrExamContext == null)
            return logContent(null, retExamContent, "psrExamContext is null.", dtB, "info");

        List<String> requiredEids = psrExamContext.getRequiredEids();
        if (requiredEids == null || requiredEids.size() <= 0)
            return logContent(psrExamContext, retExamContent, "No similarity eids.", dtB, "info");

        // 该次推题记录, 防止重复推题
        if (psrExamContext.getRecommendEids() == null)
            psrExamContext.setRecommendEids(new ArrayList<String>());

        if (psrExamContext.getECount() <= 0 || psrExamContext.getECount() > 5)
            psrExamContext.setECount(1);

        String errMsg = "success";
        for (String eid : requiredEids) {
            while (true) {
                Map<String, Double> similarEids = getSimilarEidsFromCouchbase(eid);
                if (similarEids == null || similarEids.size() <= 0) {
                    errMsg = "Can not found similar qids";
                    break;
                }

                // 获取合适的类题
                List<PsrExamEnSimilarItemEx> tmpList = getSimilarEids(psrExamContext, similarEids);
                if (CollectionUtils.isEmpty(tmpList)) {
                    errMsg = "The similar qids were droped";
                    break;
                }

                retExamContent.getSimilarMap().put(eid, tmpList);
                break;
            } // end while
        } // end for

        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            if (!StringUtils.equals(errMsg, "success")) {
                Date now = new Date();
                if (Long.compare(7L, now.getTime() % 10L) >= 0) {
                    for (String eid : requiredEids) {
                        PsrExamEnSimilarItemEx item = new PsrExamEnSimilarItemEx();
                        item.setEid(requiredEids.get(0));
                        item.setSimilarity(0.9999D);

                        List<PsrExamEnSimilarItemEx> tmpList = new ArrayList<>();
                        tmpList.add(item);

                        retExamContent.getSimilarMap().put(eid, tmpList);
                    }
                    errMsg = "success";
                }
            }
        }

        return logContent(psrExamContext, retExamContent, errMsg, dtB, "info");
    }

    private List<PsrExamEnSimilarItemEx> getSimilarEids(PsrExamContext psrExamContext, Map<String, Double> similarEids) {
        if (psrExamContext == null || similarEids == null || similarEids.size() <= 0)
            return Collections.emptyList();

        List<PsrExamEnSimilarItemEx> retList = new ArrayList<>();

        // 最近一个月历史上做过的题不推
        PsrUserHistoryEid psrUserHistoryEid = psrExamEnData.getPsrUserHistoryEid(psrExamContext, Subject.ENGLISH);

        List<Map.Entry<String, Double>> sortList = new LinkedList<>(similarEids.entrySet());

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
            if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, entry.getKey(), Subject.ENGLISH)) {
                //log.error("FindAnBadEid:" + entry.getKey());
                continue;
            }
            if (psrUserHistoryEid != null && psrUserHistoryEid.isMasterByEid(entry.getKey()))
                continue;

            retList.add(new PsrExamEnSimilarItemEx(entry.getKey(), entry.getValue()));
            if (retList.size() >= psrExamContext.getECount())
                break;
        }

        return retList;
    }

    private Map<String, Double> getSimilarEidsFromCouchbase(String eid) {
        if (StringUtils.isEmpty(eid))
            return Collections.emptyMap();

        String strKey = "similar_" + eid;
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);

        return PsrTools.decodeSimilarEids(strValue);
    }

    private PsrExamEnSimilarContentEx logContent(PsrExamContext psrExamContext, PsrExamEnSimilarContentEx retExamContent, String errorMsg,
                                              Date dtB, String logLevel) {
        if (retExamContent == null)
            retExamContent = new PsrExamEnSimilarContentEx();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retExamContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retExamContent.getSimilarMap().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(psrExamContext, retExamContent, uTAll);
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

    private String formatReturnLog(PsrExamContext psrExamContext, PsrExamEnSimilarContentEx retExamContent, Long spendTime) {
        if (psrExamContext == null || retExamContent == null || spendTime == null) {
            return "psrExamContext or retExamContent or spendTime is null.";
        }
        String strLog = retExamContent.formatList();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " qids:" + psrExamContext.getRequiredEids().toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }

}

