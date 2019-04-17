/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.NaturalSpellingBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.NaturalSpellingCategoryBO;
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
 * @author zhangbin
 * @since 2017/7/3 11:36
 */

@Named
public class NewHomeworkNaturalSpellingContentLoader extends NewHomeworkContentLoaderTemplate {

    private static final Map<Integer, String> CATEGORY_PREVIEW_VIDEO_MAP = new HashMap<>();

    static {
        // 字母学习
        CATEGORY_PREVIEW_VIDEO_MAP.put(10325, "http://v.17zuoye.cn/content/59af6e8fa3de9f6e80231f33.mp4");
        // 单词拼读
        CATEGORY_PREVIEW_VIDEO_MAP.put(10326, "http://v.17zuoye.cn/content/59af6e85a3de9f6e80231f32.mp4");
        // 趣味拼写
        CATEGORY_PREVIEW_VIDEO_MAP.put(10327, "http://v.17zuoye.cn/content/59af6e94a3de9f6e80231f35.mp4");
        // 读音归类
        CATEGORY_PREVIEW_VIDEO_MAP.put(10328, "http://v.17zuoye.cn/content/59af6e92a3de9f6e80231f34.mp4");
        // 绕口令
        CATEGORY_PREVIEW_VIDEO_MAP.put(10329, "http://v.17zuoye.cn/content/59b255c3a3de9f25f4588e22.mp4");
        // 听音选择
        CATEGORY_PREVIEW_VIDEO_MAP.put(10330, "http://v.17zuoye.cn/content/59b255c1a3de9f25f4588e21.mp4");
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.NATURAL_SPELLING;
    }

    public List<Map<String, Object>> loadContentByLevel(TeacherDetail teacher, String unitId, String bookId, Integer level) {
        // 通用版根据level和bookId调用新接口获取内容
        List<Map<String, Object>> naturalSpellingContent = new ArrayList<>();
        List<ObjectiveConfig> commonObjectiveConfigs = teachingObjectiveLoaderClient.loadUniversalObjectiveConfigForNaturalSpelling(bookId, level);
        if (CollectionUtils.isNotEmpty(commonObjectiveConfigs)) {
            for (ObjectiveConfig commonObjectiveConfig : commonObjectiveConfigs)
                for (Map<String, Object> configContent : commonObjectiveConfig.getContents()) {
                    int type = SafeConverter.toInt(configContent.get("type"));
                    if (type == ObjectiveConfig.NATURAL_SPELLING) {
                        naturalSpellingContent.add(configContent);
                    }
                }
        }
        return processContent(naturalSpellingContent, teacher, unitId, bookId);
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> naturalSpellingContent = new ArrayList<>();
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> configContents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> configContent : objectiveConfig.getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.NATURAL_SPELLING) {
                    configContents.add(configContent);
                }
            }
        }
        boolean nonUniversal = MapUtils.isNotEmpty(objectiveConfig.getExtras()) && StringUtils.equals("nonUniversal", SafeConverter.toString(objectiveConfig.getExtras().get("module")));
        if (nonUniversal) {
            if (CollectionUtils.isNotEmpty(configContents)) {
                List<Map<String, Object>> nonUniversalContents = processContent(configContents, mapper.getTeacher(), mapper.getUnitId(), mapper.getBookId());
                if (CollectionUtils.isNotEmpty(nonUniversalContents)) {
                    naturalSpellingContent.add(MapUtils.m("type", "nonUniversal", "nonUniversalContents", nonUniversalContents, "levels", Collections.emptyList()));
                }
            }
        } else {
            List<Map<String, Object>> levelList = new ArrayList<>();
            int defaultLevel = processDefaultLevel(mapper.getBookId());
            for (int level = 1; level <= 3; level++) {
                levelList.add(MapUtils.m("level", level, "levelName", "Level " + level, "defaultLevel", level == defaultLevel));
            }
            naturalSpellingContent.add(MapUtils.m("type", "universal", "nonUniversalContents", Collections.emptyList(), "levels", levelList));
        }
        return naturalSpellingContent;
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        boolean nonUniversal = MapUtils.isNotEmpty(objectiveConfig.getExtras()) && StringUtils.equals("nonUniversal", SafeConverter.toString(objectiveConfig.getExtras().get("module")));
        List<Map<String, Object>> configContents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> configContent : objectiveConfig.getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.NATURAL_SPELLING) {
                    configContents.add(configContent);
                }
            }
        }
        if (nonUniversal && CollectionUtils.isNotEmpty(configContents)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "description", "单词拼读|趣味拼写等"
            );
        }
        return Collections.emptyMap();
    }

    /**
     * 默认level定位
     * 一起：一、二年级默认level 1;三、四年级默认level 2;五、六年级默认level 3
     * 三起：三年级默认level 1;四、五年级默认level 2;六年级默认level 3
     * 其余情况全部为level 1
     */
    public int processDefaultLevel(String bookId) {
        NewBookProfile book = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (book != null) {
            int startClazzLevel = SafeConverter.toInt(book.getStartClazzLevel());
            int clazzLevel = SafeConverter.toInt(book.getClazzLevel());
            if (startClazzLevel == 1) {
                switch (clazzLevel) {
                    case 1:
                    case 2:
                        return 1;
                    case 3:
                    case 4:
                        return 2;
                    case 5:
                    case 6:
                        return 3;
                    default:
                        return 1;
                }
            } else if (startClazzLevel == 3) {
                switch (clazzLevel) {
                    case 3:
                        return 1;
                    case 4:
                    case 5:
                        return 2;
                    case 6:
                        return 3;
                    default:
                        return 1;
                }
            }
        }
        return 1;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> processContent(List<Map<String, Object>> naturalSpellingContent, TeacherDetail teacher, String unitId, String bookId) {
        //获取应用
        List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
        //老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader
                .loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);

        //所有的lessonId
        Set<String> allLessonIds = new HashSet<>();
        //所有的questionId
        Set<String> allQuestionIds = new HashSet<>();
        for (Map<String, Object> lessonMapper : naturalSpellingContent) {
            String lessonId = SafeConverter.toString(lessonMapper.get("lesson_id"));
            allLessonIds.add(lessonId);

            List<Map<String, Object>> categoryGroupMapList = Collections.emptyList();
            if (MapUtils.isNotEmpty(lessonMapper)) {
                categoryGroupMapList = (List) lessonMapper.get("category_groups");
            }

            for (Map<String, Object> map : categoryGroupMapList) {
                List<Integer> categoryIdsList = (List) map.get("category_ids");
                Map<String, Object> questionsMap = (Map) map.get("questions_map");
                if (CollectionUtils.isNotEmpty(categoryIdsList) && MapUtils.isNotEmpty(questionsMap)) {
                    for (Integer categoryId : categoryIdsList) {
                        List<String> questionIdsList = (List) questionsMap.get(SafeConverter.toString(categoryId));
                        if (CollectionUtils.isNotEmpty(questionIdsList)) {
                            allQuestionIds.addAll(questionIdsList);
                        }
                    }
                }
            }
        }
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIds);
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionByDocIds0(allQuestionIds);

        //所有的unitId
        Set<String> allUnitIds = new HashSet<>();
        if (MapUtils.isNotEmpty(lessonMap)) {
            allUnitIds = lessonMap.values().stream().map(NewBookCatalog::getParentId).collect(Collectors.toSet());
        }
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allUnitIds);

        List<Map<String, Object>> content = new ArrayList<>();
        for (Map<String, Object> lessonMapper : naturalSpellingContent) {
            List<Map<String, Object>> categoryGroupMapList = Collections.emptyList();
            if (MapUtils.isNotEmpty(lessonMapper)) {
                categoryGroupMapList = (List) lessonMapper.get("category_groups");
            }

            //获取所有的应用id
            Set<Integer> allCategoryIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(categoryGroupMapList)) {
                for (Map<String, Object> map : categoryGroupMapList) {
                    Set<Integer> categoryIdsSet = new HashSet<>((List) map.get("category_ids"));
                    allCategoryIds.addAll(categoryIdsSet);
                }
            }

            List<PracticeType> practiceTypeList = allPracticeList.stream()
                    .filter(e -> allCategoryIds.contains(e.getCategoryId()))
                    .collect(Collectors.toList());
            Map<Integer, List<PracticeType>> categoryPracticeMap = practiceTypeList.stream()
                    .filter(e -> PracticeCategory.categoryPracticeTypesMap.get(e.getCategoryId()) != null
                            && PracticeCategory.categoryPracticeTypesMap.get(e.getCategoryId()).contains(e.getId()))
                    .collect(Collectors.groupingBy(PracticeType::getCategoryId));
            Map<Integer, String> categoryIdNameMap = new HashMap<>(categoryPracticeMap.size());
            if (MapUtils.isNotEmpty(categoryPracticeMap)) {
                for (Map.Entry<Integer, List<PracticeType>> entry : categoryPracticeMap.entrySet()) {
                    List<PracticeType> practiceTypes = entry.getValue();
                    if (CollectionUtils.isNotEmpty(practiceTypes)) {
                        categoryIdNameMap.put(entry.getKey(), practiceTypes.get(0).getCategoryName());
                    }
                }
            }

            String lessonId = SafeConverter.toString(lessonMapper.get("lesson_id"));
            List<NaturalSpellingBO> naturalSpellingBOList = new ArrayList<>();
            for (Map<String, Object> map : categoryGroupMapList) {
                NaturalSpellingBO naturalSpellingBO = new NaturalSpellingBO();
                List<Integer> categoryIdsList = (List) map.get("category_ids");
                List<String> sentencesList = (List) map.get("sentence_names");
                Map<String, Object> questionsMap = (Map) map.get("questions_map");
                if (CollectionUtils.isNotEmpty(categoryIdsList)) {
                    List<NaturalSpellingCategoryBO> naturalSpellingCategoryBOList = new ArrayList<>();
                    for (Integer categoryId : categoryIdsList) {
                        NaturalSpellingCategoryBO naturalSpellingCategoryBO = new NaturalSpellingCategoryBO();
                        naturalSpellingCategoryBO.setCategoryId(categoryId);
                        naturalSpellingCategoryBO.setPreviewVideo(CATEGORY_PREVIEW_VIDEO_MAP.getOrDefault(categoryId, ""));
                        String categoryName = categoryIdNameMap.get(categoryId);
                        naturalSpellingCategoryBO.setCategoryName(categoryName);
                        int categoryIcon = PracticeCategory.icon(categoryName);
                        naturalSpellingCategoryBO.setCategoryIcon(categoryIcon);
                        naturalSpellingCategoryBO.setCategoryIcons(Collections.emptyList());
                        naturalSpellingCategoryBO.setChecked(false);
                        List<String> questionIdsList = Collections.emptyList();
                        if (MapUtils.isNotEmpty(questionsMap)) {
                            questionIdsList = (List) questionsMap.get(SafeConverter.toString(categoryId));
                        }
                        List<NewQuestion> newQuestionList = questionIdsList.stream()
                                .filter(questionMap::containsKey)
                                .map(questionMap::get)
                                .collect(Collectors.toList());
                        List<PracticeType> practiceList = categoryPracticeMap.get(categoryId);
                        if (CollectionUtils.isEmpty(practiceList)) {
                            continue;
                        }
                        List<Map<String, Object>> practiceMapperList = practiceList.stream()
                                .map(p -> {
                                    List<Map<String, Object>> questionMapperList = Collections.emptyList();
                                    if (CollectionUtils.isNotEmpty(newQuestionList)) {
                                        questionMapperList = newQuestionList.stream()
                                                .filter(Objects::nonNull)
                                                .map(q -> MapUtils.m(
                                                        "questionId", q.getId(),
                                                        "seconds", q.getSeconds(),
                                                        "upImage", q.getSubmitWays()
                                                                .stream()
                                                                .flatMap(Collection::stream)
                                                                .anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2)),
                                                        "submitWay", q.getSubmitWays()))
                                                .collect(Collectors.toList());
                                    }
                                    return MapUtils.m(
                                            "practiceId", p.getId(),
                                            "mobileVersion", p.getMobileVersion(),
                                            "practiceName", p.getPracticeName(),
                                            "questions", questionMapperList);
                                })
                                .collect(Collectors.toList());
                        naturalSpellingCategoryBO.setPractices(practiceMapperList);

                        EmbedBook book = new EmbedBook();
                        book.setBookId(bookId);
                        book.setUnitId(unitId);
                        book.setLessonId(lessonId);
                        naturalSpellingCategoryBO.setBook(book);

                        String appKey = lessonId + "-" + categoryId;
                        Integer teacherAssignTimes = teacherAssignmentRecord != null
                                ? teacherAssignmentRecord.getAppInfo().getOrDefault(appKey, 0)
                                : 0;
                        naturalSpellingCategoryBO.setTeacherAssignTimes(teacherAssignTimes);
                        naturalSpellingCategoryBOList.add(naturalSpellingCategoryBO);
                    }
                    if (CollectionUtils.isNotEmpty(naturalSpellingCategoryBOList)) {
                        naturalSpellingBO.setSentences(sentencesList);
                        naturalSpellingBO.setCategories(naturalSpellingCategoryBOList);
                        Boolean newLine = false;
                        Set<Integer> categories = new HashSet<>(categoryIdsList);
                        if (categories.contains(NatureSpellingType.REPEAT_WORD.getCategoryId())) {
                            newLine = true;
                        }
                        naturalSpellingBO.setNewLine(newLine);
                        naturalSpellingBOList.add(naturalSpellingBO);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(naturalSpellingBOList)) {
                Map<String, Object> contentMap = new LinkedHashMap<>();
                contentMap.put("lessonId", lessonId);
                String lessonName = "";
                String lessonUnitId = "";
                NewBookCatalog lesson = lessonMap.get(lessonId);
                if (lesson != null) {
                    lessonName = lesson.getName();
                    NewBookCatalog unit = unitMap.get(lesson.getParentId());
                    if (unit != null && StringUtils.equalsIgnoreCase(BookCatalogType.UNIT.name(), unit.getNodeType())) {
                        lessonUnitId = unit.getId();
                    }
                }
                contentMap.put("lessonName", lessonName);
                contentMap.put("unitId", lessonUnitId);
                contentMap.put("categoryGroups", naturalSpellingBOList);
                content.add(contentMap);
            }
        }
        if (CollectionUtils.isNotEmpty(content)) {
            // 按unitId聚合lesson
            Map<String, List<Map<String, Object>>> unitLessonMap = new LinkedHashMap<>();
            for (Map<String, Object> lessonMapper : content) {
                String lessonUnitId = SafeConverter.toString(lessonMapper.get("unitId"));
                unitLessonMap.computeIfAbsent(lessonUnitId, k -> new ArrayList<>()).add(lessonMapper);
            }
            String defaultUnitId = unitLessonMap.keySet().iterator().next();
            // 设置unitName
            Map<String, String> unitIdNameMap = new HashMap<>();
            Map<String, Integer> unitIdRankMap = new HashMap<>();
            Set<String> unitIds = unitLessonMap.keySet();
            for (String lessonUnitId : unitIds) {
                NewBookCatalog unit = unitMap.get(lessonUnitId);
                unitIdNameMap.put(lessonUnitId, unit != null ? unit.getAlias() : "");
                unitIdRankMap.put(lessonUnitId, unit != null ? SafeConverter.toInt(unit.getRank()) : 0);
            }
            List<Map<String, Object>> unitList = new ArrayList<>();
            unitLessonMap.forEach((k, v) -> unitList.add(MapUtils.m("unitId", k, "unitName", unitIdNameMap.get(k), "lessons", v, "defaultUnit", StringUtils.equals(defaultUnitId, k))));
            content = unitList.stream()
                    .sorted((a, b) -> {
                        String unitIdA = SafeConverter.toString(a.get("unitId"));
                        String unitIdB = SafeConverter.toString(b.get("unitId"));
                        int rankA = unitIdRankMap.getOrDefault(unitIdA, 0);
                        int rankB = unitIdRankMap.getOrDefault(unitIdB, 0);
                        return Integer.compare(rankA, rankB);
                    })
                    .collect(Collectors.toList());
        }
        return content;
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> lessonList = new ArrayList<>();
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
                            if (CollectionUtils.isEmpty(categoryIds)) {
                                categoryIds = new HashSet<>();
                            }
                            categoryIds.add(categoryId);
                            lessonIdCategoryIdsMap.put(lessonId, categoryIds);
                        }
                    }
                }
            });
        }

        if (MapUtils.isNotEmpty(lessonIdCategoryIdsMap)) {
            Set<String> lessonIdSet = new HashSet<>(lessonIdCategoryIdsMap.keySet());
            Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIdSet);

            Set<Integer> allCategoryIds = new HashSet<>();
            lessonIdCategoryIdsMap.values().forEach(allCategoryIds::addAll);
            List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
            List<PracticeType> practiceTypeList = allPracticeList.stream()
                    .filter(e -> allCategoryIds.contains(e.getCategoryId()))
                    .collect(Collectors.toList());
            Map<Integer, List<PracticeType>> categoryPracticeMap = practiceTypeList.stream()
                    .filter(e -> PracticeCategory.categoryPracticeTypesMap.get(e.getCategoryId()) != null
                            && PracticeCategory.categoryPracticeTypesMap.get(e.getCategoryId()).contains(e.getId()))
                    .collect(Collectors.groupingBy(PracticeType::getCategoryId));
            Map<Integer, String> categoryIdNameMap = new HashMap<>(categoryPracticeMap.size());
            if (MapUtils.isNotEmpty(categoryPracticeMap)) {
                for (Map.Entry<Integer, List<PracticeType>> entry : categoryPracticeMap.entrySet()) {
                    List<PracticeType> practiceTypes = entry.getValue();
                    if (CollectionUtils.isNotEmpty(practiceTypes)) {
                        categoryIdNameMap.put(entry.getKey(), practiceTypes.get(0).getCategoryName());
                    }
                }
            }

            for (Map.Entry<String, Set<Integer>> entry : lessonIdCategoryIdsMap.entrySet()) {
                String lessonId = entry.getKey();
                String lessonName = lessonMap.get(lessonId).getName();
                Set<Integer> categoryIds = entry.getValue();
                Map<String, Object> lessonContent = new LinkedHashMap<>();
                lessonContent.put("lessonId", lessonId);
                lessonContent.put("lessonName", lessonName);
                List<Map<String, Object>> categoryList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(categoryIds)) {
                    for (Integer categoryId : categoryIds) {
                        Map<String, Object> categoryMap = new LinkedHashMap<>();
                        categoryMap.put("categoryId", categoryId);
                        categoryMap.put("categoryName", categoryIdNameMap.get(categoryId));
                        int categoryIcon = PracticeCategory.icon(categoryIdNameMap.get(categoryId));
                        categoryMap.put("categoryIcon", categoryIcon);
                        categoryList.add(categoryMap);
                    }
                }
                lessonContent.put("categories", categoryList);
                lessonList.add(lessonContent);
            }
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "lessons", lessonList);
    }
}
