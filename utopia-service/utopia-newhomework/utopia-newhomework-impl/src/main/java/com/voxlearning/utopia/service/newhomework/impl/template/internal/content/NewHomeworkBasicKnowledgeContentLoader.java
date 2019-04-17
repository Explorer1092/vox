package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.athena.api.recom.entity.paks.NewRecommendPackage;
import com.voxlearning.athena.api.recom.entity.paks.QuestionInfo;
import com.voxlearning.athena.api.recom.entity.paks.RecommendPointQuestionInfo;
import com.voxlearning.athena.api.recom.entity.wrapper.NewRecommendPackageWrapper;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.IntelligenceExamSceneType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.SceneLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/12/28
 */
@Named
public class NewHomeworkBasicKnowledgeContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject private AthenaHomeworkLoaderClient athenaHomeworkLoaderClient;
    @Inject private SceneLoaderClient sceneLoaderClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    private static final List<IntelligenceExamSceneType> BASIC_KNOWLEDGE_SUPPORTED_SCENE_TYPES = Arrays.asList(IntelligenceExamSceneType.CHINESE_DICTATION, IntelligenceExamSceneType.CHINESE_SPECIAL_WORDS, IntelligenceExamSceneType.CHINESE_SPECIAL_VOCABULARY,
            IntelligenceExamSceneType.CHINESE_WORD_BASE_NEW, IntelligenceExamSceneType.CHINESE_WORD_ENHANCE_NEW, IntelligenceExamSceneType.CHINESE_EXPAND_USE);
    private static final Map<IntelligenceExamSceneType, List<String>> SCENE_TYPE_KNOWLEDGE_POINTS_MAP = new HashMap<>();
    private static final Map<IntelligenceExamSceneType, List<String>> SCENE_TYPE_TEST_METHODS_MAP = new HashMap<>();

    static {
        SCENE_TYPE_KNOWLEDGE_POINTS_MAP.put(IntelligenceExamSceneType.CHINESE_SPECIAL_WORDS, Collections.singletonList("KP_10100047433893"));
        SCENE_TYPE_KNOWLEDGE_POINTS_MAP.put(IntelligenceExamSceneType.CHINESE_SPECIAL_VOCABULARY, Collections.singletonList("KP_10100074215853"));
        SCENE_TYPE_KNOWLEDGE_POINTS_MAP.put(IntelligenceExamSceneType.CHINESE_WORD_BASE_NEW, Arrays.asList("KP_10100047433893", "KP_10100074215853"));
        SCENE_TYPE_KNOWLEDGE_POINTS_MAP.put(IntelligenceExamSceneType.CHINESE_WORD_ENHANCE_NEW, Arrays.asList("KP_10100047433893", "KP_10100074215853"));
        SCENE_TYPE_KNOWLEDGE_POINTS_MAP.put(IntelligenceExamSceneType.CHINESE_EXPAND_USE, Arrays.asList("KP_10100015645476", "KP_10100015708364", "KP_10100015732740", "KP_10100015939266", "KP_10100106405073"));
        SCENE_TYPE_TEST_METHODS_MAP.put(IntelligenceExamSceneType.CHINESE_EXPAND_USE, Collections.singletonList("TM_10100000638091"));
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_KNOWLEDGE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        List<String> sectionIds = mapper.getSectionIds();
        Set<Long> groupIds = mapper.getGroupIds();

        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(sectionIds)) {
            return Collections.emptyList();
        }
        String defaultSectionId = sectionIds.get(0);
        // 配置的场景ids
        Set<Integer> allConfigSceneIds = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.INTELLIGENCE_EXAM) {
                    List<Integer> sceneIds = (List<Integer>) configContent.get("scene_ids");
                    if (CollectionUtils.isNotEmpty(sceneIds)) {
                        allConfigSceneIds.addAll(sceneIds);
                    }
                }
            }
        }

        // 生字专项、词语专项、拓展运用、听写 实验组
        // 字词基础、字词拔高、拓展运用、听写 对照组
        // 前面两个场景互斥，显示生字专项的不显示字词基础
        boolean matchExperimentGroup = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "BasicKnowledge", "ExperimentGroup");
        // 句子专项、语法修辞、听写 全量开放
        Map<Integer, IntelligenceExamSceneType> sceneTypeMap = new LinkedHashMap<>();
        allConfigSceneIds.stream()
                .map(IntelligenceExamSceneType::of)
                .filter(Objects::nonNull)
                .filter(BASIC_KNOWLEDGE_SUPPORTED_SCENE_TYPES::contains)
                .filter(sceneType -> showType(sceneType, matchExperimentGroup))
                .forEach(type -> sceneTypeMap.put(type.getId(), type));
        List<String> algoTypes = sceneTypeMap.values().stream().map(IntelligenceExamSceneType::name).collect(Collectors.toList());

        List<Map<String, Object>> content = new ArrayList<>();
        Map<Long, Collection<String>> groupBookCatalogIds = new LinkedHashMap<>();
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        Integer clazzLevel = bookProfile != null ? bookProfile.getClazzLevel() : 1;
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
                    if (sectionIds.contains(section.getId())) {
                        sortedSectionIds.add(section.getId());
                    }
                }
            }
        }
        for (Long groupId : groupIds) {
            groupBookCatalogIds.put(groupId, sortedSectionIds);
        }
        NewRecommendPackageWrapper recommendPackageWrapper = null;
        // 题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        // 试题类型白名单
        List<Integer> contentTypeList = QuestionConstants.examChineseIncludeContentTypeIds;
        // 老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        if (MapUtils.isNotEmpty(sceneTypeMap)) {
            try {
                recommendPackageWrapper = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                        .loadPrimaryChineseNewSpecifiedFeaturedPackages(bookId, clazzLevel, groupBookCatalogIds, teacher.getId(), SafeConverter.toLong(teacher.getCityCode()), algoTypes);
            } catch (Exception e) {
                logger.error("NewHomeworkBasicKnowledgeContentLoader call athena error:", e);
            }
        }
        if (recommendPackageWrapper != null) {
            Map<Integer, List<Map<String, Object>>> knowledgePointsMap = loadChineseKnowledgePoints(sceneTypeMap.keySet(), sortedSectionIds);
            processPackageContent(content, recommendPackageWrapper, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList, knowledgePointsMap, teacher.getSubject(), algoTypes);
        }
        List<String> moreQuestions = null;
        try {
            moreQuestions = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                    .loadPrimaryChineseRemainderRecommendedPackages(bookId, clazzLevel, new ArrayList(sortedSectionIds));
        } catch (Exception e) {
            logger.error("NewHomeworkBasicKnowledgeContentLoader call athena error:", e);
        }
        if (CollectionUtils.isNotEmpty(moreQuestions)) {
            processMoreQuestions(moreQuestions, contentTypeMap, teacherAssignmentRecord, content, defaultSectionId, bookId, unitId);
        }
        return content;
    }

    private boolean showType(IntelligenceExamSceneType sceneType, boolean matchExperimentGroup) {
        if (IntelligenceExamSceneType.CHINESE_WORD_BASE_NEW == sceneType || IntelligenceExamSceneType.CHINESE_WORD_ENHANCE_NEW == sceneType) {
            return !matchExperimentGroup;
        } else if (IntelligenceExamSceneType.CHINESE_SPECIAL_WORDS == sceneType || IntelligenceExamSceneType.CHINESE_SPECIAL_VOCABULARY == sceneType) {
            return matchExperimentGroup;
        } else {
            return IntelligenceExamSceneType.CHINESE_DICTATION == sceneType || IntelligenceExamSceneType.CHINESE_EXPAND_USE == sceneType;
        }
    }

    private void processMoreQuestions(List<String> moreQuestions, Map<Integer, NewContentType> contentTypeMap,
                                      TeacherAssignmentRecord teacherAssignmentRecord, List<Map<String, Object>> content,
                                      String defaultSectionId, String bookId, String unitId) {
        List<Map<String, Object>> questionList = new ArrayList<>();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(moreQuestions)
                .stream()
                .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
        if (MapUtils.isNotEmpty(allQuestionMap)) {
            // 总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(Subject.CHINESE, allQuestionMap.keySet(), HomeworkContentType.QUESTION);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            book.setSectionId(defaultSectionId);

            Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
            questionList = new LinkedHashSet<>(moreQuestions)
                    .stream()
                    .filter(docIdQuestionMap::containsKey)
                    .map(docIdQuestionMap::get)
                    .filter(NewQuestion::supportOnlineAnswer)
                    .filter(q -> !Objects.equals(q.getNotFitMobile(), 1))
                    .map(question -> NewHomeworkContentDecorator.decorateNewQuestion(
                            question, contentTypeMap, totalAssignmentRecordMap,
                            teacherAssignmentRecord, book))
                    .collect(Collectors.toList());
        }
        // 推送所有题目的题型
        Set<Integer> questionTypeSet = new LinkedHashSet<>();
        questionList.forEach(map -> questionTypeSet.add(SafeConverter.toInt(map.get("questionTypeId"))));
        List<Map<String, Object>> questionTypes = questionTypeSet.stream()
                .filter(typeId -> contentTypeMap.containsKey(typeId) && contentTypeMap.get(typeId) != null)
                .map(typeId -> MapUtils.m("id", typeId, "name", contentTypeMap.get(typeId).getName()))
                .collect(Collectors.toList());
        content.add(MapUtils.m("type", "question", "questions", questionList, "questionTypes", questionTypes));
    }

    private void processPackageContent(List<Map<String, Object>> content, NewRecommendPackageWrapper packagesWrapper,
                                       String bookId, String unitId, Map<Integer, NewContentType> contentTypeMap,
                                       TeacherAssignmentRecord teacherAssignmentRecord, List<Integer> contentTypeList,
                                       Map<Integer, List<Map<String, Object>>> knowledgePointsMap, Subject subject, List<String> algoTypes) {
        List<NewRecommendPackage> recommendPackages = packagesWrapper.getRecommendPackages();
        if (CollectionUtils.isNotEmpty(recommendPackages)) {
            // 所有的题
            Set<String> allQuestionDocIds = recommendPackages
                    .stream()
                    .filter(recommendPackage -> CollectionUtils.isNotEmpty(recommendPackage.getRecommendPointQuestionInfoList()))
                    .map(NewRecommendPackage::getRecommendPointQuestionInfoList)
                    .flatMap(Collection::stream)
                    .map(QuestionInfo::getDocId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
            Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
            // 总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                    .loadTotalAssignmentRecordByContentType(subject, allQuestionMap.keySet(), HomeworkContentType.QUESTION);

            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);

            Map<String, NewRecommendPackage> recommendPackageMap = new LinkedHashMap<>();
            for (NewRecommendPackage recommendPackage : recommendPackages) {
                recommendPackageMap.put(recommendPackage.getPackageAlgoType(), recommendPackage);
            }
            List<Map<String, Object>> packageList = new ArrayList<>();
            for (String packageAlgoType : algoTypes) {
                if (recommendPackageMap.containsKey(packageAlgoType)) {
                    NewRecommendPackage recommendPackage = recommendPackageMap.get(packageAlgoType);
                    List<RecommendPointQuestionInfo> packageQuestionList = recommendPackage.getRecommendPointQuestionInfoList();
                    String id = recommendPackage.getId();
                    int difficulty = -1;
                    IntelligenceExamSceneType intelligenceExamSceneType = IntelligenceExamSceneType.of(packageAlgoType);
                    if (intelligenceExamSceneType == null || !algoTypes.contains(packageAlgoType)) {
                        continue;
                    }
                    Integer sceneId = intelligenceExamSceneType.getId();
                    if (CollectionUtils.isNotEmpty(packageQuestionList)) {
                        List<Map<String, Object>> questionMapperList = packageQuestionList
                                .stream()
                                .map(questionInfo -> {
                                    NewQuestion newQuestion = docIdQuestionMap.get(questionInfo.getDocId());
                                    // 推送在题型白名单中、支持在线作答、适合移动端展示的题
                                    if (newQuestion != null
                                            && !Objects.equals(newQuestion.getNotFitMobile(), 1)
                                            && contentTypeList.contains(newQuestion.getContentTypeId())
                                            && newQuestion.supportOnlineAnswer()) {
                                        book.setSectionId(questionInfo.getCatalogId());
                                        return NewHomeworkContentDecorator.decorateNewQuestion(
                                                newQuestion, contentTypeMap, totalAssignmentRecordMap,
                                                teacherAssignmentRecord, book);
                                    }
                                    return null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(questionMapperList)) {
                            int seconds = questionMapperList.stream()
                                    .mapToInt(questionMapper -> SafeConverter.toInt(questionMapper.get("seconds")))
                                    .sum();
                            List<Map<String, Object>> contentTypes = sceneLoaderClient.getSceneLoader().loadQuestionTypesBySceneId(sceneId)
                                    .stream()
                                    .filter(contentTypeMap::containsKey)
                                    .map(contentTypeId -> MapUtils.m("id", contentTypeId, "name", contentTypeMap.get(contentTypeId).getName()))
                                    .collect(Collectors.toList());
                            packageList.add(MapUtils.m(
                                    "id", id,
                                    "name", intelligenceExamSceneType.getName(),
                                    "difficulty", difficulty,
                                    "algoType", intelligenceExamSceneType.name(),
                                    "algoTypeName", intelligenceExamSceneType.getName(),
                                    "knowledgePoints", knowledgePointsMap.getOrDefault(sceneId, Collections.emptyList()),
                                    "contentTypes", contentTypes,
                                    "questions", questionMapperList,
                                    "seconds", seconds));
                        }
                    }
                }
            }
            List<Map<String, Object>> packageAlgoTypes = packagesWrapper.getPackageAlgoTypes()
                    .stream()
                    .filter(algoTypes::contains)
                    .map(IntelligenceExamSceneType::of)
                    .filter(Objects::nonNull)
                    .map(intelligenceExamSceneType -> MapUtils.m("type", intelligenceExamSceneType.name(), "name", intelligenceExamSceneType.getName()))
                    .collect(Collectors.toList());
            content.add(MapUtils.m("type", "algoType", "algoTypes", packageAlgoTypes));
            content.add(MapUtils.m("type", "package", "packages", packageList));
        }
    }

    private Map<Integer, List<Map<String, Object>>> loadChineseKnowledgePoints(Collection<Integer> sceneIds, Collection<String> sectionIds) {
        if (CollectionUtils.isNotEmpty(sceneIds) && CollectionUtils.isNotEmpty(sectionIds)) {
            Map<Integer, List<Map<String, Object>>> result = new LinkedHashMap<>();
            Map<Integer, Set<String>> sceneKpIdsMap = new HashMap<>();
            Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(sectionIds);
            Map<String, List<TestMethod>> testMethodMap = testMethodLoaderClient.loadTestMethodByBookCatalogIds(sectionIds);
            // 知识点、知识点特征
            List<BaseKnowledgePointRef> baseKnowledgePointRefList = newKnowledgePointRefMap.values().stream()
                    .filter(Objects::nonNull)
                    .filter(o -> CollectionUtils.isNotEmpty(o.getKnowledgePoints()))
                    .map(NewKnowledgePointRef::getKnowledgePoints)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Set<String> kpIds = new LinkedHashSet<>();
            Set<String> kpfIds = new LinkedHashSet<>();
            for (BaseKnowledgePointRef baseKnowledgePointRef : baseKnowledgePointRefList) {
                String kpId = baseKnowledgePointRef.getId();
                kpIds.add(kpId);
                List<String> featureIds = baseKnowledgePointRef.getFeatureIds();
                if (CollectionUtils.isNotEmpty(featureIds)) {
                    kpfIds.addAll(featureIds);
                }
            }
            Map<String, NewKnowledgePoint> knowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(kpIds);
            Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatures(kpfIds);
            for (BaseKnowledgePointRef baseKnowledgePointRef : baseKnowledgePointRefList) {
                String kpId = baseKnowledgePointRef.getId();
                if (knowledgePointMap.containsKey(kpId)) {
                    NewKnowledgePoint kp = knowledgePointMap.get(kpId);
                    String kpName = kp.getName();
                    List<String> featureIds = baseKnowledgePointRef.getFeatureIds();
                    for (Integer sceneId : sceneIds) {
                        IntelligenceExamSceneType sceneType = IntelligenceExamSceneType.of(sceneId);
                        Set<String> sceneKpIdSet = sceneKpIdsMap.computeIfAbsent(sceneId, k -> new HashSet<>());
                        List<String> rootKps = SCENE_TYPE_KNOWLEDGE_POINTS_MAP.get(sceneType);
                        if (CollectionUtils.isNotEmpty(rootKps) && kp.getAncestorIds().stream().anyMatch(rootKps::contains)) {
                            if (CollectionUtils.isEmpty(featureIds) && sceneKpIdSet.add(kpId)) {
                                result.computeIfAbsent(sceneId, k -> new ArrayList<>()).add(MapUtils.m("id", kpId, "name", kp.getName()));
                            } else {
                                for (String featureId : featureIds) {
                                    String id = kpId + ":" + featureId;
                                    if (knowledgePointFeatureMap.containsKey(featureId) && sceneKpIdSet.add(id)) {
                                        String featureName = knowledgePointFeatureMap.get(featureId).getName();
                                        result.computeIfAbsent(sceneId, k -> new ArrayList<>()).add(MapUtils.m("id", id, "name", kpName + "(" + featureName + ")"));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (List<TestMethod> testMethods : testMethodMap.values()) {
                if (CollectionUtils.isNotEmpty(testMethods)) {
                    for (TestMethod testMethod : testMethods) {
                        for (Integer sceneId : sceneIds) {
                            IntelligenceExamSceneType sceneType = IntelligenceExamSceneType.of(sceneId);
                            Set<String> sceneKpIdSet = sceneKpIdsMap.computeIfAbsent(sceneId, k -> new HashSet<>());
                            List<String> rootTmIds = SCENE_TYPE_TEST_METHODS_MAP.get(sceneType);
                            if (CollectionUtils.isNotEmpty(rootTmIds) && testMethod.getAncestorIds().stream().anyMatch(rootTmIds::contains) && sceneKpIdSet.add(testMethod.getId())) {
                                result.computeIfAbsent(sceneId, k -> new ArrayList<>()).add(MapUtils.m("id", testMethod.getId(), "name", testMethod.getName()));
                            }
                        }
                    }
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> questionList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(contentIdList);
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            // 所有题的第一个考法
            Set<String> allTestMethodIdSet = newQuestionMap.values()
                    .stream()
                    .filter(q -> CollectionUtils.isNotEmpty(q.testMethodList()))
                    .map(q -> q.testMethodList().get(0))
                    .collect(Collectors.toSet());
            Map<String, TestMethod> testMethodMap = testMethodLoaderClient.loadTestMethodIncludeDisabled(allTestMethodIdSet);
            questionList = contentIdList.stream()
                    .filter(questionId -> newQuestionMap.get(questionId) != null)
                    .map(questionId -> {
                        NewQuestion newQuestion = newQuestionMap.get(questionId);
                        String testMethodName = null;
                        List<String> testMethodList = newQuestion.testMethodList();
                        if (CollectionUtils.isNotEmpty(testMethodList)) {
                            String tmId = testMethodList.get(0);
                            if (testMethodMap.containsKey(tmId)) {
                                testMethodName = testMethodMap.get(tmId).getName();
                            }
                        }
                        return MapUtils.m(
                                "questionId", newQuestion.getId(),
                                "seconds", newQuestion.getSeconds(),
                                "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型",
                                "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()),
                                "upImage", newQuestion.getSubmitWays().stream().flatMap(Collection::stream).anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2)),
                                "testMethodName", testMethodName
                        );
                    })
                    .collect(Collectors.toList());
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", questionList
        );
    }
}
