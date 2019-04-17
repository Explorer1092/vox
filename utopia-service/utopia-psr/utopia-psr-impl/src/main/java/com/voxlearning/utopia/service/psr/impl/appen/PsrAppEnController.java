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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrPracticeTypePersistence;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrEkRegions;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;


/*
 * @Deprecated time 2017-04-13
 * 因小应用推荐切换为新版教材库(BookId:String),采用随机推荐的逻辑,代码在 PsrAppEnNewController
 * 此处有两个逻辑,1.随机逻辑,2.之前老的算法
 * 目前本页逻辑暂停运行,仅作为老代码实现而保留文件
 */
@Slf4j
@Named
@Deprecated
public class PsrAppEnController implements Serializable {

    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrBooksSentences psrBooksSentences;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrPracticeTypePersistence psrPracticeTypePersistence;
    @Inject private PsrEkRegions psrEkRegions;

    public PsrPrimaryAppEnContent deal(String product, Long userId,
                                       int regionCode, String bookId, String unitId, int eCount,
                                       String eType) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                product, "student", userId, regionCode, bookId, unitId, eCount, 0.0F, 0.0F);
        psrExamContext.setEType(eType);

        if (psrExamContext.isExamTestFiveEid())  // 借用一下这个状态变量 默认是false
            return randomDeal(psrExamContext);

        return deal(psrExamContext);
    }

    /*
     * 随机出题
     * 不关心单元
     * 当天做过4个题型的知识点不在推荐
     */
    public PsrPrimaryAppEnContent randomDeal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrPrimaryAppEnContent retPrimaryAppEnContent = new PsrPrimaryAppEnContent();
        if (psrExamContext == null)
            return logContent(retPrimaryAppEnContent, psrExamContext, "psrExamContext err.", dtB, "error");

        // 根据bookId 取出Unit-list 和 Ek-list,From mysql
        Map<Long, List<String>> unitSentenceList = getUnitsSentenceByBookId(psrExamContext, dtB);
        if (unitSentenceList == null || unitSentenceList.size() <= 0)
            return logContent(retPrimaryAppEnContent, psrExamContext, "Not Found Default book's UnitsAndEks by bookid:" + psrExamContext.getBookId(), dtB, "error");

        List<String> eks = new ArrayList<>();
        for (Map.Entry<Long, List<String>> entry : unitSentenceList.entrySet()) {
            eks.addAll(entry.getValue());
        }

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

    public Map<Long, List<String>> getUnitsSentenceByBookId(PsrExamContext psrExamContext, Date dtB) {
        // 根据bookId 取出Unit-list 和 Ek-list,From mysql
        Map<Long, List<String>> unitSentenceList = psrBooksSentences.getUnitsSentenceByBookId(Long.parseLong(psrExamContext.getBookId()));
        if (unitSentenceList != null && unitSentenceList.size() > 0)
            return unitSentenceList;
        logContent(null, psrExamContext, "Not Found book's UnitsAndEks by bookid:" + psrExamContext.getBookId(), dtB, "error");

        // 默认教材 新版-PEP-三年级(上)
        psrExamContext.setBookId("100278");
        psrExamContext.setUnitId("-1");
        return psrBooksSentences.getUnitsSentenceByBookId(Long.parseLong(psrExamContext.getBookId()));
    }

    public PsrPrimaryAppEnContent deal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrPrimaryAppEnContent retPrimaryAppEnContent = new PsrPrimaryAppEnContent();

        if (psrExamContext == null)
            return logContent(retPrimaryAppEnContent, psrExamContext, "psrExamContext is null.", dtB, "error");
        if (ekCouchbaseDao == null || psrBooksSentences == null)
            return logContent(retPrimaryAppEnContent, psrExamContext, "Can not connect databases.", dtB, "error");

        // 根据Uid 取出 learning_profile 的 Ek-list, 并且把当前单元的及之前单元的 ek 全部拿出来 合并成新的 ek-list
        UserAppEnContent userAppEnContent = ekCouchbaseDao.getUserAppEnContentFromCouchbase(psrExamContext.getUserId());
        if (userAppEnContent == null)
            userAppEnContent = new UserAppEnContent();

        if (userAppEnContent.isEkMapNull() || userAppEnContent.getEkMap().size() <= 0) {
            String strLog = "[NotFoundUserAppEnContent userId:" + psrExamContext.getUserId() + "]";
            log.debug(strLog);
        }

        // 根据bookId 取出Unit-list 和 Ek-list,From mysql
        Map<Long, List<String>> unitSentenceList = getUnitsSentenceByBookId(psrExamContext, dtB);
        if (unitSentenceList == null)
            unitSentenceList = new LinkedHashMap<>();

        if (unitSentenceList.size() <= 0)
            return logContent(retPrimaryAppEnContent, psrExamContext, "Not Found Default book's UnitsAndEks by bookid:" + psrExamContext.getBookId(), dtB, "error");

        // 获取用户当天实时做题数据
        // ek+et 组合 todo
        Map<String/*ek*/, List<String/*et*/>>  ekEtsCorrect = new HashMap<>();

        List<String> pointsRemove = getPointsRemoveByAppResult(psrExamContext);
        Long groupId = Long.parseLong(psrExamContext.getUnitId());
        PsrAppEnUnitIdEks psrAppEnUnitIdEks = new PsrAppEnUnitIdEks();
        psrAppEnUnitIdEks.setTodayCorrectEks(pointsRemove);
        // 新教材结构 需要把unitId 转换为groupId
        PsrBookPersistence psrBookPersistence = psrBooksSentences.getBookPersistenceByBookId(Long.parseLong(psrExamContext.getBookId()));
        if (psrBookPersistence != null && psrBookPersistence.getBookStructure() == 1)
            groupId = psrBookPersistence.getGroupIdByUnitId(Long.parseLong(psrExamContext.getUnitId()));
        if (groupId.equals(Long.parseLong(psrExamContext.getUnitId())) && groupId == -1)
            groupId = Long.parseLong(psrExamContext.getUnitId());
        Map<String/*ek*/, UserAppEnEkItem> rightEksMap = psrAppEnUnitIdEks.getUnitIdEks(groupId, userAppEnContent, unitSentenceList);

        for (String rmek : pointsRemove) {
            if (rightEksMap.containsKey(rmek))
                rightEksMap.remove(rmek);
        }

        if (rightEksMap == null || rightEksMap.size() < psrExamContext.getECount())
            rightEksMap = psrAppEnUnitIdEks.getMoreUnitIdEks(psrExamContext.getECount());

        // 没有知识点 则返回错误信息
        if (rightEksMap == null || rightEksMap.size() <= 0) {
            return logContent(retPrimaryAppEnContent, psrExamContext,
                    "NotFoundEnoughEids, removeEkOnline:" + Integer.valueOf(pointsRemove.size()).toString(),
                    dtB, "error");
        }

        // 允许 ek 重复存在,即使有一个ek可以正常处理
        // 把ek计算weight 并且按 weight 降序排列
        List<UserAppEnEkItem> rightEksListSort = getUserAppEnEkItemsWeight(rightEksMap, psrExamContext.getRegionCode());

        if (rightEksListSort == null || rightEksListSort.size() <= 0)
            return logContent(retPrimaryAppEnContent, psrExamContext, "NotFoundEnoughUserAppEnEkItemsWeight", dtB, "error");

        String algov = "ae01";  // application english

        // 轮盘算法
        int index = 0;
        int baseNumberForWeight = 100;

        boolean isEnoughEks = true;
        if (rightEksMap.size() < psrExamContext.getECount()) {
            // 如果取出的知识点 不够, 则会重复推荐
            isEnoughEks = false;
        }

        int lastItemCount = 0;
        while (retPrimaryAppEnContent.getAppEnList().size() < psrExamContext.getECount()) {
            if (lastItemCount++ > psrExamContext.getMaxEidCount()) {
                log.warn("get ek time > " + psrExamContext.getMaxEidCount() + " userId " + psrExamContext.getUserId() + " bookId " + psrExamContext.getBookId());
                break;
            }

            Random random = psrExamContext.getRandom();
            double randomTd;
            randomTd = random.nextInt(1000000);
            double weightTmp = randomTd / 1000000 * baseNumberForWeight;

            double weightNsum = 0.0;
            for (int i = 0; i < rightEksListSort.size(); i++) {
                UserAppEnEkItem userAppEnEkItem = rightEksListSort.get(i);

                weightNsum += userAppEnEkItem.getWeightPer();

                boolean bGet = false;
                if (weightTmp <= weightNsum) {
                    bGet = true;
                }
                if (!bGet && i == rightEksListSort.size() - 1) {
                    // 取最后一个item吧
                    bGet = true;
                }

                if (bGet) {
                    PsrPrimaryAppEnItem appPrimaryEnItem = new PsrPrimaryAppEnItem();
                    appPrimaryEnItem.setEid(userAppEnEkItem.getEk());
                    if (!StringUtils.isEmpty(psrExamContext.getEType()))
                        appPrimaryEnItem.setEType(psrExamContext.getEType());
                    else {
                        // 剔除当天推过的题型
                        List<String> rmEts = null;
                        if (ekEtsCorrect.containsKey(userAppEnEkItem.getEk()))
                            rmEts = ekEtsCorrect.get(userAppEnEkItem.getEk());
                        if (userAppEnEkItem.getStatus() == 'E') {
                            String et = userAppEnEkItem.getPattern(rmEts);
                            if (StringUtils.isEmpty(et)) {
                                log.error("not found ets by Ek:" + userAppEnEkItem.getEk() + " userId:" + psrExamContext.getUserId());
                                appPrimaryEnItem.setEType(userAppEnEkItem.getRandomPattern(rmEts, random));
                            } else {
                                appPrimaryEnItem.setEType(et);
                            }
                        } else {
                            // 随机四个 类型中的一个
                            appPrimaryEnItem.setEType(userAppEnEkItem.getRandomPattern(rmEts, random));
                        }
                    }
                    appPrimaryEnItem.setWeight(userAppEnEkItem.getWeight());
                    appPrimaryEnItem.setStatus(userAppEnEkItem.getStatus());
                    appPrimaryEnItem.setAlgov(algov);

                    retPrimaryAppEnContent.getAppEnList().add(appPrimaryEnItem);

                    // 当获取的知识点不够时,则不剔除,会重复推题
                    if (isEnoughEks)
                        rightEksListSort.remove(i);

                    break;
                } // end if
            }
        }

        if (retPrimaryAppEnContent.getAppEnList().size() < psrExamContext.getECount()) {
            return logContent(retPrimaryAppEnContent, psrExamContext,
                    "not found enough eks but why," + retPrimaryAppEnContent.getAppEnList().size() + " < " + psrExamContext.getECount(),
                    dtB, "info");
        }
        return logContent(retPrimaryAppEnContent, psrExamContext, "success", dtB, "info");
    }

    public List<String> getPointsRemoveByAppResult(PsrExamContext psrExamContext) {
        List<String> pointsRemove = new ArrayList<>();
        return pointsRemove;

        /*
        List<EnglishAppResult> appResults = psrEnglishAppResultCacheDao.findByUserId(psrExamContext.getUserId());
        if (appResults != null && appResults.size() > 0) {
            // 计算当天 做对 的知识点题型列表
            for (EnglishAppResult item : appResults) {
                if (!item.getAtag())
                    continue;
                String ek = "word#" + item.getEk_list();
                String et = psrPracticeTypePersistence.findAppEnPatternById(item.getPractice_id());
                if (StringUtils.isEmpty(et))
                    continue;
                List<String> ets = null;
                if (!ekEtsCorrect.containsKey(ek)) {
                    ets = new ArrayList<>();
                } else {
                    ets = ekEtsCorrect.get(ek);
                }
                if (!ets.contains(et))
                    ets.add(et);
                ekEtsCorrect.put(ek, ets);
                // 该ek的4个et都做对了,就没有题型可推荐了,so也不推荐该ek了
                if (ets.size() >= 4 && !pointsRemove.contains(ek))
                    pointsRemove.add(ek);
            }
        }
        */
    }

    public PsrPrimaryAppEnUserEks getPrimaryAppEnEksByUserId(String product, Long userId) {
        Date dtB = new Date();

        PsrPrimaryAppEnUserEks retPrimaryAppEnUserEks = new PsrPrimaryAppEnUserEks();

        // 日志 及返回值
        if (ekCouchbaseDao == null) {
            retPrimaryAppEnUserEks.setErrorContent("Can not connect couchbase");
            String strLog = retPrimaryAppEnUserEks.getErrorContent() + " [product:" + product + " userId:" + userId.toString() + "][TotalTime:0]";
            log.warn(strLog);
            return retPrimaryAppEnUserEks;
        }

        /*
         * 根据Uid 取出 learning_profile 的 Ek-list
         */
        UserAppEnContent userAppEnContent = ekCouchbaseDao.getUserAppEnContentFromCouchbase(userId);

        if (userAppEnContent == null || userAppEnContent.isEkMapNull() || userAppEnContent.getEkMap().size() <= 0) {
            retPrimaryAppEnUserEks.setErrorContent("NotFoundUserAppEnContent userId");
            String strLog = retPrimaryAppEnUserEks.getErrorContent() + " [product:" + product + " userId:" + userId.toString() + "][TotalTime:0]";
            log.warn(strLog);
            return retPrimaryAppEnUserEks;
        }

        for (Map.Entry<String, UserAppEnEkItem> entry : userAppEnContent.getEkMap().entrySet()) {
            PsrPrimaryAppEnUserEkItem item = new PsrPrimaryAppEnUserEkItem();
            item.setEk(entry.getValue().getEk());
            item.setStatus(entry.getValue().getStatus());

            retPrimaryAppEnUserEks.getEkList().add(item);
        }

        retPrimaryAppEnUserEks.setErrorContent("success");

//        Date dtE = new Date();
//        Long uTAll = dtE.getTime() - dtB.getTime();
//        String strLog = retPrimaryAppEnUserEks.formatList() + " [product:" + product + " userId:" + userId.toString() + "][TotalTime:" + uTAll.toString() + "]";
//        log.info(strLog);

        return retPrimaryAppEnUserEks;
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

    private List<UserAppEnEkItem> getUserAppEnEkItemsWeight(Map<String, UserAppEnEkItem> rightEksMap, int regionCode) {
        List<UserAppEnEkItem> userAppEnEkItems = new LinkedList<>();

        // 计算ek weight 时 需要查询 ek对应region下的热度
        EkRegionItem ekRegionItem = null;

        Double weightSum = 0.0;

        Double statusWeight = 0.0;
        Double outOfDayWeight = 0.0;
        Double accuracyRateWeight = 0.0;
        Double hotLevelWeight = 0.0;
        for (Map.Entry<String, UserAppEnEkItem> entry : rightEksMap.entrySet()) {
            UserAppEnEkItem item = new UserAppEnEkItem();
            item.setEk(entry.getKey());
            item.setStatus(entry.getValue().getStatus());
            item.setDays(entry.getValue().getDays());
            item.setAccuracyRate(entry.getValue().getAccuracyRate());
            item.setTypeRight(entry.getValue().getTypeRight());

            statusWeight = item.getStatusWeight();
            // todo 冷冻期内的 知识点怎么处理？ 剔除 还是 设置权重,剔除后不够 怎么办？
            outOfDayWeight = item.getOutOfDaysWeight();

            accuracyRateWeight = item.getAccuracyRateWeight();

            // fixme 知识点信息已经分年级存储,这个地方需要优化,暂时停用
//            ekRegionItem = psrEkRegions.getEkRegionItemByEk(entry.getKey(), regionCode);
//            if (ekRegionItem != null)
//                hotLevelWeight = ekRegionItem.getHotLevel() * 30;   // todo 30 可配置
//            else
//                hotLevelWeight = 0.0;

            item.setWeight(statusWeight + outOfDayWeight + accuracyRateWeight + hotLevelWeight);
            weightSum += item.getWeight();

            userAppEnEkItems.add(item);
        }

        Collections.sort(userAppEnEkItems, new Comparator<UserAppEnEkItem>() {
            @Override
            public int compare(UserAppEnEkItem o1, UserAppEnEkItem o2) {
                int n = 0;
                if (o2.getWeight() - o1.getWeight() < 0.0)
                    n = 1; // 升序
                else if (o2.getWeight() - o1.getWeight() > 0.0)
                    n = -1;
                return n;
            }
        });

        //weightPer
        for (int i = 0; weightSum > 0 && i < userAppEnEkItems.size(); i++) {
            userAppEnEkItems.get(i).setWeightPer(userAppEnEkItems.get(i).getWeight() / weightSum * 100);
        }

        return userAppEnEkItems;
    }
}

class Rate {
    Integer eidCount;
    Integer eidRightCount;

    public Rate() {
        eidCount = 0;
        eidRightCount = 0;
    }
}