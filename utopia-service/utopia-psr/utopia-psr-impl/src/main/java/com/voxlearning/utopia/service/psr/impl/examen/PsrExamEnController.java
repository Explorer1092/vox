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

package com.voxlearning.utopia.service.psr.impl.examen;

//import com.voxlearning.utopia.entity.content.UnitKnowledgePointRef;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentences;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.athena.PsrGoalBalance;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContextInstance;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrAdaptivePaperQids;
import com.voxlearning.utopia.service.psr.impl.data.PsrAdaptivePreUnitQids;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.service.PsrAdaptivePaperConfig;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrBooksPointsRef;
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
public class PsrExamEnController implements Serializable {
    @Inject private PsrConfig psrConfig;
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrBooksSentences psrBooksSentences;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrBooksPointsRef psrBooksPointsRef;
    @Inject private PsrAboveLevelBookEidsDao psrAboveLevelBookEidsDao;

    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrExamContextInstance psrExamContextInstance;

    @Inject private PsrAdaptivePaperQids psrAdaptivePaperQids;
    @Inject private PsrAdaptivePaperConfig psrAdaptivePaperConfig;
    @Inject private PsrAdaptivePreUnitQids psrAdaptivePreUnitQids;

    @Inject private PsrGoalBalance psrGoalBalance;

    @Inject private PsrExamEnCore psrExamEnCore;
    private EkEidListContent fillEkEidListContentDefault(){
        Map<String, List<String>> defaultInfoMap = new LinkedHashMap<>();
        List<String> info = new ArrayList<>();

        info.add("Q_10300698408926-1:1031009:0.8483965014577259");
        defaultInfoMap.put("310302176",info);

        info = new ArrayList<>();

        info.add("Q_10300707803034-3:1031015:0.9855212355212355");
        info.add("Q_10300733693900-1:1031009:0.9790209790209791");
        info.add("Q_10300712836085-4:1031015:0.975387644597588");
        info.add("Q_10300721421569-1:1031001:0.9502890173410404");
        info.add("Q_10300721518212-1:1031009:0.948051948051948");
        info.add("Q_10300712820213-3:1031012:0.945101989423319");
        info.add("Q_10300708729989-3:1031012:0.9370629370629371");
        info.add("Q_10300699165032-1:1031008:0.9305555555555556");
        defaultInfoMap.put("310302287",info);

        info = new ArrayList<>();
        info.add("Q_10300707829552-3:1031013:0.972289156626506");
        info.add("Q_10300708713698-3:1031012:0.861788617886178");
        info.add("Q_10300721155799-1:1031010:0.813664596273291");
        info.add("Q_10300430961450-1:1031012:0.811046511627907");
        defaultInfoMap.put("310300190",info);

        info = new ArrayList<>();
        info.add("Q_10300786666968-1:1031001:0.9");
        info.add("Q_10300782186192-1:1031015:0.8716577540106952");
        info.add("Q_10300707989284-3:1031013:0.8539148712559117");
        info.add("Q_10300777248134-1:1031001:0.8417582417582418");
        info.add("Q_10300707929641-3:1031009:0.8162040510127532");
        defaultInfoMap.put("310302234",info);

        info = new ArrayList<>();
        info.add("Q_10300796953672-1:1031001:0.9323671497584541");
        info.add("Q_10300697827253-1:1031002:0.8743718592964824");
        defaultInfoMap.put("310300294",info);

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
                uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade, Subject.ENGLISH);

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
            return logContent(retExamContent, null, "PsrExamEnController dealAll psrExamContext is null.", dtB, "error");

        String unitBak = psrExamContext.getUnitId();
        if (psrExamContext.isExamEnAfentiQuiz())
            psrExamContext.setUnitId("-1");

        String psrExamType = "psrexam";

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
        psrExamEnData.updateHistory(retExamContent, psrExamContext, Subject.ENGLISH);
        Integer leftCount =   psrExamContext.getECount() - retExamContent.getExamList().size();
        if (leftCount > 0) {
            EkEidListContent ekEidListContentDefault = fillEkEidListContentDefault();
            retExamContent.setExamListByEkEidListContent(ekEidListContentDefault, leftCount, "std_default", psrExamType + "_id_std_default");
            retExamContent.setErrorContent("success");
        }

        psrExamContext.setUnitId(unitBak);
        return logContent(retExamContent, psrExamContext, "success", dtB, "info");
    }

    public PsrExamContent dealWithPaperQuestionIds(PsrExamContent retExamContent, PsrExamContext psrExamContext) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 使用Paper补题
        EkEidListContent ekEidListContent = psrAdaptivePaperQids.dealWithPaperQids(psrExamContext);

        String algov = "std_paper";
        String psrExamType = psrExamContext.getPsrExamType() + "_paper";

        retExamContent.setExamListByEkEidListContent(ekEidListContent, psrExamContext.getECount() - retExamContent.getExamList().size(), algov, psrExamType);
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

        psrExamEnFilter.initBookEids(psrExamContext, Subject.ENGLISH);

        if (psrExamContext.getUnitId().equals("-1"))
            return psrExamEnCore.doCore(psrExamContext);

        Map<Integer, String> rankUnits = psrBooksSentencesNew.getUnitsRanksByBookId(psrExamContext.getBookId(), psrExamContext.getUnitId());
        Map<String, Integer> unitRanks = rankUnits.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        Integer curRank = unitRanks.containsKey(psrExamContext.getUnitId())
                ? unitRanks.get(psrExamContext.getUnitId())
                : rankUnits.entrySet().stream().min(Map.Entry.comparingByKey()).get().getKey();

        // fixme while unit from unit-n to unit-1 to prebook
        EkEidListContent ekEidListContent = new EkEidListContent();
        String bakUnitId = psrExamContext.getUnitId();
        for (int i = curRank; i >= 0; i--) {
            if (!rankUnits.containsKey(i))
                continue;

            psrExamContext.setUnitId(rankUnits.get(i));
            ekEidListContent = psrExamEnCore.doCore(psrExamContext);
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
        String strLog = retExamContent.formatList();
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString();
        strLog += " grade:" + Integer.valueOf(psrExamContext.getGrade()).toString() + "]";
        strLog += "[TotalTime:" + totalTime.toString() + "]";

        return strLog;
    }

}


//    // fixme 2015.09.01 之后暂不开放(因题库结构升级)
//    public PsrExamContent dealBak(String product, String uType,
//        Long userId, int regionCode, Long bookId, Long unitId, int eCount,
//        float minP, float maxP) {
//
//        PsrExamContent psrExamContent = null;
//        Date dtB = new Date();
//
//        PsrExamContext psrExamContext = new PsrExamContext(psrConfig, product, uType, userId,
//                regionCode, bookId, unitId, eCount, minP, maxP);
//
//        /********************************************************************************************/
//        // 是否测验
//        // fixme 2015.09.01 之后暂不开放(因题库结构升级)
//        if (psrExamContext.isExamTestFiveEid() && psrExamEnTestFiveEids.isExam(psrExamContext)) {
//            psrExamContext.setPsrExamType("examtestfiveeids");
//            psrExamContent = psrExamEnTestFiveEids.deal(psrExamContext);
//            if (psrExamContent.isSuccess() && psrExamContent.getExamList().size() >= eCount)
//                return psrExamContent;
//        }
//
//        /********************************************************************************************/
//        // 是否测验
//        // fixme 2015.09.01 之后暂不开放(因题库结构升级)
//        if (psrExamContext.isExamination() && psrExaminationCache.isExam(psrExamContext)) {
//            psrExamContext.setPsrExamType("examination");
//            psrExamContent = psrExamination.deal(psrExamContext);
//            if (psrExamContent.isSuccess() && psrExamContent.getExamList().size() >= eCount)
//                return psrExamContent;
//        }
//
//
//        /********************************************************************************************/
//        // 分组
//        // fixme 2015.09.01 之后暂不开放(因题库结构升级)
//        String group = "defaultGroup";
//        Map<String, Double> tmpMap = null;  // 每个group 对应的task列表
//
//        // 获取用户分组后的任务,如果没有任务,则走原来的psr逻辑
//        if (psrExamContext.isUserGroup()) {
//            group = psrGroupUserTask.getTaskByUid(userId);
//            tmpMap = psrGroupTask.getTask(group);
//        }
//
//        if (group.equals("defaultGroup") || tmpMap == null || tmpMap.size() <= 0) {
//            // 默认psr逻辑
//            psrExamContext.setPsrExamType("psrexam");
//            return dealAll(psrExamContext);
//        }
//
//        if (psrExamContent == null)
//            psrExamContent = new PsrExamContent();
//
//        psrExamContext.setWriteLog(false);
//        // 分组逻辑
//        for (Map.Entry<String,Double> entry : tmpMap.entrySet()) {
//            int count = 0;
//            boolean isSimilar = false;
//            switch (entry.getKey()) {
//                case "userLevelA" : // 难度=0.6
//                    psrExamContext.setMinP(0.6F);
//                    psrExamContext.setMaxP(0.7F);
//                    psrExamContext.setPsrExamType("userLevelA");
//                    break;
//                case "userLevelB" : // 难度=0.7
//                    psrExamContext.setMinP(0.7F);
//                    psrExamContext.setMaxP(0.8F);
//                    psrExamContext.setPsrExamType("userLevelB");
//                    break;
//                case "userLevelC" : // 难度=0.8
//                    psrExamContext.setMinP(0.8F);
//                    psrExamContext.setMaxP(0.9F);
//                    psrExamContext.setPsrExamType("userLevelC");
//                    break;
//                case "psrExam" : // 默认psr逻辑
//                    psrExamContext.setMinMaxP(minP, maxP);
//                    psrExamContext.setPsrExamType("psrExam");
//                    break;
//                case "similar" :    // 类题错题
//                    psrExamContext.setMinMaxP(minP, maxP);
//                    psrExamContext.setPsrExamType("similar");
//                    isSimilar = true;
//                    break;
//                default:
//                    count = 9999;
//                    break;
//            }
//            if (count == 9999)
//                continue;
//
//            count = Double.valueOf(entry.getValue() * eCount).intValue();
//            count = Math.min(eCount - psrExamContent.getExamList().size(), count);
//
//            if (count == 0)
//                continue;
//
//            psrExamContext.setECount(count);
//
//            if (isSimilar) {
//                PsrExamEnSimilarContent psrExamEnSimilarContent = psrExamEnSimilarController.dealCore(psrExamContext);
//                if (psrExamEnSimilarContent != null && psrExamEnSimilarContent.isSuccess())
//                    psrExamContent.addToExamList(psrExamEnSimilarContent);
//            } else {
//                PsrExamContent tmpExamContent = dealAll(psrExamContext);
//                if (tmpExamContent != null && tmpExamContent.isSuccess())
//                    psrExamContent.addToExamList(tmpExamContent);
//            }
//
//            if (psrExamContent.getExamList().size() >= eCount)
//                break;
//        }
//
//        // 如果没获取足够多的题
//        if (psrExamContent.getExamList().size() < eCount) {
//            psrExamContext.setMinMaxP(minP, maxP);
//            psrExamContext.setPsrExamType("defaultGroup");
//            psrExamContext.setECount(eCount - psrExamContent.getExamList().size());
//
//            PsrExamContent tmpExamContent = dealAll(psrExamContext);
//
//            // add to psrExamContent
//            psrExamContent.addToExamList(tmpExamContent);
//        }
//
//        // 日志需要
//        psrExamContext.setMinP(minP);
//        psrExamContext.setMaxP(maxP);
//        psrExamContext.setECount(eCount);
//
//        // 完成分组逻辑,记录推题结果及打印日志
//        updateHistory(psrExamContent, psrExamContext);
//
//        return logContent(psrExamContent, psrExamContext, "success", dtB, "info");
//    }

//    public PsrExamContent dealAll(PsrExamContext psrExamContext) {
//        Date dtB = new Date();
//        PsrExamContent retExamContent = new PsrExamContent();
//
//        if (psrExamContext == null) {
//            return logContent(retExamContent, null, "PsrExamEnController dealAll psrExamContext is null.", dtB, "error");
//        }
//
//        if (ekCouchbaseDao == null || unitKnowledgePointRefDao == null || psrEnglishExamResultMemcachedDao == null)
//            return logContent(retExamContent, psrExamContext, "Can not connect databases.", dtB, "error");
//
//        if (psrBooksSentences != null)
//            psrExamContext.setPsrBookPersistence(psrBooksSentences.getBookPersistenceByBookId(psrExamContext.getBookId()));
//
//        // todo 根据bookId 取出Unit-list 和 Ek-list,From mysql, 用于超纲判断 * 可配置 是否启用此功能, 如果不启用 就不对eks 按教材范围过滤.
//        // fixme del
////        List<UnitKnowledgePointRef> unitKnowledgePointRefs = psrExamContext.getUnitKnowledgePointRefs();
////        if (unitKnowledgePointRefs == null) {
////            unitKnowledgePointRefs = psrBooksPointsRef.getUnitKnowledgePointRefs(psrExamContext.getBookId());
////            if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
////                return logContent(retExamContent, psrExamContext, "NotFoundUnitsAndEks book:" + psrExamContext.getBookId(), dtB, "error");
////            psrExamContext.setUnitKnowledgePointRefs(unitKnowledgePointRefs);
////        }
//
//        List<UnitKnowledgePointRef> unitKnowledgePointRefs = psrExamEnData.getUnitKnowledgePointRefs(psrExamContext);
//        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
//            return logContent(retExamContent, psrExamContext, "NotFoundUnitsAndEks book:" + psrExamContext.getBookId(), dtB, "error");
//
//
//        // 根据Uid 取出 learning_profile 的 Ek-list, From couchbase
//        // fixme del
////        UserExamContent userExamContentText = psrExamContext.getUserExamContentText();
////        if (userExamContentText == null) {
////            userExamContentText = (UserExamContent) ekCouchbaseDao.getCouchbaseData(psrExamContext.getUType(), psrExamContext.getUserId().toString());
////            psrExamContext.setUserExamContentText(userExamContentText);
////        }
////        UserExamContent userExamContentId = psrExamContext.getUserExamContentId();
////        if (userExamContentId == null) {
////            userExamContentId = (UserExamContent) ekCouchbaseDao.getCouchbaseDataId(psrExamContext.getUType(), psrExamContext.getUserId().toString());
////            psrExamContext.setUserExamContentId(userExamContentId);
////        }
//
//        UserExamContent userExamContentId = psrExamEnData.getUserExamContentId(psrExamContext);
//
//        // 取出当天 用户的做题数据
//        // fixme del
////        List<QuestionResultLog> examResults = psrExamEnQuestionResultLog.getQuestionResultLog(psrExamContext); //psrExamContext.getExamResults();
////        if (examResults == null) {
////            examResults = psrEnglishExamResultMemcachedDao.findByUserId(psrExamContext.getUserId());
////            psrExamContext.setExamResults(examResults);
////        }
//
//        List<QuestionResultLog> examResults = psrExamEnData.getQuestionResultLog(psrExamContext);
//
//        // 取出用户的uc
//        // fixme del
////        UserExamUcContent userExamUcContentId = psrExamContext.getUserExamUcContentId();
////        if (userExamContentId == null) {
////            userExamUcContentId = (UserExamUcContent) ekCouchbaseDao.getCouchbaseDataId("uc", psrExamContext.getUserId().toString());
////            psrExamContext.setUserExamContentId(userExamContentId);
////        }
////        UserExamUcContent userExamUcContentText = psrExamContext.getUserExamUcContentText();
////        if (userExamUcContentText == null) {
////                userExamUcContentText = (UserExamUcContent) ekCouchbaseDao.getCouchbaseData("uc", psrExamContext.getUserId().toString());
////            psrExamContext.setUserExamUcContentText(userExamUcContentText);
////        }
//
//        UserExamUcContent userExamUcContentId = psrExamEnData.getUserExamUcContentId(psrExamContext);
//
//        String psrExamType = psrExamContext.getPsrExamType();
//        while (true) {
//            psrExamContext.setPointId(true);
//            psrExamContext.setGetMore(false);
//            psrExamContext.setPsrExamType(psrExamType + "_id_p");
//            if (psrExamContext.isExamEnUseId())
//                retExamContent = dealCore(retExamContent, psrExamContext);
//            if (retExamContent.isSuccess() && retExamContent.getExamList().size() >= psrExamContext.getECount())
//                break;
//
//            // fixme del
////            psrExamContext.setPointId(false);
////            psrExamContext.setGetMore(false);
////            psrExamContext.setPsrExamType(psrExamType + "_text_p");
////            retExamContent = dealCore(retExamContent, psrExamContext);
////            if (retExamContent.isSuccess() && retExamContent.getExamList().size() >= psrExamContext.getECount())
////                break;
//
//            psrExamContext.setPointId(true);
//            psrExamContext.setGetMore(true);
//            psrExamContext.setPsrExamType(psrExamType + "_id_std");
//            if (psrExamContext.isExamEnUseId())
//                retExamContent = dealCore(retExamContent, psrExamContext);
//            if (retExamContent.isSuccess() && retExamContent.getExamList().size() >= psrExamContext.getECount())
//                break;
//
//            // fixme del
////            psrExamContext.setPointId(false);
////            psrExamContext.setGetMore(true);
////            psrExamContext.setPsrExamType(psrExamType + "_text_std");
////            retExamContent = dealCore(retExamContent, psrExamContext);
//
//            break;
//        }
//
//        if (psrExamContext.getDebugLogLevel() > 0) {
//            String strLog = "";
//            if (psrExamContext.getPsrUserHistoryEid() != null)
//                strLog = psrExamContext.getPsrUserHistoryEid().formatEidMasterMapToString();
//
//            log.info("[Couchbase Debug][uid:"+ psrExamContext.getUserId() +"] ["+ strLog +"]");
//        }
//
//        // 默认的psr逻辑 , 更新数据库
//        if ((psrExamContext.isWriteLog() && !psrExamContext.getProduct().equals("TEST")) || psrExamContext.getUserId().equals(339596595L))
//            updateHistory(retExamContent, psrExamContext);
//
//        return logContent(retExamContent, psrExamContext, "success", dtB, "info");
//    }

