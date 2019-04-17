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
package com.voxlearning.utopia.service.psr.impl.exammath;

/**
 * Created by Administrator on 2016/4/21.
 */

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.athena.PsrGoalBalance;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContextInstance;
import com.voxlearning.utopia.service.psr.impl.data.PsrAdaptivePaperQids;
import com.voxlearning.utopia.service.psr.impl.data.PsrAdaptivePreUnitQids;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.service.PsrAdaptivePaperConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Named
@Data
public class PsrExamMathController implements Serializable {

    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrExamContextInstance psrExamContextInstance;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrExamMathCore psrExamMathCore;
    @Inject private PsrExamABTest psrExamABTest;
    @Inject private PsrAdaptivePaperQids psrAdaptivePaperQids;
    @Inject private PsrAdaptivePaperConfig psrAdaptivePaperConfig;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrAdaptivePreUnitQids psrAdaptivePreUnitQids;
    @Inject private PsrGoalBalance psrGoalBalance;

    private EkEidListContent fillEkEidListContentDefault(){
        Map<String, List<String>> defaultInfoMap = new LinkedHashMap<>();
        List<String> info = new ArrayList<>();

        info.add("Q_10200736117864-1:1021003:0.9181286549707602");
        defaultInfoMap.put("KP_10200045895244",info);

        info = new ArrayList<>();
        info.add("Q_10200745880494-1:1021002:0.8898809523809523");
        defaultInfoMap.put("KP_10200045896519",info);

        info = new ArrayList<>();
        info.add("Q_10200764024109-1:1021006:0.9252336448598131");
        info.add("Q_10200764038080-1:1021006:0.8214285714285714");
        defaultInfoMap.put("KP_10200045851465",info);

        info = new ArrayList<>();
        info.add("Q_10200768262172-1:1021006:0.978021978021978");
        info.add("Q_10200777869397-1:1021002:0.9759036144578314");
        info.add("Q_10200768216061-1:1021006:0.975609756097561");
        info.add("Q_10200794852425-1:1021007:0.9666666666666667");
        info.add("Q_10200777775490-1:1021002:0.9554794520547946");
        info.add("Q_10200777805721-1:1021002:0.9544235924932976");
        info.add("Q_10200768241252-1:1021006:0.95");
        info.add("Q_10200768254733-1:1021006:0.9448818897637795");
        defaultInfoMap.put("KP_10200045864686",info);

        info = new ArrayList<>();
        info.add("Q_10200769019016-1:1021004:0.9571428571428572");
        info.add("Q_10200691581805-1:1021004:0.9475806451612904");
        info.add("Q_10200773645249-1:1021006:0.9298245614035088");
        info.add("Q_10200699439408-1:1021004:0.9038461538461539");
        info.add("Q_10200775606859-1:1021006:0.9");
        info.add("Q_10200652369752-1:1021001:0.872093023255814");
        info.add("Q_10200775461599-1:1021006:0.8227848101265823");
        defaultInfoMap.put("KP_10200045843937",info);

        info = new ArrayList<>();
        info.add("Q_10200779385228-1:1021002:0.9444444444444444");
        defaultInfoMap.put("KP_10200045860971",info);

        EkEidListContent ekEidListContentDefault = new EkEidListContent();
        for (Map.Entry<String, List<String>> entry : defaultInfoMap.entrySet()) {
            String ek = entry.getKey();
            List<String> eidInfos = entry.getValue();
            //List<EidItem> eidList = new ArrayList<>();
            if (eidInfos != null){
                for(String eidInfo:eidInfos){
                    String[] elements = eidInfo.split(":");
                    if (elements.length == 3){
                        EidItem eidItem = new EidItem();
                        eidItem.setEid(elements[0]);
                        eidItem.setEt(elements[1]);
                        eidItem.setPredictRate(PsrTools.stringToDouble(elements[2]));
                        //eidList.add(eidItem);
                        ekEidListContentDefault.addItemByEk(ek,eidItem);
                    }
                }
            }
        }
        return ekEidListContentDefault;
    }

    public PsrExamContent deal(String product, String uType,
                               Long userId, int regionCode, String bookId, String unitId, int eCount,
                               float minP, float maxP, int grade) {

        PsrExamContext psrExamContext = psrExamContextInstance.getAndInitPsrExamEnContext(product,
                uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade, Subject.MATH);

        // 使用goal来推荐 新版数学教材
        PsrExamContent retContent = psrGoalBalance.balanceWithGoalPsr(psrExamContext);
        if (retContent.getExamList().size() >= psrExamContext.getECount())
            return retContent;

        return dealAll(psrExamContext);
    }

    public PsrExamContent dealAll(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrExamContent retExamContent = new PsrExamContent();

        if (psrExamContext == null)
            return logContent(retExamContent, null, "PsrExamMathController dealAll psrExamContext is null.", dtB, "error");

        String unitBak = psrExamContext.getUnitId();
        if (psrExamContext.isExamEnAfentiQuiz())
            psrExamContext.setUnitId("-1");

        // 是否AB test
        retExamContent = psrExamABTest.dealABTest(retExamContent, psrExamContext, Subject.MATH);

        String psrExamType = "psrexammath";
        // 根据Uid 取出 learning_profile 的 Ek-list, From couchbase
        UserExamContent userExamContent = psrExamContext.getUserExamContentId();
        if (userExamContent == null || userExamContent.isEkListNull() || userExamContent.getEkList().size() <= 0) {
            // 自适应补题逻辑
            psrExamContext.setAdaptive(true);
            psrExamContext.setPsrExamType(psrExamType + "_id_std");
        } else {
            // 个性化逻辑
            psrExamContext.setAdaptive(false);
            userExamContent.setUserInfoLevel(0);
            psrExamContext.setPsrExamType(psrExamType + "_id_p");
        }
        retExamContent = dealCore(retExamContent, psrExamContext);

        // 使用Paper补题 fixme del
        if (psrExamContext.getAdaptivePaperLevel() == 1
                || (psrExamContext.getAdaptivePaperLevel()==2 && psrAdaptivePaperConfig.isAdaptivePaperByBookId(psrExamContext.getBookId())))
            retExamContent = dealWithPaperQuestionIds(retExamContent, psrExamContext);

        // 使用之前推过的补充
        if (psrExamContext.getAdaptiveDefaultPreUnitLevel() >= 1 && psrExamContext.getGrade() == 1)
            retExamContent = dealWithDefaultPreUnitQuestionIds(retExamContent, psrExamContext);

        // 默认的psr逻辑 , 更新数据库
        psrExamEnData.updateHistory(retExamContent, psrExamContext, Subject.MATH);

        Integer leftCount =   psrExamContext.getECount() - retExamContent.getExamList().size();
        if (leftCount > 0) {
            EkEidListContent ekEidListContentDefault = fillEkEidListContentDefault();
            retExamContent.setExamListByEkEidListContent(ekEidListContentDefault, leftCount, "std_default", psrExamType + "_id_std_default");
            retExamContent.setErrorContent("success");

            // 自杀式补题,打log查问题,如果教材下挂载的题量少,通知内容补题
            log.info("ExamMathNotEnoughEids book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId());
        }

        // sort AB test 的数据置末尾
        retExamContent = sortResult(retExamContent);

        psrExamContext.setUnitId(unitBak);
        return logContent(retExamContent, psrExamContext, "success", dtB, "info");

    }

    private PsrExamContent sortResult(PsrExamContent result) {
        PsrExamContent retExamContent = new PsrExamContent();

        if (result == null || !result.isSuccess())
            return retExamContent;

        retExamContent.setErrorContent(result.getErrorContent());
        retExamContent.setEids(result.getEids());
        List<PsrExamItem> redmineExamList = new LinkedList<>();

        for (PsrExamItem item : result.getExamList()) {
            if (item.getAlogv().startsWith("redmine"))
                redmineExamList.add(item);
            else
                retExamContent.getExamList().add(item);
        }
        redmineExamList.forEach(p -> retExamContent.getExamList().add(p));

        return retExamContent;
    }

    public PsrExamContent dealWithPaperQuestionIds(PsrExamContent retExamContent, PsrExamContext psrExamContext) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 使用Paper补题
        EkEidListContent ekEidListContent = psrAdaptivePaperQids.dealWithPaperQids(psrExamContext);

        String algov = "std_paper";
        String psrExamType = psrExamContext.getPsrExamType() + "_" + algov;

        retExamContent.setExamListByEkEidListContent(ekEidListContent, psrExamContext.getECount() - retExamContent.getExamList().size(), algov, psrExamContext.getPsrExamType());
        retExamContent.setErrorContent("success");

        return retExamContent;
    }

    public PsrExamContent dealWithDefaultPreUnitQuestionIds(PsrExamContent retExamContent, PsrExamContext psrExamContext) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 使用Paper补题
        EkEidListContent ekEidListContent = psrAdaptivePreUnitQids.dealWithPreUnitQids(psrExamContext);

        String algov = "std_def_preunit";
        String psrExamType = psrExamContext.getPsrExamType() + "_std_def_preunit";

        retExamContent.setExamListByEkEidListContent(ekEidListContent, psrExamContext.getECount() - retExamContent.getExamList().size(), algov, psrExamType);
        retExamContent.setErrorContent("success");

        return retExamContent;
    }

    public PsrExamContent dealCore(PsrExamContent retExamContent, PsrExamContext psrExamContext) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 个性化推荐 推题 算法
        EkEidListContent ekEidListContent = getEkEidListContentBookByBookDesc(psrExamContext);

        String algov = "std2";
        UserExamContent userExamContent = psrExamContext.getUserExamContentId();
        if (userExamContent != null) {
            switch (userExamContent.getUserInfoLevel()) {
                case 0:
                    algov = "p004";
                    break;
                case 1:
                    algov = "a002";
                    break;
                case 2:
                    algov = "std2";
                    break;
                default:
                    break;
            }
        }

        retExamContent.setExamListByEkEidListContent(ekEidListContent, psrExamContext.getECount() - retExamContent.getExamList().size(), algov, psrExamContext.getPsrExamType());
        retExamContent.setErrorContent("success");

        return retExamContent;
    }

    private EkEidListContent getEkEidListContentBookByBookDesc(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return new EkEidListContent();

        // 当前课本 unit by unit
        boolean bakAdaptive = psrExamContext.isAdaptive();
        EkEidListContent ekEidListContent = getEkEidListContentUnitByUnitDesc(psrExamContext);
        if (ekEidListContent.getEids().size() >= psrExamContext.getECount())
            return ekEidListContent;

        // 如果需要补题,则向前一本教材补题
        NewBookProfile preBook = psrBooksSentencesNew.getPreBookWithSameSeriesByBookId(psrExamContext.getBookId());
        if (preBook == null || preBook.getId().equals(psrExamContext.getBookId()))
            return ekEidListContent;

        Map<Integer, String> preBookUnitRanks = psrBooksSentencesNew.getUnitsRanksByBookId(preBook.getId(), "-1");  // 前一本教材默认 -1 单元
        if (MapUtils.isEmpty(preBookUnitRanks))
            return ekEidListContent;

        String maxUnitId = preBookUnitRanks.entrySet().stream().max(Map.Entry.comparingByKey()).get().getValue();
        if (StringUtils.isBlank(maxUnitId))
            maxUnitId = "-1";

        psrExamContext.setPsrExamType(psrExamContext.getPsrExamType() + "_prebook");
        String bakBook = psrExamContext.getBookId();
        String bakUnit = psrExamContext.getUnitId();
        psrExamContext.setBookId(preBook.getId());
        psrExamContext.setUnitId(maxUnitId);
        psrExamContext.setAdaptive(true);
        ekEidListContent = getEkEidListContentUnitByUnitDesc(psrExamContext);
        psrExamContext.setBookId(bakBook);
        psrExamContext.setUnitId(bakUnit);
        psrExamContext.setAdaptive(bakAdaptive);

        return ekEidListContent;
    }

    private EkEidListContent getEkEidListContentUnitByUnitDesc(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return new EkEidListContent();

        // 判断unit的类型,module or unit
        PsrBookPersistenceNew psrBookPersistenceNew = psrExamEnData.getPsrBookPersistence(psrExamContext);
        if (psrBookPersistenceNew == null)
            return new EkEidListContent();

        if (psrExamContext.isAdaptive()) {
            // 适配逻辑,没有用户的profile,所以要初始化
            UserExamContent userExamContent = new UserExamContent();
            userExamContent.setEkList(new ArrayList<>());
            psrExamEnData.initAdaptiveUserInfo(userExamContent, psrExamContext);
            psrExamContext.setUserExamContentId(userExamContent);
        }

        psrExamEnFilter.initBookEids(psrExamContext, Subject.MATH);

        if (psrExamContext.getUnitId().equals("-1"))
            return psrExamMathCore.doCore(psrExamContext);

        EkEidListContent ekEidListContent = new EkEidListContent();
        Map<Integer, String> rankUnits = psrBooksSentencesNew.getUnitsRanksByBookId(psrExamContext.getBookId(), psrExamContext.getUnitId());
        if (MapUtils.isEmpty(rankUnits))
            return ekEidListContent;
        Map<String, Integer> unitRanks = rankUnits.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        if (MapUtils.isEmpty(unitRanks))
            return ekEidListContent;
        Integer curRank = unitRanks.containsKey(psrExamContext.getUnitId())
                ? unitRanks.get(psrExamContext.getUnitId())
                : rankUnits.entrySet().stream().min(Map.Entry.comparingByKey()).get().getKey();

        // fixme while unit from unit-n to unit-1 to prebook

        String bakUnitId = psrExamContext.getUnitId();
        for (int i = curRank; i >= 0; i--) {
            if (!rankUnits.containsKey(i))
                continue;

            psrExamContext.setUnitId(rankUnits.get(i));
            ekEidListContent = psrExamMathCore.doCore(psrExamContext);
            if (ekEidListContent.getEids().size() >= psrExamContext.getECount())
                return ekEidListContent;
        }
        psrExamContext.setUnitId(bakUnitId);

        return ekEidListContent;
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
        String strLog = retExamContent.formatList("ExamMath");
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString();
        strLog += " grade:" + Integer.valueOf(psrExamContext.getGrade()).toString() + "]";
        strLog += "[TotalTime:" + totalTime.toString() + "]";

        return strLog;
    }
}
