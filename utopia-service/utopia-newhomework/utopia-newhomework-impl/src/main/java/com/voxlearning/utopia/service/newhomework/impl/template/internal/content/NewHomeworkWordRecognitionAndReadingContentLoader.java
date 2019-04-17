package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 生字认读Content
 * @author: Mr_VanGogh
 * @date: 2018/7/14 下午5:39
 */
@Named
public class NewHomeworkWordRecognitionAndReadingContentLoader extends NewHomeworkContentLoaderTemplate {


    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_RECOGNITION_AND_READING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        if (Subject.CHINESE != mapper.getTeacher().getSubject()) {
            return content;
        }
        if (CollectionUtils.isEmpty(mapper.getSectionIds())) {
            return content;
        }
        try {
            List<Map<String, Object>> wordContent = new ArrayList<>();
            Set<String> relatedSectionIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
                for (Map<String, Object> configContent : objectiveConfig.getContents()) {
                    int type = SafeConverter.toInt(configContent.get("type"));
                    String relatedCatalogId = SafeConverter.toString(configContent.get("related_catalog_id"));
                    // 生字认读关联的sectionId不允许为空
                    if (StringUtils.isNotBlank(relatedCatalogId)
                            && mapper.getSectionIds().contains(relatedCatalogId)
                            && type == ObjectiveConfig.QUESTION_ID) {
                        relatedSectionIds.add(relatedCatalogId);
                        wordContent.add(configContent);
                    }
                }
            }
            // 课时信息
            Map<String, NewBookCatalog> relatedSectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(relatedSectionIds);
            Set<String> lessonIds = relatedSectionMap.values()
                    .stream()
                    .filter(section -> section != null && section.getParentId() != null)
                    .map(NewBookCatalog::getParentId)
                    .collect(Collectors.toSet());
            Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
            // 题目信息
            List<String> allQuestionIds = new ArrayList<>();
            wordContent.forEach(p -> allQuestionIds.addAll((List<String>) p.get("question_ids")));
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
            List<Map<String, Object>> sectionList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(wordContent)) {
                wordContent.forEach(o -> {
                    Map<String, Object> sectionMap = new LinkedHashMap<>();
                    List<Map<String, Object>> questionList = new ArrayList<>();
                    List<String> questionIdList = (List<String>) o.get("question_ids");
                    questionIdList.forEach(r -> {
                        NewQuestion question = allQuestionMap.get(r);
                        if (question != null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", question.getId());
                            map.put("seconds", question.getSeconds());
                            map.put("submitWay", question.getSubmitWays());
                            questionList.add(map);
                        }
                    });
                    if (CollectionUtils.isEmpty(questionList)) {
                        return;
                    }

                    String sectionId = SafeConverter.toString(o.get("related_catalog_id"));
                    NewBookCatalog section = relatedSectionMap.get(sectionId);
                    String lessonId = null;
                    String lessonName = null;
                    if (section != null && section.getParentId() != null) {
                        lessonId = section.getParentId();
                        if (lessonMap.get(lessonId) != null) {
                            lessonName = lessonMap.get(lessonId).getName();
                        }
                    }
                    sectionMap.put("questionBoxId", SafeConverter.toString(o.get("id")));
                    sectionMap.put("lessonId", lessonId);
                    sectionMap.put("lessonName", lessonName);
                    sectionMap.put("questions", questionList);
                    sectionMap.put("questionNum", questionList.size());
                    sectionMap.put("seconds", questionList.stream().filter(Objects::nonNull).mapToInt(q -> SafeConverter.toInt(q.get("seconds"))).sum());
                    EmbedBook embedBook = new EmbedBook();
                    embedBook.setBookId(mapper.getBookId());
                    embedBook.setUnitId(mapper.getUnitId());
                    embedBook.setLessonId(lessonId);
                    embedBook.setSectionId(sectionId);
                    sectionMap.put("book", embedBook);
                    sectionList.add(sectionMap);
                });
            }
            if (CollectionUtils.isNotEmpty(sectionList)) {
                content.add(MapUtils.m(
                        "type", "package",
                        "packages", sectionList
                ));
            }
        } catch (Exception ex) {
            logger.error("loadContent WORD_RECOGNITION_AND_READING error:", ex);
        }
        return content;
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> contentList = loadContent(mapper);
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
        List<Map<String, Object>> contentList = new ArrayList<>();
        //contentIdList格式：lessonId|questionNum|questionBoxId
        try {
            if (CollectionUtils.isNotEmpty(contentIdList)) {
                List<String> lessonIds = new ArrayList<>();
                contentIdList.forEach(contentId -> {
                    if (StringUtils.isNotBlank(contentId)) {
                        String[] splitContentIds = StringUtils.split(contentId, "|");
                        if (splitContentIds.length == 3) {
                            String lessonId = splitContentIds[0];
                            lessonIds.add(lessonId);
                        }
                    }
                });
                Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
                contentIdList.forEach(contentId -> {
                    if (StringUtils.isNotBlank(contentId)) {
                        String[] splitContentIds = StringUtils.split(contentId, "|");
                        if (splitContentIds.length == 3) {
                            String lessonId = splitContentIds[0];
                            Map<String, Object> contentMap = new LinkedHashMap<>();
                            contentMap.put("lessonId", lessonId);
                            contentMap.put("lessonName", lessonMap.get(lessonId) != null ? lessonMap.get(lessonId).getName() : null);
                            contentMap.put("questionNum", splitContentIds[1]);
                            contentMap.put("questionBoxId", splitContentIds[2]);
                            contentList.add(contentMap);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("previewContent WORD_RECOGNITION_AND_READING error:", e);
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "packages", contentList);
    }
}
