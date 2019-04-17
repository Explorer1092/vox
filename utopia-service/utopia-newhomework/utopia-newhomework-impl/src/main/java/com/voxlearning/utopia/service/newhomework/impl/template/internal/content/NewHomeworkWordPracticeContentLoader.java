package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/2/1
 */

@Named
public class NewHomeworkWordPracticeContentLoader extends NewHomeworkContentLoaderTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_PRACTICE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        TeacherDetail teacher = mapper.getTeacher();
        if (Subject.CHINESE != teacher.getSubject()) {
            return null;
        }
        List<Map<String, Object>> content = new ArrayList<>();

        // 取按题id配的内容
        List<Map<String, Object>> questionIdContent = teachingObjectiveLoaderClient.loadContentByCatalogIdsAndType(mapper.getSectionIds(), getObjectiveConfigType(), Collections.singleton(ObjectiveConfig.QUESTION_ID),
                teacher.getSubject().getId());

        // 取按知识点配的内容
        List<Map<String, Object>> questionStructureContent = teachingObjectiveLoaderClient.loadContentByCatalogIdsAndType(mapper.getSectionIds(), getObjectiveConfigType(), Collections.singleton(ObjectiveConfig.QUESTION_STRUCTURE),
                teacher.getSubject().getId());

        // 获取语文的题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadChineseQuestionContentTypeAsMap();

        // 老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), mapper.getBookId());

        Set<String> questionDocIdSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(questionIdContent)) {
            questionIdContent.forEach(questionIdMap -> {
                // 这里的id是docId
                List<String> questionIds = conversionService.convert(questionIdMap.get("question_ids"), List.class);
                if (CollectionUtils.isNotEmpty(questionIds)) {
                    questionDocIdSet.addAll(questionIds);
                }
            });
        }

        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(questionDocIdSet)
                .stream()
                .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
        if (CollectionUtils.isNotEmpty(questionStructureContent)) {
            Set<String> kpIdSet = questionStructureContent.stream().map(kpMap -> SafeConverter.toString(kpMap.get("kp_id"))).collect(Collectors.toSet());
            Map<String, List<NewQuestion>> newQuestionMap = questionLoaderClient.loadQuestionByNewKnowledgePoints0(kpIdSet, Collections.emptyList(), true, true, true, false, true);
            newQuestionMap.values().stream().flatMap(Collection::stream).forEach(newQuestion -> allQuestionMap.put(newQuestion.getId(), newQuestion));
        }

        Set<String> tagsSet = new LinkedHashSet<>();
        Set<String> kpIdSet = new LinkedHashSet<>();
        allQuestionMap.values().forEach(newQuestion -> {
            kpIdSet.addAll(newQuestion.getKnowledgePointsNew().stream().map(NewQuestionKnowledgePoint::getId).collect(Collectors.toSet()));
            List<List<String>> tagsDesc = newQuestion.getContent().getSubContents().stream().map(NewQuestionsSubContents::getTagsDesc).collect(Collectors.toList());
            tagsDesc.forEach(tagsSet::addAll);
        });
        Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(kpIdSet);

        // 过滤父节点不为'字'的知识点
        Set<String> parentKpIdSet = newKnowledgePointMap.values().stream()
                .filter(kp -> kp != null && kp.getParentId() != null)
                .map(NewKnowledgePoint::getParentId)
                .collect(Collectors.toSet());
        Map<String, NewKnowledgePoint> parentNewKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(parentKpIdSet);

        Set<String> wordsSet = newKnowledgePointMap.values().stream()
                .filter(kp -> kp != null && kp.getParentId() != null
                        && parentNewKnowledgePointMap.get(kp.getParentId()) != null
                        && "字".equals(parentNewKnowledgePointMap.get(kp.getParentId()).getName()))
                .map(NewKnowledgePoint::getName)
                .collect(Collectors.toSet());

        List<Map<String, Object>> wordPracticeQuestionContent = new ArrayList<>();
        // 用来过滤重复的题，相同的题只取第一次出现的
        Set<String> showQuestionIdSet = new LinkedHashSet<>();
        questionIdContent.forEach(questionIdMap -> {
            List<String> questionIds = conversionService.convert(questionIdMap.get("question_ids"), List.class);
            EmbedBook book = conversionService.convert(questionIdMap.get("book"), EmbedBook.class);
            if (CollectionUtils.isNotEmpty(questionIds)) {
                List<NewQuestion> newQuestions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                if (CollectionUtils.isNotEmpty(newQuestions)) {
                    Set<String> questionIdSet = newQuestions.stream().map(NewQuestion::getId).collect(Collectors.toSet());
                    // 总的使用次数
                    Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), questionIdSet, HomeworkContentType.QUESTION);
                    List<Map<String, Object>> questionMapperList = newQuestions.stream()
                            .filter(q -> q.supportOnlineAnswer() && !Objects.equals(q.getNotFitMobile(), 1) && showQuestionIdSet.add(q.getId()))
                            .map(question -> getQuestionMap(question, contentTypeMap, book, newKnowledgePointMap, teacherAssignmentRecord, totalAssignmentRecordMap))
                            .collect(Collectors.toList());
                    wordPracticeQuestionContent.addAll(questionMapperList);
                }
            }
        });

        if (CollectionUtils.isNotEmpty(questionStructureContent)) {
            Set<String> structureKpIdSet = questionStructureContent.stream().map(kpMap -> SafeConverter.toString(kpMap.get("kp_id"))).collect(Collectors.toSet());
            Map<String, List<NewQuestion>> newQuestionMap = questionLoaderClient.loadQuestionByNewKnowledgePoints0(structureKpIdSet, Collections.emptyList(), true, true, true, false, true);
            Set<String> allQuestionIdSet = newQuestionMap.values().stream().flatMap(Collection::stream).map(NewQuestion::getId).collect(Collectors.toSet());
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionIdSet, HomeworkContentType.QUESTION);
            questionStructureContent.forEach(knowledgePointMap -> {
                String kp_id = SafeConverter.toString(knowledgePointMap.get("kp_id"));
                Integer content_type_id = conversionService.convert(knowledgePointMap.get("content_type_id"), Integer.class);
                EmbedBook book = conversionService.convert(knowledgePointMap.get("book"), EmbedBook.class);
                List<NewQuestion> newQuestions = newQuestionMap.get(kp_id);
                // 按照配置包中的题目类型来过滤通过知识点拿到的题，过滤不支持在线作答的题，过滤掉不适合移动端展示的题
                if (CollectionUtils.isNotEmpty(newQuestions)) {
                    List<Map<String, Object>> tempList = newQuestions.stream()
                            .filter(q -> q.supportOnlineAnswer() && !Objects.equals(q.getNotFitMobile(), 1)
                                    && (content_type_id == 0 || content_type_id.equals(q.getContentTypeId())) && showQuestionIdSet.add(q.getId()))
                            .map(question -> getQuestionMap(question, contentTypeMap, book, newKnowledgePointMap, teacherAssignmentRecord, totalAssignmentRecordMap))
                            .collect(Collectors.toList());
                    wordPracticeQuestionContent.addAll(tempList);
                }
            });
        }
        if (wordPracticeQuestionContent.isEmpty()) {
            processTeacherLog(teacher, getObjectiveConfigType(), mapper.getUnitId());
        }
        content.add(MiscUtils.m("type", "question", "questions", wordPracticeQuestionContent, "words", wordsSet, "tags", tagsSet));
        return content;
    }

    private Map<String, Object> getQuestionMap(NewQuestion question, Map<Integer, NewContentType> contentTypeMap, EmbedBook book,
                                               Map<String, NewKnowledgePoint> knowledgePointMap, TeacherAssignmentRecord teacherAssignmentRecord,
                                               Map<String, TotalAssignmentRecord> totalAssignmentRecordMap) {
        Set<String> words = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(question.getKnowledgePointsNew())) {
            words = question.getKnowledgePointsNew().stream().filter(kp -> knowledgePointMap.get(kp.getId()) != null)
                    .map(kp -> knowledgePointMap.get(kp.getId()).getName()).collect(Collectors.toSet());
        }
        List<List<String>> tags = question.getContent().getSubContents().stream().map(NewQuestionsSubContents::getTagsDesc).collect(Collectors.toList());
        Set<String> tagsDesc = new LinkedHashSet<>();
        tags.forEach(tagsDesc::addAll);
        return MiscUtils.m(
                "id", question.getId(),
                "questionTypeId", question.getContentTypeId(),
                "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                "difficulty", question.getDifficultyInt(),
                "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt()),
                "seconds", question.getSeconds(),
                // 该题被使用次数和该老师布置过的次数
                "assignTimes", totalAssignmentRecordMap.get(question.getId()) != null ? totalAssignmentRecordMap.get(question.getId()).getAssignTimes() : 0,
                "teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getQuestionInfo()
                        .getOrDefault(TeacherAssignmentRecord.id2DocId(question.getId()), 0) : 0,
                "upImage", question.getSubmitWays().stream().flatMap(Collection::stream).anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2)),
                "book", book,
                "submitWay", question.getSubmitWays(),
                "words", words,
                "tags", tagsDesc
        );
    }
}
