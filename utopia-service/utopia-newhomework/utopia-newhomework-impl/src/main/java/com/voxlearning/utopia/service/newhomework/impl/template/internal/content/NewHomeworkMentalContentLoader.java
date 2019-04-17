package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheValueLoaderExecutor;
import com.voxlearning.athena.bean.MathematicsKnowledge;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.athena.MathematicsKnowledgeServiceClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
@Named
public class NewHomeworkMentalContentLoader extends NewHomeworkContentLoaderTemplate {

    private static final String ROOT_MENTAL_KP_ID = "KP_10200073219800";
    private static final String QUESTION_COUNT_CACHE_KEY = "MENTAL_KP_QUESTION_COUNT";

    private static final String NORMAL_KP_TYPE = "normal";
    private static final String PRE_KP_TYPE = "pre";
    private static final String FREQUENCY_KP_TYPE = "frequency";
    private static final String TREE_KP_TYPE = "tree";

    @Inject
    private MathematicsKnowledgeServiceClient mathematicsKnowledgeServiceClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        List<String> sectionIds = mapper.getSectionIds();

        if (Subject.MATH != teacher.getSubject()) {
            return content;
        }
        if (CollectionUtils.isEmpty(sectionIds)) {
            return content;
        }
        String defaultSectionId = sectionIds.get(0);

        // 教学目标获取课时下配置的知识点
        // 按照关联课时过滤
        List<Map<String, Object>> mentalQuestionContent = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                String relatedCatalogId = SafeConverter.toString(configContent.get("related_catalog_id"));
                if (type == ObjectiveConfig.QUESTION_STRUCTURE && (StringUtils.isBlank(relatedCatalogId) || sectionIds.contains(relatedCatalogId))) {
                    mentalQuestionContent.add(configContent);
                }
            }
        }

        // 大数据获取常用知识点
        MapMessage athenaResult = null;
        try {
            athenaResult = mathematicsKnowledgeServiceClient.getMathematicsKnowledgeService().loadMathematicsKPData(sectionIds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // 获取老师的使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);

        Map<String, NewKnowledgePoint> allKnowledgePointMap = new HashMap<>();

        // 获取所有口算知识点
        List<NewKnowledgePoint> allMentalKnowledgePointList = newKnowledgePointLoaderClient.loadNewKnowledgePointsByAncestorIds(Collections.singleton(ROOT_MENTAL_KP_ID)).get(ROOT_MENTAL_KP_ID);
        allMentalKnowledgePointList.forEach(kp -> allKnowledgePointMap.put(kp.getId(), kp));

        // 普通知识点
        Set<String> normalKpIdSet = new LinkedHashSet<>();
        // 前置知识点
        Set<String> preKpIdSet = new LinkedHashSet<>();
        // 常用知识点
        Set<String> frequencyKpIdSet = new LinkedHashSet<>();

        // 知识点id，sectionId映射关系
        Map<String, String> kpIdSectionIdMap = new HashMap<>();

        // 解析普通和前置知识点
        if (CollectionUtils.isNotEmpty(mentalQuestionContent)) {
            for (Map<String, Object> contentMap : mentalQuestionContent) {
                String kpId = SafeConverter.toString(contentMap.get("kp_id"));
                normalKpIdSet.add(kpId);

                String relatedCatalogId = SafeConverter.toString(contentMap.get("related_catalog_id"));
                if (StringUtils.isNotBlank(relatedCatalogId)) {
                    kpIdSectionIdMap.put(kpId, relatedCatalogId);
                } else {
                    kpIdSectionIdMap.put(kpId, defaultSectionId);
                }
            }
            if (CollectionUtils.isNotEmpty(normalKpIdSet)) {
                Map<String, NewKnowledgePoint> normalKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(normalKpIdSet);
                allKnowledgePointMap.putAll(normalKnowledgePointMap);
                // 根据普通知识点获取前置知识点
                for (NewKnowledgePoint newKnowledgePoint : normalKnowledgePointMap.values()) {
                    if (CollectionUtils.isNotEmpty(newKnowledgePoint.getPreIds())) {
                        for (String preId : newKnowledgePoint.getPreIds()) {
                            // 过滤普通知识点里面已经有的前置知识点
                            if (!kpIdSectionIdMap.containsKey(preId)) {
                                preKpIdSet.add(preId);
                                // 获取普通知识点的sectionId
                                String sectionId = kpIdSectionIdMap.getOrDefault(newKnowledgePoint.getId(), defaultSectionId);
                                kpIdSectionIdMap.put(preId, sectionId);
                            }
                        }
                    }
                }
                Map<String, NewKnowledgePoint> preKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(preKpIdSet);
                allKnowledgePointMap.putAll(preKnowledgePointMap);
            }
        }

        // 解析常用知识点
        if (athenaResult != null && athenaResult.isSuccess()) {
            Map<String, List<MathematicsKnowledge>> mathematicsKPList = (Map<String, List<MathematicsKnowledge>>) athenaResult.get("MathematicsKPList");
            if (MapUtils.isNotEmpty(mathematicsKPList)) {
                mathematicsKPList.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v)) {
                        for (MathematicsKnowledge mathematicsKnowledge : v) {
                            // 过滤普通知识点和前置知识点里面已经有的常用知识点
                            String kpId = mathematicsKnowledge.getKnowledgePoint();
                            String sectionId = mathematicsKnowledge.getSectionId();
                            if (!kpIdSectionIdMap.containsKey(kpId)) {
                                frequencyKpIdSet.add(mathematicsKnowledge.getKnowledgePoint());
                                kpIdSectionIdMap.put(kpId, sectionId);
                            }
                        }
                    }
                });
                Map<String, NewKnowledgePoint> frequencyKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(frequencyKpIdSet);
                allKnowledgePointMap.putAll(frequencyKnowledgePointMap);
            }
        }

        // 获取所有知识点(包含普通，前置，常用，知识树里面层级为4)的题量
        Set<String> allKpIds = new LinkedHashSet<>();
        allKpIds.addAll(normalKpIdSet);
        allKpIds.addAll(preKpIdSet);
        allKpIds.addAll(frequencyKpIdSet);
        for (NewKnowledgePoint newKnowledgePoint : allMentalKnowledgePointList) {
            String kpId = newKnowledgePoint.getId();
            if (4 == newKnowledgePoint.getNodeLevel()) {
                allKpIds.add(kpId);
            }
        }

        Map<String, Integer> questionCountMap = loadQuestionCountWithCache(allKpIds);
        List<Map<String, Object>> kpList = processKpList(
                normalKpIdSet, preKpIdSet, frequencyKpIdSet,
                allKnowledgePointMap, questionCountMap, teacherAssignmentRecord,
                bookId, unitId, defaultSectionId, kpIdSectionIdMap);
        List<Map<String, Object>> kpTree = processKpTree(
                allMentalKnowledgePointList, questionCountMap, teacherAssignmentRecord,
                normalKpIdSet, preKpIdSet, frequencyKpIdSet,
                bookId, unitId, defaultSectionId, kpIdSectionIdMap);

        content.add(MapUtils.m("kpList", kpList, "kpTree", kpTree));
        return content;
    }

    private List<Map<String, Object>> processKpList(Set<String> normalKpIdSet, Set<String> preKpIdSet, Set<String> frequencyKpIdSet,
                                                    Map<String, NewKnowledgePoint> allKnowledgePointMap, Map<String, Integer> questionCountMap,
                                                    TeacherAssignmentRecord teacherAssignmentRecord, String bookId, String unitId,
                                                    String defaultSectionId, Map<String, String> kpIdSectionIdMap) {
        List<Map<String, Object>> kpList = new ArrayList<>();
        processKp(normalKpIdSet, allKnowledgePointMap, questionCountMap, teacherAssignmentRecord, NORMAL_KP_TYPE, kpList, bookId, unitId, defaultSectionId, kpIdSectionIdMap);
        processKp(preKpIdSet, allKnowledgePointMap, questionCountMap, teacherAssignmentRecord, PRE_KP_TYPE, kpList, bookId, unitId, defaultSectionId, kpIdSectionIdMap);
        processKp(frequencyKpIdSet, allKnowledgePointMap, questionCountMap, teacherAssignmentRecord, FREQUENCY_KP_TYPE, kpList, bookId, unitId, defaultSectionId, kpIdSectionIdMap);
        return kpList;
    }

    private void processKp(Set<String> kpIds, Map<String, NewKnowledgePoint> newKnowledgePointMap,
                           Map<String, Integer> questionCountMap, TeacherAssignmentRecord teacherAssignmentRecord,
                           String kpType, List<Map<String, Object>> kpList,
                           String bookId, String unitId, String defaultSectionId,
                           Map<String, String> kpIdSectionIdMap) {
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

    private List<Map<String, Object>> processKpTree(List<NewKnowledgePoint> allMentalKnowledgePointList, Map<String, Integer> questionCountMap,
                                                    TeacherAssignmentRecord teacherAssignmentRecord,
                                                    Set<String> normalKpIdSet, Set<String> preKpIdSet, Set<String> frequencyKpIdSet,
                                                    String bookId, String unitId, String defaultSectionId,
                                                    Map<String, String> kpIdSectionIdMap) {
        List<Map<String, Object>> kpTree = new ArrayList<>();
        // 所有的二级知识点
        List<NewKnowledgePoint> level2KpList = new ArrayList<>();
        // 所有的三级知识点
        List<NewKnowledgePoint> level3KpList = new ArrayList<>();
        // 所有的四级知识点
        List<NewKnowledgePoint> level4KpList = new ArrayList<>();
        Map<String, List<String>> subKpMap = new HashMap<>();

        String kpType;
        int contentTypeId = 0;

        for (NewKnowledgePoint newKnowledgePoint : allMentalKnowledgePointList) {
            String parentId = newKnowledgePoint.getParentId();
            subKpMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(newKnowledgePoint.getId());

            int nodeLevel = SafeConverter.toInt(newKnowledgePoint.getNodeLevel());
            if (nodeLevel == 2) {
                level2KpList.add(newKnowledgePoint);
            } else if (nodeLevel == 3) {
                level3KpList.add(newKnowledgePoint);
            } else if (nodeLevel == 4) {
                int questionCount = questionCountMap.getOrDefault(newKnowledgePoint.getId(), 0);
                if (questionCount >= 5) {
                    level4KpList.add(newKnowledgePoint);
                }
            }
        }
        Map<String, Map<String, Object>> level4KpMap = new LinkedHashMap<>();
        for (NewKnowledgePoint newKnowledgePoint : level4KpList) {
            String kpId = newKnowledgePoint.getId();
            String kpName = newKnowledgePoint.getName();
            int questionCount = questionCountMap.getOrDefault(kpId, 0);
            int teacherAssignTimes = teacherAssignmentRecord != null ? teacherAssignmentRecord.getMentalKpInfo().getOrDefault(kpId, 0) : 0;
            kpType = processKpType(kpId, normalKpIdSet, preKpIdSet, frequencyKpIdSet);
            level4KpMap.put(kpId, MapUtils.m(
                    "kpId", kpId,
                    "kpName", kpName,
                    "questionCount", questionCount,
                    "teacherAssignTimes", teacherAssignTimes,
                    "nodeLevel", 3,
                    "kpType", kpType,
                    "contentTypeId", contentTypeId,
                    "book", buildBookMapper(kpId, bookId, unitId, defaultSectionId, kpIdSectionIdMap)
            ));
        }
        Map<String, Map<String, Object>> level3KpMap = new LinkedHashMap<>();
        for (NewKnowledgePoint newKnowledgePoint : level3KpList) {
            String kpId = newKnowledgePoint.getId();
            String kpName = newKnowledgePoint.getName();
            List<String> subIds = subKpMap.get(kpId);
            int teacherAssignTimes = teacherAssignmentRecord != null ? teacherAssignmentRecord.getMentalKpInfo().getOrDefault(kpId, 0) : 0;
            if (CollectionUtils.isNotEmpty(subIds)) {
                List<Map<String, Object>> subKps = new ArrayList<>();
                int subQuestionCount = 0;
                for (String id : subIds) {
                    if (level4KpMap.containsKey(id)) {
                        int questionCount = SafeConverter.toInt(level4KpMap.get(id).get("questionCount"));
                        subQuestionCount += questionCount;
                        subKps.add(level4KpMap.get(id));
                    }
                }
                kpType = processKpType(kpId, normalKpIdSet, preKpIdSet, frequencyKpIdSet);
                level3KpMap.put(kpId, MapUtils.m(
                        "kpId", kpId,
                        "kpName", kpName,
                        "questionCount", subQuestionCount,
                        "teacherAssignTimes", teacherAssignTimes,
                        "nodeLevel", 2,
                        "kpType", kpType,
                        "contentTypeId", contentTypeId,
                        "subKps", subKps,
                        "book", buildBookMapper(kpId, bookId, unitId, defaultSectionId, kpIdSectionIdMap)
                ));
            }
        }

        for (NewKnowledgePoint newKnowledgePoint : level2KpList) {
            String kpId = newKnowledgePoint.getId();
            String kpName = newKnowledgePoint.getName();
            List<String> subIds = subKpMap.get(kpId);
            int teacherAssignTimes = teacherAssignmentRecord != null ? teacherAssignmentRecord.getMentalKpInfo().getOrDefault(kpId, 0) : 0;
            if (CollectionUtils.isNotEmpty(subIds)) {
                List<Map<String, Object>> subKps = new ArrayList<>();
                int subQuestionCount = 0;
                for (String id : subIds) {
                    if (level3KpMap.containsKey(id)) {
                        int questionCount = SafeConverter.toInt(level3KpMap.get(id).get("questionCount"));
                        if (questionCount > 0) {
                            subQuestionCount += questionCount;
                            subKps.add(level3KpMap.get(id));
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(subKps)) {
                    kpType = processKpType(kpId, normalKpIdSet, preKpIdSet, frequencyKpIdSet);
                    kpTree.add(MapUtils.m(
                            "kpId", kpId,
                            "kpName", kpName,
                            "questionCount", subQuestionCount,
                            "teacherAssignTimes", teacherAssignTimes,
                            "nodeLevel", 1,
                            "kpType", kpType,
                            "contentTypeId", contentTypeId,
                            "subKps", subKps,
                            "book", buildBookMapper(kpId, bookId, unitId, defaultSectionId, kpIdSectionIdMap)
                    ));
                }
            }
        }
        return kpTree;
    }

    private String processKpType(String kpId, Set<String> normalKpIdSet, Set<String> preKpIdSet, Set<String> frequencyKpIdSet) {
        if (normalKpIdSet.contains(kpId)) {
            return NORMAL_KP_TYPE;
        }
        if (preKpIdSet.contains(kpId)) {
            return PRE_KP_TYPE;
        }
        if (frequencyKpIdSet.contains(kpId)) {
            return FREQUENCY_KP_TYPE;
        }
        return TREE_KP_TYPE;
    }

    private Map<String, Object> buildBookMapper(String kpId, String bookId, String unitId, String defaultSectionId, Map<String, String> kpIdSectionIdMap) {
        Map<String, Object> bookMapper = new LinkedHashMap<>();
        bookMapper.put("bookId", bookId);
        bookMapper.put("unitId", unitId);
        bookMapper.put("sectionId", kpIdSectionIdMap.getOrDefault(kpId, defaultSectionId));
        return bookMapper;
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
                                "seconds", newQuestion.getSeconds(),
                                "questionContent", newQuestion.getContent().getSubContents().get(0).getContent()
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
}
