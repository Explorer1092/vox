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
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
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
 * 根据知识点id推荐题目,仅用于小学英语
 * 主要逻辑:添加不超纲
 */

@Slf4j
@Named
@Data
public class PsrSelfStudyRecomByKp implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrExamEnData psrExamEnData;

    public Map<String, List<String>> deal(Long studentId, String bookId, Map<String, List<String>> kps, int reqCount) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                "selfstudy", "", studentId, 0, bookId, "0", reqCount, 0.0f, 0.0f);

        PsrBookPersistenceNew bookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(bookId);
        Integer classLevel = (bookPersistenceNew == null ? 1 : bookPersistenceNew.getClazzLevel());
        Subject subject = (bookPersistenceNew == null ? Subject.ENGLISH :Subject.fromSubjectId(bookPersistenceNew.getSubjectId()));
        psrExamContext.setSubject(subject);
        psrExamContext.setGrade(classLevel);

        return dealCore(psrExamContext, kps);
    }

    public Map<String/*requiredKp*/, List<String/*qid*/>> dealCore(PsrExamContext psrExamContext, Map<String, List<String>> kps) {
        Date dtB = new Date();
        Map<String, List<String>> retMap = new HashMap<>();
        if (psrExamContext == null || MapUtils.isEmpty(kps))
            return logContent(psrExamContext, retMap, "PsrSelfStudyRecomByKp para err", dtB, "info");

        // 初始化不超纲数据
        psrExamEnFilter.initBookEids(psrExamContext, psrExamContext.getSubject());
        for (String unitId : kps.keySet()) {
            List<String> requiredKps = kps.get(unitId);
            if (CollectionUtils.isEmpty(requiredKps))
                continue;

            // 计算单元不超纲使用
            psrExamContext.setUnitId(unitId);
            Map<String/*requiredKp*/, List<String/*Qid*/>> unitQids = getUnitRecomKps(psrExamContext, requiredKps);
            if (MapUtils.isEmpty(unitQids))
                continue;

            unitQids.keySet().stream().filter(p -> {return !retMap.containsKey(p);}).forEach(p -> retMap.put(p, unitQids.get(p)));
        }

        return logContent(psrExamContext, retMap, "success", dtB, "info");
    }

    // 根据单元获取请求题目的类题数据
    // 加入不超纲逻辑
    private Map<String/*requiredKp*/, List<String/*qid*/>> getUnitRecomKps(PsrExamContext psrExamContext, List<String> requiredKps) {
        Map<String, List<String>> retMap = new HashMap<>();
        if (psrExamContext == null || CollectionUtils.isEmpty(requiredKps))
            return retMap;

        UserExamContent userExamContent = psrExamEnData.getUserExamContentId(psrExamContext, psrExamContext.getSubject());
        requiredKps.stream().forEach(p -> {
            List<String> qids = getPsrQidsByKp(psrExamContext, p);
            if (CollectionUtils.isEmpty(qids))
                qids = getRandomQidsByKp(psrExamContext, p);
            retMap.put(p, qids);
        });

        return retMap;
    }

    private List<String> getPsrQidsByKp(PsrExamContext psrExamContext, String kp) {
        List<String> retList = new ArrayList<>();
        if (psrExamContext == null || StringUtils.isBlank(kp))
            return retList;

        List<EidItem> qidsOfKp = getQidsOfKpFromCouchbase(psrExamContext, kp);
        if (CollectionUtils.isEmpty(qidsOfKp))
            return retList;

        EkToEidContent ekToEidContent = new EkToEidContent();
        ekToEidContent.setEidList(qidsOfKp);
        ekToEidContent.setEk(kp);

        // 计算预估通过率, 并根据预估通过率 和 题型热度排序
        List<Map.Entry<Double, EidItem>> listQids = psrExamEnFilter.getEidsPredictRate(psrExamContext, ekToEidContent, psrExamContext.getECount(), psrExamContext.getSubject());
        if (listQids == null)
            return retList;

        UserExamContent userExamContent = psrExamEnData.getUserExamContentId(psrExamContext, psrExamContext.getSubject());

        List<Integer> posList = getDataIndex(listQids.size()); // 高能力 和 低能力的学生 按队列顺序取题
        if (userExamContent.getIrtTheta() >= psrExamContext.getLowIrtTheta() && userExamContent.getIrtTheta() <= psrExamContext.getHighIrtTheta()) {
            // 中能力的学生 从中间开始取题
            posList = getDataFromMiddleToSide(listQids.size());
        }

        for (int j = 0; posList != null && j < posList.size() && retList.size() < psrExamContext.getECount(); j++) {
            int n = posList.get(j);
            if (n < 0 || n > listQids.size())
                continue;

            EidItem eidItem = listQids.get(n).getValue();
            if (eidItem == null)
                continue;

            if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, eidItem.getEid(), psrExamContext.getSubject())) {
                //log.error("FindAnBadEid:" + eidItem.getEid() + ",badEk:" + kp);
                continue;
            }

            if (psrExamContext.getRecommendEids() == null)
                psrExamContext.setRecommendEids(new ArrayList<String>());
            if (psrExamContext.getRecommendEids().contains(eidItem.getEid()))
                continue;

            psrExamContext.getRecommendEids().add(eidItem.getEid());
            retList.add(eidItem.getEid());
        }

        return retList;
    }

    /*
     * 从队列中间位置 左右摆动
     * 返回 列表 下标
     */
    private List<Integer> getDataFromMiddleToSide(int size) {
        if (size <= 0)
            return new ArrayList<>();

        List<Integer> list = new ArrayList<>();

        int middlePos = (size + 1) / 2 - 1;  // 队列的中间位置

        int pos = 0;
        int index = 0;
        for (int i = 0; i < size; i++) {
            index = i + 1;
            if (index % 2 != 0)
                pos = middlePos - ((index - 1) / 2);
            else
                pos = middlePos + (index / 2);

            list.add(pos);
        }

        return list;
    }

    private List<Integer> getDataIndex(int size) {
        List<Integer> retList = new ArrayList<>();
        if (size <= 0)
            return retList;

        for (int i=0; i<size; i++)
            retList.add(i);

        return retList;
    }

    // 没有用户能力值的时候 随机推荐题目
    private List<String> getRandomQidsByKp(PsrExamContext psrExamContext, String requiredKp) {
        List<String> retList = new ArrayList<>();
        if (psrExamContext == null || StringUtils.isBlank(requiredKp))
            return retList;

        List<EidItem> kpQids = getQidsOfKpFromCouchbase(psrExamContext, requiredKp);
        if (CollectionUtils.isEmpty(kpQids))
            return retList;

        Map<Integer, String> tmpQids = new HashMap<>();
        Integer index = 0;
        for (EidItem item : kpQids) {
            tmpQids.put(index++, item.getEid());
        }

        List<Integer> validIndexList = new ArrayList<>();
        Random random = psrExamContext.getRandom();

        for (int i = 0; i < tmpQids.size(); i++) {
            int randomTd = random.nextInt(100000);
            int tmpIndex = randomTd % tmpQids.size();
            if (validIndexList.contains(tmpIndex))
                continue;

            validIndexList.add(tmpIndex);
            String qid = tmpQids.get(tmpIndex);

            // 不重复推题
            if (psrExamContext.getRecommendEids().contains(qid))
                continue;

            // 超纲的不推荐,测试环境数据有问题,不适用超纲逻辑
            if (RuntimeMode.isStaging() && RuntimeMode.isProduction() && psrExamEnFilter.isAboveLevelEid(psrExamContext, requiredKp, psrExamContext.getSubject()))
                continue;

            // 不符合要求的不推荐
            if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, qid, psrExamContext.getSubject())) {
                //log.error("FindAnBadEid:" + qid);
                continue;
            }

            retList.add(qid);
            psrExamContext.getRecommendEids().add(qid);
            if (retList.size() >= psrExamContext.getECount())
                break;
        }

        return retList;
    }

    private List<EidItem> getQidsOfKpFromCouchbase(PsrExamContext psrExamContext, String kp) {
        List<EidItem> retList = new ArrayList<>();
        if (psrExamContext == null || StringUtils.isBlank(kp))
            return retList;

        EkToEidContent ekToEidContent = ekCouchbaseDao.getEkToEidContentFromCouchbase(kp, psrExamContext.getGrade());
        if (ekToEidContent != null && CollectionUtils.isNotEmpty(ekToEidContent.getEidList())) {
            retList.addAll(ekToEidContent.getEidList());
        }
        Double initOffset = 0.00000001;
        Double offset = initOffset;
        EkToNewEidContent ekToNewEidContent = ekCouchbaseDao.getEkToNewEidContentFromCouchbase(kp, psrExamContext.getGrade());
        if (ekToNewEidContent != null && CollectionUtils.isNotEmpty(ekToNewEidContent.getEidList())) {
            for (String qid : ekToNewEidContent.getEidList()) {
                EidItem item = new EidItem();
                item.setEid(qid);
                item.setIrtA(1.0D + offset);
                item.setIrtB(0.0D + offset);
                item.setIrtC(0.0D + offset);
                item.setAccuracyRate(0.0D);
                item.setAllCount(0);
                retList.add(item);
                offset += initOffset;
            }
        }

        return retList;
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
        String strLog = "[PsrSelfStudyRecomByKp:return code:"+ errorMsg +"][retcount:" + retMap.size() + "]";
        strLog += retMap.toString();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " qids:" + retMap.keySet().toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }

}

