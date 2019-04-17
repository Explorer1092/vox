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

package com.voxlearning.utopia.service.psr.impl.data;

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsNewDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsNewMathDao;
import com.voxlearning.utopia.service.psr.impl.util.PsrEtRegions;
import com.voxlearning.utopia.service.psr.impl.util.PsrIrtPredictEx;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionKnowledgePoint;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;


@Slf4j
@Named
public class PsrExamEnFilter implements Serializable {
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrEtRegions psrEtRegions;
    @Inject private PsrAboveLevelBookEidsNewDao psrAboveLevelBookEidsNewDao;
    @Inject private PsrAboveLevelBookEidsNewMathDao psrAboveLevelBookEidsNewMathDao;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    // EkFilter

    // 用户请求 book中没有的知识点 不推荐
    public EksTypeListContent doFilter(PsrExamContext psrExamContext, Subject subject) {
        if (psrExamContext == null || subject == null)
            return null;

        // 取出整本书的知识点,unitId用来判断是否为moduleId
        Map<String, List<String>> unitsKnowledgePointsMap = psrBooksSentencesNew.getUnitsSentenceByBookId(psrExamContext.getBookId(), subject, psrExamContext.getUnitId());
        if (MapUtils.isEmpty(unitsKnowledgePointsMap))
            return null;

        UserExamContent userExamContentId = psrExamEnData.getUserExamContentId(psrExamContext, subject);
        List<UserEkContent> ekList = userExamContentId != null ? userExamContentId.getEkList() : null;

        String unitId = psrExamContext.getUnitId();

        if (ekList == null)
            ekList = new ArrayList<>();

        EkFilter ekFilter = psrExamContext.getEkFilter();
        if (ekFilter == null) {
            ekFilter = new EkFilter();
            psrExamContext.setEkFilter(ekFilter);
        }

        EksTypeListContent eksTypeListContent = new EksTypeListContent();

        List<String> eksPointRefs = ekFilter.getEksPointRefs();

        if (psrExamContext.isFilterByBook()) {
            // 根据unitId判断获取知识点范围
            // fixme 下次上线修改 unitId >= 0
            // 新教材结构下用groupId表示unitId

            if (eksPointRefs == null)
                eksPointRefs = new ArrayList<>();
            if (psrExamContext.isFilterByUnit() && !unitId.equals("-1") /*> 0*/) {
//                if (psrBookPersistence != null && psrBookPersistence.getBookStructure() == 1) {
//                    units = psrBookPersistence.getUnitIdsByGroupId(unitId);
//                    for (Long unit : units) {
//                        List<String> eksTmp = PsrTools.getEksFromPointRefsByUnitId(unitKnowledgePointRefs, unit);
//                        if (eksPointRefs == null)
//                            eksPointRefs = new ArrayList<>();
//                        if (eksTmp != null)
//                            eksPointRefs.addAll(eksTmp);
//                    }
//                }
//                else
//                    eksPointRefs = PsrTools.getEksFromPointRefsByUnitId(unitKnowledgePointRefs, unitId);
                eksPointRefs.addAll(unitsKnowledgePointsMap.containsKey(unitId) ? unitsKnowledgePointsMap.get(unitId) : new ArrayList<String>());
            } else {
                unitsKnowledgePointsMap.values().forEach(eksPointRefs::addAll);
            }
        }
        // 根据book过滤知识点=true 并且book没有知识点 则返回
        if (psrExamContext.isFilterByBook() && (eksPointRefs == null || eksPointRefs.size() <= 0))
            return eksTypeListContent;

        List<UserEkContent> tmpAllEkList = new ArrayList<>();
        List<String> tmpEks = new ArrayList<>();
        for (UserEkContent userEkContent : ekList) {
            if (tmpEks.contains(userEkContent.getEk()))
                continue;
            tmpAllEkList.add(userEkContent);
            tmpEks.add(userEkContent.getEk());
        }

        Double tmpMaster = 0.1111111111D;
        for (String tmpEk : eksPointRefs) {
            if (tmpEks.contains(tmpEk))
                continue;
            UserEkContent tmpContent = new UserEkContent();
            tmpContent.setEk(tmpEk);
            tmpContent.setCount((short) 0);
            tmpContent.setLevel((short) 1);
            tmpContent.setMaster(tmpMaster);  // 默认是0.1111111111 - 0.0000000001 均等
            tmpAllEkList.add(tmpContent);
            tmpEks.add(tmpEk);
            tmpMaster -= 0.0000000001D;
        }

        for (UserEkContent userEkContent : tmpAllEkList) {
            if (psrExamContext.isFilterByBook() && !eksPointRefs.contains(userEkContent.getEk())) {
                eksTypeListContent.getEkAboveLevelList().add(userEkContent);
                eksTypeListContent.setEkAboveLevelWeightSum(
                        eksTypeListContent.getEkAboveLevelWeightSum() + userEkContent.getMaster());
                continue;
            }

            switch (userEkContent.getLevel()) {
                case 2:
                    // level = 2 基本掌握
                    eksTypeListContent.getEkLevelOneList().add(userEkContent);
                    eksTypeListContent.setEkLevelOneWeightSum(
                            eksTypeListContent.getEkLevelOneWeightSum() + userEkContent.getMaster());
                    break;
                case 3:
                    // level = 3 真掌握, level = 4 预判掌握, 处理逻辑相同
                case 4:
                    eksTypeListContent.getEkLevelTwoList().add(userEkContent);
                    eksTypeListContent.setEkLevelTwoWeightSum(
                            eksTypeListContent.getEkLevelTwoWeightSum() + userEkContent.getMaster());
                    break;
                case 1:
                    // level = 1 未掌握, 也是默认值
                default:
                    // 默认未掌握
                    eksTypeListContent.getEkLevelZeroList().add(userEkContent);
                    eksTypeListContent.setEkLevelZeroWeightSum(
                            eksTypeListContent.getEkLevelZeroWeightSum() + userEkContent.getMaster());
                    break;
            }
        }

        ekFilter.setEksPointRefs(eksPointRefs);
        ekFilter.setEksTypeListContent(eksTypeListContent);
        psrExamContext.setEkFilter(ekFilter);

        return eksTypeListContent;
    }

    public List<Map.Entry<Double, String>> getEksSortByWeightFromMap(PsrExamContext psrExamContext, String ekType) {
        if (psrExamContext == null || StringUtils.isEmpty(ekType))
            return null;

        EkFilter ekFilter = psrExamContext.getEkFilter();
        if (ekFilter == null)
            ekFilter = new EkFilter();

        EksTypeListContent eksTypeListContent = ekFilter.getEksTypeListContent();
        if (eksTypeListContent == null)
            eksTypeListContent = new EksTypeListContent();

//        Map<String, List<Map.Entry<Double, String>>> ekTypeMap = ekFilter.getEkTypeMap();
//        if (ekTypeMap == null)
//            ekTypeMap = new LinkedHashMap<>();
//
//        if (ekTypeMap.containsKey(ekType))
//            return ekTypeMap.get(ekType);

        List<Map.Entry<Double, String>> list = getEksSortByWeight(psrExamContext, ekType);
        if (list == null || list.size() <= 0)
            return null;

//        ekTypeMap.put(ekType, list);

//        ekFilter.setEksTypeListContent(eksTypeListContent);
//        ekFilter.setEkTypeMap(ekTypeMap);

//        psrExamContext.setEkFilter(ekFilter);

        return list;
    }

    /*
     * 将ek-list 按权重归一化 并排序
     */
    public List<Map.Entry<Double, String>> getEksSortByWeight(PsrExamContext psrExamContext, String ekType) {
        if (psrExamContext == null || psrExamContext.getEkFilter() == null || StringUtils.isEmpty(ekType))
            return null;
        EksTypeListContent eksTypeListContent = psrExamContext.getEkFilter().getEksTypeListContent();
        if (eksTypeListContent == null)
            return null;

        List<UserEkContent> userEkContents = null;

        double weightSum = 0.0;

        if (ekType.equals("levelzero")) {
            weightSum = eksTypeListContent.getEkLevelZeroWeightSum();
            userEkContents = eksTypeListContent.getEkLevelZeroList();
        } else if (ekType.equals("levelone")) {
            weightSum = eksTypeListContent.getEkLevelOneWeightSum();
            userEkContents = eksTypeListContent.getEkLevelOneList();
        } else if (ekType.equals("leveltwo")) {
            weightSum = eksTypeListContent.getEkLevelTwoWeightSum();
            userEkContents = eksTypeListContent.getEkLevelTwoList();
        } else if (ekType.equals("abovelevel")) {
            weightSum = eksTypeListContent.getEkAboveLevelWeightSum();
            userEkContents = eksTypeListContent.getEkAboveLevelList();
        } else if (ekType.equals("down")) {
            weightSum = eksTypeListContent.getEkDownWeightSum();
            userEkContents = eksTypeListContent.getEkDownWeightList();
        } else
            return null;

        // 权重归一化
        Map<Double, String> mapQueue = new LinkedHashMap<>();
        for (int i = 0; userEkContents != null && i < userEkContents.size(); i++) {
            UserEkContent userEkContent = userEkContents.get(i);
            if (userEkContent == null)
                continue;

            double tmp = 0.0;
            if (weightSum > 0)
                tmp = userEkContent.getMaster() / weightSum * psrExamContext.getBaseNumberForWeight();   // 此算法,跟 降权方式 有关

            mapQueue.put(tmp, userEkContent.getEk());
        }

        List<Map.Entry<Double, String>> eksIds = new LinkedList<Map.Entry<Double, String>>(mapQueue.entrySet());
        Collections.sort(eksIds, new Comparator<Map.Entry<Double, String>>() {
            @Override
            public int compare(Map.Entry<Double, String> o1, Map.Entry<Double, String> o2) {
                // 升序排列
                int n = 0;
                if (o2.getKey() - o1.getKey() < 0.0)
                    n = 1;
                else if (o2.getKey() - o1.getKey() > 0.0)
                    n = -1;
                return n;
            }
        });

        return eksIds;
    }


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /* EidFilter
     * 过滤 超纲 和 已经做对过的题
     * 根据 predictRate\weightet 计算权重newWeight, 学生能力不同 计算方法不同
     * predictRate: 预估通过率
     * weightet: 该题题型在本地区热度
     * 高能力：irt-theta > 1.5  , 从小往大选择（选难策略）：依据weight*（1-weightet）进行排序选择
     * 中能力：irt-theta [0~1.5], 从中位数往两边选择（平衡策略）：依据weight*weightet进行排序选择,暂时不用中位数,用list中间位置
     * 低能力：irt-theta < 0    , 从大往小选择（选易策略）：依据weight*weightet进行排序选择
     *
     * ekCount : Ek 对应取题个数
     */
    public List<Map.Entry<Double, EidItem>> getEidsPredictRate(PsrExamContext psrExamContext, EkToEidContent ekToEidContent, int ekCount, Subject subject) {
        if (psrExamContext == null || ekToEidContent == null || subject == null)
            return null;

        List<String> recommendEids = psrExamContext.getRecommendEids();
        UserExamContent userExamContent = psrExamEnData.getUserExamContentId(psrExamContext, subject);
        if (userExamContent == null)
            return null;

        // 获取 该用户 做题记录
        PsrUserHistoryEid psrUserHistoryEid = psrExamEnData.getPsrUserHistoryEid(psrExamContext, subject);

        Map<Double, EidItem> outOfRangeMap = new LinkedHashMap<>();
        Map<Double, EidItem> rightRangeMap = new LinkedHashMap<>();
        Map<Double, EidItem> aboveRangeMap = new LinkedHashMap<>();
        Map<Double, EidItem> allRangeMap = new LinkedHashMap<>();   // 所有的题

        int notAboveLevelEids = 0; // 不超纲 eid-list, 用于记录日志

        double weightet = 0.0;
        double predictRate = 0.0;
        double newWeight = 0.0;

        for (int i = 0; !ekToEidContent.isEidListNull() && i < ekToEidContent.getEidList().size(); i++) {
            weightet = 1.0;
            predictRate = 0.0;
            newWeight = 0.0;

            EidItem eidItem = ekToEidContent.getEidList().get(i);

            String ek = ekToEidContent.getEk();

            if (eidItem == null)
                continue;

            // 已经存在推题列表的题 不重复推
            if (recommendEids != null && recommendEids.contains(eidItem.getEid()))
                continue;

            // 提取代码 获取predictRate 和newWeight，只要传入 eid 即可算出
            // 分为 有Uc 和 无Uc两种
            // 最终形成eid列表

            if (userExamContent.getUserInfoLevel() == 1) {
                double ucW = 0.5;
                newWeight = userExamContent.getUc() * ucW + eidItem.getAccuracyRate() * (1 - ucW);
                // 输出权重
                eidItem.setPredictRate(newWeight);
            } else if (userExamContent.getUserInfoLevel() == 2) {
                newWeight = eidItem.getAccuracyRate();
                // 输出权重
                eidItem.setPredictRate(newWeight);
            } else {
                predictRate = PsrIrtPredictEx.predictEx(userExamContent.getIrtTheta(), eidItem.getAccuracyRate(), eidItem.getIrtA(), eidItem.getIrtB(), eidItem.getIrtC());
                // 输出权重
                eidItem.setPredictRate(predictRate);

                EtRegionItem etRegionItem = psrEtRegions.getEtRegionItemByEt(eidItem.getEt(), psrExamContext.getGrade(), psrExamContext.getRegionCode());

                if (etRegionItem != null)
                    weightet = etRegionItem.getHotLevel();

                newWeight = predictRate * weightet;
                if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta() && weightet - 1.0 < 0)
                    newWeight = predictRate * (1 - weightet);
            }
            // 预防所有的newWeight都为0.0,会导致有些题被丢弃
            if (Double.compare(newWeight, 0.0D) == 0) {
                newWeight = newWeight + 0.00000000000001 * psrExamContext.getRandom().nextInt(1000000);
            }
            if (Double.compare(newWeight, 1.0D) == 0) {
                newWeight = newWeight - 0.00000000000001 * psrExamContext.getRandom().nextInt(1000000);
            }

            // 记录 所有的 eid列表
            allRangeMap.put(newWeight, eidItem);

            // 历史上做过的题,或者psr最近几天推荐过的题 不推
            if (isDoItRightEid(psrExamContext, eidItem.getEid(), subject)) {
                // 保存以备补充题目使用
                KeyValuePair<String, EidItem> kv = new KeyValuePair<>(ek, eidItem);
                psrExamContext.getRecommendedList().add(kv);
                continue;
            }

            // 超纲的题
            aboveRangeMap.put(newWeight, eidItem);
            if (isAboveLevelEid(psrExamContext, eidItem.getEid(), subject))
                continue;
            notAboveLevelEids++;

            // fixme 暂时去掉(新题库上线,题少), poo3 和 a001 向下(minP)扩展的时候加限度 >= minP - *
//            if (userExamContent.getUserInfoLevel() >= 2)
//                outOfRangeMap.put(newWeight, eidItem);
//            else {
//                if (eidItem.getPredictRate() >= psrExamContext.getMinP() - psrExamContext.getMinPDown())
//                    outOfRangeMap.put(newWeight, eidItem);
//            }

            outOfRangeMap.put(newWeight, eidItem);

            // 难度区间 判断
            if (eidItem.getPredictRate() < psrExamContext.getMinP() || eidItem.getPredictRate() > psrExamContext.getMaxP())
                continue;

            // 题量 足够的时候 选取 本地区 题型热度 top3 的eid
            // 暂时 推迟 实现, 因数据库中是按 Et存储,如果做topN排行 需要遍历库中的所有Et数据
            // 实现逻辑,添加一个 topN热度的 eid-list, 如果该题 题型属于 topN,则加入eid-list,并且此list题量够的话,优先提取数据

            // 如果 合适的列表不是 rightRangeMap,说明过滤后的题量不够
            rightRangeMap.put(newWeight, eidItem);
        }

        Map<Double, EidItem> pMap = rightRangeMap;

        if (rightRangeMap.size() > 0)
            pMap = rightRangeMap;
        else if (outOfRangeMap.size() > 0)
            pMap = outOfRangeMap;
        else if (aboveRangeMap.size() > 0 && isLastAdaptive(psrExamContext, allRangeMap.size(), aboveRangeMap.size())) {
            pMap = aboveRangeMap;
        }

        if (notAboveLevelEids < psrExamContext.getMinNotAboveLevelEids()) {

            String strLog = "[state:aboveLevel book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
            strLog += " ek:" + ekToEidContent.getEk();
            strLog += " eidCount:" + notAboveLevelEids;
            strLog += "<" + psrExamContext.getMinNotAboveLevelEids() + "]";

            // log.info(strLog);
        }

        if (pMap.isEmpty())
            return null;

        List<Map.Entry<Double, EidItem>> retSortList = new LinkedList<>(pMap.entrySet());

        boolean desc = true;  // 默认降序

        if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta()) {
            // 升序
            desc = false;
        }

        final boolean finalDesc = desc;
        Collections.sort(retSortList, new Comparator<Map.Entry<Double, EidItem>>() {
            @Override
            public int compare(Map.Entry<Double, EidItem> o1, Map.Entry<Double, EidItem> o2) {
                int n = 0;
                if (o2.getKey() - o1.getKey() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getKey() - o1.getKey() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });

        return retSortList;
    }

    /*
     * 适应性算法启用补题逻辑(把不超纲过滤功能去掉)
     * isAdaptive=true:自适应算法
     * 自动判断,eidCount个题都被超过过滤了且个数>=100,则说明这个知识点没题了,如果超过3个知识点出现这样的情况,不妙-超纲过滤有问题-可能是离线数据计算错误
     * aboveLevelEidCount == eidCount && aboveLevelEidCount >= 100 && aboveLevelErrorCount >= 3
     */
    public boolean isLastAdaptive(PsrExamContext psrExamContext, int eidCount, int aboveLevelEidCount) {
        if (psrExamContext == null)
            return true;

        // 判断超纲逻辑是否有问题,有问题则使用自适应算法
        if ((aboveLevelEidCount == eidCount) && aboveLevelEidCount >= 100)
            psrExamContext.setAboveLevelErrorCount(psrExamContext.getAboveLevelErrorCount() + 1);
        if (psrExamContext.getAboveLevelErrorCount() >= 5) {
            psrExamContext.setAdaptive(true);
            if (!psrExamContext.getPsrExamType().endsWith("_autoadaptive"))
                psrExamContext.setPsrExamType(psrExamContext.getPsrExamType() + "_autoadaptive");
            // 此处判断超纲教材挂载的题目出现问题,强制使用自适应
            log.error("book:" + psrExamContext.getBookId() + " adaptive err");
            return true;
        }

        return (psrExamContext.isExamEnUseAdapt());
    }

    public void initBookEids(PsrExamContext psrExamContext, Subject subject) {
        if (psrExamContext == null || psrAboveLevelBookEidsNewDao == null || psrAboveLevelBookEidsNewMathDao == null || subject == null)
            return;
        if (subject.equals(Subject.ENGLISH)) {
            AboveLevelBookUnitEidsNew bookUnitEids = psrAboveLevelBookEidsNewDao.findUnitsEidsByBookId(psrExamContext.getBookId());
            if (bookUnitEids == null || bookUnitEids.getUnits() == null || bookUnitEids.getUnits().size() <= 0)
                return;
            EidFilter eidFilter = psrExamContext.getEidFilter();
            if (eidFilter == null)
                eidFilter = new EidFilter();
            eidFilter.setBookEids(psrAboveLevelBookEidsNewDao.getEidsByBookUnitEids(bookUnitEids));
            eidFilter.setBookUnitEids(bookUnitEids);
            if (!psrExamContext.isFilterByUnit())
                return;
            eidFilter.setBookGroupIdEids(bookUnitEids);
            psrExamContext.setEidFilter(eidFilter);
        } else if (subject.equals(Subject.MATH)) {
            AboveLevelBookUnitEidsNewMath bookUnitEidsMath = psrAboveLevelBookEidsNewMathDao.findUnitsEidsByBookId(psrExamContext.getBookId());
            if (bookUnitEidsMath == null || bookUnitEidsMath.getUnits() == null || bookUnitEidsMath.getUnits().size() <= 0)
                return;
            EidFilter eidFilter = psrExamContext.getEidFilter();
            if (eidFilter == null)
                eidFilter = new EidFilter();
            eidFilter.setBookEids(psrAboveLevelBookEidsNewMathDao.getEidsByBookUnitEids(bookUnitEidsMath));
            eidFilter.setBookUnitEidsMath(bookUnitEidsMath);
            if (!psrExamContext.isFilterByUnit())
                return;
            eidFilter.setBookGroupIdEidsMath(bookUnitEidsMath);
            psrExamContext.setEidFilter(eidFilter);
        }
    }

    /*
     * 判断是否超纲
     * true ：超纲, false: 反之
     */
    public boolean isAboveLevelEid(PsrExamContext psrExamContext, String eid, Subject subject) {
        // 语文暂时按教学进度挂载题目,所以暂时没有超纲的题目列表
        if (subject != null && subject.equals(Subject.CHINESE))
            return false;

        if (psrExamContext == null || psrExamContext.getEidFilter() == null || StringUtils.isEmpty(eid) || subject == null)
            return true;

        // fixme 第一版默认unitId=0,第二版(启用unit超纲)默认值unitId=-1 不用unit超纲, unitId>=0 为使用unit超纲过滤;
        // fixme 因默认值不同所以加入过度流程,先 unitId>0,等调用者都更新到第二版的时候,在修改成unitId>=0.
        if (psrExamContext.isFilterByUnit() && !psrExamContext.getUnitId().equals("-1") /*&& Long.parseLong(psrExamContext.getUnitId()) > 0*/) {
            if (subject.equals(Subject.ENGLISH)) {
                AboveLevelBookUnitEidsNew bookUnitEidsTmp = psrExamContext.getEidFilter().getBookUnitEids();
//            PsrBookPersistence psrBookPersistence = psrExamEnData.getPsrBookPersistence(psrExamContext);
//            if (psrBookPersistence != null && psrBookPersistence.getBookStructure() == 1)
//                bookUnitEidsTmp = psrExamContext.getEidFilter().getBookGroupIdEids();

                // 根据单元过滤
                if (bookUnitEidsTmp == null || bookUnitEidsTmp.getUnits() == null)
                    return false;
                if (!bookUnitEidsTmp.getUnits().containsKey(psrExamContext.getUnitId()))
                    return false;
                List<String> tmpEids = bookUnitEidsTmp.getUnits().get(psrExamContext.getUnitId());
                if (tmpEids == null)
                    return false;
                return (!tmpEids.contains(eid));
            } else if (subject.equals(Subject.MATH)) {
                AboveLevelBookUnitEidsNewMath bookUnitEidsTmp = psrExamContext.getEidFilter().getBookUnitEidsMath();
                // 根据单元过滤
                if (bookUnitEidsTmp == null || bookUnitEidsTmp.getUnits() == null)
                    return false;
                if (!bookUnitEidsTmp.getUnits().containsKey(psrExamContext.getUnitId()))
                    return false;
                List<String> tmpEids = bookUnitEidsTmp.getUnits().get(psrExamContext.getUnitId());
                if (tmpEids == null)
                    return false;
                return (!tmpEids.contains(eid));
            }
        }

        List<String> bookEids = psrExamContext.getEidFilter().getBookEids();
        if (!psrExamContext.isFilterByBook() || bookEids == null || bookEids.size() <= 0)
            return false;

        return (!bookEids.contains(eid));
    }

    /*
     * 该用户 历史上做过的题 不推
     * true: 做对过(不推)，false：反之
     * 该用户 历史上做"过"的题 不重做,
     */
    public boolean isDoItRightEid(PsrExamContext psrExamContext, String eid, Subject subject) {
        PsrUserHistoryEid psrUserHistoryEid = psrExamEnData.getPsrUserHistoryEid(psrExamContext, subject);
        if (psrExamContext == null || StringUtils.isEmpty(eid) || psrUserHistoryEid == null)
            return false;

        if (psrUserHistoryEid.isDone(eid))
            return true;

        if (psrUserHistoryEid.isPsrByEid(eid, psrExamContext.getEidPsrDays()))
            return true;

        return false;
    }

    public String getMainNewKPByQid(PsrExamContext psrExamContext, String qid) {
        return getMainNewKPFromQuestion(psrExamContext, questionLoaderClient.loadQuestionIncludeDisabled(qid));
    }

    public String getMainNKPFromNKPList(List<NewQuestionKnowledgePoint> newKnowledgePoints) {
        String mainNKP = "";
        if (CollectionUtils.isNotEmpty(newKnowledgePoints)) {
            List<String> eks = new ArrayList<>();
            newKnowledgePoints.stream().filter(p -> p.getMain().equals(1)).forEach(p -> eks.add(p.getId()));
            if (CollectionUtils.isNotEmpty(eks))
                mainNKP = eks.get(0);
        }
        return mainNKP;
    }

    public String getMainNewKPFromQuestion(PsrExamContext psrExamContext, NewQuestion question) {
        if (psrExamContext == null || question == null)
            return null;

        String mainNKP = "";
        if (psrExamContext.getSubject().equals(Subject.MATH)) {
            mainNKP = getMainNKPFromNKPList(question.getKnowledgePointsNew());
            if (StringUtils.isBlank(mainNKP)) {
                // 查找小题的知识点,取第一个有效的主知识点
                List<String> newKps = new ArrayList<>();
                List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                if (CollectionUtils.isNotEmpty(subContents)) {
                    subContents.stream().filter(m -> {
                        return CollectionUtils.isNotEmpty(m.getAnswers());
                    }).forEach(p -> {
                        p.getAnswers().stream().forEach(a -> {
                            String tmpNKP = getMainNKPFromNKPList(a.getKnowledgePointsNew());
                            if (StringUtils.isNotBlank(tmpNKP))
                                newKps.add(tmpNKP);
                        });
                    });
                }
                if (CollectionUtils.isNotEmpty(newKps))
                    mainNKP = newKps.get(0);
            }
        }

        return mainNKP;
    }

    // fixme 题库修改频繁、离线模型数据不稳定 在线加入过滤逻辑 -----------------------------------------------
    // sub_content_type_id 只推荐id=1、4、5、7、8、9、10
    public boolean isPsrFromOnlineQuestionTable(PsrExamContext psrExamContext, String eid, Subject subject) {
        if (StringUtils.isEmpty(eid) || (psrExamContext != null && !psrExamContext.isFilterFromOnlineQuestion()))
            return true;

        NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(eid);
        if (question == null)
            return false;

        return isPsrFromOnlineQuestionTable(psrExamContext, eid, subject, question);
    }

    public boolean isPsrFromOnlineQuestionTable(PsrExamContext psrExamContext, String eid, Subject subject, NewQuestion question) {
        if (StringUtils.isEmpty(eid) || (psrExamContext != null && !psrExamContext.isFilterFromOnlineQuestion()))
            return true;

        if (question == null)
            return false;

        // deleted_at
        if (question.getDeletedAt() != null)
            return false;

        // not_fit_mobile
        if (!question.getNotFitMobile().equals(0))
            return false;

        // 熔断的题不推荐
        if (question.isBroken())
            return false;

        if (subject == Subject.CHINESE) {
            // 语文的都推荐
            if (question.getContent() != null && CollectionUtils.isNotEmpty(question.getContent().getSubContents())) {
                List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                String ps = ekCouchbaseDao.getCouchbaseDataByKey("SubContentTypeIdListPsrChinese");
                if (StringUtils.isNotBlank(ps)) {
                    String[] psrArray = StringUtils.split(ps, ":");
                    if (psrArray.length == 2) {
                        String type = psrArray[0];
                        Set<String> subContentTypeIdFromCBSs = Sets.newHashSet(StringUtils.split(psrArray[1], "|"));
                        if (StringUtils.equalsIgnoreCase(type, "black") && CollectionUtils.isNotEmpty(subContentTypeIdFromCBSs)) {
                            for (NewQuestionsSubContents subContent : subContents) {
                                if (subContent.getSubContentTypeId() == null ||
                                        subContentTypeIdFromCBSs.contains(SafeConverter.toString(subContent.getSubContentTypeId())))
                                    return false;
                            }
                        } else if (StringUtils.equalsIgnoreCase(type, "white") && CollectionUtils.isNotEmpty(subContentTypeIdFromCBSs)) {
                            for (NewQuestionsSubContents subContent : subContents) {
                                if (subContent.getSubContentTypeId() == null ||
                                        !subContentTypeIdFromCBSs.contains(SafeConverter.toString(subContent.getSubContentTypeId())))
                                    return false;
                            }
                        }
                    }
                }
            }
        }

        boolean isPsr = true;
        if (question.getContent() != null) {
            // subContentType
            List<NewQuestionsSubContents> subContentses = question.getContent().getSubContents();
            if (subContentses != null) {
                for (NewQuestionsSubContents sbContent : subContentses) {
                    int typeId = sbContent.getSubContentTypeId();
                    if (Subject.MATH.equals(subject)) {
                        if (typeId == 1 || typeId == 4 || typeId == 5)
                            isPsr = true;
                        else
                            isPsr = false;
                        return isPsr;
                    }
                    if (Subject.CHINESE.equals(subject))   // 语文的都推荐
                        return isPsr;
                    if (typeId <= 0 || typeId >= 11 || typeId == 2 || typeId == 3 || typeId == 6 || typeId == 7 || typeId == 8) {
                        isPsr = false;
                        return isPsr;
                    }
                }
            }
        }

        return isPsr;
    }
    // fixme ---------------------------------------------------------------------------------------


}

