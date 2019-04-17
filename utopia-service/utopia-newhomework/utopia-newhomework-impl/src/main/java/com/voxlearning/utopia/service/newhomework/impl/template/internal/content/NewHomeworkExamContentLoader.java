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
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
@Named
public class NewHomeworkExamContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject private AthenaHomeworkLoaderClient athenaHomeworkLoaderClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.EXAM;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();

        TeacherDetail teacher = mapper.getTeacher();
        List<String> sectionIds = mapper.getSectionIds();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        if (Subject.MATH != teacher.getSubject()) {
            return content;
        }
        if (CollectionUtils.isEmpty(sectionIds)) {
            return content;
        }
        String defaultSectionId = sectionIds.get(0);

        // 题包
        List<Map<String, Object>> packageContentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                String relatedCatalogId = SafeConverter.toString(configContent.get("related_catalog_id"));
                if ((StringUtils.isBlank(relatedCatalogId) || sectionIds.contains(relatedCatalogId))
                        && type == ObjectiveConfig.QUESTION_NAME_ID) {
                    packageContentList.add(configContent);
                }
            }
        }
        // 更多
        Map<String, List<String>> mathMoreQuestions = null;
        try {
            mathMoreQuestions = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                    .loadMathSimilarQuestions(sectionIds);
        } catch (Exception e) {
            logger.error("NewHomeworkExamContentLoader call athena error:", e);
        }
        // 题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        // 老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        // 试题类型白名单
        List<Integer> contentTypeList = QuestionConstants.homeworkMathIncludeContentTypeIds;
        if (CollectionUtils.isNotEmpty(packageContentList)) {
            processMathPackageContent(packageContentList, contentTypeMap, teacherAssignmentRecord, content, defaultSectionId, bookId, unitId);
        }
        if (MapUtils.isNotEmpty(mathMoreQuestions)) {
            processMathMoreQuestionContent(content, mathMoreQuestions, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList);
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private void processMathPackageContent(List<Map<String, Object>> packageContentList, Map<Integer, NewContentType> contentTypeMap,
                                           TeacherAssignmentRecord teacherAssignmentRecord, List<Map<String, Object>> content,
                                           String defaultSectionId, String bookId, String unitId) {

        Set<String> allQuestionDocIdSet = packageContentList.stream()
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
        Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                .loadTotalAssignmentRecordByContentType(Subject.MATH, allQuestionMap.keySet(), HomeworkContentType.QUESTION);
        List<Map<String, Object>> packageList = packageContentList.stream()
                .filter(map -> CollectionUtils.isNotEmpty((List<String>) map.get("question_ids")))
                .map(map -> {
                    String relatedCatalogId = SafeConverter.toString(map.get("related_catalog_id"));
                    String sectionId = StringUtils.isNotBlank(relatedCatalogId) ? relatedCatalogId : defaultSectionId;
                    EmbedBook book = new EmbedBook();
                    book.setBookId(bookId);
                    book.setUnitId(unitId);
                    book.setSectionId(sectionId);

                    String id = SafeConverter.toString(map.get("id"));
                    String name = SafeConverter.toString(map.get("name"));
                    int difficulty = SafeConverter.toInt(map.get("difficulty"));
                    ObjectiveConfig.UsageType usageType = ObjectiveConfig.UsageType.of(SafeConverter.toString(map.get("usage_type")));
                    List<Map<String, Object>> questionMapperList = new ArrayList<>();
                    List<String> questionDocIds = (List<String>) map.get("question_ids");
                    int seconds = 0;
                    boolean showAssigned = true;
                    for (String questionDocId : questionDocIds) {
                        NewQuestion newQuestion = docIdQuestionMap.get(questionDocId);
                        if (newQuestion != null) {
                            seconds += newQuestion.getSeconds();
                            questionMapperList.add(NewHomeworkContentDecorator.decorateNewQuestion(
                                    newQuestion,
                                    contentTypeMap,
                                    totalAssignmentRecordMap,
                                    teacherAssignmentRecord,
                                    book));
                            if (teacherAssignmentRecord == null ||
                                    teacherAssignmentRecord.getQuestionInfo().getOrDefault(newQuestion.getDocId(), 0) == 0) {
                                showAssigned = false;
                            }
                        }
                    }
                    return MapUtils.m(
                            "id", id,
                            "name", name,
                            "assignTimes", 0,
                            "teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getPackageInfo().getOrDefault(id, 0) : 0,
                            "showAssigned", showAssigned,
                            "seconds", seconds,
                            "difficultyName", QuestionConstants.newDifficultyMap.get(difficulty),
                            "questions", questionMapperList,
                            "usageName", usageType == null ? null : usageType.getDescription(),
                            "usageColor", usageType == null ? null : usageType.getColor());
                })
                .collect(Collectors.toList());
        content.add(MapUtils.m("type", "package", "packages", packageList));
    }
}
