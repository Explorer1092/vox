package com.voxlearning.utopia.service.psr.impl.examen;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.io.Serializable;

@Slf4j
@Named
@Data
@Deprecated // 2015.08.10
public class PsrExamEnSimilarController implements Serializable {
// fixme 2015.09.01 之后暂不维护
/*
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrEnglishExamResultMemcachedDao psrEnglishExamResultMemcachedDao;
    @Inject private PsrAboveLevelBookEidsDao psrAboveLevelBookEidsDao;
    @Inject private PsrExamEnGlobalWrongCache psrExamEnGlobalWrongCache;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrExamEnGetData psrExamEnGetData;

    public PsrExamEnSimilarContent deal(String product, String uType,
                                        Long userId, int regionCode, Long bookId, Long unitId, int eCount,
                                        float minP, float maxP) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP);

        return dealCore(psrExamContext);
    }

    public PsrExamEnSimilarContent dealCore(PsrExamContext psrExamContext) {
        Date dtB = new Date();

        PsrExamEnSimilarContent retExamContent = new PsrExamEnSimilarContent();

        if (ekCouchbaseDao == null || psrExamContext == null)
            return logContent(psrExamContext, retExamContent, "Can not connect databases.", dtB, "info");

        //List<String/ *eid* /> psrEids = new ArrayList<>();  // 该次推题记录, 控制防止重复推题
        if (psrExamContext.getRecommendEids() == null)
            psrExamContext.setRecommendEids(new ArrayList<String>());

        // 取出当天 用户的做题数据
        List<String/ *eid* /> examEids = new ArrayList<>();
        //List<QuestionResultLog> examResults = psrEnglishExamResultMemcachedDao.findByUserId(psrExamContext.getUserId());
        List<QuestionResultLog> examResults = psrExamEnGetData.getQuestionResultLog(psrExamContext);
        for (QuestionResultLog result : examResults) {
            examEids.add(result.getEid());
        }
        psrExamContext.setExamEids(examEids);

        psrExamEnGlobalWrongCache.initMap();

        // 错题重做的正确率比其他的题相对低一些所以这里要微调一下
        psrExamContext.setMinP(Double.valueOf(psrExamContext.getMinP() * 1.08).floatValue());
        psrExamContext.setMaxP(Double.valueOf(psrExamContext.getMaxP() * 1.08).floatValue());

        psrExamContext.setEkCouchbaseDao(ekCouchbaseDao);
        psrExamContext.setPsrAboveLevelBookEidsDao(psrAboveLevelBookEidsDao);

        // 判断是否超纲
        EidFilter eidFilter = new EidFilter();
        eidFilter.setPsrExamContext(psrExamContext);
        eidFilter.initBookEids();
        psrExamContext.setEidFilter(eidFilter);

        // 课本下没有题
        if (eidFilter.getBookEids() == null || eidFilter.getBookEids().size() <= 0)
            return logContent(psrExamContext, retExamContent, "Can not found eids by bookid: " + psrExamContext.getBookId(), dtB, "info");

        // 该uid的历史错题记录, From couchbase
        UserExamEnWrongContent userExamEnWrongContent = null;
        userExamEnWrongContent = ekCouchbaseDao.getCouchbaseUserExamEnWrong(psrExamContext.getUserId());
        if (userExamEnWrongContent == null) {
            // 没有历史错题和验证题 推荐全局易错题
            List<PsrExamEnSimilarItem> fallibilityList = getFallibility(psrExamContext, psrExamContext.getECount(), psrExamContext.getRecommendEids());
            retExamContent.setExamList(fallibilityList);
            if (fallibilityList != null && fallibilityList.size() > 0)
                return logContent(psrExamContext, retExamContent, "success", dtB, "info");

            return logContent(psrExamContext, retExamContent, "Can not found eids from fallibility.", dtB, "info");
        }

        psrExamContext.setUserExamEnWrongContent(userExamEnWrongContent);

        // 按比例 优先推 历史错题(0.6)，其次推 错题验证题(0.2)，如不够题数 则推错题验证题的 类题(0.2)
        // fixme 可配置
        Double wrongRate = 0.6;
        Double verifyRate = 0.2;
        Double similarRate = 0.2;

        List<PsrExamEnSimilarItem> wrongList = new ArrayList<>();
        List<PsrExamEnSimilarItem> verifyList = new ArrayList<>();
        List<PsrExamEnSimilarItem> similarList = new ArrayList<>();
        List<PsrExamEnSimilarItem> fallibilityList = new ArrayList<>();

        if (psrExamContext.getECount() <= 0 || psrExamContext.getECount() > 50)
            psrExamContext.setECount(5);

        boolean bGetWrongList = true;
        boolean bGetVerifyList = true;
        boolean bGetSimilarList = true;

        Integer index = 0;
        while (retExamContent.getExamList().size() < psrExamContext.getECount()) {
            if (index++ > 200) {
                log.warn("getSimilarContronller while index > 200, wrongRate:" + wrongRate.toString()
                        + " verifyRate:" + verifyRate.toString() + " similarRate:" + similarRate.toString());
                break;
            }
            // 已经取得足够多的题
            if (wrongList.size() + verifyList.size() + similarList.size() + fallibilityList.size() >= psrExamContext.getECount())
                break;

            Double tmpRate = (wrongList.size() + 0.0) / psrExamContext.getECount();
            // 优先获取 历史错题
            if (bGetWrongList && wrongRate > 0.0 && wrongRate <= 1.0 && tmpRate < wrongRate) {
                List<PsrExamEnSimilarItem> tmpList = getHistoryWrong(psrExamContext, 1, psrExamContext.getRecommendEids());
                if (tmpList == null || tmpList.size() <= 0) {
                    // 没有历史错题
                    bGetWrongList = false;
                    // 调整分配权重 粗略 调整  fixme
                    verifyRate += wrongRate;
                    continue;
                }
                wrongList.addAll(tmpList);
                continue;
            }

            tmpRate = (verifyList.size() + 0.0) / psrExamContext.getECount();
            // 其次获取 历史验证题
            if (bGetVerifyList && verifyRate > 0.0 && verifyRate <= 1.0 && tmpRate < verifyRate) {
                List<PsrExamEnSimilarItem> tmpList = getHistoryVerify(psrExamContext, 1, psrExamContext.getRecommendEids());
                if (tmpList == null || tmpList.size() <= 0) {
                    // 没有历史验证题
                    bGetVerifyList = false;
                    // 调整分配权重 粗略 调整  fixme
                    similarRate += verifyRate;
                    continue;
                }
                verifyList.addAll(tmpList);
                continue;
            }

            Integer reqCount = psrExamContext.getECount() - psrExamContext.getRecommendEids().size();

            tmpRate = (similarList.size() + 0.0) / psrExamContext.getECount();
            //  验证题的 类题
            if (bGetSimilarList && similarRate > 0.0 && similarRate <= 1.0 && tmpRate < similarRate) {
                List<PsrExamEnSimilarItem> tmpList = getSimilar(psrExamContext, reqCount, psrExamContext.getRecommendEids());
                if (tmpList == null || tmpList.size() <= 0) {
                    bGetSimilarList = false;
                    continue;
                }
                similarList.addAll(tmpList);
                continue;
            }

            // 到这里了题还是不够, 做全局易错题吧
            reqCount = psrExamContext.getECount() - psrExamContext.getRecommendEids().size();
            List<PsrExamEnSimilarItem> tmpList = getFallibility(psrExamContext, reqCount, psrExamContext.getRecommendEids());
            // 全局易错题都没有了,退出吧
            if (tmpList == null || tmpList.size() <= 0)
                break;
            fallibilityList.addAll(tmpList);
        }

        if (wrongList.size() > 0)
            retExamContent.getExamList().addAll(wrongList);
        if (verifyList.size() > 0)
            retExamContent.getExamList().addAll(verifyList);
        if (similarList.size() > 0)
            retExamContent.getExamList().addAll(similarList);
        if (fallibilityList.size() > 0)
            retExamContent.getExamList().addAll(fallibilityList);

        return logContent(psrExamContext, retExamContent, "success", dtB, "info");
    }

    public List<PsrExamEnSimilarItem> getHistoryCommon(PsrExamContext psrExamContext, Integer eCount, String type/ *wrong,verify* /, List<String> eids) {
        List<PsrExamEnSimilarItem> retList = new ArrayList<>();
        if (eCount <= 0 || StringUtils.isEmpty(type))
            return retList;
        if (eids == null)
            eids = new ArrayList<>();

        // todo 奇怪不应该走到这里，先加防护吧
        if (psrExamContext.getUserExamEnWrongContent() == null)
            return retList;

        List<UserExamEnWrongItem> itemList = null;

        if (type.equals("wrong"))
            itemList = psrExamContext.getUserExamEnWrongContent().getWrongW2WList();
        else if (type.equals("verify"))
            itemList = psrExamContext.getUserExamEnWrongContent().getWrongW2RList();
        else
            return retList;

        // itemList 已经是降序的排列
        if (itemList == null || itemList.size() <= 0)
            return retList;

        // 轮盘算法
        int baseNumberForWeight = 100;
        int index = 0;
        while (retList.size() < eCount && retList.size() < itemList.size() && index < itemList.size()) {
            if (index++ > 100) {
                log.warn("get " + type + " examen index > 100.");
                break;
            }

            Random random = psrExamContext.getRandom();
            double randomTd = random.nextInt(1000000) + 0.0;
            double weightTmp = randomTd / 1000000 * baseNumberForWeight;

            double weightNsum = 0.0;
            for (int i = 0; i < itemList.size(); i++) {
                UserExamEnWrongItem item = itemList.get(i);
                if (item == null)
                    continue;

                boolean bGet = false;
                weightNsum += item.getRate();
                if (weightTmp <= weightNsum)
                    bGet = true;
                if (!bGet && i == itemList.size() - 1)
                    bGet = true;

                if (!bGet)
                    continue;

                // 已经找到随机的eid,然后判断是否符合推题的条件,不符合则寻找下一个eid

                // 不在正确率范围的不推
                if (item.getRate() < psrExamContext.getMinP() || item.getRate() > psrExamContext.getMaxP())
                    continue;

                // 当天做过的eid 不推
                if (psrExamContext.getExamEids() != null && psrExamContext.getExamEids().contains(item.getEid()))
                    continue;

                // 本次已经推过的题 不推
                if (eids.contains(item.getEid()))
                    continue;

                // 超纲的不推
                if (psrExamContext.getEidFilter() != null && psrExamContext.getEidFilter().isAboveLevelEid(item.getEid()))
                    continue;

                // 验证题 没解冻 不推(距上一次做题 必须有一定的时间间隔)
                Date now = new Date();
                if (type.equals("verify") && now.getTime() - item.getDate().getTime() < 3 * 86400 * 1000 / *ms* /)
                    continue;

                if (bGet) {
                    // 获取到一个eid, 好开心呀，记录下来吧 :)
                    PsrExamEnSimilarItem psrExamEnSimilarItem = new PsrExamEnSimilarItem(item, type);
                    retList.add(psrExamEnSimilarItem);
                    eids.add(item.getEid());

                    break;
                }

            }  // end for
        } // end while

        return retList;
    }

    public List<PsrExamEnSimilarItem> getHistoryWrong(PsrExamContext psrExamContext, Integer eCount, List<String> eids) {
        return getHistoryCommon(psrExamContext, eCount, "wrong", eids);
    }

    public List<PsrExamEnSimilarItem> getHistoryVerify(PsrExamContext psrExamContext, Integer eCount, List<String> eids) {
        return getHistoryCommon(psrExamContext, eCount, "verify", eids);
    }

    public List<PsrExamEnSimilarItem> getSimilar(PsrExamContext psrExamContext, Integer eCount, List<String> eids) {
        List<PsrExamEnSimilarItem> retList = new ArrayList<>();
        if (eCount <= 0)
            return retList;
        if (eids == null)
            eids = new ArrayList<>();

        if (psrExamContext == null) {
            log.warn("what's the fu.. getSimilar psrExamContext == null.");
            return retList;
        }
        // 没有历史验证题,所以没有类题可推
        if (psrExamContext.getUserExamEnWrongContent() == null)
            return retList;

        List<UserExamEnWrongItem> itemList = psrExamContext.getUserExamEnWrongContent().getWrongW2RList();
        if (itemList == null || itemList.size() <= 0)
            return retList;

        // fixme ek et weight sum rate 这些数据从哪里来？ 先不推吧
        if (true)
            return retList;

        Map<String/ *eid* /, PsrExamEnSimilarItem> similarEidsMapDay = new HashMap<>();  // 冷却期内的eid的类题集合
        Map<String/ *eid* /, PsrExamEnSimilarItem> similarEidsMapAll = new HashMap<>();  // 所有eid的类题集合

        Date now = new Date();

        Double sumSimilarAll = 0.0;
        Double sumSimilarDay = 0.0;

        for (UserExamEnWrongItem item : itemList) {
            String strLine = ekCouchbaseDao.getCouchbaseDataByKey("itemsim_" + item.getEid());
            if (StringUtils.isEmpty(strLine))
                continue;
            Map<String/ *eid* /, Double/ *相似度* /> tempMap = PsrTools.decodeItemsimFromLine(strLine);
            if (tempMap == null || tempMap.size() <= 0)
                continue;

            for (Map.Entry<String, Double> entry : tempMap.entrySet()) {
                PsrExamEnSimilarItem similarItem = new PsrExamEnSimilarItem();
                similarItem.setEid(entry.getKey());
                similarItem.setAlogv("similarity");
                similarItem.setSimilarity(entry.getValue());
                similarItem.setLastDate(item.getDate());

                if (!similarEidsMapAll.containsKey(entry.getKey())) {
                    similarEidsMapAll.put(entry.getKey(), similarItem);
                    sumSimilarAll += entry.getValue();
                }

                if (!similarEidsMapDay.containsKey(entry.getKey()) && now.getTime() - item.getDate().getTime() < 3 * 86400 * 1000) {
                    similarEidsMapDay.put(entry.getKey(), similarItem);
                    sumSimilarDay += entry.getValue();
                }
            }
        }

        Double sumSimilar = sumSimilarDay;

        Map<String/ *eid* /, PsrExamEnSimilarItem> pMap = similarEidsMapDay;

        if (pMap == null || pMap.size() < eCount) {
            pMap = similarEidsMapAll;
            sumSimilar = sumSimilarAll;
        }

        if (pMap == null || pMap.size() <= 0)
            return retList;

        for (String key : pMap.keySet()) {
            if (sumSimilar <= 0.0)
                break;

            pMap.get(key).setWeight(pMap.get(key).getSimilarity() / sumSimilar * 100);
        }

        List<Map.Entry<String, PsrExamEnSimilarItem>> sortList = new ArrayList<>(pMap.entrySet());
        if (sortList.size() <= 0)
            return null;

        final boolean finalDesc = true;
        Collections.sort(sortList, new Comparator<Map.Entry<String, PsrExamEnSimilarItem>>() {
            @Override
            public int compare(Map.Entry<String, PsrExamEnSimilarItem> o1, Map.Entry<String, PsrExamEnSimilarItem> o2) {
                int n = 0;

                if (o2.getValue().getSimilarity() - o1.getValue().getSimilarity() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getValue().getSimilarity() - o1.getValue().getSimilarity() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });

        // 轮盘算法
        int baseNumberForWeight = 100;
        int index = 0;
        while (retList.size() < eCount && index++ < sortList.size() && retList.size() < sortList.size()) {

            Random random = psrExamContext.getRandom();
            double randomTd = random.nextInt(1000000) + 0.0;
            double weightTmp = randomTd / 1000000 * baseNumberForWeight;

            double weightNsum = 0.0;
            for (int i = 0; i < sortList.size(); i++) {
                PsrExamEnSimilarItem item = sortList.get(i).getValue();
                if (item == null)
                    continue;

                boolean bGet = false;
                weightNsum += item.getWeight();
                if (weightTmp <= weightNsum)
                    bGet = true;
                if (!bGet && i == sortList.size() - 1)
                    bGet = true;

                if (!bGet)
                    continue;

                // 已经找到随机的eid,然后判断是否符合推题的条件,不符合则寻找下一个eid

                // 不在正确率范围的不推
                if (item.getRate() < psrExamContext.getMinP() || item.getRate() > psrExamContext.getMaxP())
                    continue;

                // 当天做过的eid 不推
                if (psrExamContext.getExamEids() != null && psrExamContext.getExamEids().contains(item.getEid()))
                    continue;

                // 本次已经推过的题 不推
                if (eids.contains(item.getEid()))
                    continue;

                // 超纲的不推
                if (psrExamContext.getEidFilter() != null && psrExamContext.getEidFilter().isAboveLevelEid(item.getEid()))
                    continue;

                if (bGet) {
                    // 获取到一个eid, 好开心呀，记录下来吧 :)

                    // fixme ek et weight sum rate 这些数据从哪里来？
                    // todo init item

                    retList.add(item);
                    eids.add(item.getEid());

                    break;
                }
            }  // end for
        }


        return retList;
    }

    public List<PsrExamEnSimilarItem> getFallibility(PsrExamContext psrExamContext, Integer eCount, List<String> eids) {
        List<PsrExamEnSimilarItem> retList = new ArrayList<>();
        if (eCount <= 0)
            return retList;

        if (eids == null)
            eids = new ArrayList<>();

        if (psrExamEnGlobalWrongCache == null) {
            log.warn("what's the fu.. getFallibility psrExamEnGlobalWrongCache == null.");
            return retList;
        }
        if (psrExamContext == null) {
            log.warn("what's the fu.. getFallibility psrExamContext == null.");
            return retList;
        }
        if (psrExamContext.getEidFilter() == null) {
            log.warn("what's the fu.. getFallibility psrExamContext.getEidFilter() == null.");
            return retList;
        }

        // 从课本内取题
        Map<String, ExamEnGlobalWrongItem> bookGlobalWrongEids = psrExamEnGlobalWrongCache.getGlobalWrongMapByBookEids(
                psrExamContext.getBookId(), psrExamContext.getEidFilter().getBookEids());

        if (bookGlobalWrongEids == null || bookGlobalWrongEids.size() <= 0)
            return retList;

        List<Map.Entry<String, ExamEnGlobalWrongItem>> sortList = new ArrayList<>(bookGlobalWrongEids.entrySet());

        final boolean finalDesc = true;
        Collections.sort(sortList, new Comparator<Map.Entry<String, ExamEnGlobalWrongItem>>() {
            @Override
            public int compare(Map.Entry<String, ExamEnGlobalWrongItem> o1, Map.Entry<String, ExamEnGlobalWrongItem> o2) {
                int n = 0;
                if (o2.getValue().getWeight() - o1.getValue().getWeight() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getValue().getWeight() - o1.getValue().getWeight() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });

        // 轮盘算法
        int baseNumberForWeight = 100;
        int index = 0;
        while (retList.size() < eCount && index++ < sortList.size() && retList.size() < sortList.size()) {

            Random random = psrExamContext.getRandom();
            double randomTd = random.nextInt(1000000) + 0.0;
            double weightTmp = randomTd / 1000000 * baseNumberForWeight;

            double weightNsum = 0.0;
            for (int i = 0; i < sortList.size(); i++) {
                ExamEnGlobalWrongItem item = sortList.get(i).getValue();
                if (item == null)
                    continue;

                boolean bGet = false;
                weightNsum += item.getWeightPer();
                if (weightTmp <= weightNsum)
                    bGet = true;
                if (!bGet && i == sortList.size() - 1)
                    bGet = true;

                if (!bGet)
                    continue;

                // 已经找到随机的eid,然后判断是否符合推题的条件,不符合则寻找下一个eid

                // 不在正确率范围的不推
                if (item.getRate() < psrExamContext.getMinP() || item.getRate() > psrExamContext.getMaxP())
                    continue;

                // 当天做过的eid 不推
                if (psrExamContext.getExamEids() != null && psrExamContext.getExamEids().contains(item.getEid()))
                    continue;

                // 本次已经推过的题 不推
                if (eids.contains(item.getEid()))
                    continue;

                if (bGet) {
                    // 获取到一个eid, 好开心呀，记录下来吧 :)
                    PsrExamEnSimilarItem psrExamEnSimilarItem = new PsrExamEnSimilarItem(item);
                    retList.add(psrExamEnSimilarItem);
                    eids.add(item.getEid());

                    break;
                }
            }  // end for
        }

        return retList;
    }

    private PsrExamEnSimilarContent logContent(PsrExamContext psrExamContext, PsrExamEnSimilarContent retExamContent, String errorMsg,
                                              Date dtB, String logLevel) {
        if (retExamContent == null)
            retExamContent = new PsrExamEnSimilarContent();
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

    private String formatReturnLog(PsrExamContext psrExamContext, PsrExamEnSimilarContent retExamContent, Long spendTime) {
        if (psrExamContext == null || retExamContent == null || spendTime == null) {
            return "psrExamContext or retExamContent or spendTime is null.";
        }
        String strLog = retExamContent.formatList();
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId().toString() + " unit:" + psrExamContext.getUnitId().toString();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }

*/
// fixme 2015.09.01 之后暂不维护

}

