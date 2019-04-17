package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 阅读
 *
 * @author guoqiang.li
 * @since 2016/12/28
 */
@Named
public class NewHomeworkChineseReadingContentLoader extends NewHomeworkContentLoaderTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.CHINESE_READING;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        Teacher teacher = mapper.getTeacher();
        if (Subject.CHINESE != teacher.getSubject()) {
            return content;
        }
        if (CollectionUtils.isEmpty(mapper.getSectionIds())) {
            return content;
        }
        String defaultSectionId = mapper.getSectionIds().get(0);
        List<Map<String, Object>> questionContentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                String relatedCatalogId = SafeConverter.toString(configContent.get("related_catalog_id"));
                if ((StringUtils.isBlank(relatedCatalogId) || mapper.getSectionIds().contains(relatedCatalogId))
                        && type == ObjectiveConfig.QUESTION_ID) {
                    questionContentList.add(configContent);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(questionContentList)) {
            // 题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            // 老师使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), mapper.getBookId());
            processQuestionContent(questionContentList, contentTypeMap, teacherAssignmentRecord, content, defaultSectionId, mapper.getUnitId(), mapper.getBookId());
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private void processQuestionContent(List<Map<String, Object>> questionContentList, Map<Integer, NewContentType> contentTypeMap,
                                        TeacherAssignmentRecord teacherAssignmentRecord, List<Map<String, Object>> content,
                                        String defaultSectionId, String unitId, String bookId) {
        Set<String> allQuestionDocIdSet = questionContentList.stream()
                .map(map -> (List<String>) map.get("question_ids"))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIdSet)
                .stream()
                .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
        Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                .stream()
                .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
        // 总的使用次数
        Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(Subject.CHINESE, allQuestionMap.keySet(), HomeworkContentType.QUESTION);
        List<Map<String, Object>> questionList = new ArrayList<>();
        questionContentList.forEach(contentMap -> {
            String relatedCatalogId = SafeConverter.toString(contentMap.get("related_catalog_id"));
            String sectionId = StringUtils.isNotBlank(relatedCatalogId) ? relatedCatalogId : defaultSectionId;
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            book.setSectionId(sectionId);
            List<String> questionDocIdSet = (List<String>) contentMap.get("question_ids");
            List<NewQuestion> newQuestionList = questionDocIdSet.stream()
                    .filter(docIdQuestionMap::containsKey)
                    .map(docIdQuestionMap::get)
                    .collect(Collectors.toList());
            Map<String, Object> readingTypeMap = (Map<String, Object>) contentMap.get("reading_type");
            if (CollectionUtils.isNotEmpty(newQuestionList)) {
                List<Map<String, Object>> questionMapperList = newQuestionList.stream()
                        .filter(NewQuestion::supportOnlineAnswer)
                        .filter(q -> !Objects.equals(q.getNotFitMobile(), 1))
                        .map(q -> {
                            Map<String, Object> map = NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book);
                            if (MapUtils.isNotEmpty(readingTypeMap) && readingTypeMap.get(q.getDocId()) != null) {
                                int readingType = SafeConverter.toInt(readingTypeMap.get(q.getDocId()));
                                map.put("readingTypeId", readingType);
                                switch (readingType) {
                                    case 1:
                                        map.put("readingType", "原文");
                                        break;
                                    case 2:
                                        map.put("readingType", "类文");
                                        break;
                                    case 3:
                                        map.put("readingType", "拓展");
                                        break;
                                    default:
                                        map.put("readingType", null);
                                        break;
                                }
                            } else {
                                map.put("readingTypeId", 4);
                                map.put("readingType", null);
                            }
                            return map;
                        })
                        .collect(Collectors.toList());
                questionList.addAll(questionMapperList);
            }
        });
        content.add(MapUtils.m("type", "question", "questions", questionList));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> contentList = loadContent(mapper);
        if (CollectionUtils.isNotEmpty(contentList)) {
            List<Map<String, Object>> questions = new ArrayList<>();
            Set<String> questionIdSet = new HashSet<>();
            contentList.forEach(o -> {
                List<Map<String, Object>> question = (List<Map<String, Object>>) o.get("questions");
                question.forEach(p -> {
                    String questionId = (String) p.get("id");
                    questionIdSet.add(questionId);
                });
            });
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIdSet);
            for (Map<String, Object> questionsMapper : contentList) {
                List<Map<String, Object>> questionList = (List<Map<String, Object>>) questionsMapper.get("questions");
                for (Map<String, Object> questionMap : questionList) {
                    String questionId = SafeConverter.toString(questionMap.get("id"));
                    NewQuestion newQuestion = allQuestionMap.get(questionId);

                    Map<String, String> questionOthers = (Map<String, String>) newQuestion.getOthers().get("chinese_reading");
                    if (MapUtils.isNotEmpty(questionOthers)) {
                        questionMap.put("readingTitle", SafeConverter.toString(questionOthers.get("reading_title")));
                        questionMap.put("readingIntroduction", SafeConverter.toString(questionOthers.get("reading_introduction")));
                    } else {
                        questionMap.put("readingTitle", "");
                        questionMap.put("readingIntroduction", "");
                    }
                    questionMap.put("readingWordCount", newQuestion.getOthers().get("reading_word_count") != null ? newQuestion.getOthers().get("reading_word_count") : 0);
                    questionMap.put("questionCount", newQuestion.getContent().getSubContents().size());
                }
                questions.addAll(questionList);
            }
            //排序规则:依次排列课内、类文、拓展
            questions.sort(Comparator.comparingInt(a -> SafeConverter.toInt(a.get("readingTypeId"))));
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "questions", questions
            );
        }
        return Collections.emptyMap();
    }
}
