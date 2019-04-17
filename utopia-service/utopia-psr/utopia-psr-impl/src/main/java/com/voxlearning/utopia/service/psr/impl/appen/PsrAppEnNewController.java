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

package com.voxlearning.utopia.service.psr.impl.appen;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

/*
 * Author: chaoli 2017-04-13
 * 此处小应用推荐采用随机推荐的逻辑,你没看错就是随机推荐
 * 原因:小应用已经大大弱化,不需要投入太多精力去维护(老的逻辑不仅仅是在线服务的逻辑,还有一坨离线的数据计算,目前都已经停止计算)
 */

@Slf4j
@Named
public class PsrAppEnNewController implements Serializable {

    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrConfig psrConfig;

    public PsrPrimaryAppEnContent deal(String product, Long userId,
                                       int regionCode, String bookId, String unitId, int eCount,
                                       String eType) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                product, "student", userId, regionCode, bookId, unitId, eCount, 0.0F, 0.0F);
        psrExamContext.setEType(eType);

        return deal(psrExamContext);
    }

    public PsrPrimaryAppEnContent deal(PsrExamContext psrExamContext) {
        return randomDeal(psrExamContext);
    }

    public PsrPrimaryAppEnContent randomDeal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrPrimaryAppEnContent retPrimaryAppEnContent = new PsrPrimaryAppEnContent();
        if (psrExamContext == null)
            return logContent(retPrimaryAppEnContent, psrExamContext, "psrExamContext err.", dtB, "error");

        // 根据bookId 取出Unit-list 和 Ek-list,From mysql
        Map<String, List<String>> unitSentenceList = getUnitsSentenceByBookIdNew(psrExamContext, dtB);
        if (MapUtils.isEmpty(unitSentenceList))
            return logContent(retPrimaryAppEnContent, psrExamContext, "Not Found Default book's UnitsAndEks by bookid:" + psrExamContext.getBookId(), dtB, "error");

        List<String> eks = new ArrayList<>();
        unitSentenceList.values().stream().filter(CollectionUtils::isNotEmpty).forEach(eks::addAll);

        retPrimaryAppEnContent = getRetPrimaryAppEnContentRandom(psrExamContext, eks);

        if (retPrimaryAppEnContent.getAppEnList().size() < psrExamContext.getECount()) {
            return logContent(retPrimaryAppEnContent, psrExamContext,
                    "not found enough eks but why," + retPrimaryAppEnContent.getAppEnList().size() + " < " + psrExamContext.getECount(),
                    dtB, "info");
        }
        return logContent(retPrimaryAppEnContent, psrExamContext, "success", dtB, "info");
    }

    // book: 846, brief: PEP小学英语三年级上
    private PsrPrimaryAppEnContent getAdaptiveEks(PsrPrimaryAppEnContent retPrimaryAppEnContent, PsrExamContext psrExamContext) {
        if (retPrimaryAppEnContent == null)
            retPrimaryAppEnContent = new PsrPrimaryAppEnContent();
        if (psrExamContext == null)
            return retPrimaryAppEnContent;

        if (retPrimaryAppEnContent.getAppEnList().size() >= psrExamContext.getECount())
            return retPrimaryAppEnContent;

        List<String> bakEks = Arrays.asList("word#book","word#crayon","word#eraser","word#my","word#pencil","word#ruler","word#school","word#what","word#your","word#pen");

        UserAppEnEkItem userAppEnEkItem = new UserAppEnEkItem();
        List<PsrPrimaryAppEnItem> appEnList = retPrimaryAppEnContent.getAppEnList();
        if (CollectionUtils.isEmpty(appEnList))
            appEnList = new ArrayList<>();
        for (String ek : bakEks) {
            if (appEnList.size() >= psrExamContext.getECount())
                break;
            if (psrExamContext.getRecommendEids().contains(ek))
                continue;

            PsrPrimaryAppEnItem item = new PsrPrimaryAppEnItem();
            String et = StringUtils.isEmpty(psrExamContext.getEType()) ? userAppEnEkItem.getRandomPattern() : psrExamContext.getEType();
            item.setEid(ek);
            item.setEType(et);
            item.setStatus('E');
            item.setWeight(0.0);
            item.setAlgov("Adaptive");

            appEnList.add(item);
            psrExamContext.getRecommendEids().add(ek);
        }

        retPrimaryAppEnContent.setAppEnList(appEnList);

        return retPrimaryAppEnContent;
    }

    private PsrPrimaryAppEnContent getRetPrimaryAppEnContentRandom(PsrExamContext psrExamContext, List<String/*ek*/> eks) {
        PsrPrimaryAppEnContent retPrimaryAppEnContent = new PsrPrimaryAppEnContent();

        if (eks == null || eks.size() <= 0 || psrExamContext == null)
            return retPrimaryAppEnContent;

        Random random = psrExamContext.getRandom();
        List<PsrPrimaryAppEnItem> appEnList = new ArrayList<>();
        UserAppEnEkItem userAppEnEkItem = new UserAppEnEkItem();

        int count = 0;
        while (appEnList.size() < psrExamContext.getECount() && count++ < 500) {
            PsrPrimaryAppEnItem item = new PsrPrimaryAppEnItem();
            int index = random.nextInt(10000) % eks.size();
            String ek = eks.get(index);

            if (psrExamContext.getRecommendEids().contains(ek))
                continue;

            String et = StringUtils.isEmpty(psrExamContext.getEType()) ? userAppEnEkItem.getRandomPattern() : psrExamContext.getEType();
            item.setEid(ek);
            item.setEType(et);
            item.setStatus('E');
            item.setWeight(0.0);
            item.setAlgov("Random");

            appEnList.add(item);
            psrExamContext.getRecommendEids().add(ek);
        }

        retPrimaryAppEnContent.setAppEnList(appEnList);

        return getAdaptiveEks(retPrimaryAppEnContent, psrExamContext);
    }

    public Map<String, List<String>> getUnitsSentenceByBookIdNew(PsrExamContext psrExamContext, Date dtB) {
        // 根据bookId 取出Unit-list 和 Ek-list,From mysql
        Map<String, List<String>> unitSentenceList = psrBooksSentencesNew.getUnitsSentenceNameByBookId(psrExamContext.getBookId(), psrExamContext.getUnitId());
        //Map<Long, List<String>> unitSentenceList = psrBooksSentences.getUnitsSentenceByBookId(Long.parseLong(psrExamContext.getBookId()));
        if (unitSentenceList != null && unitSentenceList.size() > 0)
            return unitSentenceList;
        logContent(null, psrExamContext, "Not Found book's UnitsAndEks by bookid:" + psrExamContext.getBookId(), dtB, "error");

        // 默认教材 新版-PEP-三年级(上)
        psrExamContext.setBookId("BK_10300000265057");
        psrExamContext.setUnitId("-1");
        return psrBooksSentencesNew.getUnitsSentenceNameByBookId(psrExamContext.getBookId(), psrExamContext.getUnitId());
    }

    private PsrPrimaryAppEnContent logContent(PsrPrimaryAppEnContent retPrimaryAppEnContent,
                                                   PsrExamContext psrExamContext, String errorMsg,
                                                   Date dtB, String logLevel) {
        if (retPrimaryAppEnContent == null)
            retPrimaryAppEnContent = new PsrPrimaryAppEnContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retPrimaryAppEnContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retPrimaryAppEnContent.getAppEnList().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatToString(retPrimaryAppEnContent, psrExamContext, uTAll);
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

        return retPrimaryAppEnContent;
    }

    private String formatToString(PsrPrimaryAppEnContent retPrimaryAppEnContent, PsrExamContext psrExamContext,
                                  Long spendTime) {
        if (retPrimaryAppEnContent == null || psrExamContext == null)
            return "formatToString para err.";
        String strLog = retPrimaryAppEnContent.formatList();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }
}
