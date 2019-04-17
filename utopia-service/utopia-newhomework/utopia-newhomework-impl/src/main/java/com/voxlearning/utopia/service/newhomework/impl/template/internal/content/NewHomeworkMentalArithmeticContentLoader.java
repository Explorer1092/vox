package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheValueLoaderExecutor;
import com.voxlearning.athena.api.cuotizhenduan.entity.MentalArithmeticPak;
import com.voxlearning.athena.api.cuotizhenduan.entity.MentalArithmeticQuestion;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.BaseKnowledgePointRef;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePointRef;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.athena.WrongQuestionDiagnosisLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangbin
 * @since 2017/12/22
 */

@Named
public class NewHomeworkMentalArithmeticContentLoader extends NewHomeworkContentLoaderTemplate {

    private static final String QUESTION_COUNT_CACHE_KEY = "MENTAL_ARITHMETIC_KP_QUESTION_COUNT";
    private static final String NORMAL_KP_TYPE = "normal";
    private static final String PRE_KP_TYPE = "pre";


    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private WrongQuestionDiagnosisLoaderClient wrongQuestionDiagnosisLoaderClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;


    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL_ARITHMETIC;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        List<String> sectionIds = mapper.getSectionIds();

        if (Subject.MATH != teacher.getSubject() || CollectionUtils.isEmpty(sectionIds)) {
            return content;
        }
        String defaultSectionId = sectionIds.get(0);
        // 获取老师的使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);

        Map<String, NewKnowledgePoint> allKnowledgePointMap = new HashMap<>();
        // 获取所有口算知识点
        List<NewKnowledgePoint> allMentalKnowledgePointList = newKnowledgePointLoaderClient.loadNewKnowledgePointsByAncestorIds(Collections.singleton(NewHomeworkConstants.ROOT_MENTAL_KP_ID)).get(NewHomeworkConstants.ROOT_MENTAL_KP_ID);
        allMentalKnowledgePointList.forEach(kp -> allKnowledgePointMap.put(kp.getId(), kp));

        // 普通知识点
        Set<String> normalKpIdSet = new LinkedHashSet<>();
        // 前置知识点
        Set<String> preKpIdSet = new LinkedHashSet<>();
        // 知识点id，sectionId映射关系
        Map<String, String> kpIdSectionIdMap = new HashMap<>();

        // 获取所选课时下关联的知识点
        Map<String, NewKnowledgePointRef> kpRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(sectionIds);
        if (MapUtils.isNotEmpty(kpRefMap)) {
            kpRefMap.forEach((sectionId, kpRef) -> {
                Set<String> sectionNormalKpIdSet = new LinkedHashSet<>();
                Set<String> sectionReviewKpIdSet = new LinkedHashSet<>();
                if (CollectionUtils.isNotEmpty(kpRef.getKnowledgePoints())) {
                    for (BaseKnowledgePointRef baseKnowledgePointRef : kpRef.getKnowledgePoints()) {
                        String version = baseKnowledgePointRef.getVersion();
                        String kpId = baseKnowledgePointRef.getId();
                        if (StringUtils.equals(NewHomeworkConstants.ROOT_MENTAL_KP_ID, version)) {
                            kpIdSectionIdMap.put(kpId, sectionId);
                            if (SafeConverter.toBoolean(baseKnowledgePointRef.getKeyPoint())) {
                                sectionNormalKpIdSet.add(kpId);
                            } else {
                                sectionReviewKpIdSet.add(kpId);
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(sectionNormalKpIdSet)) {
                    normalKpIdSet.addAll(sectionNormalKpIdSet);
                }
                if (CollectionUtils.isNotEmpty(sectionReviewKpIdSet)) {
                    preKpIdSet.addAll(sectionReviewKpIdSet);
                }
            });
        }

        // 从前置知识点中过滤掉同时存在的普通知识点
        Set<String> commonKpIdSet = new LinkedHashSet<>(preKpIdSet);
        commonKpIdSet.retainAll(normalKpIdSet);
        preKpIdSet.removeAll(commonKpIdSet);

        Set<String> allKpIds = new LinkedHashSet<>();
        allKpIds.addAll(normalKpIdSet);
        allKpIds.addAll(preKpIdSet);
        Map<String, Integer> questionCountMap = loadQuestionCountWithCache(allKpIds);
        Map<String, NewKnowledgePoint> knowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(allKpIds);
        allKnowledgePointMap.putAll(knowledgePointMap);

        // 知识点列表
        List<Map<String, Object>> kpList = processKpList(
                bookId,
                unitId,
                defaultSectionId,
                normalKpIdSet,
                preKpIdSet,
                allKnowledgePointMap,
                questionCountMap,
                kpIdSectionIdMap,
                teacherAssignmentRecord);

        // 推荐题目列表
        List<Map<String, Object>> questionsList = processRecommendationQuestionsByBigData(bookId, unitId, sectionIds, preKpIdSet);
        if (mapper.isWaterfall()) {
            return questionsList;
        }
        // 知识树列表
        List<Map<String, Object>> kpTrees = processKpTree(bookId);

        // 自定义口算题、推荐题目标签是否互换
        boolean change = false;
        if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "TeacherMentalArithmetic", "ChangeTab")) {
            change = true;
        }
        content.add(MapUtils.m(
                "change", change,
                "kpList", kpList,
                "recommendationQuestions", questionsList,
                "kpTrees", kpTrees));

        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> kpList = loadContent(mapper);
        if (CollectionUtils.isNotEmpty(kpList)) {
            int questionCount = 0;
            for (Map<String, Object> questionsMapper : kpList) {
                List<Map<String, Object>> questions = (List<Map<String, Object>>) questionsMapper.get("questions");
                if (CollectionUtils.isNotEmpty(questions)) {
                    questionCount += questions.size();
                }
            }
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "questionCount", questionCount,
                    "kpList", kpList
            );
        }
        return Collections.emptyMap();
    }

    // 生成推荐题目
    private List<Map<String, Object>> processRecommendationQuestionsByBigData(String bookId, String unitId,
                                                                     List<String> sectionIds,
                                                                     Set<String> normalKpIdSet) {
        List<Map<String, Object>> recommendationQuestions = new ArrayList<>();
        MentalArithmeticPak mentalArithmeticPak = null;
        try {
            mentalArithmeticPak = wrongQuestionDiagnosisLoaderClient.getCuotizhenduanLoader().loadMentalArithmeticPaks(sectionIds);
        }catch (Exception e) {
            logger.error("NewHomeworkMentalArithmeticContentLoader call athena error:", e);
        }
        if(mentalArithmeticPak != null){
            List<String> questiondDocIds =  new ArrayList<>();
            List<String> courseIds = new ArrayList<>();
            for(MentalArithmeticQuestion maq : mentalArithmeticPak.getQuestions()){
                questiondDocIds.addAll(maq.getDocIds());
                questiondDocIds.addAll(maq.getPostTestDocIds());
                courseIds.add(maq.getCourseId());
            }
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadLatestQuestionByDocIds(questiondDocIds);
            //课程信息
            Map<String, IntelDiagnosisCourse> allIntelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
            for (MentalArithmeticQuestion maq : mentalArithmeticPak.getQuestions()) {
                Map<String, Object> kpMap = new LinkedHashMap<>();
                String kpId = maq.getKpId();
                IntelDiagnosisCourse course = allIntelDiagnosisCourseMap.get(maq.getCourseId());
                if(course != null){
                    kpMap.put("courseId", course.getId());
                    kpMap.put("courseName", course.getName());
                    List<String> postQuestions = new ArrayList<>();
                    for(String ptId : maq.getPostTestDocIds()){
                        NewQuestion newQuestion = questionMap.get(ptId);
                        if(newQuestion != null){
                            postQuestions.add(newQuestion.getId());
                        }
                    }
                    kpMap.put("postQuestions", postQuestions);
                }
                kpMap.put("kpId", kpId);
                kpMap.put("kpName", maq.getKpName());
                kpMap.put("kpType", normalKpIdSet.contains(maq.getKpId()) ? NORMAL_KP_TYPE : PRE_KP_TYPE);
                kpMap.put("contentTypeId", 0);
                List<Map<String, Object>> questions = new ArrayList<>();
                for (String qdocId : maq.getDocIds()) {
                    NewQuestion newQuestion = questionMap.get(qdocId);
                    if (newQuestion != null) {
                        questions.add(MapUtils.m(
                                "questionId", newQuestion.getId(),
                                "seconds", newQuestion.getSeconds(),
                                "knowledgePoint", kpId
                        ));
                    }
                }
                kpMap.put("questionCount", questions.size());
                kpMap.put("questions", questions);
                kpMap.put("book", MapUtils.m(
                        "bookId", bookId,
                        "unitId", unitId,
                        "sectionId", maq.getBookCatalogId()
                ));
                recommendationQuestions.add(kpMap);
            }
        }
        return recommendationQuestions;
    }

    // 生成知识点树
    private List<Map<String, Object>> processKpTree(String bookId) {
        Comparator<NewBookCatalog> comparator = Comparator.comparingInt(e1 -> SafeConverter.toInt(e1.getRank()));
        // 获取bookId下的所有units
        Map<String, List<NewBookCatalog>> bookCatalogsMap = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT);
        Map<String, String> unitIdNameMap = new HashMap<>();
        List<String> unitIds = new ArrayList<>();
        if (MapUtils.isNotEmpty(bookCatalogsMap)) {
            List<NewBookCatalog> newBookCatalogs = bookCatalogsMap.get(bookId);
            if (CollectionUtils.isNotEmpty(newBookCatalogs)) {
                newBookCatalogs = newBookCatalogs.stream()
                        .filter(Objects::nonNull)
                        .sorted(comparator)
                        .collect(Collectors.toList());
                for (NewBookCatalog newBookCatalog : newBookCatalogs) {
                    unitIds.add(newBookCatalog.getId());
                    unitIdNameMap.put(newBookCatalog.getId(), newBookCatalog.getName());
                }
            }
        }

        Map<String, List<NewBookCatalog>> unitSectionsMap = newContentLoaderClient.loadChildren(unitIds, BookCatalogType.SECTION);
        unitSectionsMap = MapUtils.resort(unitSectionsMap, unitIds);
        Map<String, String> sectionIdUnitIdMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(unitSectionsMap)) {
            unitSectionsMap.forEach((unitId, sections) -> {
                if (CollectionUtils.isNotEmpty(sections)) {
                    sections.stream()
                            .filter(Objects::nonNull)
                            .sorted(comparator)
                            .forEach(section -> sectionIdUnitIdMap.put(section.getId(), unitId));
                }
            });
        }

        // 教材下所有单元的知识点树
        List<Map<String, Object>> kpTrees = new ArrayList<>();
        Map<String, String> kpIdSectionIdMap = new LinkedHashMap<>();
        Map<String, Set<String>> unitIdKpIdsMap = new LinkedHashMap<>();

        // 获取unitIds下的所有知识点
        Map<String, NewKnowledgePointRef> knowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(sectionIdUnitIdMap.keySet());
        knowledgePointRefMap = MapUtils.resort(knowledgePointRefMap, sectionIdUnitIdMap.keySet());
        knowledgePointRefMap.forEach((sectionId, kpRef) -> {
            if (kpRef != null && CollectionUtils.isNotEmpty(kpRef.getKnowledgePoints())) {
                kpRef.getKnowledgePoints().forEach(baseKnowledgePointRef -> {
                    String version = baseKnowledgePointRef.getVersion();
                    String kpId = baseKnowledgePointRef.getId();
                    if (StringUtils.equals(version, NewHomeworkConstants.ROOT_MENTAL_KP_ID) && SafeConverter.toBoolean(baseKnowledgePointRef.getKeyPoint())) {
                        kpIdSectionIdMap.put(kpId, sectionId);
                        String unitId = sectionIdUnitIdMap.get(sectionId);
                        unitIdKpIdsMap.computeIfAbsent(unitId, k -> new LinkedHashSet<>()).add(kpId);
                    }
                });
            }
        });

        Map<String, NewKnowledgePoint> allKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(kpIdSectionIdMap.keySet());
        Map<String, String> allKpIdNameMap = new HashMap<>();
        if (MapUtils.isNotEmpty(allKnowledgePointMap)) {
            allKnowledgePointMap.values().forEach(e -> allKpIdNameMap.put(e.getId(), e.getName()));
        }

        // 生成知识点树
        if (CollectionUtils.isNotEmpty(unitIds)) {
            Map<String, Integer> questionCountMap = loadQuestionCountWithCache(kpIdSectionIdMap.keySet());
            for (String id : unitIds) {
                Map<String, Object> unitsMap = new LinkedHashMap<>();
                unitsMap.put("unitId", id);
                unitsMap.put("unitName", unitIdNameMap.get(id));
                List<Map<String, Object>> knowledgePoints = new ArrayList<>();
                Set<String> kpIdSet = unitIdKpIdsMap.get(id);
                if (CollectionUtils.isNotEmpty(kpIdSet)) {
                    for (String kpId : kpIdSet) {
                        if (StringUtils.isBlank(allKpIdNameMap.get(kpId)) || questionCountMap.getOrDefault(kpId, 0) == 0) {
                            continue;
                        }
                        knowledgePoints.add(MapUtils.m(
                                "kpId", kpId,
                                "kpName", allKpIdNameMap.get(kpId),
                                "kpType", NORMAL_KP_TYPE,
                                "contentTypeId", 0,
                                "questionCount", questionCountMap.getOrDefault(kpId, 0),
                                "book", MapUtils.m(
                                        "bookId", bookId,
                                        "unitId", id,
                                        "sectionId", kpIdSectionIdMap.get(kpId)
                                )
                        ));
                    }
                }
                unitsMap.put("knowledgePoints", knowledgePoints);
                if (CollectionUtils.isNotEmpty(knowledgePoints)) {
                    kpTrees.add(unitsMap);
                }
            }
        }
        return kpTrees;
    }

    private List<Map<String, Object>> processKpList(String bookId,
                                                    String unitId,
                                                    String defaultSectionId,
                                                    Set<String> normalKpIdSet,
                                                    Set<String> preKpIdSet,
                                                    Map<String, NewKnowledgePoint> allKnowledgePointMap,
                                                    Map<String, Integer> questionCountMap,
                                                    Map<String, String> kpIdSectionIdMap,
                                                    TeacherAssignmentRecord teacherAssignmentRecord) {
        List<Map<String, Object>> kpList = new ArrayList<>();
        processKp(normalKpIdSet, allKnowledgePointMap, questionCountMap, teacherAssignmentRecord, NORMAL_KP_TYPE, kpList, bookId, unitId, defaultSectionId, kpIdSectionIdMap);
        processKp(preKpIdSet, allKnowledgePointMap, questionCountMap, teacherAssignmentRecord, PRE_KP_TYPE, kpList, bookId, unitId, defaultSectionId, kpIdSectionIdMap);
        return kpList;
    }

    private void processKp(Set<String> kpIds, Map<String, NewKnowledgePoint> newKnowledgePointMap,
                           Map<String, Integer> questionCountMap, TeacherAssignmentRecord teacherAssignmentRecord,
                           String kpType, List<Map<String, Object>> kpList, String bookId, String unitId,
                           String defaultSectionId, Map<String, String> kpIdSectionIdMap) {
        for (String id : kpIds) {
            int questionCount = questionCountMap.getOrDefault(id, 0);
            if (questionCount >= 5) {
                if (newKnowledgePointMap.containsKey(id)) {
                    String kpName = newKnowledgePointMap.get(id).getName();
                    int teacherAssignTimes = teacherAssignmentRecord != null ? teacherAssignmentRecord.getMentalKpInfo().getOrDefault(id, 0) : 0;
                    kpList.add(MapUtils.m(
                            "kpId", id,
                            "kpName", kpName,
                            "kpType", kpType,
                            "contentTypeId", 0,
                            "questionCount", questionCount,
                            "teacherAssignTimes", teacherAssignTimes,
                            "book", buildBookMapper(id, bookId, unitId, defaultSectionId, kpIdSectionIdMap)
                    ));
                }
            }
        }
    }

    private Map<String, Object> buildBookMapper(String kpId, String bookId, String unitId, String defaultSectionId, Map<String, String> kpIdSectionIdMap) {
        Map<String, Object> bookMapper = new LinkedHashMap<>();
        bookMapper.put("bookId", bookId);
        bookMapper.put("unitId", unitId);
        bookMapper.put("sectionId", kpIdSectionIdMap.getOrDefault(kpId, defaultSectionId));
        return bookMapper;
    }

    private Map<String, Integer> loadQuestionCountWithCache(Collection<String> kpIds) {
        if (CollectionUtils.isEmpty(kpIds)) {
            return Collections.emptyMap();
        }
        CacheValueLoaderExecutor<String, Integer> loader = HomeworkCache.getHomeworkCacheFlushable().createCacheValueLoader();
        return loader.keyGenerator(id -> QUESTION_COUNT_CACHE_KEY + "-" + id)
                .keys(kpIds)
                .loads()
                .externalLoader(this::internalLoadQuestionCount)
                .loadsMissed()
                .expiration(86400)
                .write()
                .getAndResortResult();
    }

    private Map<String, Integer> internalLoadQuestionCount(Collection<String> kpIds) {
        if (CollectionUtils.isEmpty(kpIds)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String id : kpIds) {
            List<String> qIds = questionLoaderClient
                    .loadQuestionIdsByNewKnowledgePointId(Collections.singletonList(id), QuestionConstants.mentalIncludeContentTypeIds, Collections.emptySet(), 2000, true, true, true, true);
            result.put(id, qIds.size());
        }
        return result;
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> mentalQuestionList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(contentIdList);
            mentalQuestionList = contentIdList.stream()
                    .filter(questionId -> newQuestionMap.get(questionId) != null
                            && newQuestionMap.get(questionId).getContent() != null
                            && CollectionUtils.isNotEmpty(newQuestionMap.get(questionId).getContent().getSubContents()))
                    .map(questionId -> {
                        NewQuestion newQuestion = newQuestionMap.get(questionId);
                        return MapUtils.m(
                                "questionId", newQuestion.getId(),
//                                "questionContent", newQuestion.getContent().getSubContents().get(0).getContent(),
                                "seconds", newQuestion.getSeconds()
                        );
                    })
                    .collect(Collectors.toList());
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", mentalQuestionList
        );
    }
}
