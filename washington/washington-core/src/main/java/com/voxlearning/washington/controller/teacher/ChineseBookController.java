/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.constant.ChineseSentenceType;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.LessonDat;
import com.voxlearning.utopia.service.content.api.mapper.ExLessonDat;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 14-8-28.
 */
@Controller
@RequestMapping("chinese/book")
public class ChineseBookController extends AbstractTeacherController {

    /**
     * 选择练习--应试练习
     */
    @RequestMapping(value = "lesson/{unitId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lesson(@PathVariable("unitId") Long unitId) {
        List<ExLessonDat> lessons = chineseContentLoaderClient.loadChineseUnitExLessons(unitId);
        Collection<Long> lessonIds = new LinkedHashSet<>();
        for (ExLessonDat lesson : lessons) {
            CollectionUtils.addNonNullElement(lessonIds, lesson.getId());
        }
        Map<Long, List<ChineseSentence>> sentences = chineseContentLoaderClient.loadChineseLessonSentences(lessonIds, ChineseSentenceType.WORD);
        for (ExLessonDat exLesson : lessons) {
            List<ChineseSentence> list = sentences.get(exLesson.getId());
            if (list == null) {
                list = Collections.emptyList();
            }
            for (ChineseSentence sentence : list) {
                exLesson.getPointList().add(sentence.getContent());
            }
        }
        MapMessage message = MapMessage.successMessage();
        message.add("total", lessons.size());
        message.add("rows", lessons);
        return message;
    }

    /**
     * 选择单元--显示课程数据
     */
    @RequestMapping(value = "lesson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lesson() {
        Long unitId = getRequestLong("unitId");
        List<LessonDat> lessons = chineseContentLoaderClient.loadChineseUnitLessons(unitId);
        return MapMessage.successMessage().add("rows", lessons);
    }

    /**
     * 选择练习--应试练习
     */
    @RequestMapping(value = "practice.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessonPractice() {
        Long lessonId = getRequestLong("lessonId");
        ExLessonDat exLesson = chineseContentLoaderClient.loadChineseExLesson(lessonId);

        List<ChineseSentence> sentences = chineseContentLoaderClient.loadChineseLessonSentences(exLesson.getId(), ChineseSentenceType.WORD);

        for (ChineseSentence sentence : sentences) {
            exLesson.getPointList().add(sentence.getContent());
        }

        return MapMessage.successMessage().add("row", exLesson);
    }

    /**
     * 选择练习--应试练习
     */
    @RequestMapping(value = "exam.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessonExam() {

        Long lessonId = getRequestLong("lessonId");
        List<NewQuestion> questionList = questionLoaderClient.loadQuestionsForChineseByLessonIds(Collections.singletonList(lessonId));
        questionList = questionList.stream().filter(q->!q.isDeletedTrue()).collect(Collectors.toList());
        Map<String, List<NewQuestion>> questionMap = new HashMap<>();

        Map<Integer, Set<String>> patternQuestions = new HashMap<>();
        for(NewQuestion q: questionList) {
            String qid = q.processQuestionId();
            List<NewQuestion> questions = questionMap.get(qid);
            if(questions == null){
                questions = new ArrayList<>();
            }
            questions.add(q);
            questionMap.put(qid, questions);

            Set<String> eids = patternQuestions.get(q.getContentTypeId());
            if (eids == null) {
                eids = new HashSet<>();
            }
            eids.add(qid);
            patternQuestions.put(q.getContentTypeId(), eids);
        }

        Map<Integer, NewContentType> patternMap = questionContentTypeLoaderClient.loadQuestionContentTypes(patternQuestions.keySet());
        List<Map<String, Object>> questions = new ArrayList<>();
        for(Integer patternId : patternQuestions.keySet()){
            Map<String, Object> obj = new HashMap<>();
            int rank = QuestionConstants.examChineseIncludeContentTypeIds.indexOf(patternId);
            obj.put("patternId", patternId);
            obj.put("patternName", patternMap.get(patternId) != null ? patternMap.get(patternId).getName():"");
            obj.put("rank", rank);
            List<Map<String, Object>> exams = new ArrayList<>();
            for(String qid : patternQuestions.get(patternId)){
                Map<String, Object> exam = new HashMap<>();
                exam.put("id", qid);
                int seconds = 0;
                for(NewQuestion q : questionMap.get(qid)){
                    seconds += q.getSeconds();
                }
                exam.put("seconds", seconds);
                exams.add(exam);
            }
            obj.put("exams", exams);
            questions.add(obj);
        }
        questions = questions.stream().sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o1.get("rank")), SafeConverter.toInt(o2.get("rank")))).collect(Collectors.toList());

        return MapMessage.successMessage().add("examList", questions);
    }
}
