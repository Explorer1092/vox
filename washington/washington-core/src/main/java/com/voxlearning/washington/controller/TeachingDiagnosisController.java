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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.business.api.TeachingDiagnosisExperimentService;
import com.voxlearning.utopia.business.api.constant.BusinessErrorType;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.TeachingDiagnosisQuestionResult;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTask;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.service.business.consumer.TeachingDiagnosisServiceClient;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.net.message.exam.*;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Controller
@RequestMapping("/teaching/diagnosis")
public class TeachingDiagnosisController extends AbstractController {

    @Inject
    private TeachingDiagnosisServiceClient teachingDiagnosisServiceClient;

    @ImportService(interfaceClass = TeachingDiagnosisExperimentService.class)
    private TeachingDiagnosisExperimentService teachingDiagnosisExperimentService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        StudentDetail studentDetail = fetchStudent();
        if (studentDetail == null) {
            return "redirect:/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/middle-page/transit.vhtml?redirectUrl=resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/experiment-courses/index.vhtml";
        }
        TeachingDiagnosisTask teachingDiagnosisTasks = teachingDiagnosisServiceClient.fetchDiagnosisTaskCheckedExperimented(studentDetail.getId());
        if (teachingDiagnosisTasks == null) {
            return "redirect:/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/middle-page/transit.vhtml?redirectUrl=resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/experiment-courses/index.vhtml";
        }
        String redirectUrl = null;
        try {
            redirectUrl = URLEncoder.encode("resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/experiment-courses/course.vhtml?taskId=" + teachingDiagnosisTasks.getId(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("url编码失败");
            e.printStackTrace();
        }
        return "redirect:/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/middle-page/transit.vhtml?redirectUrl=" + redirectUrl;
    }

    @RequestMapping(value = "pre/questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchPreQuestions() {
        StudentDetail studentDetail = fetchStudent();
        if (studentDetail == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }
        return teachingDiagnosisServiceClient.fetchPreQuestionsByStudent(studentDetail);
    }

    @RequestMapping(value = "pre/processeresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processePreQuestionResult() {
        StudentDetail studentDetail = fetchStudent();
        if (studentDetail == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }

        String json = getRequestString("data");
        SaveTeachingDiagnosisPreQuestionResultRequest sarr = JsonUtils.fromJson(json, SaveTeachingDiagnosisPreQuestionResultRequest.class);
        if (null == sarr || sarr.result == null
                || CollectionUtils.isEmpty(sarr.result.getExamResults())
                || sarr.result.getExtra() == null
                || StringUtils.isAnyEmpty(sarr.result.getQid(), sarr.result.getExtra().getExperimentId(), sarr.result.getExtra().getExperimentGroupId())
                || sarr.result.getExtra().getCreateTime() == null) {
            logger.error("processePreQuestionResult error data:{}", json);
            return MapMessage.errorMessage(BusinessErrorType.PARAMETER_CHECK_ERROR.getInfo())
                    .setErrorCode(BusinessErrorType.PARAMETER_CHECK_ERROR.getCode());
        }

        PreQuestionResultContext context = generatePreQuestionResult(studentDetail, sarr.result);
        return teachingDiagnosisServiceClient.processPreQuestionResult(context);
    }

    private PreQuestionResultContext generatePreQuestionResult(StudentDetail student, TeachingDianosisPreQuestionResultRequest result) {
        // 处理学生的做题结果
        List<List<String>> userAnswer = result.getExamResults().get(0).getAnswer();
        UserAnswerMapper uam = new UserAnswerMapper(result.getExamResults().get(0).getExamId(), 1D, userAnswer);
        uam.setUserAgent(getRequest().getHeader("User-Agent"));
        uam.setUserId(currentUser().getId());
        uam.setHomeworkType(StudyType.selfstudy.name());
        QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        if (qsr == null || qsr.getSubScoreResults() == null) {
            logger.error("generatePreQuestionResult Error,userId={},questionID={},userAnswer={}", student.getId(), result.getQid(), userAnswer);
            return null;
        }

        List<List<String>> answer = new ArrayList<>();
        List<List<Boolean>> subMaster = new ArrayList<>();
        for (SubScoreResult ssr : qsr.getSubScoreResults()) {
            answer.add(ssr.getStandardAnswer());
            subMaster.add(ssr.getIsRight());
        }

        PreQuestionResultContext context = new PreQuestionResultContext();
        context.getResult().put("questionId", result.getQid());
        context.getResult().put("questionInfo", MapUtils.m("answers", answer,
                "userAnswers", userAnswer, "subMaster", subMaster, "master", qsr.getIsRight()));
        context.setStudent(student);
        context.setSubject(Subject.of(result.getSubject()));
        context.setQuestionId(result.getQid());
        context.setMaster(qsr.getIsRight());
        context.setLast(result.getLast());
        context.setAnswer(userAnswer);
        context.setFinishTime(result.getExamResults().get(0).getFinishTime());
        String exp = Optional.ofNullable(result.getExtra())
                .filter(e -> StringUtils.isNotBlank(e.getExperimentId()))
                .map(TeachingDianosisPreQuestionExtraRequest::getExperimentId)
                .orElse("");
        context.setExperimentId(exp);
        return context;
    }

    @RequestMapping(value = "course/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchCourseIndex() {
        StudentDetail studentDetail = fetchStudent();
        if (studentDetail == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }
        String taskId = getRequestString("taskId");
        return teachingDiagnosisServiceClient.fetchIndexMessage(taskId);
    }

    @RequestMapping(value = "course/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchCourseDetail() {
        try {
            StudentDetail studentDetail = fetchStudent();
            if (studentDetail == null) {
                return MapMessage.errorMessage("请登录");
            }

            String taskId = getRequestString("taskId");
            if (StringUtils.isBlank(taskId)) {
                return MapMessage.errorMessage("参数错误");
            }
            TeachingDiagnosisTask task = teachingDiagnosisServiceClient.fetchDiagnosisTaskById(taskId);
            TeachingDiagnosisExperimentConfig experimentConfig = Optional.ofNullable(task)
                    .filter(e -> StringUtils.isNotBlank(e.getExperimentId()))
                    .map(e -> teachingDiagnosisExperimentService.loadExperimentById(e.getExperimentId()))
                    .orElse(null);
            String postQId = Optional.ofNullable(experimentConfig)
                    .map(TeachingDiagnosisExperimentConfig::getPostQuestion)
                    .filter(StringUtils::isNotBlank)
                    .orElse("");

            return MapMessage.successMessage()
                    .set("preQuestions", task.getPreviewQuestionList())
                    .set("testQuestions", Collections.singletonList(postQId))
                    .set("experimentGroupId", task.getExperimentGroupId())
                    .set("experimentId", task.getExperimentId());
        } catch (Exception e) {
            logger.error("fetchCourseDetail error. ", e);
            return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo()).setErrorCode(BusinessErrorType.DEFAULT.getCode());
        }
    }

    @RequestMapping(value = "course/processeresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processeCourseQuestionResult() {
        StudentDetail studentDetail = fetchStudent();
        if (studentDetail == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }

        String json = getRequestString("data");
        SaveTeachingDiagnosisCourseQuestionResultRequest sarr = JsonUtils.fromJson(json, SaveTeachingDiagnosisCourseQuestionResultRequest.class);
        if (sarr == null || sarr.result == null
                || CollectionUtils.isEmpty(sarr.result.getExamResults())
                || sarr.result.getExtra() == null
                || sarr.result.getExtra().getCreateTime() == null
                || StringUtils.isAnyBlank(sarr.result.getQid(), sarr.result.getExtra().getCourseId(), sarr.result.getExtra().getTaskId())) {
            logger.error("processePreQuestionResult error data:{}", json);
            return MapMessage.errorMessage(BusinessErrorType.PARAMETER_CHECK_ERROR.getInfo())
                    .setErrorCode(BusinessErrorType.PARAMETER_CHECK_ERROR.getCode());
        }
        return processeCourseQuestionResult(studentDetail, sarr.result);
    }

    private MapMessage processeCourseQuestionResult(StudentDetail student, TeachingDianosisCourseQuestionResultRequest result) {
        try {
            // 处理学生的做题结果
            List<List<String>> userAnswer = result.getExamResults().get(0).getAnswer();
            UserAnswerMapper uam = new UserAnswerMapper(result.getExamResults().get(0).getExamId(), 1D, userAnswer);
            uam.setUserAgent(getRequest().getHeader("User-Agent"));
            uam.setUserId(currentUser().getId());
            uam.setHomeworkType(StudyType.selfstudy.name());
            QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
            if (qsr == null || qsr.getSubScoreResults() == null) {
                logger.error("processeCourseQuestionResult Error,userId={},questionID={},userAnswer={}", student.getId(), result.getQid(), userAnswer);
                return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo())
                        .setErrorCode(BusinessErrorType.DEFAULT.getCode());
            }

            List<List<String>> answer = new ArrayList<>();
            List<List<Boolean>> subMaster = new ArrayList<>();
            for (SubScoreResult ssr : qsr.getSubScoreResults()) {
                answer.add(ssr.getStandardAnswer());
                subMaster.add(ssr.getIsRight());
            }

            TeachingDiagnosisQuestionResult questionResult = new TeachingDiagnosisQuestionResult();
            questionResult.setCourseId(result.getExtra().getCourseId());
            questionResult.setMaster(qsr.getIsRight());
            questionResult.setQuestionId(result.getQid());
            questionResult.setTaskId(result.getExtra().getTaskId());
            questionResult.setStudentId(student.getId());
            questionResult.setUserAnswer(userAnswer);
            questionResult.setDuration(result.getExamResults().get(0).getFinishTime());
            MapMessage res = teachingDiagnosisServiceClient.saveCourseQuestionResult(questionResult, result.getLast());
            if (!res.isSuccess()) {
                return res;
            }
            res.put("questionId", result.getQid());
            res.put("questionInfo", MapUtils.m("answers", answer,
                    "userAnswers", userAnswer, "subMaster", subMaster, "master", qsr.getIsRight()));
            return res;
        } catch (Exception e) {
            logger.error("processeCourseQuestionResult Error,userId={},result={}", student.getId(), result, e);
            return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo())
                    .setErrorCode(BusinessErrorType.DEFAULT.getCode());
        }
    }

    private StudentDetail fetchStudent() {
        User user = currentUser();
        if (user == null || (!user.isParent() && !user.isStudent())) return null;

        StudentDetail student;
        if (user.isParent()) {
            Long studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"), 0L);
            if (studentId.equals(0L)) studentId = getRequestLong("sid");
            if (studentId.equals(0L)) return null;
            student = studentLoaderClient.loadStudentDetail(studentId);
        } else {
            student = user instanceof StudentDetail ? (StudentDetail) user : currentStudentDetail();
        }
        return student;
    }
}
