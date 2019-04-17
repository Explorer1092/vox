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

package com.voxlearning.utopia.service.psr.impl.appmath;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomProvider;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrPracticeTypePersistence;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Named
@Deprecated
public class PsrAppMathController implements Serializable {
    private static final long serialVersionUID = -5665972091473036957L;
    //private static final long THRESHOLD = 86400L; // 一天

    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrMathBooksPoints psrMathBooksPoints;
    @Inject private PsrMathPointsRelationship psrMathPointsRelationship;
    @Inject private PsrPracticeTypePersistence psrPracticeTypePersistence;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrMathBasePersistence psrMathBasePersistence;
    @Inject private PsrMathPointsPatternsScore psrMathPointsPatternsScore;

    public PsrPrimaryAppMathContent deal(String product, Long userId,
                                         int regionCode, String bookId, String unitId, int eCount) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig, product, "student", userId,
                regionCode, bookId, unitId, eCount, 0.0F, 0.0F);
        if (psrExamContext.isExamTestFiveEid()) { // 借用一下这个状态变量 默认是false
            // 分组条件,Uid 百位是偶数且个位数不为零

            Long posOfHundred = psrExamContext.getUserId() / 100;
            String userBucket = "equal";

            if (psrExamContext.isUserGroup() && posOfHundred % 2 == 0L && psrExamContext.getUserId() % 10 != 0) {
                //return kpScoreDeal(psrExamContext);
                userBucket = "retain";
            }

            //Map<String/*pattern*/, KeyValuePair<Integer, Double>> patterns = psrMathPointsPatternsScore.getScoreByPointName("point#一个因数中间有0的乘法（竖式运算）");
            return weightRandomDeal(psrExamContext, userBucket);
        }
        return randomDeal(psrExamContext);
        //return deal(psrExamContext);
    }


    public PsrPrimaryAppMathContent weightRandomDeal(PsrExamContext psrExamContext, String userBucket) {
        Date dtB = new Date();
        PsrPrimaryAppMathContent retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (psrExamContext == null)
            return logContent(retPrimaryAppMathContent, psrExamContext, "psrExamContext err.", dtB, "error");

        // 取出book中的全部points, points 按先学后学顺序 排序
        List<String> pointsOfBook = getPointsByBookId(psrExamContext, dtB);
        if (pointsOfBook == null || pointsOfBook.size() <= 0)
            return logContent(retPrimaryAppMathContent, psrExamContext,
                    "Can not found default book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        retPrimaryAppMathContent = getPsrPrimaryAppMathContent(pointsOfBook, psrExamContext, userBucket);
        return logContent(retPrimaryAppMathContent, psrExamContext, "success", dtB, "info");
    }

    public PsrPrimaryAppMathContent getPsrPrimaryAppMathContent(List<String> pointsOfBook, PsrExamContext psrExamContext, String userBucket) {

        PsrPrimaryAppMathContent psrPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (pointsOfBook == null || pointsOfBook.size() <= 0 || psrExamContext == null)
            return psrPrimaryAppMathContent;

        List<PsrPrimaryAppMathItem> appMathList = new ArrayList<>();
        Map<String, String> ekEtCount = getEkEtHistroyCountByUserID(psrExamContext.getUserId(), psrExamContext.getThresholdValue());
        List<PsrEkEtInfo> psrEkEtInfoList = getPsrEkEtInfoListWithsort(pointsOfBook, ekEtCount, userBucket);
        Map<String, String> ekEtRecommended = new HashMap<>();
        Long currentTimeStamp = System.currentTimeMillis() / 1000;
        int count = 0;
        while (appMathList.size() < psrExamContext.getECount() && count++ < 500) {
            PsrEkEtInfo psrEkEtInfoSelected = getPreviewStatusEkEt(psrEkEtInfoList, psrExamContext);
            if (psrEkEtInfoSelected == null)
                continue;
            Integer currentCount = 0;
            String strKey = psrEkEtInfoSelected.getEk() + ":" + psrEkEtInfoSelected.getEt();
            if (ekEtCount.containsKey(strKey)) {
                String[] strValue = ekEtCount.get(strKey).split(":");
                if (strValue.length == 2) {
                    currentCount = PsrTools.stringToInt(strValue[0]);
                }
            }
            if (ekEtRecommended.containsKey(strKey)) {
                String[] strValue = ekEtRecommended.get(strKey).split(":");
                if (strValue.length == 2) {
                    currentCount += PsrTools.stringToInt(strValue[0]);
                }
            }
            if (psrEkEtInfoSelected.getMaxCount() > currentCount)/*已推荐次数*/ {
                PsrPrimaryAppMathItem item = getPsrPrimaryAppMathItem(psrEkEtInfoSelected, userBucket);
                if (item != null) {
                    appMathList.add(item);
                    ++currentCount;
                    ekEtRecommended.put(strKey, currentCount.toString() + ":" + currentTimeStamp.toString());
                }
            }
        }
        // 保存入库
        String strEk = "";
        for (Map.Entry<String, String> entry : ekEtCount.entrySet()) {
            String strKey = entry.getKey();
            if (!ekEtRecommended.containsKey(strKey)) {
                strEk += strKey + ":" + entry.getValue() + ";";
            }
        }
        for (Map.Entry<String, String> entry : ekEtRecommended.entrySet()) {
            strEk += entry.getKey() + ":" + entry.getValue() + ";";
        }
        if (strEk.length() > 0) {
            strEk = strEk.substring(0, strEk.length() - 1);
        }

        if (!StringUtils.isEmpty(strEk))
            ekCouchbaseDao.setCouchbaseData("appmathuserlastpoint_" + psrExamContext.getUserId().toString(), strEk);

        psrPrimaryAppMathContent.setAppMathList(appMathList);
        return psrPrimaryAppMathContent;
    }

    /*
        @Data
        class aa {
            Integer maxCount;
            Double weight;
        }
    */
    public Map<String, String> getEkEtHistroyCountByUserID(Long userId, Long threshold) {
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey("appmathuserlastpoint_" + userId.toString());
        if (StringUtils.isEmpty(strValue))
            return Collections.emptyMap();
        // EK:ET:COUNT:TIMESTAMP;EK:ET:COUNT:TIMESTAMP
        String[] items = strValue.split(";");
        if (items.length <= 0)
            return Collections.emptyMap();

        Map<String, String> retMap = new HashMap<>();
        Long currentTimeStamp = System.currentTimeMillis() / 1000;
        for (String item : items) {
            String[] ekEtCountInfo = item.split(":");
            if (ekEtCountInfo.length < 4)
                continue;
            Long historyTimeStamp = PsrTools.stringToLong(ekEtCountInfo[3]);
            if ((currentTimeStamp - historyTimeStamp) >= threshold)
                continue;
            retMap.put(ekEtCountInfo[0] + ":" + ekEtCountInfo[1], ekEtCountInfo[2] + ":" + ekEtCountInfo[3]);
        }
        return retMap;
    }

    public List<PsrEkEtInfo> getPsrEkEtInfoListWithsort(List<String> pointsOfBook, Map<String, String> ekEtCount, String userBucket) {
        Map<String/*ek*/, Map<String/*et*/, KeyValuePair<Integer, Double>>> etMap = new HashMap<>();
        for (String pointName : pointsOfBook) {
            if ((!StringUtils.isBlank(pointName)) && (!etMap.containsKey(pointName))) {
                Map<String/*pattern*/, KeyValuePair<Integer, Double>> patterns = psrMathPointsPatternsScore.getScoreByPointName(pointName, userBucket);
                etMap.put(pointName, patterns);
            }
        }

        List<PsrEkEtInfo> psrEkEtInfoList = new ArrayList<>();
        for (Map.Entry<String, Map<String, KeyValuePair<Integer, Double>>> entry : etMap.entrySet()) {
            for (Map.Entry<String, KeyValuePair<Integer, Double>> entry2 : entry.getValue().entrySet()) {
                String ek = entry.getKey();
                String et = entry2.getKey();
                Integer maxCount = entry2.getValue().getKey();
                Integer currentCount = 0;
                String strKey = ek + ":" + et;
                if (ekEtCount.containsKey(strKey)) {
                    String[] strValue = ekEtCount.get(strKey).split(":");
                    if (strValue.length == 2) {
                        currentCount = PsrTools.stringToInt(strValue[0]);
                    }
                }
                if (currentCount >= maxCount) {//当前推荐次数已经达到最大推荐次数
                    continue;
                }

                PsrEkEtInfo info = new PsrEkEtInfo();
                info.setEk(/*entry.getKey()*/ek);
                info.setEt(/*entry2.getKey()*/et);
                info.setMaxCount(/*entry2.getValue().getKey()*/maxCount);
                info.setWeight(entry2.getValue().getValue());
                psrEkEtInfoList.add(info);
            }
        }

        // 归一化
        Double weightSum = 0.0;
        for (PsrEkEtInfo ekEtInfo : psrEkEtInfoList) {
            weightSum += ekEtInfo.getWeight();
        }
        for (PsrEkEtInfo ekEtInfo : psrEkEtInfoList) {
            if (weightSum > 0)
                ekEtInfo.setWeightPer(ekEtInfo.getWeight() / weightSum * 100);
        }
        // 根据综合权重排序
        Collections.sort(psrEkEtInfoList, new Comparator<PsrEkEtInfo>() {
            @Override
            public int compare(PsrEkEtInfo o1, PsrEkEtInfo o2) {
                int n = 0;
                if (o2.getWeight() - o1.getWeight() < 0.0)
                    n = 1; // 升序
                else if (o2.getWeight() - o1.getWeight() > 0.0)
                    n = -1;
                return n;
            }
        });
        return psrEkEtInfoList;
    }

    // 轮盘算法
    public PsrEkEtInfo getPreviewStatusEkEt(List<PsrEkEtInfo> psrEkEtInfoList, PsrExamContext psrExamContext) {
        if (psrEkEtInfoList == null)
            return null;

        int index = 0;
        int baseNumberForWeight = 100;

        Random random = psrExamContext == null ? RandomProvider.getInstance().getRandom() : psrExamContext.getRandom();
        double randomTd = random.nextInt(1000000);
        double weightTmp = randomTd / 1000000 * baseNumberForWeight;

        //UserAppMathEkItem appMathEk = null;
        PsrEkEtInfo psrEkEtinfoSelected = null;
        double weightNsum = 0.0;
        for (int i = 0; i < psrEkEtInfoList.size(); i++) {
            PsrEkEtInfo psrEkEtinfo = psrEkEtInfoList.get(i);
            weightNsum += psrEkEtinfo.getWeightPer();
            index = 0;
            if (weightTmp <= weightNsum) {
                index = i;
                psrEkEtinfoSelected = psrEkEtInfoList.get(index);
                break;  // for
            }
            if (i == psrEkEtInfoList.size() - 1) {
                // 遍历完了,但还是没有合适的,就随机最后一个了呗
                psrEkEtinfoSelected = psrEkEtInfoList.get(index);
                break;  // for
            }
        }
        return psrEkEtinfoSelected;
    }

    public PsrPrimaryAppMathItem getPsrPrimaryAppMathItem(PsrEkEtInfo psrEkEtinfoSelected, String userBucket) {
        MathEkEtContent mathEkEtContent = ekCouchbaseDao.getMathEkEtContentFromCouchbase(psrEkEtinfoSelected.getEk());

        Integer time = 50;
        if (mathEkEtContent != null) {
            for (Map.Entry<Integer, MathEtTime> entry : mathEkEtContent.getEtMap().entrySet()) {
                if (!entry.getValue().getEt().equals(psrEkEtinfoSelected.getEt()))
                    continue;
                time = entry.getValue().getTime();
                break;
            }
        }

        PsrPrimaryAppMathItem item = new PsrPrimaryAppMathItem();

        item.setEk(psrEkEtinfoSelected.getEk());
        item.setEType(psrEkEtinfoSelected.getEt());
        item.setTime(time);
        item.setStatus('E');
        item.setWeight(psrEkEtinfoSelected.getWeight());
        //item.setAlgov("Random20160204");
        item.setAlgov("Random_" + userBucket);

        return item;
    }

    /*
     * 随机出题
     * 不关心单元
     * 当天做过4个题型的知识点不在推荐
     */
    public PsrPrimaryAppMathContent randomDeal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrPrimaryAppMathContent retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (psrExamContext == null)
            return logContent(retPrimaryAppMathContent, psrExamContext, "psrExamContext err.", dtB, "error");

        // 取出book中的全部points, points 按先学后学顺序 排序
        List<String> pointsOfBook = getPointsByBookId(psrExamContext, dtB);
        if (pointsOfBook == null || pointsOfBook.size() <= 0)
            return logContent(retPrimaryAppMathContent, psrExamContext,
                    "Can not found default book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        retPrimaryAppMathContent = getPsrPrimaryAppMathContentRandom(pointsOfBook, psrExamContext);

        return logContent(retPrimaryAppMathContent, psrExamContext, "success", dtB, "info");
    }

    public PsrPrimaryAppMathContent getPsrPrimaryAppMathContentRandom(List<String> pointsOfBook, PsrExamContext psrExamContext) {

        PsrPrimaryAppMathContent psrPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (pointsOfBook == null || pointsOfBook.size() <= 0 || psrExamContext == null)
            return psrPrimaryAppMathContent;

        Random random = psrExamContext.getRandom();
        List<PsrPrimaryAppMathItem> appMathList = new ArrayList<>();

        int count = 0;
        while (appMathList.size() < psrExamContext.getECount() && count++ < 500) {
            PsrPrimaryAppMathItem item = new PsrPrimaryAppMathItem();
            int index = random.nextInt(10000) % pointsOfBook.size();
            String ek = pointsOfBook.get(index);
            MathEtTime et = getRandomPattern(ek, psrExamContext);
            if (et == null)
                continue;
            item.setEk(ek);
            item.setEType(et.getEt());
            item.setTime(et.getTime());
            item.setStatus('E');
            item.setWeight(0.0);
            item.setAlgov("Random");
            // 做ABtest的时候 使用不同的algov
            if (psrExamContext.isUserGroup())
                item.setAlgov("16366-B");

            appMathList.add(item);
        }

        psrPrimaryAppMathContent.setAppMathList(appMathList);

        return psrPrimaryAppMathContent;
    }

    public MathEtTime getRandomPattern(String ek, PsrExamContext psrExamContext) {
        if (StringUtils.isBlank(ek) || psrExamContext == null)
            return null;

        UserAppMathEkItem appMathEkItem = new UserAppMathEkItem();
        appMathEkItem.setEk(ek);
        List<MathEtTime> ets = getRandomEtListByEk(appMathEkItem, psrExamContext);
        if (ets == null || ets.size() <= 0)
            return null;

        MathEtTime retEt = ets.get(0);
        return retEt;
    }

    /*
     * redmine 16338
     * A B test
     * A Uid百位数为偶数且末尾不为零的用户,使用kp的score 作为轮盘算法的参数
     * B 其他用户 随机推题
     */
    public PsrPrimaryAppMathContent kpScoreDeal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrPrimaryAppMathContent retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (psrExamContext == null)
            return logContent(retPrimaryAppMathContent, psrExamContext, "psrExamContext err.", dtB, "error");

        // 取出book中的全部points, points 按先学后学顺序 排序
        List<PsrMathPointPersistence> pointsOfBook = getPointsInfoByBookId(psrExamContext, dtB);
        if (pointsOfBook == null || pointsOfBook.size() <= 0)
            return logContent(retPrimaryAppMathContent, psrExamContext,
                    "Can not found default book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        List<UserAppMathEkItem> pointList = getPointsWithScore(pointsOfBook);

        retPrimaryAppMathContent = getKpScoreDealRandom(psrExamContext, pointList);

        return logContent(retPrimaryAppMathContent, psrExamContext, "success", dtB, "info");
    }

    public List<UserAppMathEkItem> getPointsWithScore(List<PsrMathPointPersistence> pointsOfBook) {
        if (null == pointsOfBook || pointsOfBook.size() <= 0)
            return null;

        List<UserAppMathEkItem> pointList = new ArrayList<>();

        // init score
        for (PsrMathPointPersistence point : pointsOfBook) {
            UserAppMathEkItem item = new UserAppMathEkItem();
            item.setEk(point.getPointName());
            Integer score = psrMathBooksPoints.getScoreByPointId(point.getPointId()); // get it from couchbase by pointId
            item.setWeight(score);
            pointList.add(item);
        }

        return pointList;
    }

    public List<UserAppMathEkItem> getPointListWithSort(List<UserAppMathEkItem> pointList) {
        if (null == pointList)
            return null;

        // 根据综合权重排序
        Collections.sort(pointList, new Comparator<UserAppMathEkItem>() {
            @Override
            public int compare(UserAppMathEkItem o1, UserAppMathEkItem o2) {
                int n = 0;
                if (o2.getWeight() - o1.getWeight() < 0.0)
                    n = 1; // 升序
                else if (o2.getWeight() - o1.getWeight() > 0.0)
                    n = -1;
                return n;
            }
        });

        // 归一化
        Double weightSum = 0.0;
        for (UserAppMathEkItem ekItem : pointList) {
            weightSum += ekItem.getWeight();
        }
        for (UserAppMathEkItem ekItem : pointList) {
            if (weightSum > 0)
                ekItem.setWeightPer(ekItem.getWeight() / weightSum * 100);
        }

        return pointList;
    }

    public PsrPrimaryAppMathContent getKpScoreDealRandom(PsrExamContext psrExamContext,
                                                         List<UserAppMathEkItem> pointList) {

        PsrPrimaryAppMathContent retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (null == psrExamContext || null == pointList)
            return retPrimaryAppMathContent;

        // sort with weight
        getPointListWithSort(pointList);

        List<PsrPrimaryAppMathItem> appMathList = new ArrayList<>();
        int index = 0;
        while (pointList.size() > 0 && appMathList.size() < psrExamContext.getECount()) {
            if (index++ > 200) {
                log.warn("pointReviewList.size < eCount and index++ > 200.");
                break;
            }
            // 随机一个 知识点
            UserAppMathEkItem appMathEk = getPreviewStatusEk(pointList, psrExamContext);
            if (appMathEk == null)
                break;
            if (StringUtils.isEmpty(appMathEk.getEk())) {
                continue;
            }
            // 随机一个题型
            MathEtTime et = getRandomPattern(appMathEk.getEk(), psrExamContext);
            if (et == null)
                continue;
            PsrPrimaryAppMathItem item = new PsrPrimaryAppMathItem();
            item.setEk(appMathEk.getEk());
            item.setEType(et.getEt());
            item.setTime(et.getTime());
            item.setStatus('E');
            item.setWeight(0.0);
            item.setAlgov("16366-A");

            appMathList.add(item);
        }

        retPrimaryAppMathContent.setAppMathList(appMathList);
        return retPrimaryAppMathContent;
    }

    public List<String> getPointsByBookId(PsrExamContext psrExamContext, Date dtB) {
        // 取出book中的全部points, points 按先学后学顺序 排序
        List<String> pointsOfBook = psrMathBooksPoints.getPointsByBookId(Long.parseLong(psrExamContext.getBookId()));
        if (pointsOfBook != null && pointsOfBook.size() > 0)
            return pointsOfBook;
        logContent(null, psrExamContext, "Can not found book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        // 使用默认教材 人教版三年级上－新版
        psrExamContext.setBookId("137");
        psrExamContext.setUnitId("-1");
        return psrMathBooksPoints.getPointsByBookId(Long.parseLong(psrExamContext.getBookId()));
    }

    public List<PsrMathPointPersistence> getPointsInfoByBookId(PsrExamContext psrExamContext, Date dtB) {
        // 取出book中的全部points, points 按先学后学顺序 排序
        List<PsrMathPointPersistence> pointsOfBook = psrMathBooksPoints.getPointsInfoByBookId(Long.parseLong(psrExamContext.getBookId()));
        if (pointsOfBook != null && pointsOfBook.size() > 0)
            return pointsOfBook;
        logContent(null, psrExamContext, "Can not found book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        // 使用默认教材 人教版三年级上－新版
        psrExamContext.setBookId("137");
        psrExamContext.setUnitId("-1");
        return psrMathBooksPoints.getPointsInfoByBookId(Long.parseLong(psrExamContext.getBookId()));
    }

    public PsrPrimaryAppMathContent deal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrPrimaryAppMathContent retPrimaryAppMathContent = new PsrPrimaryAppMathContent();

        if (psrExamContext == null)
            return logContent(retPrimaryAppMathContent, null, "psrExamContext is null.", dtB, "error");
        if (ekCouchbaseDao == null || psrMathBooksPoints == null || psrMathPointsRelationship == null)
            return logContent(retPrimaryAppMathContent, psrExamContext, "Can not connect databases.", dtB, "error");

        UserAppMathContent userAppMathContent = ekCouchbaseDao.getUserAppMathContentFromCouchbase(psrExamContext.getUserId());
        if (userAppMathContent == null) {
            userAppMathContent = new UserAppMathContent();
            Map<String, UserAppMathEkItem> ekMap = new LinkedHashMap<>();
            Map<String, UserAppMathEtItem> etMap = new LinkedHashMap<>();
            userAppMathContent.setEkMap(ekMap);
            userAppMathContent.setEtMap(etMap);
        }

        // 取出book中的全部points, points 按先学后学顺序 排序
        List<String> pointsOfBook = getPointsByBookId(psrExamContext, dtB);
        if (pointsOfBook == null || pointsOfBook.size() <= 0) {
            return logContent(retPrimaryAppMathContent, psrExamContext,
                    "Can not found default book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");
        }

        // 更新课本中用户对知识点的掌握状态,并生成备选集合
        List<UserAppMathEkItem> pointStatusList = getPointStatusList(pointsOfBook, userAppMathContent);

        // 取出当天 用户的做题数据, 某个知识点的某个题型被某个人做的次数>=3 则不推荐该知识点+题型
        Map<String/*ek*/, Map<String/*et*/, Integer>> mathAppResultEkEts = getMathAppResultEkEts(psrExamContext);

        // 获取复习知识点列表(D-A 且非处于冷冻期),以及E状态知识点列表
        List<UserAppMathEkItem> pointReviewList = new ArrayList<>();
        List<UserAppMathEkItem> pointEStatusList = new ArrayList<>();
        for (UserAppMathEkItem appMathEkItem : pointStatusList) {
            if (appMathEkItem.getStatus() == 'E') {
                pointEStatusList.add(appMathEkItem);
            } else {
                // status = D C B A S
                if (appMathEkItem.isOutOfDays())
                    pointReviewList.add(appMathEkItem);
            }
        }

        String strEk = "";
        String algov = "am01"; // application math

        while (true) {
            if (pointReviewList.size() > 0) {
                // review
                retPrimaryAppMathContent = getReviewStatusAppMathContent(psrExamContext, userAppMathContent, pointReviewList, retPrimaryAppMathContent, mathAppResultEkEts, algov);
                if (StringUtils.equals(retPrimaryAppMathContent.getErrorContent(), "success")
                        && retPrimaryAppMathContent.getAppMathList().size() >= psrExamContext.getECount())
                    break;
            }
            if (pointEStatusList.size() > 0) {
                // eStatus
                retPrimaryAppMathContent = getEStatusAppMathContent(psrExamContext, pointEStatusList, retPrimaryAppMathContent, mathAppResultEkEts, algov);
                if (StringUtils.equals(retPrimaryAppMathContent.getErrorContent(), "success")
                        && retPrimaryAppMathContent.getAppMathList().size() >= psrExamContext.getECount())
                    break;
            }
            if (pointStatusList.size() > 0) {
                // 如果没有新知识点没有复习知识点,则使用全量的知识点并且按当天推荐的次数从少到多依次推荐,知识点内部的题型也是从当天推荐的次数从少到多依次推荐
                retPrimaryAppMathContent = getAdaptStatusAppMathContent(psrExamContext, pointStatusList, retPrimaryAppMathContent, mathAppResultEkEts, algov);
                break;
            }
            break;
        }
        if (retPrimaryAppMathContent == null)
            retPrimaryAppMathContent = new PsrPrimaryAppMathContent();

        // log and break;
        if (retPrimaryAppMathContent.getAppMathList().size() < psrExamContext.getECount()
                && !StringUtils.equals(retPrimaryAppMathContent.getErrorContent(), "success")) {
            return logContent(retPrimaryAppMathContent, psrExamContext, "Can not found point from pointStatusList size:" + pointStatusList.size(), dtB, "error");
        }
        if (retPrimaryAppMathContent.getAppMathList().size() > 0)
            strEk = retPrimaryAppMathContent.getAppMathList().get(0).getEk();

        // 保存入库 最近一次做过的知识点
        if (!StringUtils.isEmpty(strEk))
            ekCouchbaseDao.setCouchbaseData("appmathuserlastpoint_" + psrExamContext.getUserId().toString(), strEk);

        return logContent(retPrimaryAppMathContent, psrExamContext, "success", dtB, "info");
    }

    public PsrPrimaryAppMathContent getAdaptStatusAppMathContent(PsrExamContext psrExamContext,
                                                                 List<UserAppMathEkItem> pointStatusList,
                                                                 PsrPrimaryAppMathContent retPrimaryAppMathContent,
                                                                 Map<String/*ek*/, Map<String/*et*/, Integer>> mathAppResultEkEts, String algov) {

        if (retPrimaryAppMathContent == null)
            retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (pointStatusList == null || pointStatusList.size() == 0) {
            retPrimaryAppMathContent.setErrorContent("pointStatusList is empty");
            return retPrimaryAppMathContent;
        }
        if (StringUtils.isEmpty(algov))
            algov = "am01";
        if (mathAppResultEkEts == null)
            mathAppResultEkEts = new HashMap<>();

        List<UserAppMathEkItem> tmpStatusList = new ArrayList<>();
        tmpStatusList.addAll(pointStatusList);

        // fixme while read ekItem from list until the list is emtpy or find an ekitem and ekitem's etItem

        UserAppMathEkItem ekItem = null;
        List<MathEtTime> eTypeList = null;
        Integer ekPsrCount = 0;
        Integer index = 0;
        while (true) {
            ekItem = null;
            ekPsrCount = 0;
            eTypeList = null;

            // 小学数学的知识点个数在百级别
            if (tmpStatusList.isEmpty() || index++ > 1000)
                break;
            // 查找推荐次数最少的ek
            for (UserAppMathEkItem userAppMathEkItem : tmpStatusList) {
                Integer count = 0;
                if (mathAppResultEkEts.containsKey(userAppMathEkItem.getEk()))
                    count = mathAppResultEkEts.get(userAppMathEkItem.getEk()).size();
                if (ekItem == null || ekPsrCount < count) {
                    ekItem = userAppMathEkItem;
                    ekPsrCount = count;
                }
            }
            // rm ek
            tmpStatusList.remove(ekItem);
            if (ekItem != null) {
                eTypeList = getRandomEtListByEk(ekItem, psrExamContext);
                if (eTypeList != null && eTypeList.size() > 0)
                    break;
            }
        }

        if (ekItem == null) {
            retPrimaryAppMathContent.setErrorContent("Can not found ek from pointStatusList size:" + pointStatusList.size());
            return retPrimaryAppMathContent;
        }

        if (eTypeList != null && eTypeList.size() > 0) {
            for (MathEtTime etItem : eTypeList) {
                PsrPrimaryAppMathItem appMathItem = new PsrPrimaryAppMathItem();
                appMathItem.setEk(ekItem.getEk());
                appMathItem.setEType(etItem.getEt());
                appMathItem.setTime(etItem.getTime());
                appMathItem.setStatus(ekItem.getStatus());
                appMathItem.setWeight(ekItem.getWeight());
                retPrimaryAppMathContent.getAppMathList().add(appMathItem);
            }
        }

        if (retPrimaryAppMathContent.getAppMathList().size() < psrExamContext.getECount()) {
            retPrimaryAppMathContent.setErrorContent("not enough eType, pointStatusList:" + pointStatusList.size());
        }

        retPrimaryAppMathContent.setErrorContent("success");
        return retPrimaryAppMathContent;
    }

    public PsrPrimaryAppMathContent getReviewStatusAppMathContent(PsrExamContext psrExamContext,
                                                                  UserAppMathContent userAppMathContent,
                                                                  List<UserAppMathEkItem> pointReviewList,
                                                                  PsrPrimaryAppMathContent retPrimaryAppMathContent,
                                                                  Map<String/*ek*/, Map<String/*et*/, Integer>> mathAppResultEkEts, String algov) {

        if (retPrimaryAppMathContent == null)
            retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (pointReviewList == null || pointReviewList.size() == 0) {
            retPrimaryAppMathContent.setErrorContent("pointReviewList is empty");
            return retPrimaryAppMathContent;
        }
        if (StringUtils.isEmpty(algov))
            algov = "am01";
        if (userAppMathContent == null)
            userAppMathContent = new UserAppMathContent();
        if (mathAppResultEkEts == null)
            mathAppResultEkEts = new HashMap<>();

        // 复习的知识点集合,设置综合权重（子集个数权重 、 状态权重）
        setWeight(pointReviewList);

        // 调权, 也就是检查的逻辑
        updateWeight(pointReviewList, psrExamContext.getUserId(), userAppMathContent);

        // 根据综合权重排序
        Collections.sort(pointReviewList, new Comparator<UserAppMathEkItem>() {
            @Override
            public int compare(UserAppMathEkItem o1, UserAppMathEkItem o2) {
                int n = 0;
                if (o2.getWeight() - o1.getWeight() < 0.0)
                    n = 1; // 升序
                else if (o2.getWeight() - o1.getWeight() > 0.0)
                    n = -1;
                return n;
            }
        });

        // 归一化
        Double weightSum = 0.0;
        for (UserAppMathEkItem ekItem : pointReviewList) {
            weightSum += ekItem.getWeight();
        }
        for (UserAppMathEkItem ekItem : pointReviewList) {
            if (weightSum > 0)
                ekItem.setWeightPer(ekItem.getWeight() / weightSum * 100);
        }

        int index = 0;
        while (pointReviewList.size() > 0) {
            if (index++ > 200) {
                log.warn("pointReviewList.size < eCount and index++ > 200.");
                break;
            }
            // 随机一个 知识点， 并随机10个题型
            UserAppMathEkItem appMathEk = getPreviewStatusEk(pointReviewList, psrExamContext);
            if (appMathEk == null)
                break;
            pointReviewList.remove(appMathEk);
            if (StringUtils.isEmpty(appMathEk.getEk())) {
                continue;
            }

            Map<String, Integer> ets = null;
            if (mathAppResultEkEts.containsKey(appMathEk.getEk()))
                ets = mathAppResultEkEts.get(appMathEk.getEk());
            List<MathEtTime> eTypeList = getRandomEtListByEk(appMathEk, psrExamContext, ets);
            if (eTypeList == null || eTypeList.size() <= 0)
                continue;

            for (MathEtTime etItem : eTypeList) {
                PsrPrimaryAppMathItem appMathItem = new PsrPrimaryAppMathItem();
                appMathItem.setEk(appMathEk.getEk());
                appMathItem.setEType(etItem.getEt());
                appMathItem.setTime(etItem.getTime());
                appMathItem.setStatus(appMathEk.getStatus());
                appMathItem.setWeight(appMathEk.getWeight());
                retPrimaryAppMathContent.getAppMathList().add(appMathItem);
            }
            break;
        }  // end while

        if (retPrimaryAppMathContent.getAppMathList().size() < psrExamContext.getECount()) {
            retPrimaryAppMathContent.setErrorContent("not enough eType, reviewSize:" + pointReviewList.size());
        }

        retPrimaryAppMathContent.setErrorContent("success");
        return retPrimaryAppMathContent;
    }

    public PsrPrimaryAppMathContent getEStatusAppMathContent(PsrExamContext psrExamContext,
                                                             List<UserAppMathEkItem> pointEStatusList,
                                                             PsrPrimaryAppMathContent retPrimaryAppMathContent,
                                                             Map<String/*ek*/, Map<String/*et*/, Integer>> mathAppResultEkEts, String algov) {
        if (retPrimaryAppMathContent == null)
            retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (pointEStatusList == null || pointEStatusList.size() == 0) {
            retPrimaryAppMathContent.setErrorContent("pointEStatusList is empty");
            return retPrimaryAppMathContent;
        }
        if (StringUtils.isEmpty(algov))
            algov = "am01";
        if (mathAppResultEkEts == null)
            mathAppResultEkEts = new HashMap<>();

        // 找知识点和对应的题型,当天做过的知识点+题型 >= 3 次的不推
        // 找最容易的知识点 及 题型
        UserAppMathEkItem mathEk = null;
        UserAppMathEtItem mathEt = null;
        int maxEListSize = pointEStatusList.size();
        int index = 0;
        while (index++ < maxEListSize) {
            // get ek
            mathEk = getEasyEstatusEk(pointEStatusList);
            if (mathEk == null)
                break;
            pointEStatusList.remove(mathEk);
            if (StringUtils.isEmpty(mathEk.getEk()))
                continue;

            // get et
            Map<String, Integer> ets = null;
            if (mathAppResultEkEts.containsKey(mathEk.getEk()))
                ets = mathAppResultEkEts.get(mathEk.getEk());
            mathEt = getEasyEstatusEtByEk(mathEk, psrExamContext, ets);
            if (mathEt == null || StringUtils.isEmpty(mathEt.getEt()))
                continue;

            break;
        }
        if (mathEk == null || StringUtils.isEmpty(mathEk.getEk())) {
            retPrimaryAppMathContent.setErrorContent("Can not found points from pointEStatusList size :" + maxEListSize + " index:" + index);
            return retPrimaryAppMathContent;
        }
        if (mathEt == null || StringUtils.isEmpty(mathEt.getEt())) {
            retPrimaryAppMathContent.setErrorContent("Can not found point'pattern point: " + mathEk.getEk() + " index:" + index);
            return retPrimaryAppMathContent;
        }

        while (retPrimaryAppMathContent.getAppMathList().size() < psrExamContext.getECount()) {
            PsrPrimaryAppMathItem item = new PsrPrimaryAppMathItem();
            item.setEk(mathEk.getEk());
            item.setEType(mathEt.getEt());
            item.setTime(mathEt.getTime());
            item.setStatus(mathEk.getStatus());
            item.setWeight(mathEk.getWeight());

            item.setAlgov(algov);

            retPrimaryAppMathContent.getAppMathList().add(item);
        }
        retPrimaryAppMathContent.setErrorContent("success");
        return retPrimaryAppMathContent;
    }

    // 更新课本中用户对知识点的掌握状态,并生成备选集合
    public List<UserAppMathEkItem> getPointStatusList(List<String> pointsOfBook, UserAppMathContent userAppMathContent) {
        List<UserAppMathEkItem> retList = new ArrayList<>();
        if (pointsOfBook == null || userAppMathContent == null)
            return retList;

        for (String point : pointsOfBook) {
            if (!userAppMathContent.isEkMapNull() && userAppMathContent.getEkMap().containsKey(point)) {
                retList.add(userAppMathContent.getEkMap().get(point));
                continue;
            }
            UserAppMathEkItem item = new UserAppMathEkItem();
            item.setEk(point);
            retList.add(item);
        }

        return retList;
    }

    // 取出当天 用户的做题数据, 某个知识点的某个题型被某个人做的次数>=3 则不推荐该知识点+题型
    public Map<String/*ek*/, Map<String/*et*/, Integer>> getMathAppResultEkEts(PsrExamContext psrExamContext) {
        Map<String/*ek*/, Map<String/*et*/, Integer>> mathAppResultEkEts = new HashMap<>();
        return mathAppResultEkEts;

//        List<MathematicsAppResult> psrMathAppResults = psrMathAppResultCacheDao.findByUserId(psrExamContext.getUserId());
//        if (psrMathAppResults != null && psrMathAppResults.size() > 0) {
//            // 计算每个point+pattern 当天出现的次数
//            for (MathematicsAppResult item : psrMathAppResults) {
//                if (item == null) continue;
//
//                String ek = psrMathBooksPoints.getPointNameByPointId(PsrTools.stringToLong(item.getEk_list()));
//                String et = psrMathBasePersistence.findAppMathPatternByPointIdEid(item.getEk_list(), item.getEid());
//                if (StringUtils.isEmpty(et))
//                    continue;
//                Map<String/*et*/, Integer/*times*/> itemEts;
//                if (!mathAppResultEkEts.containsKey(ek)) {
//                    itemEts = new HashMap<>();
//                    itemEts.put(et, 1);
//                } else {
//                    itemEts = mathAppResultEkEts.get(ek);
//                    if (itemEts == null)
//                        continue;
//                    Integer etCount = 0;
//                    if (itemEts.containsKey(et)) {
//                        etCount = itemEts.get(et);
//                    }
//                    etCount++;
//                    itemEts.put(et, etCount);
//                }
//                mathAppResultEkEts.put(ek, itemEts);
//            }
//        }
//        return mathAppResultEkEts;

    }

    public PsrPrimaryAppUserEks getPrimaryAppMathEksByUserId(String product, Long userId) {
        Date dtB = new Date();

        PsrPrimaryAppUserEks retPrimaryAppUserEks = new PsrPrimaryAppUserEks();

        // 日志 及返回值
        if (ekCouchbaseDao == null) {
            retPrimaryAppUserEks.setErrorContent("Can not connect couchbase");
            String strLog = retPrimaryAppUserEks.formatList("Math") + "[product:" + product + " userId:" + userId.toString() + "][TotalTime:0]";
            log.error(strLog);
            return retPrimaryAppUserEks;
        }

        /*
         * 根据Uid 取出 learning_profile 的 Ek-list
         */
        UserAppMathContent userAppMathContent = ekCouchbaseDao.getUserAppMathContentFromCouchbase(userId);

        if (userAppMathContent == null || userAppMathContent.isEkMapNull() || userAppMathContent.getEkMap().size() <= 0) {
            retPrimaryAppUserEks.setErrorContent("NotFoundUserAppMathContent userId");
            String strLog = retPrimaryAppUserEks.formatList("Math") + "[product:" + product + " userId:" + userId.toString() + "][TotalTime:0]";
            log.warn(strLog);
            return retPrimaryAppUserEks;
        }

        for (Map.Entry<String, UserAppMathEkItem> entry : userAppMathContent.getEkMap().entrySet()) {
            PsrPrimaryAppUserEkItem item = new PsrPrimaryAppUserEkItem();
            item.setEk(entry.getValue().getEk());
            item.setStatus(entry.getValue().getStatus());

            retPrimaryAppUserEks.getEkList().add(item);
        }

        retPrimaryAppUserEks.setErrorContent("success");

//        Date dtE = new Date();
//        Long uTAll = dtE.getTime() - dtB.getTime();
//        String strLog = retPrimaryAppUserEks.formatList("Math") + "[product:" + product + " userId:" + userId.toString() + "][TotalTime:" + uTAll.toString() + "]";
//        log.info(strLog);

        return retPrimaryAppUserEks;
    }

    public Integer getPrimaryAppMathEtTimeByEkEt(String product, String point, String pattern) {
        Integer time = 0;

        if (StringUtils.isEmpty(point) || StringUtils.isEmpty(pattern))
            return time;

        MathEkEtContent mathEkEtContent = ekCouchbaseDao.getMathEkEtContentFromCouchbase(point);

        if (mathEkEtContent == null)
            return time;

        for (Map.Entry<Integer, MathEtTime> entry : mathEkEtContent.getEtMap().entrySet()) {
            if (!entry.getValue().getEt().equals(pattern))
                continue;
            time = entry.getValue().getTime();
            break;
        }

        //log.info("getPrimaryAppMathEtTimeByEkEt point:" + point + " pattern:" + pattern + " time:" + time);

        return time;
    }

    private PsrPrimaryAppMathContent logContent(PsrPrimaryAppMathContent retPrimaryAppMathContent,
                                                PsrExamContext psrExamContext, String errorMsg,
                                                Date dtB, String logLevel) {
        if (retPrimaryAppMathContent == null)
            retPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retPrimaryAppMathContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retPrimaryAppMathContent.getAppMathList().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatToString(retPrimaryAppMathContent, psrExamContext, uTAll);
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

        return retPrimaryAppMathContent;
    }

    private String formatToString(PsrPrimaryAppMathContent retPrimaryAppMathContent,
                                  PsrExamContext psrExamContext, Long spendTime) {
        String strLog = retPrimaryAppMathContent.formatList();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }

    private void setWeight(List<UserAppMathEkItem> points) {
        if (points == null)
            return;

        List<String> tmpList = null;
        Double tmpWeight = 0.0;
        int allSubSetCount = 0;
        for (UserAppMathEkItem item : points) {
            if (psrMathPointsRelationship.getMapPoints().containsKey(item.getEk())) {
                tmpList = psrMathPointsRelationship.getMapPoints().get(item.getEk());
                if (tmpList != null) {
                    item.setSubsetCount(tmpList.size());
                    allSubSetCount += item.getSubsetCount();
                }
            }
        }
        for (UserAppMathEkItem item : points) {
            tmpWeight = 0.0;
            if (allSubSetCount > 0)
                tmpWeight = (item.getSubsetCount() + 0.0) / allSubSetCount;
            // 知识点子集个数权重,子集多则权重小,需要考察子集
            // todo getStatusWeight 中的100  和 下面的100 需要可配置。
            item.setWeight(item.getStatusWeight() + tmpWeight * 100);
        }
    }

    private void updateWeight(List<UserAppMathEkItem> points, Long userId, UserAppMathContent userAppMathContent) {
        if (points == null)
            return;

        // 获取该用户上次做math 知识点
        String lastPoint = ekCouchbaseDao.getCouchbaseDataByKey("appmathuserlastpoint_" + userId.toString());
        if (StringUtils.isEmpty(lastPoint))
            return;
        if (userAppMathContent == null || userAppMathContent.isEkMapNull())
            return;
        if (userAppMathContent.getEkMap().containsKey(lastPoint)) {
            UserAppMathEkItem item = userAppMathContent.getEkMap().get(lastPoint);
            if (item == null || item.getStatus() == 'E')
                return;
            // 优先复习高级节点
            if (item.getAccuracyRate() >= 0.8)
                return;

            // todo 参数可配置 0.8 0.3 0.1

            // 自身节点降权,孩子节点升权
            double father_weight = item.getWeight();
            item.setWeight(item.getWeight() * (1.0 - 0.3));

            List<String> lastPointChilds = null;
            if (psrMathPointsRelationship.getMapPoints().containsKey(lastPoint)) {
                lastPointChilds = psrMathPointsRelationship.getMapPoints().get(lastPoint);
                if (lastPointChilds != null && lastPointChilds.size() > 0) {
                    for (UserAppMathEkItem ekItem : points) {
                        if (lastPointChilds.contains(ekItem.getEk())) {
                            ekItem.setWeight(ekItem.getWeight() * (1.0 + 0.3));
                            if (ekItem.getWeight() < father_weight)
                                ekItem.setWeight(father_weight + 0.1 * ekItem.getWeight());
                        }
                    }
                }
            }
        }
    }

    // 轮盘算法
    public UserAppMathEkItem getPreviewStatusEk(List<UserAppMathEkItem> pointReviewList, PsrExamContext psrExamContext) {
        if (pointReviewList == null)
            return null;

        int index = 0;
        int baseNumberForWeight = 100;

        Random random = psrExamContext == null ? RandomProvider.getInstance().getRandom() : psrExamContext.getRandom();
        double randomTd = random.nextInt(1000000);
        double weightTmp = randomTd / 1000000 * baseNumberForWeight;

        UserAppMathEkItem appMathEk = null;
        double weightNsum = 0.0;
        for (int i = 0; i < pointReviewList.size(); i++) {
            UserAppMathEkItem userAppMathEkItem = pointReviewList.get(i);
            weightNsum += userAppMathEkItem.getWeightPer();
            index = 0;
            if (weightTmp <= weightNsum) {
                index = i;
                appMathEk = pointReviewList.get(index);
                break;  // for
            }
            if (i == pointReviewList.size() - 1) {
                // 遍历完了,但还是没有合适的,就随机最后一个了呗
                appMathEk = pointReviewList.get(index);
                break;  // for
            }
        }

        return appMathEk;
    }


    public List<MathEtTime> getRandomEtListByEk(UserAppMathEkItem appMathEk, PsrExamContext psrExamContext, Map<String, Integer> ets) {
        if (appMathEk == null || psrExamContext == null)
            return null;

        MathEkEtContent mathEkEtContent = ekCouchbaseDao.getMathEkEtContentFromCouchbase(appMathEk.getEk());
        if (mathEkEtContent == null || mathEkEtContent.getEtMap() == null || mathEkEtContent.getEtMap().size() <= 0) {
            return null;
        }

        int index = 0;
        Map<Integer, MathEtTime> tmpMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, MathEtTime> entry : mathEkEtContent.getEtMap().entrySet()) {
            if (ets != null && ets.containsKey(entry.getValue().getEt()) && ets.get(entry.getValue().getEt()) >= psrExamContext.getEkCountByUserDone())
                continue;
            tmpMap.put(index++, entry.getValue());
        }
        if (tmpMap.size() <= 0)
            return null;

        List<MathEtTime> retEtList = new ArrayList<>();
        // 随机获取eCount个题型
        index = 0;
        Random random = psrExamContext.getRandom();
        while (index++ < psrExamContext.getECount()) {
            int typeIndex = random.nextInt(10000) % tmpMap.size();
            retEtList.add(tmpMap.get(typeIndex));
        }
        return retEtList;
    }

    public List<MathEtTime> getRandomEtListByEk(UserAppMathEkItem appMathEk, PsrExamContext psrExamContext) {
        return getRandomEtListByEk(appMathEk, psrExamContext, null);
    }

    public UserAppMathEkItem getEasyEstatusEk(List<UserAppMathEkItem> pointEStatusList) {
        if (pointEStatusList == null || pointEStatusList.size() <= 0)
            return null;
        // 找最容易的知识点
        UserAppMathEkItem mathEk = null;
        for (UserAppMathEkItem mathEkItemTmp : pointEStatusList) {
            if (mathEk == null) {
                mathEk = mathEkItemTmp;
                continue;
            }
            if (mathEk.getAccuracyRate() < mathEkItemTmp.getAccuracyRate())
                mathEk = mathEkItemTmp;
        }

        //Can not found points from pointEStatusList
        return mathEk;
    }

    public UserAppMathEtItem getEasyEstatusEtByEk(UserAppMathEkItem mathEk, PsrExamContext psrExamContext, Map<String, Integer> ets) {
        if (mathEk == null || psrExamContext == null)
            return null;

        // 获取E状态知识点对应的题型列表
        UserAppMathEtItem mathEt = null;
        List<UserAppMathEtItem> etItemList = mathEk.getEStatusEtList();
        if (etItemList.size() > 0) {
            // 找最容易的题型
            for (UserAppMathEtItem etItem : etItemList) {
                if (ets != null && ets.containsKey(etItem.getEt()) && ets.get(etItem.getEt()) >= psrExamContext.getEkCountByUserDone())  // todo 3 可配置
                    continue;
                if (mathEt == null) {
                    mathEt = etItem;
                    continue;
                }
                if (etItem.getAccuracyRate() > 0.8)  // todo 0.8 可配置
                    continue;
                if (mathEt.getAccuracyRate() < etItem.getAccuracyRate()) {
                    mathEt = etItem;
                    mathEt.setTime(getPrimaryAppMathEtTimeByEkEt("psr", mathEk.getEk(), mathEt.getEt()));
                }
            }
        }
        if (mathEt == null) {
            // 从couchbase 中获取 知识点对应的题型,已经按照正确率升序排列
            MathEkEtContent mathEkEtContentTmp = ekCouchbaseDao.getMathEkEtContentFromCouchbase(mathEk.getEk());
            if (mathEkEtContentTmp != null && mathEkEtContentTmp.getEtMap().size() > 0) {
                mathEt = new UserAppMathEtItem();
                Integer etAccuracyRateTmp = 0;
                for (Map.Entry<Integer/*rank*/, MathEtTime> entry : mathEkEtContentTmp.getEtMap().entrySet()) {
                    // 同一用户同一知识点同一题型 当天做过三次的不推
                    if (ets != null && ets.containsKey(entry.getValue().getEt()) && ets.get(entry.getValue().getEt()) >= psrExamContext.getEkCountByUserDone())  // todo 3 可配置
                        continue;
                    if (etAccuracyRateTmp < entry.getKey()) {
                        mathEt.setEt(entry.getValue().getEt());
                        mathEt.setTime(entry.getValue().getTime());
                        etAccuracyRateTmp = entry.getKey();
                    }
                }
            }
        }

        return mathEt;
    }

    public UserAppMathEtItem getEasyEstatusEtByEk(UserAppMathEkItem mathEk, PsrExamContext psrExamContext) {
        return getEasyEstatusEtByEk(mathEk, psrExamContext, null);
    }
}

