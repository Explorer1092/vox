package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.athena.api.guihuaceping.GuiHuaCePingService;
import com.voxlearning.athena.api.guihuaceping.entity.HomeworkFinishInfo;
import com.voxlearning.athena.api.guihuaceping.entity.KnowledgePointGraspInfo;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.BaseKnowledgePointRef;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePointRef;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanKnowledgeBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanKnowledgeDetailBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanStudyHabitBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.TermPlanUnitDetailBO;
import com.voxlearning.utopia.service.newhomework.api.service.TermPlanService;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.FeatureLoaderClient;
import com.voxlearning.utopia.service.question.consumer.SolutionMethodRefLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TestMethodLoaderClient;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangbin
 * @since 2018/3/12
 */

@Named
@Service(interfaceClass = TermPlanService.class)
@ExposeService(interfaceClass = TermPlanService.class)
public class TermPlanServiceImpl implements TermPlanService {

    private static final List<String> CATEGORY = Arrays.asList("词汇", "句型语法");

    /**
     * 小英2015版
     */
    private static final String WORDS = "KP_10300028528577";    //词汇
    private static final String SENTENCE = "KP_10300028530745"; //句式
    private static final String GRAMMAR = "KP_10300035578351";  //语法

    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private SolutionMethodRefLoaderClient solutionMethodRefLoaderClient;
    @Inject
    private TestMethodLoaderClient testMethodLoaderClient;
    @Inject
    private FeatureLoaderClient featureLoaderClient;
    @Getter
    @ImportService(interfaceClass = GuiHuaCePingService.class)
    private GuiHuaCePingService guiHuaCePingService;

    @Override
    public TermPlanKnowledgeBO loadKnowledgePointGraspInfos(Long groupId, String unitId) {
        TermPlanKnowledgeBO termPlanKnowledgeBO = new TermPlanKnowledgeBO();

        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        Subject subject = null;
        if (group != null) {
            subject = group.getSubject();
        }
        Collection<String> bookCatalogIds = this.loadBookCatalogIds(subject, unitId);

        Set<String> allKpIds = new HashSet<>();

        // 英语（只有词汇和句型语法）
        Set<String> wordsKpIdSet = new HashSet<>();
        Set<String> sentenceKpIdSet = new HashSet<>();
        Map<String, Set<String>> catalogIdKpIds;

        // 数学
        Map<String, Set<String>> sectionIdKpIds = new LinkedHashMap<>();
        Map<String, String> mathKpIdKpNameMap = new LinkedHashMap<>();

        if (Subject.ENGLISH.equals(subject)) {
            Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(bookCatalogIds);
            catalogIdKpIds = getCatalogIdKpIds(newKnowledgePointRefMap);
            if (MapUtils.isNotEmpty(catalogIdKpIds)) {
                for (Map.Entry<String, Set<String>> entry : catalogIdKpIds.entrySet()) {
                    allKpIds.addAll(entry.getValue());
                }
            }

            Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(allKpIds);
            if (MapUtils.isNotEmpty(newKnowledgePointMap)) {
                for (Map.Entry<String, NewKnowledgePoint> entry : newKnowledgePointMap.entrySet()) {
                    NewKnowledgePoint newKnowledgePoint = entry.getValue();
                    if (newKnowledgePoint != null) {
                        List<String> ancestorIds = newKnowledgePoint.getAncestorIds();
                        if (CollectionUtils.isNotEmpty(ancestorIds)) {
                            if (ancestorIds.contains(WORDS) && !ancestorIds.contains(SENTENCE)) {
                                wordsKpIdSet.add(newKnowledgePoint.getId());
                            } else if (ancestorIds.contains(GRAMMAR) || ancestorIds.contains(SENTENCE)) {
                                sentenceKpIdSet.add(newKnowledgePoint.getId());
                            }
                        }
                    }
                }
            }
        } else if (Subject.MATH.equals(subject)) {
            this.loadMathKnowledgePoints(bookCatalogIds, sectionIdKpIds, mathKpIdKpNameMap);
            if (MapUtils.isNotEmpty(sectionIdKpIds)) {
                for (Map.Entry<String, Set<String>> entry : sectionIdKpIds.entrySet()) {
                    if (CollectionUtils.isNotEmpty(entry.getValue())) {
                        allKpIds.addAll(entry.getValue());
                    }
                }
            }
        }

        // 统一调用大数据接口
        List<KnowledgePointGraspInfo> knowledgePointGraspInfos = guiHuaCePingService.loadKnowledgePointGraspInfos(groupId, allKpIds, unitId);

        Map<String, Double> kpRateMap = new HashMap<>();
        int totalKnowledgePointNum = 0;
        int doneKnowledgePointNum = 0;
        Integer clazzRightRate = 0;
        Integer cityRightRate = 0;
        Integer cityTopTenRightRate = 0;
        double sumGroupRightRate = 0;
        if (Subject.ENGLISH.equals(subject)) {
            totalKnowledgePointNum = wordsKpIdSet.size() + sentenceKpIdSet.size();
        } else if (Subject.MATH.equals(subject)) {
            totalKnowledgePointNum = allKpIds.size();
        }
        termPlanKnowledgeBO.setTotalKnowledgePointNum(totalKnowledgePointNum);
        if (CollectionUtils.isNotEmpty(knowledgePointGraspInfos)) {
            cityRightRate = (int) (SafeConverter.toFloat(knowledgePointGraspInfos.iterator().next().getCityRightRate()) * 100);
            cityTopTenRightRate = (int) (SafeConverter.toFloat(knowledgePointGraspInfos.iterator().next().getCityTopTenPercentRightRate()) * 100);
            for (KnowledgePointGraspInfo kpInfo : knowledgePointGraspInfos) {
                //大数据现在返回所有知识点的信息
                if (kpInfo.getGroupRightRate() != null) {
                    String doneKpId = kpInfo.getKpId();
                    if (Subject.ENGLISH.equals(subject)) {
                        if (wordsKpIdSet.contains(doneKpId) || sentenceKpIdSet.contains(doneKpId)) {
                            sumGroupRightRate += SafeConverter.toFloat(kpInfo.getGroupRightRate());
                            doneKnowledgePointNum++;
                        }
                    } else if (Subject.MATH.equals(subject)) {
                        if (allKpIds.contains(doneKpId)) {
                            sumGroupRightRate += SafeConverter.toFloat(kpInfo.getGroupRightRate());
                            doneKnowledgePointNum++;
                        }
                    }
                    kpRateMap.put(doneKpId, kpInfo.getGroupRightRate());
                }
            }
        }
        termPlanKnowledgeBO.setDoneKnowledgePointNum(doneKnowledgePointNum);
        if (doneKnowledgePointNum > 0) {
            String clazzRightRateStr = String.format("%.2f", sumGroupRightRate / doneKnowledgePointNum);
            clazzRightRate = (int) (SafeConverter.toFloat(clazzRightRateStr) * 100);
            termPlanKnowledgeBO.setClazzRightRate(clazzRightRate);
        }
        termPlanKnowledgeBO.setCityRightRate(cityRightRate);
        termPlanKnowledgeBO.setCityTopTenRightRate(cityTopTenRightRate);

        List<Map<String, Object>> unitDetails = new ArrayList<>();
        if (Subject.ENGLISH.equals(subject)) {
            if (MapUtils.isNotEmpty(getContent(wordsKpIdSet, 0, kpRateMap))) {
                unitDetails.add(getContent(wordsKpIdSet, 0, kpRateMap));
            }
            if (MapUtils.isNotEmpty(getContent(sentenceKpIdSet, 1, kpRateMap))) {
                unitDetails.add(getContent(sentenceKpIdSet, 1, kpRateMap));
            }
        } else if (Subject.MATH.equals(subject)) {
            Map<String, String> sectionIdNameMap = new HashMap<>();
            Map<String, NewBookCatalog> sectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(bookCatalogIds);
            if (MapUtils.isNotEmpty(sectionMap)) {
                for (Map.Entry<String, NewBookCatalog> entry : sectionMap.entrySet()) {
                    sectionIdNameMap.put(entry.getKey(), entry.getValue().getName());
                }
            }
            if (MapUtils.isNotEmpty(sectionIdNameMap)) {
                for (Map.Entry<String, String> entry : sectionIdNameMap.entrySet()) {
                    Map<String, Object> content = new LinkedHashMap<>();
                    content.put("category", entry.getValue());
                    int totalKpNum = 0;
                    int doneKpNum = 0;
                    double sumRightRate = 0D;
                    Integer rightRate = 0;
                    Set<String> kpIds = sectionIdKpIds.get(entry.getKey());
                    if (CollectionUtils.isNotEmpty(kpIds)) {
                        totalKpNum = kpIds.size();
                        for (String kpId : kpIds) {
                            if (kpRateMap.keySet().contains(kpId)) {
                                doneKpNum++;
                                sumRightRate += SafeConverter.toFloat(kpRateMap.get(kpId));
                            }
                        }
                    }
                    content.put("totalKnowledgePointNum", totalKpNum);
                    content.put("doneKnowledgePointNum", doneKpNum);
                    if (doneKpNum > 0) {
                        String rightRateStr = String.format("%.2f", sumRightRate / doneKpNum);
                        rightRate = (int) (SafeConverter.toFloat(rightRateStr) * 100);
                    }
                    content.put("rightRate", rightRate);
                    if (totalKpNum > 0) {
                        unitDetails.add(content);
                    }
                }
            }
        }
        termPlanKnowledgeBO.setUnitDetails(unitDetails);
        return termPlanKnowledgeBO;
    }

    @Override
    public List<TermPlanKnowledgeDetailBO> loadKnowledgeDetail(Long groupId, String unitId) {
        List<TermPlanKnowledgeDetailBO> termPlanKnowledgeDetailBOList = new ArrayList<>();
        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        Subject subject = null;
        if (group != null) {
            subject = group.getSubject();
        }
        Collection<String> bookCatalogIds = this.loadBookCatalogIds(subject, unitId);

        Set<String> allKpIds = new HashSet<>();

        // 英语
        Set<String> wordsKpIdSet = new HashSet<>();
        Set<String> sentenceKpIdSet = new HashSet<>();
        Map<String, String> englishKpIdKpNameMap = new HashMap<>();

        // 数学
        Map<String, Set<String>> sectionIdKpIds = new LinkedHashMap<>();
        Map<String, String> mathKpIdKpNameMap = new LinkedHashMap<>();

        if (Subject.ENGLISH.equals(subject)) {
            Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(bookCatalogIds);
            Map<String, Set<String>> catalogIdKpIds = getCatalogIdKpIds(newKnowledgePointRefMap);
            if (MapUtils.isNotEmpty(catalogIdKpIds)) {
                for (Map.Entry<String, Set<String>> entry : catalogIdKpIds.entrySet()) {
                    allKpIds.addAll(entry.getValue());
                }
            }

            Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(allKpIds);
            if (MapUtils.isNotEmpty(newKnowledgePointMap)) {
                for (Map.Entry<String, NewKnowledgePoint> entry : newKnowledgePointMap.entrySet()) {
                    NewKnowledgePoint newKnowledgePoint = entry.getValue();
                    if (newKnowledgePoint != null) {
                        englishKpIdKpNameMap.put(newKnowledgePoint.getId(), newKnowledgePoint.getName());
                        List<String> ancestorIds = newKnowledgePoint.getAncestorIds();
                        if (CollectionUtils.isNotEmpty(ancestorIds)) {
                            if (ancestorIds.contains(WORDS) && !ancestorIds.contains(SENTENCE)) {
                                wordsKpIdSet.add(newKnowledgePoint.getId());
                            } else if (ancestorIds.contains(GRAMMAR) || ancestorIds.contains(SENTENCE)) {
                                sentenceKpIdSet.add(newKnowledgePoint.getId());
                            }
                        }
                    }
                }
            }
        } else if (Subject.MATH.equals(subject)) {
            this.loadMathKnowledgePoints(bookCatalogIds, sectionIdKpIds, mathKpIdKpNameMap);
            if (MapUtils.isNotEmpty(sectionIdKpIds)) {
                for (Map.Entry<String, Set<String>> entry : sectionIdKpIds.entrySet()) {
                    if (CollectionUtils.isNotEmpty(entry.getValue())) {
                        allKpIds.addAll(entry.getValue());
                    }
                }
            }
        }


        List<KnowledgePointGraspInfo> knowledgePointGraspInfos = guiHuaCePingService.loadKnowledgePointGraspInfos(groupId, allKpIds, unitId);
        Map<String, Double> kpRateMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(knowledgePointGraspInfos)) {
            for (KnowledgePointGraspInfo kpInfo : knowledgePointGraspInfos) {
                kpRateMap.put(kpInfo.getKpId(), kpInfo.getGroupRightRate());
            }
        }

        if (Subject.ENGLISH.equals(subject)) {
            termPlanKnowledgeDetailBOList.add(getKnowledgeDetail(wordsKpIdSet, 0, kpRateMap, englishKpIdKpNameMap));
            termPlanKnowledgeDetailBOList.add(getKnowledgeDetail(sentenceKpIdSet, 1, kpRateMap, englishKpIdKpNameMap));
        } else if (Subject.MATH.equals(subject)) {
            Map<String, String> sectionIdNameMap = new HashMap<>();
            Map<String, NewBookCatalog> sectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(bookCatalogIds);
            if (MapUtils.isNotEmpty(sectionMap)) {
                for (Map.Entry<String, NewBookCatalog> entry : sectionMap.entrySet()) {
                    sectionIdNameMap.put(entry.getKey(), entry.getValue().getName());
                }
            }
            if (MapUtils.isNotEmpty(sectionIdNameMap)) {
                for (Map.Entry<String, String> entry : sectionIdNameMap.entrySet()) {
                    TermPlanKnowledgeDetailBO termPlanKnowledgeDetailBO = new TermPlanKnowledgeDetailBO();
                    termPlanKnowledgeDetailBO.setCategory(entry.getValue());
                    List<TermPlanKnowledgeDetailBO.KnowledgeRightRate> knowledgeRightRates = new ArrayList<>();
                    Set<String> kpIds = sectionIdKpIds.get(entry.getKey());
                    if (CollectionUtils.isNotEmpty(kpIds)) {
                        for (String kpId : kpIds) {
                            TermPlanKnowledgeDetailBO.KnowledgeRightRate knowledgeRightRate = new TermPlanKnowledgeDetailBO.KnowledgeRightRate();
                            knowledgeRightRate.setKnowledgePointId(kpId);
                            knowledgeRightRate.setKnowledgePointName(mathKpIdKpNameMap.get(kpId));
                            if (kpRateMap.get(kpId) != null) {
                                String rightRateStr = String.format("%.2f", kpRateMap.get(kpId));
                                knowledgeRightRate.setKnowledgeRightRate((int) (SafeConverter.toFloat(rightRateStr) * 100));
                            } else {
                                knowledgeRightRate.setKnowledgeRightRate(-1);
                            }
                            knowledgeRightRates.add(knowledgeRightRate);
                        }
                    }
                    termPlanKnowledgeDetailBO.setKnowledgeRightRates(knowledgeRightRates);
                    termPlanKnowledgeDetailBOList.add(termPlanKnowledgeDetailBO);
                }
            }
        }
        return termPlanKnowledgeDetailBOList;
    }

    @Override
    public TermPlanStudyHabitBO loadHomeworkFinishInfo(Long groupId, String unitId) {
        HomeworkFinishInfo finishInfo = guiHuaCePingService.loadHomeworkFinishInfo(groupId, unitId);

        TermPlanStudyHabitBO termPlanStudyHabitBO = new TermPlanStudyHabitBO();
        if (finishInfo != null) {
            termPlanStudyHabitBO.setUnitAssignNum(SafeConverter.toInt(finishInfo.getAssignCnt()));

            TermPlanStudyHabitBO.HomeworkFinishRate homeworkFinishRate = new TermPlanStudyHabitBO.HomeworkFinishRate();
            if (finishInfo.getHomeworkFinishRate() != null) {
                String finishRateStr = String.format("%.2f", SafeConverter.toFloat(finishInfo.getHomeworkFinishRate()));
                homeworkFinishRate.setFinishRate((int) (SafeConverter.toFloat(finishRateStr) * 100));
            }
            if (finishInfo.getCityFinishRate() != null) {
                String cityFinishRateStr = String.format("%.2f", SafeConverter.toFloat(finishInfo.getCityFinishRate()));
                homeworkFinishRate.setCityFinishRate((int) (SafeConverter.toFloat(cityFinishRateStr) * 100));
            }
            if (finishInfo.getCityTopTenPercentFinishRate() != null) {
                String cityTopTenFinishRateStr = String.format("%.2f", SafeConverter.toFloat(finishInfo.getCityTopTenPercentFinishRate()));
                homeworkFinishRate.setCityTopTenFinishRate((int) (SafeConverter.toFloat(cityTopTenFinishRateStr) * 100));
            }
            termPlanStudyHabitBO.setHomeworkFinishRate(homeworkFinishRate);

            TermPlanStudyHabitBO.CorrectFinishRate correctFinishRate = new TermPlanStudyHabitBO.CorrectFinishRate();
            if (finishInfo.getHomeworkCorrectRate() != null) {
                String correctRateStr = String.format("%.2f", SafeConverter.toFloat(finishInfo.getHomeworkCorrectRate()));
                correctFinishRate.setCorrectRate((int) (SafeConverter.toFloat(correctRateStr) * 100));
            }
            if (finishInfo.getCityCorrectRate() != null) {
                String cityCorrectRateStr = String.format("%.2f", SafeConverter.toFloat(finishInfo.getCityCorrectRate()));
                correctFinishRate.setCityCorrectRate((int) (SafeConverter.toFloat(cityCorrectRateStr) * 100));
            }
            if (finishInfo.getCityTopTenPercentCorrectRate() != null) {
                String cityTopTenCorrectRateStr = String.format("%.2f", SafeConverter.toFloat(finishInfo.getCityTopTenPercentCorrectRate()));
                correctFinishRate.setCityTopTenCorrectRate((int) (SafeConverter.toFloat(cityTopTenCorrectRateStr) * 100));
            }
            termPlanStudyHabitBO.setCorrectFinishRate(correctFinishRate);
        }
        return termPlanStudyHabitBO;
    }

    @Override
    public List<TermPlanUnitDetailBO> changeUnit(Map<Long, Long> clazzIdGroupIdMap, Map<String, String> unitIdNameMap, String defaultUnitId) {
        List<TermPlanUnitDetailBO> termPlanUnitDetailBOList = new ArrayList<>();
        Long groupId = clazzIdGroupIdMap.values().iterator().next();
        Set<String> unitIds = unitIdNameMap.keySet();

        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        Subject subject = null;
        if (group != null) {
            subject = group.getSubject();
        }

        Map<String, Set<String>> unitIdKpIds = new HashMap<>();
        Map<String, Set<String>> sectionIdKpIds = new LinkedHashMap<>();
        Map<String, String> mathKpIdKpNameMap = new LinkedHashMap<>();

        if (Subject.ENGLISH.equals(subject)) {
            Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(unitIds);
            Map<String, Set<String>> catalogIdKpIds = getCatalogIdKpIds(newKnowledgePointRefMap);

            Set<String> kpIds = new HashSet<>();
            if (MapUtils.isNotEmpty(catalogIdKpIds)) {
                for (Map.Entry<String, Set<String>> entry : catalogIdKpIds.entrySet()) {
                    if (CollectionUtils.isNotEmpty(entry.getValue())) {
                        kpIds.addAll(entry.getValue());
                    }
                }
            }
            Set<String> wordsKpIdSet = new HashSet<>();
            Set<String> sentenceKpIdSet = new HashSet<>();
            Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(kpIds);
            if (MapUtils.isNotEmpty(newKnowledgePointMap)) {
                for (Map.Entry<String, NewKnowledgePoint> entry : newKnowledgePointMap.entrySet()) {
                    NewKnowledgePoint newKnowledgePoint = entry.getValue();
                    if (newKnowledgePoint != null) {
                        List<String> ancestorIds = newKnowledgePoint.getAncestorIds();
                        if (CollectionUtils.isNotEmpty(ancestorIds)) {
                            if (ancestorIds.contains(WORDS) && !ancestorIds.contains(SENTENCE)) {
                                wordsKpIdSet.add(newKnowledgePoint.getId());
                            } else if (ancestorIds.contains(GRAMMAR) || ancestorIds.contains(SENTENCE)) {
                                sentenceKpIdSet.add(newKnowledgePoint.getId());
                            }
                        }
                    }
                }
            }
            if (MapUtils.isNotEmpty(catalogIdKpIds)) {
                for (Map.Entry<String, Set<String>> entry : catalogIdKpIds.entrySet()) {
                    Set<String> categoryKpIds = new HashSet<>();
                    if (CollectionUtils.isNotEmpty(entry.getValue())) {
                        for (String kpId : entry.getValue()) {
                            if (wordsKpIdSet.contains(kpId) || sentenceKpIdSet.contains(kpId)) {
                                categoryKpIds.add(kpId);
                            }
                        }
                    }
                    unitIdKpIds.put(entry.getKey(), categoryKpIds);
                }
            }
        } else if (Subject.MATH.equals(subject)) {
            Map<String, Collection<String>> unitIdSectionIds = loadSectionIdsByUnitIds(subject, unitIds);
            Set<String> catalogIds = unitIdSectionIds.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            this.loadMathKnowledgePoints(catalogIds, sectionIdKpIds, mathKpIdKpNameMap);

            for (Map.Entry<String, Collection<String>> entry : unitIdSectionIds.entrySet()) {
                String unitId = entry.getKey();
                Collection<String> sectionIds = entry.getValue();
                Set<String> kpIds = new HashSet<>();
                if (CollectionUtils.isNotEmpty(sectionIds)) {
                    for (String sectionId : sectionIds) {
                        if (CollectionUtils.isNotEmpty(sectionIdKpIds.get(sectionId))) {
                            kpIds.addAll(sectionIdKpIds.get(sectionId));
                        }
                    }
                }
                unitIdKpIds.put(unitId, kpIds);
            }
        }

        Set<String> allUnitDoneKpIds = new HashSet<>();
        if (MapUtils.isNotEmpty(unitIdKpIds)) {
            for (Map.Entry<String, Set<String>> entry : unitIdKpIds.entrySet()) {
                String unitId = entry.getKey();
                Set<String> kpIds = entry.getValue();
                List<KnowledgePointGraspInfo> graspInfoList = guiHuaCePingService.loadKnowledgePointGraspInfos(groupId, kpIds, unitId);
                if (CollectionUtils.isNotEmpty(graspInfoList)) {
                    for (KnowledgePointGraspInfo info : graspInfoList) {
                        if (info.getGroupRightRate() != null) {
                            allUnitDoneKpIds.add(info.getKpId());
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(unitIds)) {
            for (String unitId : unitIds) {
                TermPlanUnitDetailBO termPlanUnitDetailBO = new TermPlanUnitDetailBO();
                termPlanUnitDetailBO.setUnitId(unitId);
                termPlanUnitDetailBO.setUnitName(unitIdNameMap.get(unitId));
                termPlanUnitDetailBO.setSelected(unitId.equals(defaultUnitId));
                Set<String> kpIds = unitIdKpIds.get(unitId);
                int totalKpNum = 0;
                int doneKpNum = 0;
                if (CollectionUtils.isNotEmpty(kpIds)) {
                    totalKpNum = kpIds.size();
                    for (String kpId : kpIds) {
                        if (allUnitDoneKpIds.contains(kpId)) {
                            doneKpNum++;
                        }
                    }
                }
                termPlanUnitDetailBO.setDoneKnowledgePointNum(doneKpNum);
                termPlanUnitDetailBO.setTotalKnowledgePointNum(totalKpNum);
                termPlanUnitDetailBOList.add(termPlanUnitDetailBO);
            }
        }
        return termPlanUnitDetailBOList;
    }

    private Collection<String> loadBookCatalogIds(Subject subject, String unitId) {
        Collection<String> bookCatalogIds = new ArrayList<>();
        if (Subject.ENGLISH.equals(subject)) {
            bookCatalogIds = Collections.singletonList(unitId);
        } else if (Subject.MATH.equals(subject)) {
            List<String> lessonIds = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON)
                    .get(unitId)
                    .stream()
                    .sorted(new NewBookCatalog.RankComparator())
                    .map(NewBookCatalog::getId)
                    .collect(Collectors.toList());
            Map<String, List<NewBookCatalog>> lessonSectionMap = newContentLoaderClient.loadChildren(lessonIds, BookCatalogType.SECTION);
            Set<String> sortedSectionIds = new LinkedHashSet<>();
            for (String lessonId : lessonIds) {
                List<NewBookCatalog> sectionList = lessonSectionMap.get(lessonId);
                if (CollectionUtils.isNotEmpty(sectionList)) {
                    sectionList.sort(new NewBookCatalog.RankComparator());
                    for (NewBookCatalog section : sectionList) {
                        sortedSectionIds.add(section.getId());
                    }
                }
            }
            bookCatalogIds = sortedSectionIds;
        }
        return bookCatalogIds;
    }

    private Map<String, Collection<String>> loadSectionIdsByUnitIds(Subject subject, Collection<String> unitIds) {
        Map<String, List<NewBookCatalog>> lessonMap = newContentLoaderClient.loadChildren(unitIds, BookCatalogType.LESSON);
        Map<String, Collection<String>> unitIdSectionIds = new LinkedHashMap<>(unitIds.size());
        if (MapUtils.isNotEmpty(lessonMap)) {
            for (Map.Entry<String, List<NewBookCatalog>> entry : lessonMap.entrySet()) {
                String unitId = entry.getKey();
                unitIdSectionIds.put(unitId, loadBookCatalogIds(subject, unitId));
            }
        }
        return unitIdSectionIds;
    }

    private Map<String, Set<String>> getCatalogIdKpIds(Map<String, NewKnowledgePointRef> newKnowledgePointRefMap) {
        Map<String, Set<String>> catalogIdKpIds = new HashMap<>();
        if (MapUtils.isNotEmpty(newKnowledgePointRefMap)) {
            for (Map.Entry<String, NewKnowledgePointRef> entry : newKnowledgePointRefMap.entrySet()) {
                String sectionId = entry.getKey();
                NewKnowledgePointRef newKnowledgePointRef = entry.getValue();
                List<BaseKnowledgePointRef> knowledgePoints = new ArrayList<>();
                if (newKnowledgePointRef != null) {
                    knowledgePoints = newKnowledgePointRef.getKnowledgePoints();
                }
                Set<String> kpIds = new HashSet<>();
                if (CollectionUtils.isNotEmpty(knowledgePoints)) {
                    for (BaseKnowledgePointRef baseKnowledgePointRef : knowledgePoints) {
                        kpIds.add(baseKnowledgePointRef.getId());
                    }
                }
                catalogIdKpIds.put(sectionId, kpIds);
            }
        }
        return catalogIdKpIds;
    }

    // 数学学科的每个课时的知识点（包括知识点,知识点：知识点特征，考法，解法）
    private void loadMathKnowledgePoints(Collection<String> sectionIds, Map<String, Set<String>> sectionIdKpIds, Map<String, String> kpIdKpName) {
        Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(sectionIds);
        Map<String, List<TestMethodRef>> testMethodRefMap = testMethodLoaderClient.loadTestMethodRefByBookCatalogIds(sectionIds);
        Map<String, List<SolutionMethodRef>> solutionMethodRefMap = solutionMethodRefLoaderClient.loadSolutionMethodRefByBookCatalogIds(sectionIds);

        Map<String, Set<String>> sectionIdKpIdsMap = new HashMap<>();
        Map<String, Set<String>> sectionIdKpfIdsMap = new HashMap<>();
        Map<String, Set<String>> sectionIdTmIdsMap = new HashMap<>();
        Map<String, Set<String>> sectionIdSmIdsMap = new HashMap<>();

        Set<String> notContainKpfIds = new LinkedHashSet<>();

        if (MapUtils.isNotEmpty(newKnowledgePointRefMap)) {
            for (Map.Entry<String, NewKnowledgePointRef> entry : newKnowledgePointRefMap.entrySet()) {
                String sectionId = entry.getKey();
                Set<String> kpIds = new LinkedHashSet<>();
                Set<String> kpfIds = new LinkedHashSet<>();
                NewKnowledgePointRef newKnowledgePointRef = entry.getValue();
                List<BaseKnowledgePointRef> knowledgePoints = new ArrayList<>();
                if (newKnowledgePointRef != null) {
                    knowledgePoints = newKnowledgePointRef.getKnowledgePoints();
                }
                if (CollectionUtils.isNotEmpty(knowledgePoints)) {
                    for (BaseKnowledgePointRef baseKnowledgePointRef : knowledgePoints) {
                        String kpId = baseKnowledgePointRef.getId();
                        notContainKpfIds.add(kpId);
                        List<String> featureIds = baseKnowledgePointRef.getFeatureIds();
                        if (CollectionUtils.isNotEmpty(featureIds)) {
                            kpfIds.addAll(featureIds);
                            for (String featureId : featureIds) {
                                String id = kpId + ":" + featureId;
                                kpIds.add(id);
                            }
                        } else {
                            kpIds.add(kpId);
                        }
                    }
                }
                sectionIdKpIdsMap.put(sectionId, kpIds);
                sectionIdKpfIdsMap.put(sectionId, kpfIds);
            }
        }

        if (MapUtils.isNotEmpty(testMethodRefMap)) {
            for (Map.Entry<String, List<TestMethodRef>> entry : testMethodRefMap.entrySet()) {
                String sectionId = entry.getKey();
                Set<String> tmIds = new LinkedHashSet<>();
                List<TestMethodRef> testMethodRefList = entry.getValue();
                if (CollectionUtils.isNotEmpty(testMethodRefList)) {
                    for (TestMethodRef testMethodRef : testMethodRefList) {
                        List<EmbedTestMethodRef> testMethods = testMethodRef.getTestMethods();
                        if (CollectionUtils.isNotEmpty(testMethods)) {
                            for (EmbedTestMethodRef embedTestMethodRef : testMethods) {
                                if (StringUtils.isNotBlank(embedTestMethodRef.getId())) {
                                    tmIds.add(embedTestMethodRef.getId());
                                }
                            }
                        }
                    }
                }
                notContainKpfIds.addAll(tmIds);
                sectionIdTmIdsMap.put(sectionId, tmIds);
            }
        }

        if (MapUtils.isNotEmpty(solutionMethodRefMap)) {
            for (Map.Entry<String, List<SolutionMethodRef>> entry : solutionMethodRefMap.entrySet()) {
                String sectionId = entry.getKey();
                Set<String> smIds = new LinkedHashSet<>();
                List<SolutionMethodRef> solutionMethodRefList = entry.getValue();
                if (CollectionUtils.isNotEmpty(solutionMethodRefList)) {
                    for (SolutionMethodRef solutionMethodRef : solutionMethodRefList) {
                        List<EmbedSolutionMethodRef> solutionMethods = solutionMethodRef.getSolutionMethods();
                        if (CollectionUtils.isNotEmpty(solutionMethods)) {
                            for (EmbedSolutionMethodRef embedSolutionMethodRef : solutionMethods) {
                                if (StringUtils.isNotBlank(embedSolutionMethodRef.getId())) {
                                    smIds.add(embedSolutionMethodRef.getId());
                                }
                            }
                        }
                    }
                }
                notContainKpfIds.addAll(smIds);
                sectionIdSmIdsMap.put(sectionId, smIds);
            }
        }

        for (String sectionId : sectionIds) {
            Set<String> kpIds = new LinkedHashSet<>();
            if (CollectionUtils.isNotEmpty(sectionIdKpIdsMap.get(sectionId))) {
                kpIds.addAll(sectionIdKpIdsMap.get(sectionId));
            }
            if (CollectionUtils.isNotEmpty(sectionIdTmIdsMap.get(sectionId))) {
                kpIds.addAll(sectionIdTmIdsMap.get(sectionId));
            }
            if (CollectionUtils.isNotEmpty(sectionIdSmIdsMap.get(sectionId))) {
                kpIds.addAll(sectionIdSmIdsMap.get(sectionId));
            }
            sectionIdKpIds.put(sectionId, kpIds);
        }


        // 开始计算所有的知识点名称
        Set<String> allKpfIds = new LinkedHashSet<>();
        if (MapUtils.isNotEmpty(sectionIdKpfIdsMap)) {
            for (Map.Entry<String, Set<String>> entry : sectionIdKpfIdsMap.entrySet()) {
                allKpfIds.addAll(entry.getValue());
            }
        }
        Map<String, String> kpfIdKpName = new LinkedHashMap<>();
        // 知识点特征
        Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatures(allKpfIds);
        if (MapUtils.isNotEmpty(knowledgePointFeatureMap)) {
            for (Map.Entry<String, KnowledgePointFeature> entry : knowledgePointFeatureMap.entrySet()) {
                String kpfId = entry.getKey();
                KnowledgePointFeature knowledgePointFeature = entry.getValue();
                String featureName = "";
                if (StringUtils.isNotBlank(knowledgePointFeature.getName())) {
                    featureName = knowledgePointFeature.getName();
                }
                kpfIdKpName.put(kpfId, featureName);
            }
        }

        Map<String, String> idNameMap = testMethodLoaderClient.getNameById(notContainKpfIds);

        if (MapUtils.isNotEmpty(sectionIdKpIds)) {
            for (Map.Entry<String, Set<String>> entry : sectionIdKpIds.entrySet()) {
                Set<String> kpIds = entry.getValue();
                if (CollectionUtils.isNotEmpty(kpIds)) {
                    for (String kpId : kpIds) {
                        if (kpId.contains(":")) {
                            String[] str = kpId.split(":");
                            if (str.length == 2) {
                                String kpName = idNameMap.get(str[0]) + "(" + kpfIdKpName.get(str[1]) + ")";
                                kpIdKpName.put(kpId, kpName);
                            }
                        } else {
                            kpIdKpName.put(kpId, idNameMap.get(kpId));
                        }
                    }
                }
            }
        }
    }

    private Map<String, Object> getContent(Set<String> kpIdSet, int index, Map<String, Double> kpRateMap) {
        Map<String, Object> content = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(kpIdSet)) {
            content.put("category", CATEGORY.get(index));
            int totalKpNum = kpIdSet.size();
            int doneKpNum = 0;
            double sumRightRate = 0D;
            Integer rightRate = 0;
            for (String kpId : kpIdSet) {
                if (kpRateMap.keySet().contains(kpId)) {
                    doneKpNum++;
                    sumRightRate += SafeConverter.toFloat(kpRateMap.get(kpId));
                }
            }
            content.put("totalKnowledgePointNum", totalKpNum);
            content.put("doneKnowledgePointNum", doneKpNum);
            if (doneKpNum > 0) {
                String rightRateStr = String.format("%.2f", sumRightRate / doneKpNum);
                rightRate = (int) (SafeConverter.toFloat(rightRateStr) * 100);
            }
            content.put("rightRate", rightRate);
        }
        return content;
    }

    private TermPlanKnowledgeDetailBO getKnowledgeDetail(Set<String> kpIdSet, int index, Map<String, Double> kpRateMap, Map<String, String> kpIdKpNameMap) {
        TermPlanKnowledgeDetailBO termPlanKnowledgeDetailBO = new TermPlanKnowledgeDetailBO();
        if (CollectionUtils.isNotEmpty(kpIdSet)) {
            termPlanKnowledgeDetailBO.setCategory(CATEGORY.get(index));
            List<TermPlanKnowledgeDetailBO.KnowledgeRightRate> knowledgeRightRates = new ArrayList<>();
            for (String kpId : kpIdSet) {
                TermPlanKnowledgeDetailBO.KnowledgeRightRate knowledgeRightRate = new TermPlanKnowledgeDetailBO.KnowledgeRightRate();
                knowledgeRightRate.setKnowledgePointId(kpId);
                knowledgeRightRate.setKnowledgePointName(kpIdKpNameMap.get(kpId));
                if (kpRateMap.get(kpId) != null) {
                    String rightRateStr = String.format("%.2f", kpRateMap.get(kpId));
                    knowledgeRightRate.setKnowledgeRightRate((int) (SafeConverter.toFloat(rightRateStr) * 100));
                } else {
                    knowledgeRightRate.setKnowledgeRightRate(-1);
                }
                knowledgeRightRates.add(knowledgeRightRate);
            }
            termPlanKnowledgeDetailBO.setKnowledgeRightRates(knowledgeRightRates);
        }
        return termPlanKnowledgeDetailBO;
    }
}
