package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.BasicAppCategoryType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/6/29
 */
@Named
public class NewHomeworkBasicAppContentLoader extends NewHomeworkContentLoaderTemplate {
    private static final Map<String, List<Integer>> CATEGORY_GROUP_MAP = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_GROUP_NAME_MAP = new LinkedHashMap<>();
    private static final Set<Integer> SUPPORTED_CATEGORY_IDS = new HashSet<>();

    static {
        CATEGORY_GROUP_MAP.put("听", Arrays.asList(10313, 10312)); // 听音选词，句子听力
        CATEGORY_GROUP_MAP.put("说", Arrays.asList(10310, 10305, 10303, 10304)); // 单词跟读，全文跟读，全文朗读，全文背诵
        CATEGORY_GROUP_MAP.put("读", Arrays.asList(10314, 10322, 10311)); // 看图识词，连词成句，单词辨识
        CATEGORY_GROUP_MAP.put("写", Arrays.asList(10307, 10306)); // 单词排序，单词拼写

        CATEGORY_GROUP_NAME_MAP.put("听", "Listening");
        CATEGORY_GROUP_NAME_MAP.put("说", "Speaking");
        CATEGORY_GROUP_NAME_MAP.put("读", "Reading");
        CATEGORY_GROUP_NAME_MAP.put("写", "Writing");

        SUPPORTED_CATEGORY_IDS.addAll(CATEGORY_GROUP_MAP
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
        );
    }

    public List<Map<String, Object>> getHomeworkContent(TeacherDetail teacher, String unitId, String bookId) {
        List<Map<String, Object>> basicAppContent = teachingObjectiveLoaderClient
                .loadContentByCatalogIdsAndType(Collections.singletonList(unitId), getObjectiveConfigType(), Collections.singleton(ObjectiveConfig.BASIC_APP), teacher.getSubject().getId());
        return processContent(teacher, unitId, bookId, basicAppContent);
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {

        List<Map<String, Object>> basicAppContent = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.BASIC_APP) {
                    basicAppContent.add(configContent);
                }
            }
        }
        return processContent(mapper.getTeacher(), mapper.getUnitId(), mapper.getBookId(), basicAppContent);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> processContent(TeacherDetail teacher, String unitId, String bookId, List<Map<String, Object>> basicAppContent) {
        List<Map<String, Object>> content = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(basicAppContent)) {
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            // 遍历content，对相同lesson的配置包做聚合
            Map<String, Set<Integer>> lessonIdCategoryIdsMap = new HashMap<>();
            basicAppContent.forEach(map -> {
                String lessonId = SafeConverter.toString(map.get("lesson_id"));
                List<Integer> categoryIds = (List<Integer>) map.get("category_ids");
                if (StringUtils.isNotBlank(lessonId) && CollectionUtils.isNotEmpty(categoryIds)) {
                    Set<Integer> categoryIdSet = lessonIdCategoryIdsMap.get(lessonId);
                    if (categoryIdSet != null) {
                        categoryIdSet.addAll(categoryIds);
                    } else {
                        categoryIdSet = new HashSet<>(categoryIds);
                        lessonIdCategoryIdsMap.put(lessonId, categoryIdSet);
                    }
                }
            });
            // 拿到所有的lesson和sentence
            Set<String> lessonIdSet = lessonIdCategoryIdsMap.keySet();
            Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIdSet);
            Map<String, List<Sentence>> lessonSentenceMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIdSet);
            lessonMap.values()
                    .stream()
                    .sorted(Comparator.comparingInt(a -> SafeConverter.toInt(a.getRank())))
                    .filter(lesson -> lessonIdCategoryIdsMap.containsKey(lesson.getId()))
                    .forEach(lesson -> {
                        String lessonId = lesson.getId();
                        Set<Integer> categoryIdSet = lessonIdCategoryIdsMap.get(lessonId);
                        Map<String, Object> lessonCategoryMap = buildLessonCategoryMap(bookId, unitId, lessonId, categoryIdSet, lessonMap, lessonSentenceMap, teacherAssignmentRecord);
                        if (MapUtils.isNotEmpty(lessonCategoryMap)) {
                            content.add(lessonCategoryMap);
                        }
                    });
        }
        if (content.isEmpty()) {
            processTeacherLog(teacher, getObjectiveConfigType(), unitId);
        }
        // 对lesson根据category分组
        Map<BasicAppCategoryType, List<Map<String, Object>>> groupLessonMap = new LinkedHashMap<>();
        for (BasicAppCategoryType basicAppCategoryType : BasicAppCategoryType.values()) {
            List<Integer> categoryIds = basicAppCategoryType.getCategoryIds();
            for (Map<String, Object> lessonMapper : content) {
                List<Map<String, Object>> categories = (List<Map<String, Object>>) lessonMapper.get("categories");
                boolean match = categories.stream()
                        .anyMatch(category -> categoryIds.contains(SafeConverter.toInt(category.get("categoryId"))));
                if (match) {
                    groupLessonMap.computeIfAbsent(basicAppCategoryType, k -> new ArrayList<>()).add(lessonMapper);
                }
            }
        }
        List<Map<String, Object>> groupContent = new ArrayList<>();
        groupLessonMap.forEach((k, v) -> groupContent.add(MapUtils.m("groupName", k.getName(), "groupType", CATEGORY_GROUP_NAME_MAP.get(k.getName()), "categoryGroup", k.name(), "lessons", v)));
        return groupContent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        Set<Integer> allCategoryIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> configContent : objectiveConfig.getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.BASIC_APP) {
                    List<Integer> categoryIds = (List<Integer>) configContent.get("category_ids");
                    if (CollectionUtils.isNotEmpty(categoryIds)) {
                        allCategoryIds.addAll(categoryIds);
                    }
                }
            }
        }
        // 这里只返回分组
        List<Map<String, Object>> categoryGroupList = new ArrayList<>();
        for (BasicAppCategoryType basicAppCategoryType : BasicAppCategoryType.values()) {
            if (basicAppCategoryType.getCategoryIds().stream().anyMatch(allCategoryIds::contains)) {
                categoryGroupList.add(
                        MapUtils.m("categoryGroup", basicAppCategoryType.name(),
                                "categoryGroupName", basicAppCategoryType.getName(),
                                "description", basicAppCategoryType.getDescription())
                );
            }
        }
        if (CollectionUtils.isNotEmpty(categoryGroupList)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "categoryGroups", categoryGroupList
            );
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> lessonList = Collections.emptyList();
        Map<String, Set<Integer>> lessonIdCategoryIdsMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            contentIdList.forEach(contentId -> {
                if (StringUtils.isNotBlank(contentId)) {
                    String splitContentIds[] = StringUtils.split(contentId, "|");
                    if (splitContentIds.length == 2) {
                        String lessonId = splitContentIds[0];
                        Integer categoryId = SafeConverter.toInt(splitContentIds[1]);
                        if (!Objects.equals(categoryId, 0)) {
                            Set<Integer> categoryIds = lessonIdCategoryIdsMap.get(lessonId);
                            if (categoryIds == null) {
                                categoryIds = new HashSet<>();
                            }
                            categoryIds.add(categoryId);
                            lessonIdCategoryIdsMap.put(lessonId, categoryIds);
                        }
                    }
                }
            });
        }
        TeacherAssignmentRecord teacherAssignmentRecord = StringUtils.isBlank(bookId) ? null :
                teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        if (MapUtils.isNotEmpty(lessonIdCategoryIdsMap)) {
            // 先拿到所有的lesson和sentence
            Set<String> lessonIdSet = new HashSet<>(lessonIdCategoryIdsMap.keySet());
            Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIdSet);
            Map<String, List<Sentence>> lessonSentenceMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIdSet);
            lessonList = lessonIdCategoryIdsMap.entrySet().stream()
                    .map(entry -> buildLessonCategoryMap(null, null, entry.getKey(), entry.getValue(), lessonMap, lessonSentenceMap, teacherAssignmentRecord))
                    .filter(MapUtils::isNotEmpty)
                    .collect(Collectors.toList());
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "lessons", lessonList);
    }

    public Map<String, Object> buildLessonCategoryMap(String bookId, String unitId,
                                                      String lessonId, Set<Integer> categoryIds,
                                                      Map<String, NewBookCatalog> lessonMap,
                                                      Map<String, List<Sentence>> lessonSentenceMap,
                                                      TeacherAssignmentRecord teacherAssignmentRecord) {
        Map<String, Object> lessonCategoryMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(lessonMap) && MapUtils.isNotEmpty(lessonSentenceMap)) {
            NewBookCatalog lesson = lessonMap.get(lessonId);
            if (lesson != null) {
                List<Sentence> sentences = lessonSentenceMap.get(lessonId);
                if (CollectionUtils.isNotEmpty(sentences) && CollectionUtils.isNotEmpty(categoryIds)) {
                    List<Long> sentenceIds = sentences.stream().map(Sentence::getId).collect(Collectors.toList());
                    List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
                    List<PracticeType> practiceTypeList = allPracticeList.stream()
                            .filter(p -> categoryIds.contains(p.getCategoryId()))
                            .collect(Collectors.toList());
                    Map<Integer, List<PracticeType>> categoryPracticeMap = practiceTypeList.stream()
                            .filter(p -> PracticeCategory.categoryPracticeTypesMap.get(p.getCategoryId()) != null && PracticeCategory.categoryPracticeTypesMap.get(p.getCategoryId()).contains(p.getId()))
                            .collect(Collectors.groupingBy(PracticeType::getCategoryId));
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadRandomQuestionBySentenceIdsAndCategoryIds(sentenceIds, categoryIds, true);
                    List<Map<String, Object>> categoryList = categoryPracticeMap.entrySet()
                            .stream()
                            .filter(e -> CollectionUtils.isNotEmpty(e.getValue()))
                            .filter(e -> SUPPORTED_CATEGORY_IDS.contains(e.getKey()))
                            .sorted(Comparator.comparingInt(a -> a.getValue().iterator().next().getCategoryRankValue()))
                            .map(e -> {
                                Integer categoryId = e.getKey();
                                List<PracticeType> practiceList = e.getValue();
                                String categoryName = practiceList.iterator().next().getCategoryName();
                                int categoryIcon = PracticeCategory.icon(categoryName);
                                List<NewQuestion> newQuestionList = sentenceIds.stream()
                                        .map(sid -> questionMap.get(sid + "_" + categoryId))
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                if (CollectionUtils.isEmpty(newQuestionList)) {
                                    return null;
                                }
                                List<Map<String, Object>> practiceMapperList = practiceList.stream()
                                        .map(p -> {
                                            List<Map<String, Object>> questionMapperList = newQuestionList.stream()
                                                    .filter(Objects::nonNull)
                                                    .map(q -> MapUtils.m("questionId", q.getId(),
                                                            "seconds", q.getSeconds(),
                                                            "upImage", q.getSubmitWays().stream().flatMap(Collection::stream).anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2)),
                                                            "submitWay", q.getSubmitWays()))
                                                    .collect(Collectors.toList());
                                            return MapUtils.m(
                                                    "practiceId", p.getId(),
                                                    "mobileVersion", p.getMobileVersion(),
                                                    "practiceName", p.getPracticeName(),
                                                    "questions", questionMapperList);
                                        })
                                        .collect(Collectors.toList());
                                String appKey = lessonId + "-" + categoryId;
                                Integer teacherAssignTimes = teacherAssignmentRecord != null ? teacherAssignmentRecord.getAppInfo().getOrDefault(appKey, 0) : 0;
                                EmbedBook book = new EmbedBook();
                                book.setBookId(bookId);
                                book.setUnitId(unitId);
                                return MapUtils.m("categoryName", categoryName,
                                        "categoryId", categoryId,
                                        "categoryIcon", categoryIcon,
                                        "checked", false,
                                        "practices", practiceMapperList,
                                        "teacherAssignTimes", teacherAssignTimes,
                                        "book", book);
                            })
                            .filter(MapUtils::isNotEmpty)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(categoryList)) {
                        Set<Integer> sentenceCategoryIds = new HashSet<>();
                        sentenceCategoryIds.addAll(BasicAppCategoryType.PARAGRAPH.getCategoryIds());
                        sentenceCategoryIds.addAll(BasicAppCategoryType.SENTENCE.getCategoryIds());
                        lessonCategoryMap.put("lessonName", lesson.getAlias());
                        lessonCategoryMap.put("lessonId", lesson.getId());
                        lessonCategoryMap.put("sentences", sentences.stream().map(Sentence::getEnText).collect(Collectors.toList()));
                        lessonCategoryMap.put("isSentence", categoryList.stream().anyMatch(category -> sentenceCategoryIds.contains(SafeConverter.toInt(category.get("categoryId")))));
                        lessonCategoryMap.put("categories", categoryList);
                        Map<Integer, Object> categoryMap = new LinkedHashMap<>();
                        for (Map<String, Object> categoryMapper : categoryList) {
                            int categoryId = SafeConverter.toInt(categoryMapper.get("categoryId"));
                            if (categoryId != 0) {
                                categoryMap.put(categoryId, categoryMapper);
                            }
                        }
                        List<Map<String, Object>> categoryGroupList = new ArrayList<>();
                        CATEGORY_GROUP_MAP.forEach((k, v) -> {
                            List<Object> categoryGroup = v.stream()
                                    .filter(categoryMap::containsKey)
                                    .map(categoryMap::get)
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(categoryGroup)) {
                                categoryGroupList.add(MapUtils.m(
                                        "groupName", k,
                                        "groupType", CATEGORY_GROUP_NAME_MAP.get(k),
                                        "categories", categoryGroup
                                ));
                            }
                        });
                        lessonCategoryMap.put("categoryGroups", categoryGroupList);
                    }
                }
            }
        }
        return lessonCategoryMap;
    }
}
