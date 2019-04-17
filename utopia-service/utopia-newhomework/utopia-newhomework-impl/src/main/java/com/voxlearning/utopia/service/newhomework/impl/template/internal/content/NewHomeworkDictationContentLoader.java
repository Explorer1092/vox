package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
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
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class NewHomeworkDictationContentLoader extends NewHomeworkContentLoaderTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DICTATION;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        String bookId = mapper.getBookId();
        String unitId = mapper.getUnitId();
        Teacher teacher = mapper.getTeacher();
        // 获取unit下的所有lesson
        List<NewBookCatalog> lessonList = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON)
                .get(unitId);
        // 获取lesson下的所有sentence
        if (CollectionUtils.isNotEmpty(lessonList)) {
            Map<String, NewBookCatalog> lessonMap = lessonList.stream()
                    .collect(Collectors.toMap(NewBookCatalog::getId, Function.identity(), (o1, o2) -> o1, LinkedHashMap::new));
            Set<String> lessonIdSet = lessonMap.keySet();
            Map<String, List<Sentence>> lessonSentenceMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIdSet);
            Set<Long> sentenceIds = lessonSentenceMap.values()
                    .stream()
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(sentence -> !sentence.getEnText().contains(" "))
                    .map(Sentence::getId)
                    .collect(Collectors.toSet());
            // 根据sentenceId获取听写题目
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadDictationQuestionsBySentenceIds(sentenceIds);
            if (MapUtils.isNotEmpty(questionMap)) {
                Map<Long, NewQuestion> sentenceQuestionMap = questionMap.values()
                        .stream()
                        .filter(q -> CollectionUtils.isNotEmpty(q.getSentenceIds()))
                        .collect(Collectors.toMap(p -> p.getSentenceIds().get(0), Function.identity(), (o1, o2) -> o1));
                Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
                Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                        .loadTotalAssignmentRecordByContentType(Subject.ENGLISH, questionMap.keySet(), HomeworkContentType.QUESTION);
                TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
                for (String lessonId : lessonIdSet) {
                    List<Sentence> lessonSentenceList = lessonSentenceMap.get(lessonId);
                    if (CollectionUtils.isNotEmpty(lessonSentenceList)) {
                        EmbedBook book = new EmbedBook();
                        book.setBookId(bookId);
                        book.setUnitId(unitId);
                        book.setLessonId(lessonId);
                        List<Map<String, Object>> questionMapperList = new ArrayList<>();
                        for (Sentence sentence : lessonSentenceList) {
                            NewQuestion newQuestion = sentenceQuestionMap.get(sentence.getId());
                            if (newQuestion != null) {
                                Map<String, Object> questionMapper = NewHomeworkContentDecorator.decorateNewQuestion(newQuestion, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book);
                                questionMapper.put("sentence", sentence.getEnText());
                                questionMapperList.add(questionMapper);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(questionMapperList)) {
                            Map<String, Object> lessonContentMapper = new HashMap<>();
                            lessonContentMapper.put("lessonId", lessonId);
                            lessonContentMapper.put("lessonName", lessonMap.get(lessonId).getAlias());
                            lessonContentMapper.put("questions", questionMapperList);
                            content.add(lessonContentMapper);
                        }
                    }
                }
            }
        }
        return content;
    }
}
