package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.athena.api.recom.entity.paks.NewRecommendPackage;
import com.voxlearning.athena.api.recom.entity.paks.QuestionInfo;
import com.voxlearning.athena.api.recom.entity.paks.RecommendPointQuestionInfo;
import com.voxlearning.athena.api.recom.entity.wrapper.NewRecommendPackageWrapper;
import com.voxlearning.utopia.core.helper.ObjectCopyUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.BaseKnowledgePointRef;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePointRef;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.IntelligenceExamSceneType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.recommendation.RecommendedHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.SceneLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/1/5
 */
@Named
public class NewHomeworkIntelligenceExamContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject private AthenaHomeworkLoaderClient athenaHomeworkLoaderClient;
    @Inject private SceneLoaderClient sceneLoaderClient;
    @Inject private RecommendedHomeworkLoaderClient recommendedHomeworkLoaderClient;

    private static final Set<Integer> MATH_UNIT_REVIEW_SCENE_IDS = new HashSet<>();
    private static final int ORAL_PRACTICE_SCENE_ID = 25;

    static {
        MATH_UNIT_REVIEW_SCENE_IDS.addAll(Arrays.asList(20, 21));
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.INTELLIGENCE_EXAM;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        TeacherDetail teacher = mapper.getTeacher();
        Set<Long> groupIds = mapper.getGroupIds();
        List<String> sectionIds = mapper.getSectionIds();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        boolean waterfall = mapper.isWaterfall();

        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        Set<Integer> allConfigSceneIds = new LinkedHashSet<>();
        // 获取内容配置的场景id、知识点id
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

        // 枚举里面支持的场景
        Map<Integer, IntelligenceExamSceneType> sceneTypeMap = new LinkedHashMap<>();
        allConfigSceneIds.stream()
                .map(IntelligenceExamSceneType::of)
                .filter(Objects::nonNull)
                .forEach(type -> sceneTypeMap.put(type.getId(), type));
        List<String> algoTypes = sceneTypeMap.values().stream().map(IntelligenceExamSceneType::name).collect(Collectors.toList());

        List<Map<String, Object>> content = new ArrayList<>();
        Map<Long, Collection<String>> groupBookCatalogIds = new LinkedHashMap<>();
        // 必出精题
        NewRecommendPackageWrapper recommendPackageWrapper = null;
        Subject subject = teacher.getSubject();
        // 题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        // 试题类型白名单
        List<Integer> contentTypeList = teacher.getSubject() == Subject.ENGLISH ? QuestionConstants.englishExamIncludeContentTypeIds :
                teacher.getSubject() == Subject.MATH ? QuestionConstants.homeworkMathIncludeContentTypeIds : QuestionConstants.examChineseIncludeContentTypeIds;
        // 老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        //教师学校ID
        Map<String, String> teacherSchoolMap = new HashMap<>();
        teacherSchoolMap.put("SCHOOL_ID", SafeConverter.toString(teacher.getTeacherSchoolId()));
        if (subject == Subject.ENGLISH) {
            // 英语题包
            for (Long groupId : groupIds) {
                groupBookCatalogIds.put(groupId, Collections.singleton(unitId));
            }
            if (CollectionUtils.isNotEmpty(algoTypes)) {
                try {
                    com.voxlearning.recom.homework.api.entity.NewRecommendPackageWrapper englishRecommendPackageWrapper = recommendedHomeworkLoaderClient.getHomeworkRecomLoader()
                            .loadEnglishSpecifiedFeaturedPackagesV2(bookId, groupBookCatalogIds, teacher.getId(), SafeConverter.toLong(teacher.getCityCode()), algoTypes, teacherSchoolMap);
                    recommendPackageWrapper = ObjectCopyUtils.copyPropertiesByJson(NewRecommendPackageWrapper.class, englishRecommendPackageWrapper);
                } catch (Exception e) {
                    logger.error("NewHomeworkIntelligenceExamContentLoader call recom error:", e);
                }
            }
            if (recommendPackageWrapper != null) {
                Map<Integer, List<Map<String, Object>>> knowledgePointsMap = loadEnglishKnowledgePoints(unitId, allConfigSceneIds);
                processPackageContent(content, recommendPackageWrapper, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList, knowledgePointsMap, Subject.ENGLISH, mapper.isWaterfall());
            }
            // 瀑布流方式不要更多
            // 口语不要更多
            if (!waterfall && ObjectiveConfigType.ORAL_PRACTICE != getObjectiveConfigType()) {
                List<String> englishMoreQuestions = recommendedHomeworkLoaderClient.getHomeworkRecomLoader()
                        .loadPrimaryEnglishMoreTopicsRecommendedPackages(bookId, unitId, teacher.getId(), SafeConverter.toLong(teacher.getCityCode()));
                processEnglishMoreQuestionList(content, englishMoreQuestions, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList);
            }
        } else if (subject == Subject.MATH) {
            // 数学题包
            List<String> lessonIds = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON)
                    .get(unitId)
                    .stream()
                    .sorted(new NewBookCatalog.RankComparator())
                    .map(NewBookCatalog::getId)
                    .collect(Collectors.toList());
            Map<String, List<NewBookCatalog>> lessonSectionMap = newContentLoaderClient.loadChildren(lessonIds, BookCatalogType.SECTION);
            Set<String> sortedSectionIds = new LinkedHashSet<>();
            Set<String> allSectionIds = new LinkedHashSet<>();
            for (String lessonId : lessonIds) {
                List<NewBookCatalog> sectionList = lessonSectionMap.get(lessonId);
                if (CollectionUtils.isNotEmpty(sectionList)) {
                    sectionList.sort(new NewBookCatalog.RankComparator());
                    for (NewBookCatalog section : sectionList) {
                        allSectionIds.add(section.getId());
                        if (sectionIds.contains(section.getId())) {
                            sortedSectionIds.add(section.getId());
                        }
                    }
                }
            }
            for (Long groupId : groupIds) {
                groupBookCatalogIds.put(groupId, sortedSectionIds);
            }
            if (CollectionUtils.isNotEmpty(algoTypes)) {
                try {
                    recommendPackageWrapper = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                            .loadPrimaryMathSpecifiedFeaturedPackagesV2(bookId, groupBookCatalogIds, teacher.getId(), algoTypes, teacherSchoolMap);
                } catch (Exception e) {
                    logger.error("NewHomeworkIntelligenceExamContentLoader call athena error:", e);
                }
            }
            if (recommendPackageWrapper != null) {
                boolean isUnitReview = allConfigSceneIds.stream().anyMatch(MATH_UNIT_REVIEW_SCENE_IDS::contains);
                List<Map<String, Object>> knowledgePoints = isUnitReview ? loadMathKnowledgePoints(allSectionIds) : loadMathKnowledgePoints(sortedSectionIds);
                Map<Integer, List<Map<String, Object>>> knowledgePointsMap = new HashMap<>();
                // 数学每个场景下的知识点都相同
                for (Integer sceneType : sceneTypeMap.keySet()) {
                    knowledgePointsMap.put(sceneType, knowledgePoints);
                }
                processPackageContent(content, recommendPackageWrapper, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList, knowledgePointsMap, Subject.MATH, mapper.isWaterfall());
            }

            // 瀑布流方式不要更多
            if (!waterfall) {
                // 数学更多
                Map<String, List<String>> mathMoreQuestions = null;
                try {
                    mathMoreQuestions = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                            .loadMathSimilarQuestions(sortedSectionIds);
                } catch (Exception e) {
                    logger.error("NewHomeworkIntelligenceExamContentLoader call athena error:", e);
                }
                if (MapUtils.isNotEmpty(mathMoreQuestions)) {
                    processMathMoreQuestionContent(content, mathMoreQuestions, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList);
                }
            }
        }
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> contentList = loadContent(mapper);
        if (CollectionUtils.isNotEmpty(contentList)) {
            List<Map<String, Object>> algoTypes = new ArrayList<>();
            List<Map<String, Object>> packages = new ArrayList<>();
            for (Map<String, Object> content : contentList) {
                String type = SafeConverter.toString(content.get("type"));
                if ("algoType".equals(type)) {
                    algoTypes = (List<Map<String, Object>>) content.get("algoTypes");
                } else if ("package".equals(type)) {
                    packages = (List<Map<String, Object>>) content.get("packages");
                }
            }
            if (CollectionUtils.isNotEmpty(algoTypes) && CollectionUtils.isNotEmpty(packages)) {
                return MapUtils.m(
                        "objectiveConfigId", objectiveConfig.getId(),
                        "type", getObjectiveConfigType().name(),
                        "typeName", getObjectiveConfigType().getValue(),
                        "name", objectiveConfig.getName(),
                        "algoTypes", algoTypes,
                        "packages", packages,
                        "showMoreQuestions", ObjectiveConfigType.INTELLIGENCE_EXAM == this.getObjectiveConfigType()
                );
            }
        }
        return Collections.emptyMap();
    }

    private void processPackageContent(List<Map<String, Object>> content, NewRecommendPackageWrapper recommendPackageWrapper,
                                       String bookId, String unitId, Map<Integer, NewContentType> contentTypeMap,
                                       TeacherAssignmentRecord teacherAssignmentRecord, List<Integer> contentTypeList,
                                       Map<Integer, List<Map<String, Object>>> knowledgePointsMap, Subject subject, boolean isWaterfall) {
        List<NewRecommendPackage> recommendPackages = recommendPackageWrapper.getRecommendPackages();
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

            List<Map<String, Object>> packageList = new ArrayList<>();
            int packageCount = recommendPackages.size();
            for (int index = 0; index < packageCount; index++) {
                NewRecommendPackage recommendPackage = recommendPackages.get(index);
                List<RecommendPointQuestionInfo> packageQuestionList = recommendPackage.getRecommendPointQuestionInfoList();
                String id = recommendPackage.getId();
                if (StringUtils.isBlank(id)) {
                    id = unitId + "#" + (index + 1);
                }
                IntelligenceExamSceneType algoType = IntelligenceExamSceneType.of(recommendPackage.getPackageAlgoType());
                if (algoType == null) {
                    continue;
                }
                String name = algoType.getName();
                Integer difficulty = recommendPackage.getDifficulty();
                // 英语难度都设置为-1
                if (subject == Subject.ENGLISH) {
                    difficulty = -1;
                }
                if (CollectionUtils.isNotEmpty(packageQuestionList)) {
                    List<Map<String, Object>> questionMapperList = packageQuestionList
                            .stream()
                            .map(questionInfo -> {
                                NewQuestion newQuestion = docIdQuestionMap.get(questionInfo.getDocId());
                                // 推送在题型白名单中、支持在线作答、适合移动端展示的题
                                // 或者新口语题
                                if (newQuestion != null && !Objects.equals(newQuestion.getNotFitMobile(), 1)
                                        && ((contentTypeList.contains(newQuestion.getContentTypeId()) && newQuestion.supportOnlineAnswer())
                                        || (ORAL_PRACTICE_SCENE_ID == algoType.getId() && questionContentTypeLoaderClient.isNewOral(newQuestion.findSubContentTypeIds())))) {
                                    if (Objects.equals(QuestionConstants.SUBJECT_PRIMARY_MATH, newQuestion.getSubjectId())) {
                                        book.setSectionId(questionInfo.getCatalogId());
                                    }
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
                        List<Map<String, Object>> contentTypes = sceneLoaderClient.getSceneLoader().loadQuestionTypesBySceneId(algoType.getId())
                                .stream()
                                .filter(contentTypeMap::containsKey)
                                .map(contentTypeId -> MapUtils.m("id", contentTypeId, "name", contentTypeMap.get(contentTypeId).getName()))
                                .collect(Collectors.toList());
                        String packageDescription = "";
                        if (isWaterfall) {
                            packageDescription = sceneLoaderClient.loadSceneDescription(unitId, algoType.getId());
                        }
                        packageList.add(MapUtils.m(
                                "id", id,
                                "name", name,
                                "difficulty", difficulty,
                                "algoType", algoType.name(),
                                "algoTypeName", algoType.getName(),
                                "knowledgePoints", knowledgePointsMap.getOrDefault(algoType.getId(), Collections.emptyList()),
                                "contentTypes", contentTypes,
                                "questions", questionMapperList,
                                "seconds", seconds,
                                "packageDescription", packageDescription));
                    }
                }
            }
            List<Map<String, Object>> packageAlgoTypes = recommendPackageWrapper.getPackageAlgoTypes()
                    .stream()
                    .map(IntelligenceExamSceneType::of)
                    .filter(Objects::nonNull)
                    .map(intelligenceExamSceneType -> MapUtils.m("type", intelligenceExamSceneType.name(), "name", intelligenceExamSceneType.getName()))
                    .collect(Collectors.toList());
            content.add(MapUtils.m("type", "algoType", "algoTypes", packageAlgoTypes));
            content.add(MapUtils.m("type", "package", "packages", packageList));
        }
    }

    private List<Map<String, Object>> loadMathKnowledgePoints(Collection<String> sectionIds) {
        // 这里不再用教学目标里面配置的知识点
        // 改用基础数据管理里面的知识点，与学情评估大数据那边取的数据保持一致
        Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(sectionIds);
        Map<String, List<TestMethodRef>> testMethodRefMap = testMethodLoaderClient.loadTestMethodRefByBookCatalogIds(sectionIds);
        Map<String, List<SolutionMethodRef>> solutionMethodRefMap = solutionMethodRefLoaderClient.loadSolutionMethodRefByBookCatalogIds(sectionIds);

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
            String version = baseKnowledgePointRef.getVersion();
            String kpId = baseKnowledgePointRef.getId();
            // 过滤口算知识点
            if (!StringUtils.equals(version, NewHomeworkConstants.ROOT_MENTAL_KP_ID)) {
                kpIds.add(kpId);
                List<String> featureIds = baseKnowledgePointRef.getFeatureIds();
                if (CollectionUtils.isNotEmpty(featureIds)) {
                    kpfIds.addAll(featureIds);
                }
            }
        }

        // 考法id
        Set<String> tmIds = testMethodRefMap.values().stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(o -> CollectionUtils.isNotEmpty(o.getTestMethods()))
                .map(TestMethodRef::getTestMethods)
                .flatMap(Collection::stream)
                .map(EmbedTestMethodRef::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 解法id
        Set<String> smIds = solutionMethodRefMap.values().stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(o -> CollectionUtils.isNotEmpty(o.getSolutionMethods()))
                .map(SolutionMethodRef::getSolutionMethods)
                .flatMap(Collection::stream)
                .map(EmbedSolutionMethodRef::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));


        // 知识点特征
        Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatures(kpfIds);

        Set<String> idSet = new HashSet<>();
        idSet.addAll(kpIds);
        idSet.addAll(tmIds);
        idSet.addAll(smIds);
        Map<String, String> idNameMap = testMethodLoaderClient.getNameById(idSet);

        List<Map<String, Object>> knowledgePoints = new ArrayList<>();
        Set<String> allKpIdSet = new HashSet<>();

        for (BaseKnowledgePointRef baseKnowledgePointRef : baseKnowledgePointRefList) {
            String kpId = baseKnowledgePointRef.getId();
            if (idNameMap.containsKey(kpId)) {
                String kpName = idNameMap.get(kpId);
                List<String> featureIds = baseKnowledgePointRef.getFeatureIds();
                if (CollectionUtils.isEmpty(featureIds) && allKpIdSet.add(kpId)) {
                    knowledgePoints.add(MapUtils.m("id", kpId, "name", kpName));
                } else {
                    for (String featureId : featureIds) {
                        String id = kpId + ":" + featureId;
                        if (knowledgePointFeatureMap.containsKey(featureId) && allKpIdSet.add(id)) {
                            String featureName = knowledgePointFeatureMap.get(featureId).getName();
                            knowledgePoints.add(MapUtils.m("id", id, "name", kpName + "(" + featureName + ")"));
                        }
                    }
                }
            }
        }

        for (String id : tmIds) {
            if (idNameMap.containsKey(id) && allKpIdSet.add(id)) {
                knowledgePoints.add(MapUtils.m("id", id, "name", idNameMap.get(id)));
            }
        }

        for (String id : smIds) {
            if (idNameMap.containsKey(id) && allKpIdSet.add(id)) {
                knowledgePoints.add(MapUtils.m("id", id, "name", idNameMap.get(id)));
            }
        }

        knowledgePoints.sort((Comparator.comparing(o -> SafeConverter.toString(o.get("id")))));
        return knowledgePoints;
    }

    @SuppressWarnings("unchecked")
    private void processEnglishMoreQuestionList(List<Map<String, Object>> content, List<String> moreQuestionDocIds,
                                                String bookId, String unitId, Map<Integer, NewContentType> contentTypeMap,
                                                TeacherAssignmentRecord teacherAssignmentRecord, List<Integer> contentTypeList) {
        List<Map<String, Object>> examQuestionList = new ArrayList<>();
//        List<NewQuestion> newQuestions = questionLoaderClient.loadQuestionByNewKnowledgePointsAndUnit(configKpIds, unitId, Collections.emptyList(), true, true, true, true);
//        if (CollectionUtils.isEmpty(newQuestions)) {
//            newQuestions = questionLoaderClient.loadQuestionByNewKnowledgePoints(configKpIds, Collections.emptyList(), true, true, true, true).values()
//                    .stream()
//                    .flatMap(Collection::stream)
//                    .collect(Collectors.toList());
//        }
        List<NewQuestion> newQuestions = questionLoaderClient.loadQuestionByDocIds(moreQuestionDocIds);
        Set<String> allQuestionIdSet = newQuestions.stream().map(NewQuestion::getId).collect(Collectors.toSet());
        Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                .loadTotalAssignmentRecordByContentType(Subject.ENGLISH, allQuestionIdSet, HomeworkContentType.QUESTION);
        EmbedBook book = new EmbedBook();
        book.setBookId(bookId);
        book.setUnitId(unitId);
        if (CollectionUtils.isNotEmpty(newQuestions)) {
            examQuestionList = newQuestions.stream()
                    .filter(q -> contentTypeList.contains(q.getContentTypeId()) && q.supportOnlineAnswer() && !Objects.equals(q.getNotFitMobile(), 1))
                    .map(q -> NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book))
                    .collect(Collectors.toList());
        }
        // 推送所有题目的知识点
//        List<Map<String, Object>> newKnowledgePoints = new ArrayList<>();
//        Set<String> questionKpIdSet = examQuestionList.stream()
//                .filter(q -> q.get("knowledgePoints") != null)
//                .map(q -> (List<String>) q.get("knowledgePoints"))
//                .flatMap(Collection::stream)
//                .collect(Collectors.toSet());
//        Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(configKpIds);
//        if (MapUtils.isNotEmpty(newKnowledgePointMap)) {
//            Map<String, List<NewKnowledgePoint>> kpTypeMap = newKnowledgePointMap.values()
//                    .stream()
//                    .filter(kp -> questionKpIdSet.contains(kp.getId()))
//                    .collect(Collectors.groupingBy(NewKnowledgePoint::getPointType));
//            QuestionConstants.knowledgePointTypes
//                    .forEach(type -> {
//                                if (kpTypeMap.containsKey(type)) {
//                                    List<NewKnowledgePoint> newKnowledgePointList = kpTypeMap.get(type);
//                                    Map<String, Object> kpMap = new LinkedHashMap<>();
//                                    String kpType = QuestionConstants.knowledgePointTypeMap.get(type);
//                                    kpMap.put("kpType", kpType != null ? kpType : type);
//                                    kpMap.put("knowledgePoints", newKnowledgePointList.stream().map(kp -> MapUtils.m("kpId", kp.getId(), "kpName", kp.getName())).collect(Collectors.toList()));
//                                    newKnowledgePoints.add(kpMap);
//                                }
//                            }
//                    );
//        }
        // 推送所有题目的题型
        Set<Integer> examQuestionTypeSet = new LinkedHashSet<>();
        examQuestionList.forEach(map -> examQuestionTypeSet.add(SafeConverter.toInt(map.get("questionTypeId"))));
        List<Map<String, Object>> examQuestionTypes = contentTypeList
                .stream()
                .filter(examQuestionTypeSet::contains)
                .filter(typeId -> contentTypeMap.containsKey(typeId) && contentTypeMap.get(typeId) != null)
                .map(typeId -> MapUtils.m("id", typeId, "name", contentTypeMap.get(typeId).getName()))
                .collect(Collectors.toList());
        content.add(MapUtils.m("type", "question", "questions", examQuestionList, "questionTypes", examQuestionTypes));
    }

    private Map<Integer, List<Map<String, Object>>> loadEnglishKnowledgePoints(String unitId, Set<Integer> sceneIds) {
        if (CollectionUtils.isNotEmpty(sceneIds)) {
            Map<Integer, List<String>> kpIdsMap = new LinkedHashMap<>();
            Set<String> kpIdSet = new HashSet<>();
            for (Integer sceneId : sceneIds) {
                List<String> kpIds = sceneLoaderClient.loadKnowledgePointIdsByBookCatalogIdAndSceneId(unitId, sceneId);
                kpIdSet.addAll(kpIds);
                kpIdsMap.put(sceneId, kpIds);
            }
            Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(kpIdSet);
            Map<Integer, List<Map<String, Object>>> result = new LinkedHashMap<>();
            kpIdsMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    List<Map<String, Object>> kpList = v.stream()
                            .filter(newKnowledgePointMap::containsKey)
                            .map(newKnowledgePointMap::get)
                            .map(kp -> MapUtils.m("id", kp.getId(), "name", kp.getName()))
                            .collect(Collectors.toList());
                    result.put(k, kpList);
                } else {
                    result.put(k, Collections.emptyList());
                }
            });
            return result;
        }
        return Collections.emptyMap();
    }
}
