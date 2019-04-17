package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.mapper.wordteach.WordsPracticeBO;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.stone.data.*;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教师端内容展示&预览
 *
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/11/26 上午11:28
 */
@Named
public class NewHomeworkWordTeachAndPracticeContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        try {
            TeacherDetail teacher = mapper.getTeacher();
            String unitId = mapper.getUnitId();
            String bookId = mapper.getBookId();
            List<String> sectionIds = mapper.getSectionIds();

            Set<String> allStoneDataIds = new HashSet<>();
            LinkedHashMap<String, List<String>> sectionStoneDataIdMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
                for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                    int type = SafeConverter.toInt(configContent.get("type"));
                    if (type == ObjectiveConfig.ZI_CI_JIANG_LIAN) {
                        String relatedCatalogId = SafeConverter.toString(configContent.get("related_catalog_id"));
                        if (!sectionIds.contains(relatedCatalogId)) {
                            continue;
                        }
                        List<String> stoneDataIds = conversionService.convert(configContent.get("stone_data_ids"), List.class);
                        // 字词讲练关联的stoneDataId & 课时ID 不允许为空
                        if (CollectionUtils.isNotEmpty(stoneDataIds) && StringUtils.isNotEmpty(relatedCatalogId)) {
                            allStoneDataIds.addAll(stoneDataIds);
                            sectionStoneDataIdMap.put(relatedCatalogId, stoneDataIds);
                        }
                    }
                }
            }

            // stoneDataId为空时，直接返回。。。
            if (MapUtils.isEmpty(sectionStoneDataIdMap)) {
                return content;
            }

            // 获取题包
            Map<String, StoneBufferedData> stoneBufferedDataMap = stoneDataLoaderClient.getStoneBufferedDataList(allStoneDataIds)
                    .stream()
                    .collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));
            if (MapUtils.isEmpty(stoneBufferedDataMap)) {
                return content;
            }

            List<String> allQuestionIds = new LinkedList<>();
            Set<String> allCourseIds = new HashSet<>();
            stoneBufferedDataMap.values().forEach(s -> {
                if (s.getWordsPractice() != null) {
                    WordsPractice wordsPractice = s.getWordsPractice();
                    if (wordsPractice.getWordExercise() != null && CollectionUtils.isNotEmpty(wordsPractice.getWordExercise().getQuestionIds())) {
                        allQuestionIds.addAll(wordsPractice.getWordExercise().getQuestionIds());
                    }
                    if (wordsPractice.getImageText() != null) {
                        wordsPractice.getImageText().getImageTextRhymes().forEach(q -> {
                            if (CollectionUtils.isNotEmpty(q.getQuestionIds())) {
                                allQuestionIds.addAll(q.getQuestionIds());
                            }
                        });
                    }
                    if (wordsPractice.getChineseCharacterCulture() != null && CollectionUtils.isNotEmpty(wordsPractice.getChineseCharacterCulture().getCourseIds())) {
                        allCourseIds.addAll(wordsPractice.getChineseCharacterCulture().getCourseIds());
                    }
                }
            });
            // 课程信息
            Map<String, IntelDiagnosisCourse> allIntelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(allCourseIds);
            // 题目信息
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity(), (t1, t2) -> t2, LinkedHashMap::new));
            // 题目总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionMap.keySet(), HomeworkContentType.QUESTION);
            // 老师使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            // 所有题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            // 课时信息
            Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionStoneDataIdMap.keySet());

            for (Map.Entry<String, List<String>> entry : sectionStoneDataIdMap.entrySet()) {
                Map<String, Object> resultMap = new LinkedHashMap<>();
                // 课时信息
                resultMap.put("id", entry.getKey());
                NewBookCatalog newBookCatalog;
                String lessonId = "";
                if (MapUtils.isNotEmpty(newBookCatalogMap)) {
                    newBookCatalog = newBookCatalogMap.get(entry.getKey());
                    if ( newBookCatalog != null) {
                        resultMap.put("sectionName", newBookCatalog.getName());
                        lessonId = newBookCatalog.getParentId();
                    } else {
                        resultMap.put("sectionName", "");
                        lessonId = "";
                    }
                }

                //教材信息
                EmbedBook book = new EmbedBook();
                book.setBookId(bookId);
                book.setUnitId(unitId);
                book.setSectionId(entry.getKey());
                book.setLessonId(lessonId);

                //题包内容
                LinkedList<Map<String, Object>> resultList = new LinkedList<>();
                for (String stoneId : entry.getValue()) {
                    StoneBufferedData stoneBufferedData = stoneBufferedDataMap.get(stoneId);
                    if (stoneBufferedData.getWordsPractice() == null) {
                        continue;
                    }
                    WordsPractice wordsPractice = stoneBufferedData.getWordsPractice();
                    if (wordsPractice == null) {
                        continue;
                    }

                    WordsPracticeBO wordsPracticeBO = new WordsPracticeBO();
                    // 字词训练模块
                    if (wordsPractice.getWordExercise() != null) {
                        WordExercise wordExercise = wordsPractice.getWordExercise();
                        //此题包下的题目信息
                        Map<String, NewQuestion> questionMap = new LinkedHashMap<>();
                        wordExercise.getQuestionIds()
                                .forEach(o -> {
                                    if (allQuestionMap.get(o) != null) {
                                        questionMap.put(o, allQuestionMap.get(o));
                                    }
                                });
                        //组装题目信息
                        List<Map<String, Object>> questionList = questionMap.values().stream()
                                .map(q -> {
                                    Map<String, Object> map = NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book);
                                    boolean hintIntervention = false;
                                    if (CollectionUtils.isNotEmpty(q.getIntervene())) {
                                        hintIntervention = true;
                                    } else {
                                        for (int index = 0; index <  q.getContent().getSubContents().size(); index++) {
                                            NewQuestionsSubContents subContents = q.getContent().getSubContents().get(index);
                                            if (subContents != null && CollectionUtils.isNotEmpty(subContents.getIntervene())) {
                                                hintIntervention = true;
                                                break;
                                            }
                                        }
                                    }
                                    map.put("hintIntervention", hintIntervention);
                                    return map;
                                })
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(questionList)) {
                            Map<String, Object> wordExerciseMap = new LinkedHashMap<>();
                            wordExerciseMap.put("id", wordExercise.getUuid());
                            wordExerciseMap.put("questionBoxType", WordTeachModuleType.WORDEXERCISE);
                            wordExerciseMap.put("questionBoxTypeTitle", WordTeachModuleType.WORDEXERCISE.getName());
                            wordExerciseMap.put("intention", wordExercise.getIntention());
                            wordExerciseMap.put("questions", questionList);
                            wordExerciseMap.put("questionCount", questionList.size());
                            wordExerciseMap.put("showAssigned", teacherAssignmentRecord != null && teacherAssignmentRecord.getPackageInfo().getOrDefault(stoneId + "-" + WordTeachModuleType.WORDEXERCISE, 0) > 0);
                            wordExerciseMap.put("seconds", calculateQuestionSecond(questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum()));
                            wordsPracticeBO.setWordExerciseMap(wordExerciseMap);
                        }
                    }
                    // 图文入韵模块
                    if (wordsPractice.getImageText() != null) {
                        ImageText imageText = wordsPractice.getImageText();
                        List<ImageTextRhyme> imageTextRhymes = imageText.getImageTextRhymes();
                        LinkedList<Map<String, Object>> imageTextRhymeList = new LinkedList<>();
                        for (ImageTextRhyme imageTextRhyme : imageTextRhymes) {
                            //此题包下的题目信息
                            Map<String, NewQuestion> questionMap = new LinkedHashMap<>();
                            imageTextRhyme.getQuestionIds()
                                    .forEach(o -> {
                                        if (allQuestionMap.get(o) != null && allQuestionMap.get(o).getContentTypeId() == 1010017) {
                                            questionMap.put(o, allQuestionMap.get(o));
                                        }
                                    });
                            //组装题目信息
                            List<Map<String, Object>> questionList = questionMap.values().stream()
                                    .map(q -> NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book))
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(questionList)) {
                                Map<String, Object> imageTextRhymeMap = new HashMap<>();
                                imageTextRhymeMap.put("id", imageTextRhyme.getUuid());
                                imageTextRhymeMap.put("questionBoxType", WordTeachModuleType.IMAGETEXTRHYME);
                                imageTextRhymeMap.put("title", imageTextRhyme.getTitle());
                                imageTextRhymeMap.put("imageUrl", imageTextRhyme.getImageUrl() != null ? imageTextRhyme.getImageUrl() : NewHomeworkConstants.WORD_TEACH_IMAGE_TEXT_RHYME_DEFAULT_IMG);
                                imageTextRhymeMap.put("questions", questionList);
                                imageTextRhymeMap.put("seconds", questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum());
                                imageTextRhymeMap.put("showAssigned", teacherAssignmentRecord != null && teacherAssignmentRecord.getPackageInfo().getOrDefault(stoneId + "-" + WordTeachModuleType.IMAGETEXTRHYME, 0) > 0);
                                imageTextRhymeList.add(imageTextRhymeMap);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(imageTextRhymeList)) {
                            Map<String, Object> imageTextMap = new HashMap<>();
                            imageTextMap.put("id", imageText.getUuid());
                            imageTextMap.put("questionBoxType", WordTeachModuleType.IMAGETEXTRHYME);
                            imageTextMap.put("questionBoxTypeTitle", WordTeachModuleType.IMAGETEXTRHYME.getName());
                            imageTextMap.put("imageTextRhymeList", imageTextRhymeList);
                            imageTextMap.put("questionCount", imageTextRhymeList.size());
                            imageTextMap.put("doUrl", UrlUtils.buildUrlQuery("/exam/flash/imagetextrhyme/view" + Constants.AntiHijackExt,
                                    MiscUtils.m("stoneDataId", stoneId, "wordTeachModuleType", WordTeachModuleType.IMAGETEXTRHYME)));
                            imageTextMap.put("seconds", calculateQuestionSecond(imageTextRhymeList.stream().mapToInt(q -> SafeConverter.toInt(q.get("seconds"))).sum()));
                            // 题包是否全部布置过
                            boolean showAssigned = true;
                            for (Map<String, Object> imageTextRhyme : imageTextRhymeList) {
                                boolean assigned = SafeConverter.toBoolean(imageTextRhyme.get("showAssigned"));
                                if (!assigned) {
                                    showAssigned = false;
                                    break;
                                }
                            }
                            imageTextMap.put("showAssigned", showAssigned);
                            wordsPracticeBO.setImageTextMap(imageTextMap);
                        }
                    }
                    // 汉字文化模块
                    if (wordsPractice.getChineseCharacterCulture() != null) {
                        ChineseCharacterCulture chineseCharacterCulture = wordsPractice.getChineseCharacterCulture();
                        if (CollectionUtils.isNotEmpty(chineseCharacterCulture.getCourseIds())) {
                            //课程信息
                            Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = new HashMap<>();
                            chineseCharacterCulture.getCourseIds().forEach(c -> {
                                IntelDiagnosisCourse intelDiagnosisCourse = allIntelDiagnosisCourseMap.get(c);
                                if (intelDiagnosisCourse != null) {
                                    intelDiagnosisCourseMap.put(c, intelDiagnosisCourse);
                                }
                            });

                            Map<String, Object> chineseCharacterCultureMap = new LinkedHashMap<>();
                            if (MapUtils.isNotEmpty(intelDiagnosisCourseMap)) {
                                chineseCharacterCultureMap.put("id", chineseCharacterCulture.getUuid());
                                chineseCharacterCultureMap.put("questionBoxType", WordTeachModuleType.CHINESECHARACTERCULTURE);
                                chineseCharacterCultureMap.put("questionBoxTypeTitle", WordTeachModuleType.CHINESECHARACTERCULTURE.getName());
                                chineseCharacterCultureMap.put("courses", intelDiagnosisCourseMap.values());
                                chineseCharacterCultureMap.put("questionCount", intelDiagnosisCourseMap.values().size());
                                chineseCharacterCultureMap.put("showAssigned", teacherAssignmentRecord != null && teacherAssignmentRecord.getPackageInfo().getOrDefault(stoneId + "-" + WordTeachModuleType.CHINESECHARACTERCULTURE, 0) > 0);
                                chineseCharacterCultureMap.put("seconds", calculateQuestionSecond(intelDiagnosisCourseMap.values().stream().mapToInt(intelDiagnosisCourse -> SafeConverter.toInt(intelDiagnosisCourse.getSeconds())).sum()));
                                wordsPracticeBO.setChineseCharacterCultureMap(chineseCharacterCultureMap);
                            }
                        }
                    }

                    if (wordsPracticeBO.getWordExerciseMap() == null && wordsPracticeBO.getImageTextMap() == null && wordsPracticeBO.getChineseCharacterCultureMap() == null) {
                        continue;
                    }

                    Map<String, Object> hugePakMap = new LinkedHashMap<>();
                    hugePakMap.put("stoneDataId", stoneId);
                    hugePakMap.put("stoneDataTitle", stoneBufferedData.getCustomName());
                    hugePakMap.put("wordsPractice", wordsPracticeBO);
                    resultList.add(hugePakMap);
                }
                resultMap.put("stoneData", resultList);
                resultMap.put("book", book);
                content.add(resultMap);
            }
        } catch (Exception e) {
            logger.error("Failed to load NewHomeworkWordTeachAndPracticeContentLoader, mapper:{}", mapper, e);
        }
        return content;
    }

    private int calculateQuestionSecond(int second) {
        int min = new BigDecimal(second).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue();
        return min * 60;
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> contentList = loadContent(mapper);
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        if (CollectionUtils.isNotEmpty(contentList)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "packages", contentList
            );
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {

        Map<String, List<Map<String, Set<WordTeachModuleType>>>> sectionMap = new LinkedHashMap<>();

        if (CollectionUtils.isNotEmpty(contentIdList)) {
            for (String contentId : contentIdList) {
                String[] splitContentIds = StringUtils.split(contentId, "|");
                if (splitContentIds.length == 3) {
                    String sectionId = splitContentIds[0];
                    String stoneDataId = splitContentIds[1];
                    List<String> practiceTypes = StringUtils.toList(splitContentIds[2], String.class);
                    if (CollectionUtils.isNotEmpty(practiceTypes)) {
                        Set<WordTeachModuleType> practiceTypeSet = practiceTypes.stream()
                                .map(WordTeachModuleType::of)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                        if (CollectionUtils.isNotEmpty(practiceTypeSet)) {
                            List<Map<String, Set<WordTeachModuleType>>> wordTeachModuleTypeList = new ArrayList<>();
                            Map<String, Set<WordTeachModuleType>> wordTeachModuleTypeMap = new LinkedHashMap<>();
                            if (CollectionUtils.isEmpty(sectionMap.get(sectionId))) {
                                wordTeachModuleTypeMap.put(stoneDataId, practiceTypeSet);
                                wordTeachModuleTypeList.add(wordTeachModuleTypeMap);
                            } else {
                                wordTeachModuleTypeList = sectionMap.get(sectionId);
                                wordTeachModuleTypeMap.put(stoneDataId, practiceTypeSet);
                                wordTeachModuleTypeList.add(wordTeachModuleTypeMap);
                            }
                            sectionMap.put(sectionId, wordTeachModuleTypeList);
                        }
                    }
                }
            }
        }

        // 课时信息
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionMap.keySet());

        // 获取题包
        Set<String> stoneDataIds = new HashSet<>();
        for (List<Map<String, Set<WordTeachModuleType>>> list : sectionMap.values()) {
            for (Map<String, Set<WordTeachModuleType>> map : list) {
                stoneDataIds.addAll(map.keySet());
            }
        }
        Map<String, StoneBufferedData> stoneBufferedDataMap = stoneDataLoaderClient.getStoneBufferedDataList(stoneDataIds)
                .stream()
                .collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));

        // 题目信息
        Set<String> allQuestionIds = new HashSet<>();
        // 课程信息
        Set<String> allCourseIds = new HashSet<>();
        stoneBufferedDataMap.values().forEach(s -> {
            if (s.getWordsPractice() != null) {
                WordsPractice wordsPractice = s.getWordsPractice();
                if (wordsPractice.getWordExercise() != null && CollectionUtils.isNotEmpty(wordsPractice.getWordExercise().getQuestionIds())) {
                    allQuestionIds.addAll(wordsPractice.getWordExercise().getQuestionIds());
                }
                if (wordsPractice.getImageText() != null) {
                    wordsPractice.getImageText().getImageTextRhymes().forEach(q -> {
                        if (CollectionUtils.isNotEmpty(q.getQuestionIds())) {
                            allQuestionIds.addAll(q.getQuestionIds());
                        }
                    });
                }
                if (wordsPractice.getChineseCharacterCulture() != null && CollectionUtils.isNotEmpty(wordsPractice.getChineseCharacterCulture().getCourseIds())) {
                    allCourseIds.addAll(wordsPractice.getChineseCharacterCulture().getCourseIds());
                }
            }
        });
        Map<String, IntelDiagnosisCourse> allIntelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(allCourseIds);
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionIds)
                .stream()
                .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));

        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Map.Entry<String, List<Map<String, Set<WordTeachModuleType>>>> entry : sectionMap.entrySet()) {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("id", entry.getKey());
            resultMap.put("sectionName", newBookCatalogMap.get(entry.getKey()).getName());

            List<Map<String, Object>> hugePakList = Collections.emptyList();
            for (Map<String, Set<WordTeachModuleType>> map : entry.getValue()) {
                for (Map.Entry<String, Set<WordTeachModuleType>> setEntry : map.entrySet()) {
                    hugePakList = new ArrayList<>();
                    String stoneId = setEntry.getKey();
                    Set<WordTeachModuleType> wordTeachModuleTypes = setEntry.getValue();
                    StoneBufferedData stoneBufferedData = stoneBufferedDataMap.get(stoneId);
                    WordsPractice wordsPractice = stoneBufferedData.getWordsPractice();

                    Map<String, Object> hugePakMap = new LinkedHashMap<>();
                    WordsPracticeBO wordsPracticeBO = new WordsPracticeBO();
                    // 字词训练包
                    if (wordTeachModuleTypes.contains(WordTeachModuleType.WORDEXERCISE)) {
                        if (wordsPractice.getWordExercise() != null) {
                            WordExercise wordExercise = wordsPractice.getWordExercise();
                            //此题包下的题目信息
                            Map<String, NewQuestion> questionMap = new HashMap<>();
                            wordExercise.getQuestionIds()
                                    .forEach(o -> {
                                        if (allQuestionMap.get(o) != null) {
                                            questionMap.put(o, allQuestionMap.get(o));
                                        }
                                    });
                            Map<String, Object> wordExerciseMap = new LinkedHashMap<>();
                            wordExerciseMap.put("id", wordExercise.getUuid());
                            wordExerciseMap.put("questionBoxType", WordTeachModuleType.WORDEXERCISE);
                            wordExerciseMap.put("questionBoxTypeTitle", WordTeachModuleType.WORDEXERCISE.getName());
                            wordExerciseMap.put("questionCount", wordExercise.getQuestionIds().size());
                            wordExerciseMap.put("seconds", questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum());
                            wordsPracticeBO.setWordExerciseMap(wordExerciseMap);
                        }
                    }
                    // 图文入韵包
                    if (wordTeachModuleTypes.contains(WordTeachModuleType.IMAGETEXTRHYME)) {
                        ImageText imageText = wordsPractice.getImageText();
                        int questionCount = 0;
                        int seconds = 0;
                        for (ImageTextRhyme imageTextRhyme : imageText.getImageTextRhymes()) {
                            //此题包下的题目信息
                            Map<String, NewQuestion> questionMap = new HashMap<>();
                            imageTextRhyme.getQuestionIds()
                                    .forEach(o -> {
                                        if (allQuestionMap.get(o) != null && allQuestionMap.get(o).getContentTypeId() == 1010017) {
                                            questionMap.put(o, allQuestionMap.get(o));
                                        }
                                    });
                            seconds += questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum();
                            questionCount += imageTextRhyme.getQuestionIds().size();
                        }

                        Map<String, Object> imageTextMap = new HashMap<>();
                        imageTextMap.put("id", imageText.getUuid());
                        imageTextMap.put("questionBoxType", WordTeachModuleType.IMAGETEXTRHYME);
                        imageTextMap.put("questionBoxTypeTitle", WordTeachModuleType.IMAGETEXTRHYME.getName());
                        imageTextMap.put("questionCount", questionCount);
                        imageTextMap.put("seconds", seconds);
                        wordsPracticeBO.setImageTextMap(imageTextMap);
                    }
                    // 汉字文化包
                    if (wordTeachModuleTypes.contains(WordTeachModuleType.CHINESECHARACTERCULTURE)) {
                        ChineseCharacterCulture chineseCharacterCulture = wordsPractice.getChineseCharacterCulture();
                        if (CollectionUtils.isNotEmpty(chineseCharacterCulture.getCourseIds())) {
                            Map<String, Object> chineseCharacterCultureMap = new LinkedHashMap<>();
                            //课程信息
                            Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = new HashMap<>();
                            chineseCharacterCulture.getCourseIds().forEach(c -> {
                                IntelDiagnosisCourse intelDiagnosisCourse = allIntelDiagnosisCourseMap.get(c);
                                if (intelDiagnosisCourse != null) {
                                    intelDiagnosisCourseMap.put(c, intelDiagnosisCourse);
                                }
                            });
                            chineseCharacterCultureMap.put("id", chineseCharacterCulture.getUuid());
                            chineseCharacterCultureMap.put("questionBoxType", WordTeachModuleType.CHINESECHARACTERCULTURE);
                            chineseCharacterCultureMap.put("questionBoxTypeTitle", WordTeachModuleType.CHINESECHARACTERCULTURE.getName());
                            chineseCharacterCultureMap.put("questionCount", chineseCharacterCulture.getCourseIds().size());
                            chineseCharacterCultureMap.put("seconds", intelDiagnosisCourseMap.values().stream().mapToDouble(intelDiagnosisCourse -> SafeConverter.toDouble(intelDiagnosisCourse.getSeconds())).sum());
                            wordsPracticeBO.setChineseCharacterCultureMap(chineseCharacterCultureMap);
                        }
                    }
                    hugePakMap.put("stoneDataId", stoneBufferedData.getId());
                    hugePakMap.put("stoneDataTitle", stoneBufferedData.getCustomName());
                    hugePakMap.put("wordsPractice", wordsPracticeBO);
                    hugePakList.add(hugePakMap);
                }
            }
            resultMap.put("wordsPractice", hugePakList);
            resultList.add(resultMap);
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "stoneDatas", resultList);
    }
}
