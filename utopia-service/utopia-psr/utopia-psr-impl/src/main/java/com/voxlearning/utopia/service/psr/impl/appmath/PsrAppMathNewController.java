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

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrPracticeTypePersistence;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Named
public class PsrAppMathNewController implements Serializable {
    private static final long serialVersionUID = -5665972091473036957L;
    //private static final long THRESHOLD = 86400L; // 一天

    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrMathBooksPoints psrMathBooksPoints;
    @Inject private PsrMathPointsRelationship psrMathPointsRelationship;
    @Inject private PsrPracticeTypePersistence psrPracticeTypePersistence;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrMathBasePersistence psrMathBasePersistence;
    @Inject private PsrMathPointsPatternsScore psrMathPointsPatternsScore;

    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;

    public PsrPrimaryAppMathContent deal(String product, Long userId,
                                         int regionCode, String bookId, String unitId, int eCount) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig, product, "student", userId,
                regionCode, bookId, unitId, eCount, 0.0F, 0.0F);

/*
        // 分组条件,Uid 百位是偶数且个位数不为零
        Long posOfHundred = psrExamContext.getUserId() / 100;
        String userBucket = "equal";
        if (psrExamContext.isUserGroup() && posOfHundred % 2 == 0L && psrExamContext.getUserId() % 10 != 0) {
            //return kpScoreDeal(psrExamContext);
            userBucket = "retain";
        }
        return weightRandomDeal(psrExamContext, userBucket);
*/

        return randomDeal(psrExamContext);
    }


    public Map<String, String> getPointsByBookIdWithDefault(PsrExamContext psrExamContext, Date dtB) {
        if (psrExamContext == null)
            return Collections.emptyMap();

        Map<String, String> retMap = getPointsByBookId(psrExamContext.getBookId(), dtB);
        if (MapUtils.isNotEmpty(retMap))
            return retMap;

        logContent(null, psrExamContext, "Can not found book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        // 使用默认教材 人教版三年级上－新版
        String defaultBookId = "BK_10200001550278";
        return getPointsByBookId(defaultBookId, dtB);
    }

    // return key:pointName, value:pointId
    public Map<String, String> getPointsByBookId(String bookId, Date dtB) {
        // 取出book中的全部points, points 按先学后学顺序 排序
        PsrBookPersistenceNew psrBookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(bookId);
        Map<String, PsrUnitPersistenceNew> psrUnitPersistenceNewMap = psrBookPersistenceNew.getPsrUnitPersistenceMap("-1");
        if (MapUtils.isEmpty(psrUnitPersistenceNewMap))
            return Collections.emptyMap();

        Map<String, String> retMap = new HashMap<>();
        psrUnitPersistenceNewMap.values().stream().filter(p -> MapUtils.isNotEmpty(p.getSentences())).forEach(p -> {
            p.getSentences().values().stream().filter(s -> StringUtils.isNotBlank(s.getName())).forEach(s -> retMap.put("point#" + s.getName(), s.getId()));
        });

        if (MapUtils.isNotEmpty(retMap))
            return retMap;

//        List<String> pointsOfBook = psrMathBooksPoints.getPointsByBookId(Long.parseLong(psrExamContext.getBookId()));
//        if (pointsOfBook != null && pointsOfBook.size() > 0)
//            return pointsOfBook;

        return Collections.emptyMap();
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
        Map<String, String> pointsOfBook = getPointsByBookIdWithDefault(psrExamContext, dtB);
        if (pointsOfBook == null || pointsOfBook.size() <= 0)
            return logContent(retPrimaryAppMathContent, psrExamContext,
                    "Can not found default book's points by bookid:" + psrExamContext.getBookId(), dtB, "error");

        retPrimaryAppMathContent = getPsrPrimaryAppMathContentRandom(pointsOfBook, psrExamContext);

        return logContent(retPrimaryAppMathContent, psrExamContext, "success", dtB, "info");
    }

    public PsrPrimaryAppMathContent getPsrPrimaryAppMathContentRandom(Map<String, String> pointsOfBook, PsrExamContext psrExamContext) {

        PsrPrimaryAppMathContent psrPrimaryAppMathContent = new PsrPrimaryAppMathContent();
        if (MapUtils.isEmpty(pointsOfBook) || psrExamContext == null)
            return psrPrimaryAppMathContent;

        Random random = psrExamContext.getRandom();
        List<PsrPrimaryAppMathItem> appMathList = new ArrayList<>();
        List<String> pointList = new ArrayList<>(pointsOfBook.keySet());

        int count = 0;
        while (appMathList.size() < psrExamContext.getECount() && count++ < 500) {
            PsrPrimaryAppMathItem item = new PsrPrimaryAppMathItem();
            int index = random.nextInt(10000) % pointList.size();
            String ek = pointList.get(index);

            item.setEk(ek);
            item.setEType(pointsOfBook.get(ek));  // 此处复用这个字段,填入KPID
            item.setTime(20);
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

    public List<MathEtTime> getRandomEtListByEk(UserAppMathEkItem appMathEk, PsrExamContext psrExamContext) {
        return getRandomEtListByEk(appMathEk, psrExamContext, null);
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*

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
            // 已推荐次数
            if (psrEkEtInfoSelected.getMaxCount() > currentCount) {
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
        // 1map key : ek
        // 2map key : et
        Map<String, Map<String, KeyValuePair<Integer, Double>>> etMap = new HashMap<>();
        for (String pointName : pointsOfBook) {
            if ((!StringUtils.isBlank(pointName)) && (!etMap.containsKey(pointName))) {
                // key : pattern
                Map<String, KeyValuePair<Integer, Double>> patterns = psrMathPointsPatternsScore.getScoreByPointName(pointName, userBucket);
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
                info.setEk(ek);
                info.setEt(et);
                info.setMaxCount(maxCount);
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
*/


}

