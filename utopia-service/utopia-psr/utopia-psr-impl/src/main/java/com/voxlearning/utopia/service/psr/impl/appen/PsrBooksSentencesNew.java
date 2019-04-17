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

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.psr.entity.PsrBookPersistenceNew;
import com.voxlearning.utopia.service.psr.entity.PsrLessonPersistenceNew;
import com.voxlearning.utopia.service.psr.entity.PsrUnitPersistenceNew;
import com.voxlearning.utopia.service.question.api.entity.EmbedSolutionMethodRef;
import com.voxlearning.utopia.service.question.api.entity.EmbedTestMethodRef;
import com.voxlearning.utopia.service.question.api.entity.SolutionMethodRef;
import com.voxlearning.utopia.service.question.api.entity.TestMethodRef;
import com.voxlearning.utopia.service.question.consumer.SolutionMethodRefLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TestMethodLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Named

/**
 * Created by Administrator on 2016/3/29.
 */
public class PsrBooksSentencesNew implements InitializingBean {

    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject private TestMethodLoaderClient testMethodLoaderClient;
    @Inject private SolutionMethodRefLoaderClient solutionMethodRefLoaderClient;

    protected Map<String/*bookId*/,PsrBookPersistenceNew> bookPersistenceMap = new ConcurrentHashMap<>();
    private Map<String/*bookId*/, NewBookProfile> seriesPreBooks = new ConcurrentHashMap<>();  // 同系列教材下的前一本Book
    private Map<String/*bookId*/, Map<String,String>> bookNewKpToOldKp = new ConcurrentHashMap<>();

    public NewBookProfile getPreBookWithSameSeriesByBookId(String bookId) {
        if (StringUtils.isBlank(bookId))
            return null;
        if (seriesPreBooks.containsKey(bookId))
            return seriesPreBooks.get(bookId);

        NewBookProfile newBookProfile = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile == null){
            log.error("PsrBookSentenceNew getPreBookWithSameSeriesByBookId find bookid err : " + bookId);
            return null;
        }

        Integer preClassLevel = newBookProfile.getClazzLevel();
        Integer preTermType = newBookProfile.getTermType();
        switch (newBookProfile.getTermType()) {
            case 2:
                preTermType = 1;
                break;
            case 1:
                preClassLevel = newBookProfile.getClazzLevel() - 1;
                preTermType = 2;
                break;
            default:
                log.error("PsrBookSentenceNew termType : " + newBookProfile.getTermType() + " err find bookid : " + bookId);
                return null;
        }

        if (preClassLevel > 0) {
            List<NewBookProfile> preNewBookProfileList = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesId(
                    Subject.fromSubjectId(newBookProfile.getSubjectId()), ClazzLevel.parse(preClassLevel), Term.of(preTermType), newBookProfile.getSeriesId()
            );
            if (CollectionUtils.isEmpty(preNewBookProfileList))
                return null;

            // 优先找出初版年份一样的
            for (NewBookProfile item : preNewBookProfileList) {
                if (!seriesPreBooks.containsKey(bookId))
                    seriesPreBooks.put(bookId, item);
                if (item.getPublisher().equals(newBookProfile.getPublisher()))
                    seriesPreBooks.put(bookId, item);
                if (item.getYear().equals(newBookProfile.getYear())) {
                    seriesPreBooks.put(bookId, item);
                    continue;
                }
            }
        } else {
            seriesPreBooks.put(bookId, newBookProfile); // 一年级上册 默认自己
        }

        return seriesPreBooks.containsKey(bookId) ? seriesPreBooks.get(bookId) : null;
    }

    // 返回unitId 或者 lessonId下的主知识点列表
    // bookCatalogId:此处是UnitId 或者 LessonId,必不能使用ModuleId
    private Map<String/*kpId*/, List<String/*kpFeature*/>> getKnowledgePointIdsByBookCatalogId(NewBookProfile newBookProfile, String bookCatalogId) {
        Map<String/*kpId*/, List<String/*kpFeature*/>> knowledgePointIds = new HashMap<>();
        if (newBookProfile == null || StringUtils.isBlank(bookCatalogId))
            return knowledgePointIds;

        List<NewKnowledgePointRef>  knowledgePointRefList =  newKnowledgePointLoaderClient.findByBookCatalogId(bookCatalogId);
        if (CollectionUtils.isEmpty(knowledgePointRefList))
            return knowledgePointIds;

        knowledgePointRefList.stream().forEach(p -> {
                    if (p.getKnowledgePoints() != null){
                        p.getKnowledgePoints().stream().filter(BaseKnowledgePointRef::getKeyPoint).forEach(m -> {
                            knowledgePointIds.put(m.getId(), m.getFeatureIds());
                        });
                    }}
        );

        return knowledgePointIds;
    }

    // 获取lesson的详细信息
    private Map<String/*lessonId*/, PsrLessonPersistenceNew> getLessonDetailByLessonIds(NewBookProfile newBookProfile, List<NewBookCatalog> lessonList) {
        Map<String/*lessonId*/, PsrLessonPersistenceNew> retMap = new LinkedHashMap<>();
        if (newBookProfile == null || CollectionUtils.isEmpty(lessonList))
            return retMap;

        for(NewBookCatalog lesson : lessonList) {
            PsrLessonPersistenceNew psrLessonPersistenceNew = new PsrLessonPersistenceNew();
            psrLessonPersistenceNew.setLessonId(lesson.getId());
            psrLessonPersistenceNew.setName(lesson.getName());
            psrLessonPersistenceNew.setSentences(getKpSentences(newBookProfile, lesson.getId()));
            retMap.put(lesson.getId(),psrLessonPersistenceNew);
        }

        return retMap;
    }

    // catalogId : unitId or lessonId
    private Map<String, NewKnowledgePoint> getKpSentences(NewBookProfile newBookProfile, String catalogId) {
        Map<String/*kpid or kpid:kpfeature*/,NewKnowledgePoint> kpSentences = new HashMap<>();
        Map<String, List<String>> kpFeatures = getKnowledgePointIdsByBookCatalogId(newBookProfile, catalogId);
        if (MapUtils.isEmpty(kpFeatures) && Subject.MATH.getId() != newBookProfile.getSubjectId())
            return kpSentences;

        Map<String/*kpid*/,NewKnowledgePoint> kpSentencesTmp = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(kpFeatures.keySet());
        if (Subject.MATH.getId()==newBookProfile.getSubjectId() && newBookProfile.getLatestVersion() == 1) {
            if (MapUtils.isNotEmpty(kpFeatures) && MapUtils.isNotEmpty(kpSentencesTmp)) {
                // 针对数学新教材,把知识点+特征 当做新的知识点
                for (String kpId : kpFeatures.keySet()) {
                    kpSentences.put(kpId, kpSentencesTmp.get(kpId));

                    List<String> features = kpFeatures.get(kpId);
                    if (CollectionUtils.isEmpty(features))
                        continue;
                    for (String feature : features) {
                        if (StringUtils.isBlank(feature))
                            continue;
                        if (kpSentencesTmp.containsKey(kpId))
                            kpSentences.put(kpId + ":" + feature, kpSentencesTmp.get(kpId));
                    }
                }
            }
            // add testMethod and solutionMethod
            Map<String, NewKnowledgePoint> testAndSolutionNKP = getMethodFromCatalogId(newBookProfile, catalogId);
            if (MapUtils.isNotEmpty(testAndSolutionNKP)) {
                kpSentences.putAll(testAndSolutionNKP);
            }
        } else {
            kpSentences = kpSentencesTmp;
        }

        return kpSentences;
    }

    // 目前只有小数下面有考法解法
    // 把catalogId下面的考法和解法取出来,伪装成NewKnowledgePoint返回出去,当做一个特殊的KP
    private Map<String/*tid,sid*/, NewKnowledgePoint> getMethodFromCatalogId(NewBookProfile newBookProfile, String catalogId) {
        Map<String/*tid,sid*/, NewKnowledgePoint> retMap = new HashMap<>();
        if (StringUtils.isBlank(catalogId) || newBookProfile == null || Subject.MATH.getId() != newBookProfile.getSubjectId())
            return retMap;

        // 考法
        List<TestMethodRef> testMethodRefs = testMethodLoaderClient.loadTestMethodRefByBookCatalogId(catalogId);
        if (CollectionUtils.isNotEmpty(testMethodRefs)) {
            testMethodRefs.stream().forEach(p -> {
                if (CollectionUtils.isNotEmpty(p.getTestMethods())) {
                    p.getTestMethods().stream().filter(EmbedTestMethodRef::getKeyTestMethod).forEach(t -> {
                        NewKnowledgePoint nkp = new NewKnowledgePoint();
                        nkp.setId(t.getId());
                        nkp.setSubjectId(p.getSubjectId());
                        retMap.put(t.getId(), nkp);
                    });
                }
            });
        }

        // 解法
        List<SolutionMethodRef> solutionMethodRefs = solutionMethodRefLoaderClient.loadSolutionMethodRefByBookCatalogId(catalogId);
        if (CollectionUtils.isNotEmpty(solutionMethodRefs)) {
            solutionMethodRefs.stream().forEach(p -> {
                if (CollectionUtils.isNotEmpty(p.getSolutionMethods())) {
                    p.getSolutionMethods().stream().filter(EmbedSolutionMethodRef::getKeySolutionMethod).forEach(s -> {
                        NewKnowledgePoint nkp = new NewKnowledgePoint();
                        nkp.setId(s.getId());
                        nkp.setSubjectId(p.getSubjectId());
                        retMap.put(s.getId(), nkp);
                    });
                }
            });
        }

        return retMap;
    }


    // 获取UnitId下的Lesson和Section列表
    private List<NewBookCatalog> getLessonAndSectionListByUnitId(NewBookProfile newBookProfile, String unitId) {
        List<NewBookCatalog> retList = new ArrayList<>();
        if (StringUtils.isBlank(unitId))
            return retList;

        List<NewBookCatalog> sectionList = newContentLoaderClient.loadChildren(Collections.singleton(unitId),BookCatalogType.SECTION).get(unitId);
        if (CollectionUtils.isNotEmpty(sectionList))
            retList.addAll(sectionList);

        // 新的数学教材只挂载 section下面的新知识点
        if (newBookProfile.getSubjectId() == Subject.MATH.getId() && newBookProfile.getLatestVersion() == 1)
            return retList;

        List<NewBookCatalog> lessonList = newContentLoaderClient.loadChildren(Collections.singleton(unitId),BookCatalogType.LESSON).get(unitId);
        if (CollectionUtils.isNotEmpty(lessonList))
            retList.addAll(lessonList);

        return retList;
    }

    // 此处是UnitId,必不能使用ModuleId
    private Map<String/*unitId*/, PsrUnitPersistenceNew> getPsrUnitPersistenceNewByUnits(NewBookProfile newBookProfile, List<NewBookCatalog> units) {
        Map<String/*unitId*/, PsrUnitPersistenceNew> retMap = new LinkedHashMap<>();
        if (newBookProfile == null || CollectionUtils.isEmpty(units))
            return retMap;

        for (NewBookCatalog unit : units) {
            PsrUnitPersistenceNew psrUnitPersistenceNew = new PsrUnitPersistenceNew();
            psrUnitPersistenceNew.setUnitId(unit.getId());
            psrUnitPersistenceNew.setName(unit.getName());
            psrUnitPersistenceNew.setRank(unit.getRank());
            psrUnitPersistenceNew.setLessonPersistenceMap(
                    getLessonDetailByLessonIds(newBookProfile, getLessonAndSectionListByUnitId(newBookProfile, unit.getId()))
            );

            if (newBookProfile.getSubjectId() != Subject.MATH.getId() || newBookProfile.getLatestVersion() == 0) {
                // 新数学教材不使用单元下面挂载的知识点,只使用section下挂载的知识点
                Map<String/*kpid or kpid:kpfeature*/, NewKnowledgePoint> unitKpSentences = getKpSentences(newBookProfile, unit.getId());
                if (MapUtils.isNotEmpty(unitKpSentences))
                    psrUnitPersistenceNew.getSentences().putAll(unitKpSentences);
            }

            if (MapUtils.isNotEmpty(psrUnitPersistenceNew.getLessonPersistenceMap()))
                psrUnitPersistenceNew.getLessonPersistenceMap().values().stream().filter(p -> {return MapUtils.isNotEmpty(p.getSentences());}).forEach(p ->
                                psrUnitPersistenceNew.getSentences().putAll(p.getSentences())
                );
            retMap.put(unit.getId(), psrUnitPersistenceNew);
        }

        return retMap;
    }

    private void initPsrBookPersistenceNewWithModules(NewBookProfile newBookProfile, List<NewBookCatalog> moduleList, PsrBookPersistenceNew psrBookPersistenceNew/*out*/) {
        if (newBookProfile == null || CollectionUtils.isEmpty(moduleList))
            return;
        if (psrBookPersistenceNew == null)
            psrBookPersistenceNew = new PsrBookPersistenceNew();

        // 此处是unitIds,当有Module(章)的时候,把unit的rank重新排序
        Map<String/*unitId*/, PsrUnitPersistenceNew> unitMap = new LinkedHashMap<>();
        // 此处是ModuleIds,当有Module(章)的时候,用module代替原来的unit,可以认为module是groupUnit
        Map<String/*moduleId*/, PsrUnitPersistenceNew> moduleMap = new LinkedHashMap<>();
        // 记录module对应的unit列表
        Map<String/*moduleId*/, List<String/*unitId*/>> moduleToUnitsMap = new LinkedHashMap<>();

        Integer rank = 1;
        for (NewBookCatalog module : moduleList) {
            List<NewBookCatalog> units = newContentLoaderClient.loadChildren(Collections.singleton(module.getId()), BookCatalogType.UNIT).get(module.getId());
            if (CollectionUtils.isEmpty(units))
                continue;

            Map<String/*unitId*/, PsrUnitPersistenceNew> tmpMap = getPsrUnitPersistenceNewByUnits(newBookProfile, units);
            if (MapUtils.isEmpty(tmpMap))
                continue;

            PsrUnitPersistenceNew unitPersistenceNew = new PsrUnitPersistenceNew();
            unitPersistenceNew.setName(module.getName());
            unitPersistenceNew.setRank(module.getRank());
            unitPersistenceNew.setUnitId(module.getId());
            unitPersistenceNew.setSentences(new LinkedHashMap<>());
            unitPersistenceNew.setLessonPersistenceMap(new LinkedHashMap<>());

            // 2. modulePersistence
            // sort by rank 1 -> 2 -> n
            Map<Integer, PsrUnitPersistenceNew> rankUnit = new HashMap<>();
            tmpMap.values().stream().forEach(p -> {
                        rankUnit.put(p.getRank(), p);
                        unitPersistenceNew.getLessonPersistenceMap().putAll(p.getLessonPersistenceMap());
                        unitPersistenceNew.getSentences().putAll(p.getSentences());
                    }
            );
            moduleMap.put(module.getId(), unitPersistenceNew);

            // 1. unitPersistence
            for (Map.Entry<Integer, PsrUnitPersistenceNew> entry : rankUnit.entrySet()) {
                entry.getValue().setRank(rank++);
                unitMap.put(entry.getValue().getUnitId(), entry.getValue());
            }

            // 3. module to units
            moduleToUnitsMap.put(module.getId(), units.stream().map(NewBookCatalog::getId).collect(Collectors.toList()));
        }

        psrBookPersistenceNew.setUnitPersistenceMap(unitMap);
        psrBookPersistenceNew.setModulePersistenceMap(moduleMap);
        psrBookPersistenceNew.setModuleToUnitsMap(moduleToUnitsMap);
    }

    /*
     * 教材结构:1. series -> book -> module -> unit -> lesson -> section -> sentence
     *        2. series -> book -> unit -> lesson -> section -> sentence
     * 优先获取结构1,其次是2
     */
    public PsrBookPersistenceNew getBookPersistenceByBookId(String bookId) {
        if (StringUtils.isBlank(bookId))
            return null;

        if (bookPersistenceMap.containsKey(bookId))
            return bookPersistenceMap.get(bookId);

        NewBookProfile newBookProfile = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile == null) {
            log.error("PsrBookSentenceNew getBookPersistenceByBookId find bookid err : " + bookId);
            return null;
        }

        PsrBookPersistenceNew psrBookPersistenceNew = new PsrBookPersistenceNew();
        psrBookPersistenceNew.setBookId(bookId);
        psrBookPersistenceNew.setName(newBookProfile.getName());
        psrBookPersistenceNew.setSeriesId(newBookProfile.getSeriesId());
        psrBookPersistenceNew.setStatus(newBookProfile.getStatus());
        psrBookPersistenceNew.setSubjectId(newBookProfile.getSubjectId());
        psrBookPersistenceNew.setClazzLevel(newBookProfile.getClazzLevel());
        psrBookPersistenceNew.setLatestVersion(newBookProfile.getLatestVersion());

        List<NewBookCatalog> moduleList = newContentLoaderClient.loadChildren(Collections.singleton(bookId),BookCatalogType.MODULE).get(bookId);
        if ( ! CollectionUtils.isEmpty(moduleList)) {
            // 优先处理module教材结构
            initPsrBookPersistenceNewWithModules(newBookProfile, moduleList, psrBookPersistenceNew);
        } else {
            // 如果没有module,则按普通教材结构
            Map<String/*unitId*/, PsrUnitPersistenceNew> unitPersistenceNewMap = getPsrUnitPersistenceNewByUnits(
                    newBookProfile, newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT).get(bookId));
            psrBookPersistenceNew.setUnitPersistenceMap(unitPersistenceNewMap);
        }

        if (bookPersistenceMap.size() < 1500)
            bookPersistenceMap.put(bookId, psrBookPersistenceNew);
        else
            log.error("getBookPersistenceByBookId buffer full: " + bookPersistenceMap.size());

        return psrBookPersistenceNew;
    }

    public Map<String, Integer> getModulesRanksByBookId(String bookId) {
        Map<String, Integer> retMap = new HashMap<>();
        PsrBookPersistenceNew psrBookPersistenceNew = getBookPersistenceByBookId(bookId);
        if (psrBookPersistenceNew == null)
            return retMap;

        return psrBookPersistenceNew.getModulesRanks();
    }

    public Map<Integer,String> getUnitsRanksByBookId(String bookId, String unitId) {
        Map<Integer,String> retMap = new HashMap<>();
        PsrBookPersistenceNew psrBookPersistenceNew = getBookPersistenceByBookId(bookId);
        if (psrBookPersistenceNew == null)
            return retMap;

        Map<String, PsrUnitPersistenceNew> psrUnitPersistenceMap = psrBookPersistenceNew.getPsrUnitPersistenceMap(unitId);
        if (MapUtils.isEmpty(psrUnitPersistenceMap))
            return retMap;

        psrUnitPersistenceMap.values().stream().forEach(p -> retMap.put(p.getRank(), p.getUnitId()));

        return retMap;
    }

    public Map<String, List<String>> getUnitsSentenceByBookId(String bookId, Subject subject, String unitId) {
        Map<String, List<String>> retMap = new LinkedHashMap<>(); //map<unitid, list<kp_id> >

        PsrBookPersistenceNew psrBookPersistenceNew = getBookPersistenceByBookId(bookId);
        if (psrBookPersistenceNew == null)
            return retMap;

        Map<String, PsrUnitPersistenceNew> psrUnitPersistenceMap = psrBookPersistenceNew.getPsrUnitPersistenceMap(unitId);
        if (psrUnitPersistenceMap == null)
            return retMap;

        for (Map.Entry<String, PsrUnitPersistenceNew> entry : psrUnitPersistenceMap.entrySet()) {
            PsrUnitPersistenceNew psrUnitPersistenceNew = entry.getValue();
            if (psrUnitPersistenceNew == null)
                continue;

            List<String> tmpList = new ArrayList<>();

            Map<String/*kpid*/,NewKnowledgePoint/**/> unitKpMap = psrUnitPersistenceNew .getSentences();
            if (unitKpMap == null)
                continue;
            for (Map.Entry<String,NewKnowledgePoint> entrap: unitKpMap.entrySet()){
                NewKnowledgePoint unitNewKnowledgePoint = entrap.getValue();
                if (unitNewKnowledgePoint == null)
                    continue;
                if (subject == null || subject.equals(Subject.ENGLISH)) {
                    if (!unitNewKnowledgePoint.getPointType().equals("WORDS") && !unitNewKnowledgePoint.getParentId().equals("TOPICS"))
                        continue;
                }
                String kpId = unitNewKnowledgePoint.getId();
                // 数学新教材 使用 kp:feature
                if (psrBookPersistenceNew.getLatestVersion() == 1 && psrBookPersistenceNew.getSubjectId() == Subject.MATH.getId())
                    kpId = entrap.getKey();
                if (!tmpList.contains(kpId))
                    tmpList.add(kpId);
            }

            Map<String,PsrLessonPersistenceNew> psrLessonPersistenceMap = psrUnitPersistenceNew.getLessonPersistenceMap();
            if (psrLessonPersistenceMap == null)
                continue;
            for (Map.Entry <String,PsrLessonPersistenceNew> entry1: psrLessonPersistenceMap.entrySet()){
                PsrLessonPersistenceNew psrLessonPersistenceNew = entry1.getValue();
                if (psrLessonPersistenceNew == null)
                    continue;

                Map<String, NewKnowledgePoint> psrSentences = psrLessonPersistenceNew.getSentences();
                if (psrSentences == null)
                    continue;

                for (Map.Entry<String, NewKnowledgePoint> entry2 : psrSentences.entrySet()) {
                    NewKnowledgePoint newKnowledgePoint = entry2.getValue();
                    if (newKnowledgePoint == null)
                        continue;
                    if (subject == null || subject.equals(Subject.ENGLISH)) {
                        if (!newKnowledgePoint.getPointType().equals("WORDS") && !newKnowledgePoint.getPointType().equals("TOPICS"))
                            continue;
                    }
                    String kpId = newKnowledgePoint.getId();
                    // 数学新教材 使用 kp:feature
                    if (psrBookPersistenceNew.getLatestVersion() == 1 && psrBookPersistenceNew.getSubjectId() == Subject.MATH.getId())
                        kpId = entry2.getKey();
                    if (!tmpList.contains(kpId))
                        tmpList.add(kpId);
                }

            }
            retMap.put(entry.getKey(), tmpList);
        }
        return retMap;
    }

    public Map<String, List<String>> getUnitsSentenceNameByBookId(String bookId, String unitId) {
        Map<String, List<String>> retMap = new LinkedHashMap<>(); //map<unitid, list<kp_name> >

        PsrBookPersistenceNew psrBookPersistenceNew = getBookPersistenceByBookId(bookId);
        if (psrBookPersistenceNew == null)
            return retMap;

        Map<String, PsrUnitPersistenceNew> psrUnitPersistenceMap = psrBookPersistenceNew.getPsrUnitPersistenceMap(unitId);
        if (psrUnitPersistenceMap == null)
            return retMap;

        for (Map.Entry<String, PsrUnitPersistenceNew> entry : psrUnitPersistenceMap.entrySet()) {
            PsrUnitPersistenceNew psrUnitPersistenceNew = entry.getValue();
            if (psrUnitPersistenceNew == null)
                continue;

            List<String> tmpList = new ArrayList<>();

            Map<String/*kpid*/,NewKnowledgePoint/**/> unitKpMap = psrUnitPersistenceNew .getSentences();
            for (Map.Entry<String,NewKnowledgePoint> entryx: unitKpMap.entrySet()){
                NewKnowledgePoint unitNewKnowledgePoint = entryx.getValue();
                if (unitNewKnowledgePoint == null)
                    continue;
                if (!unitNewKnowledgePoint.getPointType().equals("WORDS") && !unitNewKnowledgePoint.getParentId().equals("TOPICS"))
                    continue;
                if (!tmpList.contains("word#"+unitNewKnowledgePoint.getName()))
                    tmpList.add("word#"+unitNewKnowledgePoint.getName());
            }

            Map<String,PsrLessonPersistenceNew> psrLessonPersistenceMap = psrUnitPersistenceNew.getLessonPersistenceMap();
            for (Map.Entry <String,PsrLessonPersistenceNew> entry1: psrLessonPersistenceMap.entrySet()){
                PsrLessonPersistenceNew psrLessonPersistenceNew = entry1.getValue();
                if (psrLessonPersistenceNew == null)
                    continue;

                Map<String, NewKnowledgePoint> psrSentences = psrLessonPersistenceNew.getSentences();
                if (psrSentences == null)
                    continue;

                for (Map.Entry<String, NewKnowledgePoint> entry2 : psrSentences.entrySet()) {
                    NewKnowledgePoint newKnowledgePoint = entry2.getValue();
                    if (newKnowledgePoint == null)
                        continue;
                    if (!newKnowledgePoint.getPointType().equals("WORDS") && !newKnowledgePoint.getPointType().equals("TOPICS"))
                        continue;
                    if (!tmpList.contains("word#"+newKnowledgePoint.getName()))
                        tmpList.add("word#"+newKnowledgePoint.getName()); // kp name
                }

            }
            retMap.put(entry.getKey(), tmpList);
        }
        return retMap;
    }

    public Map<String,String> getOldKnowledgePointIdByBookId(String bookId, Subject subject){
        if (StringUtils.isBlank(bookId))
            return Collections.emptyMap();

        if (bookNewKpToOldKp.containsKey(bookId))
            return bookNewKpToOldKp.get(bookId);

        Map<String, String> retMap = new LinkedHashMap<>(); //map<kp_id, old_kp_id> >

        PsrBookPersistenceNew psrBookPersistenceNew = getBookPersistenceByBookId(bookId);
        if (psrBookPersistenceNew == null)
            return retMap;

        Map<String, PsrUnitPersistenceNew> psrUnitPersistenceMap = psrBookPersistenceNew.getUnitPersistenceMap();
        if (psrUnitPersistenceMap == null)
            return retMap;

        for (Map.Entry<String, PsrUnitPersistenceNew> entry : psrUnitPersistenceMap.entrySet()) {
            PsrUnitPersistenceNew psrUnitPersistenceNew = entry.getValue();
            if (psrUnitPersistenceNew == null)
                continue;

            List<String> tmpList = new ArrayList<>();

            Map<String/*kpid*/,NewKnowledgePoint/**/> unitKpMap = psrUnitPersistenceNew .getSentences();
            if (unitKpMap == null)
                continue;
            for (Map.Entry<String,NewKnowledgePoint> entryx: unitKpMap.entrySet()){
                NewKnowledgePoint unitNewKnowledgePoint = entryx.getValue();
                if (unitNewKnowledgePoint == null)
                    continue;
                if (subject == null || subject.equals(Subject.ENGLISH)) {
                    if (!unitNewKnowledgePoint.getPointType().equals("WORDS") && !unitNewKnowledgePoint.getParentId().equals("TOPICS"))
                        continue;
                }
                if (!retMap.containsKey(unitNewKnowledgePoint.getId())){
                    List<EmbedOldKnowledgePoint> oldKnowledgePoint = unitNewKnowledgePoint.getOld();
                    if (oldKnowledgePoint.size() >= 1)
                        retMap.put(unitNewKnowledgePoint.getId(), oldKnowledgePoint.get(0).getId());
                }
            }

            Map<String,PsrLessonPersistenceNew> psrLessonPersistenceMap = psrUnitPersistenceNew.getLessonPersistenceMap();
            if (psrLessonPersistenceMap == null)
                continue;
            for (Map.Entry <String,PsrLessonPersistenceNew> entry1: psrLessonPersistenceMap.entrySet()){
                PsrLessonPersistenceNew psrLessonPersistenceNew = entry1.getValue();
                if (psrLessonPersistenceNew == null)
                    continue;

                Map<String, NewKnowledgePoint> psrSentences = psrLessonPersistenceNew.getSentences();
                if (psrSentences == null)
                    continue;

                for (Map.Entry<String, NewKnowledgePoint> entry2 : psrSentences.entrySet()) {
                    NewKnowledgePoint newKnowledgePoint = entry2.getValue();
                    if (newKnowledgePoint == null)
                        continue;
                    if (subject == null || subject.equals(Subject.ENGLISH)) {
                        if (!newKnowledgePoint.getPointType().equals("WORDS") && !newKnowledgePoint.getPointType().equals("TOPICS"))
                            continue;
                    }
                    if (!retMap.containsKey(newKnowledgePoint.getId())) {
                        List<EmbedOldKnowledgePoint> oldKnowledgePoint = newKnowledgePoint.getOld();
                        if (oldKnowledgePoint.size() >= 1)
                            retMap.put(newKnowledgePoint.getId(), oldKnowledgePoint.get(0).getId());
                    }
                }
            }
        }

        if (bookNewKpToOldKp.size() <= 1000 && MapUtils.isNotEmpty(retMap))
            bookNewKpToOldKp.put(bookId, retMap);

        return retMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrBooksSentencesNew-Loader") {
            @Override
            public void runSafe() {
                bookPersistenceMap.clear();
                seriesPreBooks.clear();
                bookNewKpToOldKp.clear();
                log.info("PsrBooksSentencesNew map clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60 * 60 * 1000, 60 * 60 * 1000);
    }
}
