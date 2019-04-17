/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * Teacher homework report Api for App
 * Created by Shuai Huan on 2015/1/12.
 */
@Controller
@RequestMapping(value = "/v1/teacher/report")
@Slf4j
public class TeacherReportApiController extends AbstractTeacherApiController {

    @RequestMapping(value = "homework/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_PAGE_NUMBER, "页码");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // FIXME api直接下线，老版本不支持访问了
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        Teacher teacher = getCurrentTeacher();
//        Long clazzId = conversionService.convert(getRequest().getParameter(REQ_CLAZZ_ID), Long.class);
//        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER);
//        Pageable pageable = new PageRequest(currentPage - 1, 10);
//        List<Map<String, Object>> pageContents = new ArrayList<>();
//        Page<HomeworkHistoryListMapper> historyListMapperPage = businessHomeworkServiceClient.getHomeworkHistoryByClazzId(teacher, clazzId, pageable);
//        for (HomeworkHistoryListMapper mapper : historyListMapperPage.getContent()) {
//            Map<String, Object> obj = new HashMap<>();
//            obj.put(RES_HOMEWORK_START_DATE, mapper.getStartDate());
//            obj.put(RES_HOMEWORK_END_DATE, mapper.getEndDate());
//            obj.put(RES_UNIT_NAMES, mapper.getUnitsContext());
//            obj.put(RES_HOMEWORK_ID, mapper.getHomeworkId());
//            pageContents.add(obj);
//        }
//        Page<Map<String, Object>> page = new PageImpl<>(pageContents, pageable, historyListMapperPage.getTotalElements());
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        resultMap.add("page", page);
//        return resultMap;
    }

    @RequestMapping(value = "homework/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportDetail() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // FIXME api直接下线，老版本不支持访问了
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        String sessionKey = attachUser2RequestApp(117685L);
//        Teacher teacher = getCurrentTeacher();
//        String homeworkId = getRequestString(REQ_PIC_HOMEWORK_ID);
//        MapMessage report = new MapMessage();
//        TeacherCheckHomeworkIntegral tchi = homeworkLoaderClient.findByHomeworkId(homeworkId);
//        resultMap.add(RES_HOMEWORK_INTEGRAL, tchi != null ? tchi.getGold() : 0);
//        switch (teacher.getSubject()) {
//            case ENGLISH:
//                report = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(homeworkId, HomeworkType.ENGLISH);
//                break;
//            case MATH:
//                report = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(homeworkId, HomeworkType.MATH);
//                break;
//            case CHINESE:
//                report = homeworkReportLoaderClient.loadHomeworkHistoryReportDetail(homeworkId, HomeworkType.CHINESE);
//                break;
//            default:
//        }
//        if (!report.isSuccess()) {
//            resultMap.add(RES_RESULT, RES_RESULT_REPORT_ERROR_MSG);
//            return resultMap;
//        }
//
//        resultMap.add(RES_HOMEWORK_FINISH_COUNT, report.get("completeCount"));
//        resultMap.add(RES_HOMEWORK_UNFINISH_COUNT, report.get("joinCount"));
//        resultMap.add(RES_HOMEWORK_UNDO_COUNT, report.get("undoCount"));
//        resultMap.add(RES_HOMEWORK_CLAZZ_AVERAGE_SCORE, report.get("avgScore"));
//        resultMap.add(RES_HOMEWORK_CLAZZ_AVERAGE_FINISH_TIME, SafeConverter.toLong(report.get("avgFinishTimeLong")) / 1000);
//        List<Map<String, Object>> students = new ArrayList<>();
//        List<Map<String, Object>> studentInfos = (List<Map<String, Object>>) report.get("studentList");
//        for (Map<String, Object> info : studentInfos) {
//            Long studentId = SafeConverter.toLong(info.get("userId"));
//            Map<String, Object> obj = new HashMap<>();
//            obj.put(RES_USER_ID, studentId);
//            obj.put(RES_REAL_NAME, info.get("userName"));
//            obj.put(RES_HOMEWORK_STUDENT_SCORE, info.get("score"));
//            obj.put(RES_HOMEWORK_STUDENT_FINISH_STATE, info.get("finished") != null ? info.get("finished") : false);
//            obj.put(RES_HOMEWORK_STUDENT_JOIN_STATE, info.get("finished") != null);
//            List<Map<String, Object>> scores = new ArrayList<>();
//            List<Map<String, Object>> scoreTitelList = (List<Map<String, Object>>) report.get("avgScoreTitle");
//            List<Integer> avgScore = (List<Integer>) info.get("avgScore");
//            int i = 0;
//            for (Map title : scoreTitelList) {
//                HomeworkResultType homeworkResultType = (HomeworkResultType) title.get("key");
//                Map<String, Object> score = new HashMap<>();
//                score.put(RES_HOMEWORK_STUDENT_SCORE, avgScore != null ? avgScore.get(i) : 0);
//                score.put(RES_HOMEWORK_STUDENT_SCORE_TITLE, homeworkResultType.getDescription());
//                score.put(RES_HOMEWORK_STUDENT_SCORE_TYPE, homeworkResultType.getMobileScoreType());
//                scores.add(score);
//                i++;
//            }
//            obj.put(RES_HOMEWORK_STUDENT_SCORE_LIST, scores);
//
//            students.add(obj);
//        }
//        resultMap.add(RES_STUDENT_LIST, students);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return resultMap;
    }

    @RequestMapping(value = "student/homework/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportStudentDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequired(REQ_USER_CODE, "用户账号");
            validateRequest(REQ_HOMEWORK_ID, REQ_USER_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // FIXME api直接下线，老版本不支持访问了
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        String sessionKey = attachUser2RequestApp(10002L);
//        Teacher teacher = getCurrentTeacher();
//        String homeworkId = getRequestString(REQ_PIC_HOMEWORK_ID);
//        Long userId = getRequestLong(REQ_USER_CODE);
//        User student = studentLoaderClient.loadStudent(userId);
//        if (student == null) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
//            return resultMap;
//        }
//        HomeworkComment homeworkComment = null;
//        switch (teacher.getSubject()) {
//            case ENGLISH:
//                MapMessage englishMapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportStudentDetail(homeworkId, HomeworkType.ENGLISH, userId);
//                if (englishMapMessage.isSuccess()) {
//                    Homework englishHomework = homeworkLoaderClient.loadEnglishHomeworkIncludeDisabled(homeworkId);
//                    List<String> examIds = new ArrayList<>();
//                    if (englishHomework.determineDistribution().isContainExam()) {
//                        String paperJson = englishHomework.getPaperJson();
//                        List<Map> examMaps = JsonUtils.fromJsonToList(paperJson, Map.class);
//                        for (Map<String, Object> exameMap : examMaps) {
//                            examIds.addAll((Collection<? extends String>) exameMap.get("exams"));
//                        }
//                    }
//                    ExtensionEnglishHomework extension = homeworkLoaderClient.loadEnglishExtensionHomework(homeworkId);
//                    EnglishHomeworkResultCombo er = comboResultLoaderClient.loadEnglishResultCombo(extension, userId);
//                    List<Map> homeworkTypeTitles = (List<Map>) englishMapMessage.get("avgScoreTitle");
//                    List<Map> studentList = (List<Map>) englishMapMessage.get("studentList");
//                    Map scoreMap = null;
//                    for (Map st : studentList) {
//                        Long studentId = SafeConverter.toLong(st.get("userId"));
//                        if (studentId.equals(userId)) {
//                            scoreMap = st;
//                            break;
//                        }
//                    }
//                    List<Map<String, Object>> englishStudents = new ArrayList<>();
//                    List<Map<String, Object>> basicPracticeScores = new ArrayList<>();
//                    List<Map<String, Object>> examScores = new ArrayList<>();
//                    if (scoreMap != null) {
//                        Map<String, Object> scoreObj = new HashMap<>();
//                        scoreObj.put(RES_HOMEWORK_STUDENT_SCORE, SafeConverter.toInt(scoreMap.get("score")));
//                        scoreObj.put(RES_HOMEWORK_STUDENT_FINISH_TIME, SafeConverter.toLong(scoreMap.get("finishTimeLong")) / 1000);
//                        scoreObj.put(RES_USER_ID, userId);
//                        scoreObj.put(RES_REAL_NAME, student.getProfile().getRealname());
//                        scoreObj.put(RES_HOMEWORK_STUDENT_FINISH_STATE, scoreMap.get("finished"));
//                        List<Map> avgScores = (List<Map>) scoreMap.get("avgScore");
//                        int readingScore = 0;
//                        boolean readingExist = false;
//                        int basicScore = 0;
//                        boolean basicExist = false;
//                        int examScore = 0;
//                        boolean examExist = false;
//
//                        for (Map homeworkTypeTitle : homeworkTypeTitles) {
//                            HomeworkResultType homeworkResultType = (HomeworkResultType) homeworkTypeTitle.get("key");
//                            switch (homeworkResultType) {
//                                case ENGLISH_READING:
//                                    readingExist = true;
//                                    break;
//                                case ENGLISH_BASIC:
//                                    basicExist = true;
//                                    break;
//                                case ENGLISH_EXAM:
//                                    examExist = true;
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//
//                        Map<String, ExaminationQuestion> examinationQuestionMap = new HashMap<>();
//                        if (examExist) {
//                            examinationQuestionMap = examQuestionLoaderClient.loadEnglishExamQuestions(examIds);
//                        }
//
//                        for (Map avgScore : avgScores) {
//                            HomeworkResultType homeworkResultType = (HomeworkResultType) avgScore.get("key");
//                            switch (homeworkResultType) {
//                                case ENGLISH_READING:
//                                    readingScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                case ENGLISH_BASIC:
//                                    List<Map> basicPractices = (List<Map>) avgScore.get("practiceScores");
//                                    for (Map practiceScore : basicPractices) {
//                                        Map<String, Object> obj = new HashMap<>();
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE_TITLE, practiceScore.get("category"));
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE, practiceScore.get("score") != null ? practiceScore.get("score") : "--");
//                                        List<String> recordingList = practiceScore.get("voice") != null ? (List<String>) practiceScore.get("voice") : Collections.emptyList();
//                                        String recording = practiceScore.get("voice") != null ? StringUtils.join(recordingList, "|") : "";
//                                        recording = StringUtils.isNotBlank(recording) ? StringUtils.replace(recording, ".flv", ".flv.mp3") : "";
//
//                                        if (recording.contains("records.cloud.chivox.com")) {
//                                            List<String> all = new ArrayList<>();
//                                            for (String item : StringUtils.split(recording, "|")) {
//                                                all.add("http://" + item + ".mp3");
//                                            }
//                                            recording = StringUtils.join(all, "|");
//                                        }
//                                        obj.put(RES_HOMEWORK_STUDENT_RECORDING, recording);
//                                        basicPracticeScores.add(obj);
//                                    }
//                                    basicScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                case ENGLISH_EXAM:
//                                    Map<String, HomeworkExamResult> examResultMap = new HashMap<>();
//                                    for (HomeworkExamResult examResult : er.getExamResults()) {
//                                        examResultMap.put(examResult.getExamId(), examResult);
//                                    }
//                                    for (ExaminationQuestion exam : examinationQuestionMap.values()) {
//                                        Map<String, Object> examObj = new HashMap<>();
//                                        String eid = exam.getId();
//                                        examObj.put(RES_HOMEWORK_EXAM_PATTERN, exam.getPattern());
//                                        examObj.put(RES_HOMEWORK_EXAM_ID, eid);
//                                        int answer = 0;
//                                        if (examResultMap.get(eid) != null) {
//                                            answer = examResultMap.get(eid).getMaster() ? 1 : 2;
//                                        }
//                                        examObj.put(RES_HOMEWORK_EXAM_ANSWER, answer);
//                                        examScores.add(examObj);
//                                    }
//                                    examScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                default:
//                                    break;
//                            }
//
//                        }
//
//                        scoreObj.put(RES_HOMEWORK_STUDENT_BASIC_SCORE, basicScore);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_EXAM_SCORE, examScore);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_READING_SCORE, readingScore);
//                        scoreObj.put(RES_HOMEWORK_BASIC_EXIST, basicExist);
//                        scoreObj.put(RES_HOMEWORK_EXAM_EXIST, examExist);
//                        scoreObj.put(RES_HOMEWORK_READING_EXIST, readingExist);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_BASIC_DETAILS, basicPracticeScores);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_EXAM_DETAILS, examScores);
//                        homeworkComment = homeworkCommentLoaderClient.loadHomeworkComments(homeworkId)
//                                .homeworkType(HomeworkType.ENGLISH)
//                                .filter(t -> Objects.equals(t.getStudentId(), userId))
//                                .findFirst();
//                        scoreObj.put(RES_HOMEWORK_COMMENT, homeworkComment == null ? "" : homeworkComment.getComment());
//                        scoreObj.put(RES_INTEGRAL, homeworkComment == null ? "" : homeworkComment.getRewardIntegral());
//                        scoreObj.put(RES_SPEECH_DATA_URL, "");
//                        englishStudents.add(scoreObj);
//
//                        resultMap.add(RES_HOMEWORK_STUDENT_DETIAL, CollectionUtils.isNotEmpty(englishStudents) ? englishStudents.get(0) : Collections.emptyList());
//                        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//                    }
//                }
//
//                break;
//            case MATH:
//                MapMessage mathMapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportStudentDetail(homeworkId, HomeworkType.MATH, userId);
//                if (mathMapMessage.isSuccess()) {
//                    MathHomework mathHomework = homeworkLoaderClient.loadMathHomeworkIncludeDisabled(SafeConverter.toLong(homeworkId));
//                    List<String> examIds = new ArrayList<>();
//                    if (mathHomework.determineDistribution().isContainExam()) {
//                        String paperJson = mathHomework.getPaperJson();
//                        List<Map> examMaps = JsonUtils.fromJsonToList(paperJson, Map.class);
//                        for (Map<String, Object> exameMap : examMaps) {
//                            examIds.addAll((Collection<? extends String>) exameMap.get("exams"));
//                        }
//                    }
//
//                    ExtensionMathHomework extension = homeworkLoaderClient.loadExtensionMathHomework(SafeConverter.toLong(homeworkId));
//                    MathHomeworkResultCombo er = comboResultLoaderClient.loadMathResultCombo(extension, userId);
//                    List<Map> homeworkTypeTitles = (List<Map>) mathMapMessage.get("avgScoreTitle");
//                    List<Map> studentList = (List<Map>) mathMapMessage.get("studentList");
//                    Map scoreMap = null;
//                    for (Map st : studentList) {
//                        Long studentId = SafeConverter.toLong(st.get("userId"));
//                        if (studentId.equals(userId)) {
//                            scoreMap = st;
//                            break;
//                        }
//                    }
//                    List<Map<String, Object>> mathStudents = new ArrayList<>();
//                    List<Map<String, Object>> basicPracticeScores = new ArrayList<>();
//                    List<Map<String, Object>> specialPracticeScores = new ArrayList<>();
//                    List<Map<String, Object>> examScores = new ArrayList<>();
//                    if (scoreMap != null) {
//                        Map<String, Object> scoreObj = new HashMap<>();
//                        scoreObj.put(RES_HOMEWORK_STUDENT_SCORE, SafeConverter.toInt(scoreMap.get("score")));
//                        scoreObj.put(RES_HOMEWORK_STUDENT_FINISH_TIME, SafeConverter.toLong(scoreMap.get("finishTimeLong")) / 1000);
//                        scoreObj.put(RES_USER_ID, userId);
//                        scoreObj.put(RES_REAL_NAME, student.getProfile().getRealname());
//                        scoreObj.put(RES_HOMEWORK_STUDENT_FINISH_STATE, scoreMap.get("finished"));
//                        List<Map> avgScores = (List<Map>) scoreMap.get("avgScore");
//                        int specialScore = 0;
//                        boolean specialExist = false;
//                        int basicScore = 0;
//                        boolean basicExist = false;
//                        int examScore = 0;
//                        boolean examExist = false;
//                        for (Map homeworkTypeTitle : homeworkTypeTitles) {
//                            HomeworkResultType homeworkResultType = (HomeworkResultType) homeworkTypeTitle.get("key");
//                            switch (homeworkResultType) {
//                                case MATH_SPECIAL:
//                                    specialExist = true;
//                                    break;
//                                case MATH_CACULATE:
//                                    basicExist = true;
//                                    break;
//                                case MATH_EXAM:
//                                    examExist = true;
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//                        Map<String, MathExaminationQuestion> mathExaminationQuestionMap = new HashMap<>();
//                        if (examExist) {
//                            mathExaminationQuestionMap = examQuestionLoaderClient.loadMathExamQuestions(examIds);
//                        }
//
//                        for (Map avgScore : avgScores) {
//                            HomeworkResultType homeworkResultType = (HomeworkResultType) avgScore.get("key");
//                            switch (homeworkResultType) {
//                                case MATH_SPECIAL:
//                                    List<Map> specialPractices = (List<Map>) avgScore.get("practiceScores");
//                                    for (Map practiceScore : specialPractices) {
//                                        Map<String, Object> obj = new HashMap<>();
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE_TITLE, practiceScore.get("pointName"));
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE, practiceScore.get("right") != null ? practiceScore.get("right") : "--");
//                                        specialPracticeScores.add(obj);
//                                    }
//                                    specialScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                case MATH_CACULATE:
//                                    List<Map> caculatePractices = (List<Map>) avgScore.get("practiceScores");
//                                    for (Map practiceScore : caculatePractices) {
//                                        Map<String, Object> obj = new HashMap<>();
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE_TITLE, practiceScore.get("pointName"));
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE, practiceScore.get("right") != null ? practiceScore.get("right") : "--");
//                                        basicPracticeScores.add(obj);
//                                    }
//                                    basicScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                case MATH_EXAM:
//
//                                    Map<String, HomeworkExamResult> mathExamResultMap = new HashMap<>();
//                                    for (HomeworkExamResult homeworkExamResult : er.getExamResults()) {
//                                        mathExamResultMap.put(homeworkExamResult.getExamId(), homeworkExamResult);
//                                    }
//                                    for (MathExaminationQuestion mathExam : mathExaminationQuestionMap.values()) {
//                                        Map<String, Object> examObj = new HashMap<>();
//                                        String eid = mathExam.getId();
//                                        examObj.put(RES_HOMEWORK_EXAM_PATTERN, mathExam.getPattern());
//                                        examObj.put(RES_HOMEWORK_EXAM_ID, eid);
//                                        int answer = 0;
//                                        if (mathExamResultMap.get(eid) != null) {
//                                            answer = mathExamResultMap.get(eid).getMaster() ? 1 : 2;
//                                        }
//                                        examObj.put(RES_HOMEWORK_EXAM_ANSWER, answer);
//                                        examScores.add(examObj);
//                                    }
//                                    examScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//
//                        scoreObj.put(RES_HOMEWORK_STUDENT_BASIC_SCORE, basicScore);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_EXAM_SCORE, examScore);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_SPECIAL_SCORE, specialScore);
//                        scoreObj.put(RES_HOMEWORK_BASIC_EXIST, basicExist);
//                        scoreObj.put(RES_HOMEWORK_EXAM_EXIST, examExist);
//                        scoreObj.put(RES_HOMEWORK_SPECIAL_EXIST, specialExist);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_BASIC_DETAILS, basicPracticeScores);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_SPECIAL_DETAILS, specialPracticeScores);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_EXAM_DETAILS, examScores);
//                        homeworkComment = homeworkCommentLoaderClient.loadHomeworkComments(homeworkId)
//                                .homeworkType(HomeworkType.MATH)
//                                .filter(t -> Objects.equals(t.getStudentId(), userId))
//                                .findFirst();
//                        scoreObj.put(RES_HOMEWORK_COMMENT, homeworkComment == null ? "" : homeworkComment.getComment());
//                        scoreObj.put(RES_INTEGRAL, homeworkComment == null ? "" : homeworkComment.getRewardIntegral());
//                        mathStudents.add(scoreObj);
//                        resultMap.add(RES_HOMEWORK_STUDENT_DETIAL, CollectionUtils.isNotEmpty(mathStudents) ? mathStudents.get(0) : Collections.emptyList());
//                        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//                    }
//                }
//                break;
//
//            case CHINESE:
//                MapMessage chineseMapMessage = homeworkReportLoaderClient.loadHomeworkHistoryReportStudentDetail(homeworkId, HomeworkType.CHINESE, userId);
//                if (chineseMapMessage.isSuccess()) {
//                    List<Map> homeworkTypeTitles = (List<Map>) chineseMapMessage.get("avgScoreTitle");
//                    List<Map> studentList = (List<Map>) chineseMapMessage.get("studentList");
//                    Map scoreMap = null;
//                    for (Map st : studentList) {
//                        Long studentId = SafeConverter.toLong(st.get("userId"));
//                        if (studentId.equals(userId)) {
//                            scoreMap = st;
//                            break;
//                        }
//                    }
//                    List<Map<String, Object>> chineseStudents = new ArrayList<>();
//                    List<Map<String, Object>> basicPracticeScores = new ArrayList<>();
//                    if (scoreMap != null) {
//                        Map<String, Object> scoreObj = new HashMap<>();
//                        scoreObj.put(RES_HOMEWORK_STUDENT_SCORE, SafeConverter.toInt(scoreMap.get("score")));
//                        scoreObj.put(RES_HOMEWORK_STUDENT_FINISH_TIME, SafeConverter.toLong(scoreMap.get("finishTimeLong")) / 1000);
//                        scoreObj.put(RES_USER_ID, userId);
//                        scoreObj.put(RES_REAL_NAME, student.getProfile().getRealname());
//                        scoreObj.put(RES_HOMEWORK_STUDENT_FINISH_STATE, scoreMap.get("finished"));
//                        List<Map> avgScores = (List<Map>) scoreMap.get("avgScore");
//                        int basicScore = 0;
//                        boolean basicExist = false;
//                        for (Map homeworkTypeTitle : homeworkTypeTitles) {
//                            HomeworkResultType homeworkResultType = (HomeworkResultType) homeworkTypeTitle.get("key");
//                            switch (homeworkResultType) {
//                                case CHINESE_BASIC:
//                                    basicExist = true;
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//
//                        for (Map avgScore : avgScores) {
//                            HomeworkResultType homeworkResultType = (HomeworkResultType) avgScore.get("key");
//                            switch (homeworkResultType) {
//                                case CHINESE_BASIC:
//                                    List<Map> specialPractices = (List<Map>) avgScore.get("practiceScores");
//                                    for (Map practiceScore : specialPractices) {
//                                        Map<String, Object> obj = new HashMap<>();
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE_TITLE, practiceScore.get("category"));
//                                        obj.put(RES_HOMEWORK_STUDENT_SCORE, practiceScore.get("score") != null ? practiceScore.get("score") : "--");
//                                        basicPracticeScores.add(obj);
//                                    }
//                                    basicScore = SafeConverter.toInt(avgScore.get("score"));
//                                    break;
//                                default:
//                                    break;
//                            }
//
//                        }
//
//                        scoreObj.put(RES_HOMEWORK_STUDENT_BASIC_SCORE, basicScore);
//                        scoreObj.put(RES_HOMEWORK_STUDENT_EXAM_SCORE, 0);
//                        scoreObj.put(RES_HOMEWORK_BASIC_EXIST, basicExist);
//                        scoreObj.put(RES_HOMEWORK_EXAM_EXIST, false);
//                        homeworkComment = homeworkCommentLoaderClient.loadHomeworkComments(homeworkId)
//                                .homeworkType(HomeworkType.CHINESE)
//                                .filter(t -> Objects.equals(t.getStudentId(), userId))
//                                .findFirst();
//                        scoreObj.put(RES_HOMEWORK_COMMENT, homeworkComment == null ? "" : homeworkComment.getComment());
//                        scoreObj.put(RES_INTEGRAL, homeworkComment == null ? "" : homeworkComment.getRewardIntegral());
//                        scoreObj.put(RES_HOMEWORK_STUDENT_BASIC_DETAILS, basicPracticeScores);
//                        chineseStudents.add(scoreObj);
//                        resultMap.add(RES_HOMEWORK_STUDENT_DETIAL, CollectionUtils.isNotEmpty(chineseStudents) ? chineseStudents.get(0) : Collections.emptyList());
//                        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//
//                    }
//
//                }
//
//                break;
//            default:
//        }
//
////        String a = JsonUtils.toJson(resultMap);
//        return resultMap;
    }


    @RequestMapping(value = "comment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportStudentComment() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // FIXME api直接下线，老版本不支持访问了
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        Teacher teacher = getCurrentTeacher();
//        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
//        //老师自定义的评语列表
//        TeacherCommentLibrary teacherCommentLibrary = homeworkLoaderClient.findTeacherCommentByTeacherIdAndClazzId(teacher.getId(), clazzId);
//        List<String> commentList = new ArrayList<>();
//        if (teacherCommentLibrary != null) {
//            commentList.addAll(teacherCommentLibrary.getCommentList());
//        }
//        switch (teacher.getSubject()) {
//            case ENGLISH:
//                commentList.addAll(Arrays.asList("完成得不错！",
//                        "恭喜你，你已经取得了很大的进步！",
//                        "有些小错误，下次要多加注意。",
//                        "如果你更加努力的话，我相信你会做得更好！",
//                        "如果能把所有作业都按时完成，你会进步得很快！",
//                        "Wonderful!",
//                        "Excellent!",
//                        "Nice work!",
//                        "I think you can do better if you try harder.",
//                        "I’m glad to see you are making progress."));
//                break;
//            case MATH:
//                commentList.addAll(Arrays.asList("做得太棒了！",
//                        "你的作业质量比以前有了很大的进步！",
//                        "你是一个很有数学才能的学生！",
//                        "你的计算能力有了很大提高！",
//                        "对于计算题，也要注意留心观察与思考！",
//                        "多想一想前后知识的联系，你就会变得更聪明！",
//                        "你的目标，应该是在数学方面成为同学们的榜样！",
//                        "有的题目如果你能再认真读下已知条件，就一定能做对！"));
//                break;
//            case CHINESE:
//                commentList.addAll(Arrays.asList("做得太棒了！",
//                        "恭喜你，你已经取得了很大的进步！",
//                        "有些小错误，下次要多加注意。",
//                        "如果你更加努力的话，我相信你会做得更好！",
//                        "如果能把所有作业都按时完成，你会进步得很快！",
//                        "你的作业质量比以前有了很大的进步！"));
//                break;
//            default:
//                logger.warn("teacher subject unknown,subject:{}", teacher.getSubject());
//        }
//
//        resultMap.add(RES_TEACHER_COMMENT_LIST, commentList);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
////        String a = JsonUtils.toJson(resultMap);
//        return resultMap;
    }


    @RequestMapping(value = "writecomment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage writeHomeworkComment() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequired(REQ_STUDENT_LIST, "学生ID列表");
            validateRequired(REQ_HOMEWORK_COMMENT, "作业留言");
            validateRequired(REQ_INTEGRAL, "奖励学豆");
            validateRequest(REQ_HOMEWORK_ID, REQ_HOMEWORK_COMMENT, REQ_INTEGRAL);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // FIXME api直接下线，老版本不支持访问了
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        String comment = getRequestString(REQ_HOMEWORK_COMMENT);
//        if (StringUtils.length(comment) > 100) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_TOO_LONG_MSG);
//            return resultMap;
//        }
//
//        Teacher teacher = getCurrentTeacher();
//        HomeworkType homeworkType = HomeworkType.UNKNOWN;
//        switch (teacher.getSubject()) {
//            case ENGLISH:
//                homeworkType = HomeworkType.ENGLISH;
//                break;
//            case MATH:
//                homeworkType = HomeworkType.MATH;
//                break;
//            case CHINESE:
//                homeworkType = HomeworkType.CHINESE;
//                break;
//            default:
//                logger.warn("teacher subject unknown,subject:{}", teacher.getSubject());
//        }
//        @SuppressWarnings("unchecked")
//        Map<String, Object> detail = new HashMap<>();
//        String studentIds = getRequestString(REQ_STUDENT_LIST);
//        List<String> studentIdList = new ArrayList<>();
//        for (String uid : studentIds.split(",")) {
//            if (StringUtils.isNotBlank(uid)) {
//                studentIdList.add(uid);
//            }
//        }
//        detail.put("studentIds", studentIdList);
//        detail.put("integral", getRequestInt(REQ_INTEGRAL));
//        detail.put("comment", comment);
//        List<Map<String, Object>> details = Collections.singletonList(detail);
//        String homeworkId = getRequestString(REQ_PIC_HOMEWORK_ID);
//        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
//        MapMessage mapMessage = businessTeacherServiceClient.writeHomeworkComment(
//                teacherDetail,
//                details,
//                HomeworkLocation.newInstance(homeworkType, homeworkId));
//        if (mapMessage.isSuccess()) {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
//        }
//
////        String a = JsonUtils.toJson(resultMap);
//
//        return resultMap;
    }


}
