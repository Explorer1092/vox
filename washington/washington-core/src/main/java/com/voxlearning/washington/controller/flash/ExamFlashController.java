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

package com.voxlearning.washington.controller.flash;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusDubbing;
import com.voxlearning.utopia.service.newhomework.api.entity.UploaderResourceLibrary;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.ByIdsReq;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.ViewHintReq;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.api.mapper.QuestionMapper;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.TikuStrategy;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.net.message.exam.*;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("exam/flash")
@Slf4j
public class ExamFlashController extends AbstractController {

    @Inject
    private TikuStrategy tikuStrategy;

    // 通过试题ID列表查询试题列表
    @RequestMapping(value = "load/question/byids.vpage")
    @ResponseBody
    public MapMessage loadExamQuestionByIds() {
        GetQuestionByIdsRequest flashReq = getRequestObject(GetQuestionByIdsRequest.class);
        if (flashReq == null) {// 后面会出NPE，这里提前检查
            // 不确定前端能否正确handle error message，需要谭sir确认
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error");
        }
        if (noAccessPermission(currentUser())) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        // 在做数据提交的时候做一下处理
        Map<String, QuestionMapper> resultMap = questionLoaderClient.loadQuestionMapperByQids(flashReq.ids, false, false, true);
        return MapMessage.successMessage().add("result", resultMap);
    }

    // 通过试题docId列表查询试题列表
    @RequestMapping(value = "load/question/bydocids.vpage")
    @ResponseBody
    public MapMessage loadExamQuestionByDocIds() {
        GetQuestionByIdsRequest flashReq = getRequestObject(GetQuestionByIdsRequest.class);
        if (flashReq == null) {
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error");
        }
        if (noAccessPermission(currentUser())) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        // 在做数据提交的时候做一下处理
        Map<String, QuestionMapper> resultMap = questionLoaderClient.loadQuestionMapperByDocIds(flashReq.ids);
        return MapMessage.successMessage().add("result", resultMap);
    }

    // 通过试题ID列表查询试题列表
    @RequestMapping(value = "load/newquestion/byids.vpage")
    @ResponseBody
    public MapMessage loadQuestionByIds() {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        GetQuestionByIdsRequest flashReq = getRequestObject(GetQuestionByIdsRequest.class);
        if (flashReq == null) {
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error");
        }
        if (noAccessPermission(currentUser())) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        return tikuStrategy.loadQuestionFilterAnswersByIds(flashReq.ids, flashReq.containsAnswer);
    }

    // 通过试卷ID取试卷模块内容
    @RequestMapping(value = "load/paper/parts/byid.vpage")
    @ResponseBody
    public MapMessage loadPaperById() {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        GetPaperByIdRequest flashReq = getRequestObject(GetPaperByIdRequest.class);
        if (flashReq == null) {
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER_NOT_JSON);
        }

        return paperLoaderClient.loadPaperPartsByDocid(flashReq.id);
    }

    // 通过试卷ID取试卷模块内容 -- 模考专用
    @RequestMapping(value = "load/newexam/paper/parts/byid.vpage")
    @ResponseBody
    public MapMessage loadPaperByIdForNewExam() {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        GetPaperByIdRequest flashReq = getRequestObject(GetPaperByIdRequest.class);
        if (flashReq == null) {
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER_NOT_JSON);
        }

        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(flashReq.id);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
        }
        return MapMessage.successMessage().add("result", newPaper.getParts());
    }

    // 通过试题ID列表查询试题列表-- afenti专用
    @RequestMapping(value = "load/question/afentibyids.vpage")
    @ResponseBody
    public MapMessage loadExamQuestionByIdsForAfenti() {
        GetQuestionByIdsRequest flashReq = getRequestObject(GetQuestionByIdsRequest.class);
        if (flashReq == null) {// 后面会出NPE，这里提前检查
            // 不确定前端能否正确handle error message，需要谭sir确认
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error");
        }
        return MapMessage.successMessage().add("result", questionLoaderClient.loadQuestionMapperByQidsExcludeDisabled(flashReq.ids));
    }

    // 通过试题ID列表查询试题对应的视频列表 -- afenti 错题中心列表
    @RequestMapping(value = "load/video/afentibyids.vpage")
    @ResponseBody
    public MapMessage loadVideoByIdsForAfenti() {
        GetQuestionByIdsRequest flashReq = getRequestObject(GetQuestionByIdsRequest.class);
        if (flashReq == null) {
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error");
        }
        return MapMessage.successMessage().add("result", afentiLoaderClient.loadQuestionVideosByQuestionIds(flashReq.ids));
    }

    // 保存假期作业结果中的应试部分
    @RequestMapping(value = "process/vh/result.vpage")
    @ResponseBody
    public MapMessage processVacationHomeworkResult() {
        return MapMessage.errorMessage("功能已下线");
    }

    // 保存作业结果中的应试部分
    @RequestMapping(value = "process/homework/result.vpage")
    @ResponseBody
    public MapMessage processHomeworkResult() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 保存英语测验结果
     */
    @RequestMapping(value = "process/quiz/result.vpage")
    @ResponseBody
    public MapMessage processQuizResult() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 处理老作业数据接口
     * 用于需要做判分及预览用，题库方面用的较多。
     */
    @RequestMapping(value = "process/old/homework/result.vpage")
    @ResponseBody
    public MapMessage processOldHomeworkResult() {
        SaveExamResultRequest reqObj = getRequestObject(SaveExamResultRequest.class);

        String questionStr = getRequestString("questionjson");
        NewQuestion question = JsonUtils.fromJson(questionStr, NewQuestion.class);

        if (reqObj == null) {
            return MapMessage.errorMessage("提交结果数据异常");
        }
        HomeworkExamResultRequest her = reqObj.result;
        List<QuestionResultMapper> qrList = her.getHomeworkExamResults();
        Map<String, Map<String, Object>> result = new HashMap<>();
        try {
            //计算试题分数
            for (QuestionResultMapper qr : qrList) {
                UserAnswerMapper um = new UserAnswerMapper(qr.getExamId(), 200D, qr.getAnswer(), true);
                um.setUserAgent(getRequest().getHeader("User-Agent"));
                um.setUserId(0L);
                um.setHomeworkId("process/old/homework");
                um.setHomeworkType("process/old/homework");
                // 用于自定义的判分校验
                if (Objects.nonNull(question)) {
                    um.setNewQuestion(question);
                }
                QuestionScoreResult questionScoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(um);
                List<List<String>> answer = new ArrayList<>();
                List<List<Boolean>> subMaster = new ArrayList<>();
                for (SubScoreResult ssr : questionScoreResult.getSubScoreResults()) {
                    answer.add(ssr.getStandardAnswer());
                    subMaster.add(ssr.getIsRight());
                }
                result.put(qr.getExamId(), MiscUtils.m("fullScore", 10, "score", questionScoreResult.getTotalScore(), "answers", answer, "userAnswers", qr.getAnswer(), "subMaster", subMaster, "master", questionScoreResult.getIsRight()));
            }

        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage();
            }
            return MapMessage.errorMessage("上传英语" + her.getLearningType() + "结果失败").setErrorCode(ex.getMessage());
        }

        return MapMessage.successMessage().add("result", result);
    }

    /**
     * 用于做判分及预览用，题库和CRM方面用。
     */
    @RequestMapping(value = "review/question.vpage")
    @ResponseBody
    public MapMessage reviewQuestion() {
        SaveHomeworkResultRequest reqObj = getRequestObject(SaveHomeworkResultRequest.class);

        String questionStr = getRequestString("questionjson");
        NewQuestion question = JsonUtils.fromJson(questionStr, NewQuestion.class);

        if (Objects.isNull(question)) {
            question = questionLoaderClient.loadQuestionsIncludeDisabled(Collections.singletonList(reqObj.getQuestionId())).get(reqObj.getQuestionId());
        }
        List<Integer> typeIds = question.getContent().getSubContents().stream().map(NewQuestionsSubContents::getSubContentTypeId).collect(Collectors.toList());
        Boolean isOral = questionContentTypeLoaderClient.isOral(typeIds);
        if (isOral) {
            return MapMessage.successMessage("口语题");
        }

        if (reqObj == null) {
            return MapMessage.errorMessage("提交结果数据异常");
        }
        Map<String, Map<String, Object>> result = new HashMap<>();
        try {
            UserAnswerMapper um = new UserAnswerMapper(reqObj.getQuestionId(), 200D, reqObj.getAnswer(), true);
            um.setUserAgent(getRequest().getHeader("User-Agent"));
            um.setUserId(0L);
            um.setHomeworkId("review/question");
            um.setHomeworkType("review/question");
            // 用于自定义的判分校验
            if (StringUtils.isNotBlank(questionStr)) {
                um.setNewQuestion(question);
            }
            QuestionScoreResult questionScoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(um);
            List<List<String>> answer = new ArrayList<>();
            List<List<Boolean>> subMaster = new ArrayList<>();
            List<Double> subScore = new ArrayList<>();
            for (SubScoreResult ssr : questionScoreResult.getSubScoreResults()) {
                answer.add(ssr.getStandardAnswer());
                subMaster.add(ssr.getIsRight());
                subScore.add(ssr.getScore());
            }
            result.put(reqObj.getQuestionId(), MiscUtils.m(
                    "fullScore", 10,
                    "score", questionScoreResult.getTotalScore(),
                    "subScore", subScore,
                    "answers", answer,
                    "userAnswers", reqObj.getAnswer(),
                    "subMaster", subMaster,
                    "master", questionScoreResult.getIsRight()));

        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage();
            }
            return MapMessage.errorMessage("上传英语" + reqObj.getLearningType() + "结果失败").setErrorCode(ex.getMessage());
        }
        return MapMessage.successMessage().add("result", result);
    }

    /**
     * 提交作业结果
     * 单题提交
     *
     * @return
     */
    @RequestMapping(value = "newhomework/processresult.vpage")
    @ResponseBody
    public MapMessage processNewHomeworkResult() {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_NEW_HOMEWORK_RESULT", "newhomework/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        SaveHomeworkResultRequest result = getRequestObject(SaveHomeworkResultRequest.class);
        if (result == null || StringUtils.isBlank(result.getHomeworkId()) || StringUtils.isBlank(result.getQuestionId())) {
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
        }

        try {
            return homeworkResultProcessor.processSaveHomeworkResultRequest(user, result, getRequest(), getWebRequestContext());
        } catch (Exception ex) {
            logger.error("Failed to save user {} homework result", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    // 查看提示信息-上报数据
    @RequestMapping(value = "newhomework/viewhint.vpage")
    @ResponseBody
    public MapMessage viewHint() {
        User user = getHomeworkUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "NEW_HOMEWORK_VIEW_HINT", "newhomework/viewhint.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        ViewHintReq request = getRequestObject(ViewHintReq.class);
        if (request == null || StringUtils.isBlank(request.getHomeworkId()) || StringUtils.isBlank(request.getQuestionId()))
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(request));

        newHomeworkQueueServiceClient.interventionViewhintProducer(user.getId(), request);
        return MapMessage.successMessage();
    }

    /**
     * 批量提交作业结果
     *
     * @return
     */
    @RequestMapping(value = "newhomework/batch/processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage batchProcessNewHomeworkResult() {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_NEW_HOMEWORK_RESULT", "newhomework/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        SaveNewHomeworkResultRequest result = getRequestObject(SaveNewHomeworkResultRequest.class);
        if (result == null || StringUtils.isBlank(result.getHomeworkId())) {
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
        }
        // 新绘本不一定必须有习题（可以只选口语）
        // 纸质口算没有题目
        boolean mustHaveHomeworkAnswers = !ObjectiveConfigType.LEVEL_READINGS.name().equals(result.getObjectiveConfigType())
                && !ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name().equals(result.getObjectiveConfigType())
                && !ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.name().equals(result.getObjectiveConfigType());
        if (mustHaveHomeworkAnswers && CollectionUtils.isEmpty(result.getStudentHomeworkAnswers())) {
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
        }

        try {
            MapMessage msg = homeworkResultProcessor.processSaveNewHomeworkResultRequest(user, result, getRequest(), getWebRequestContext());
            if (msg.isSuccess()) {
                return MapMessage.successMessage().add("result", msg.get("result"));
            } else {
                String errorCode = msg.getErrorCode();
                if (StringUtils.equals(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_FINISHED, errorCode)) {
                    return MapMessage.errorMessage("练习已完成").setErrorCode(errorCode);
                }
                return MapMessage.errorMessage("提交结果失败").setErrorCode(msg.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error("Failed to save user {} homework result", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    // 订正作业的保存结果
    @RequestMapping(value = "newhomework/correct/processresult.vpage")
    @ResponseBody
    public MapMessage correctProcessReuslt() {
        return MapMessage.successMessage();
    }

    // 完成作业得分
    @RequestMapping(value = "newhomework/score.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkScore() {
        User user = getHomeworkUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        StudentDetail studentDetail = currentStudentDetail();
        String homeworkId = getRequestParameter("homeworkId", "");
        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), false);
        if (newHomeworkResult != null && newHomeworkResult.getFinishAt() != null) {
            Integer integral = newHomeworkResult.getIntegral();
            // 20170410日前newHomeworkResult.integral参数是没有的。所以需要做兼容处理
            if (integral == null) {
                integral = newHomeworkResultLoaderClient.homeworkIntegral(newHomework.isHomeworkTerminated(), newHomeworkResult);
                //看看是否有奖励活动
                integral = newHomeworkResultLoaderClient.generateFinishHomeworkActivityIntegral(integral, newHomework, null);
            }
            return MapMessage.successMessage().add("integral", integral).add("subject", newHomework.getSubject());
        } else {
            return MapMessage.errorMessage("正在生成分数请稍等");
        }
    }

    /**
     * 用于判断主观作答题型，在PC和mobile切换时的文件是否上传校验
     * xueosng.zhang 2016-03-09
     */
    @RequestMapping(value = "newhomework/subjectivefiles.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage validateSubjective() {
        User user = getHomeworkUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        StudentDetail studentDetail = currentStudentDetail();
        String homeworkId = getRequestParameter("hid", "");
        String questionId = getRequestParameter("qid", "");
        ObjectiveConfigType type = ObjectiveConfigType.valueOf(getRequestParameter("type", ""));
        String learningType = getRequestParameter("learningType", "homework");
        StudyType studyType = StudyType.of(learningType);
        if (studyType == StudyType.vacationHomework) {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            return vacationHomeworkServiceClient.loadSubjectiveFiles(homeworkId, type, questionId);
        } else {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), true);
            try {
                LinkedHashMap<String, String> answerMap = newHomeworkResult.getPractices().get(type).getAnswers();
                if (MapUtils.isEmpty(answerMap)) {
                    LinkedHashMap<String, String> tempAnswerMap = new LinkedHashMap<>();
                    LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResult.getPractices().get(type).getAppAnswers();
                    if (MapUtils.isNotEmpty(appAnswers)) {
                        appAnswers.values().forEach(o -> tempAnswerMap.putAll(o.getAnswers()));
                    }
                    answerMap = tempAnswerMap;
                }
                String processId = answerMap.get(questionId);
                if (StringUtils.isNotBlank(processId)) {
                    NewHomeworkProcessResult processResult = newHomeworkProcessResultLoaderClient.load(homeworkId, processId);
                    return MapMessage.successMessage().add("files", processResult.getFiles());
                }
            } catch (Exception ignored) {
            }
        }
        return MapMessage.successMessage().add("files", new ArrayList<>());
    }

    // 保存考试结果
    @RequestMapping(value = "newexam/processresult.vpage")
    @ResponseBody
    public MapMessage processNewExamResult() {
        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_NEW_EXAM_RESULT", "newexam/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        SaveNewExamResultRequest result = getRequestObject(SaveNewExamResultRequest.class);
        if (result == null || StringUtils.isBlank(result.getNewExamId()) || StringUtils.isBlank(result.getQuestionId()) || StringUtils.isBlank(result.getQuestionDocId()))
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));

        StudyType studyType = StudyType.of(result.getLearningType());
        if (StudyType.examination != studyType) return MapMessage.errorMessage("学习类型异常" + JsonUtils.toJson(result));


        NewExamResultContext context = new NewExamResultContext();
        context.setUserId(user.getId());
        context.setUser(user);
        context.setNewExamId(result.getNewExamId());
        context.setLearningType(studyType);
        context.setPaperId(result.getPaperId());
        if (result.getPartId() == null) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", user.getId(),
                    "mod1", result.getNewExamId(),
                    "mod2", JsonUtils.toJson(result),
                    "op", "newExam process"
            ));
            return MapMessage.errorMessage("提交结果数据异常,请退出重试");
        }
        context.setPartId(result.getPartId());
        context.setQuestionId(result.getQuestionId());
        context.setQuestionDocId(result.getQuestionDocId());
        context.setClientType(result.getClientType());
        context.setClientName(result.getClientName());
        context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : getWebRequestContext().getRealRemoteAddress());
        context.setUserAgent(getRequest().getHeader("User-Agent"));
        context.setAnswer(result.getAnswer().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()));
        if (result.getDurationMilliseconds() == null) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", user.getId(),
                    "mod1", result.getNewExamId(),
                    "mod2", JsonUtils.toJson(result),
                    "op", "newExam process"
            ));
            return MapMessage.errorMessage("提交结果数据异常,请退出重试");
        }
        context.setDurationMilliseconds(result.getDurationMilliseconds() < 0 || result.getDurationMilliseconds() > 3600000 ? 3600000 : result.getDurationMilliseconds());//如果时间异常则算1小时
        if (CollectionUtils.isNotEmpty(result.getFileUrls())) {
            context.setFileUrls(result.getFileUrls().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(result.getOralScoreDetails())) {
            context.setOralScoreDetails(result.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()));
        }
        try {
            return atomicLockManager.wrapAtomic(newExamServiceClient)
                    .keys(result.getNewExamId(), user.getId())
                    .proxy()
                    .processorNewExamResult(context);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("数据处理中请稍等").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
            }
            return MapMessage.errorMessage("提交结果失败").setErrorCode(ex.getMessage());
        }
    }

    // 交卷
    @RequestMapping(value = "newexam/submit.vpage")
    @ResponseBody
    public MapMessage submitNewExam() {
        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        SubmitNewExamRequest result = getRequestObject(SubmitNewExamRequest.class);
        if (result == null || StringUtils.isBlank(result.getNewExamId())) {
            return MapMessage.errorMessage("交卷数据异常" + JsonUtils.toJson(result)).setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        } else {
            try {
                return atomicLockManager.wrapAtomic(newExamServiceClient)
                        .keys(result.getNewExamId(), user.getId())
                        .proxy()
                        .submitNewExam(result.getNewExamId(), user.getId(), result.getClientType(), result.getClientName());
            } catch (Exception ex) {
                if (ex instanceof DuplicatedOperationException) {
                    return MapMessage.errorMessage("数据处理中请稍等").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
                }
                return MapMessage.errorMessage("交卷失败").setErrorCode(ex.getMessage());
            }
        }
    }

    // 保存作业结果
    @RequestMapping(value = "question/errorlog/process.vpage")
    @ResponseBody
    public MapMessage processQuestionErrorLog() {
        User user = getHomeworkUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        SaveQuestionErrorLogRequest result = getRequestObject(SaveQuestionErrorLogRequest.class);
        StudyType studyType = StudyType.of(result.getStudyType());
        Map<String, String> info = new HashMap<>();
        if (StringUtils.isBlank(result.getQuestionId()) || StringUtils.isBlank(result.getErrorCode()))
            return MapMessage.errorMessage("没有题ID或者错误编码");
        info.put("questionId", result.getQuestionId());
        info.put("errorCode", result.getErrorCode());
        info.put("userId", SafeConverter.toString(user.getId()));
        if (StringUtils.isNoneBlank()) info.put("homeworkId", result.getHomeworkId());
        if (studyType != null) info.put("studyType", studyType.name());
        if (StringUtils.isNotBlank(result.getBookId())) info.put("bookId", result.getBookId());
        if (StringUtils.isNotBlank(result.getUnitId())) info.put("unitId", result.getUnitId());
        if (StringUtils.isNotBlank(result.getLessonId())) info.put("lessonId", result.getLessonId());
        if (StringUtils.isNotBlank(result.getSectionId())) info.put("sectionId", result.getSectionId());
        if (StringUtils.isNotBlank(result.getExamId())) info.put("examId", result.getExamId());
        if (StringUtils.isNotBlank(result.getPracticeId())) info.put("practiceId", result.getPracticeId());
        if (StringUtils.isNotBlank(result.getUnitRank())) info.put("unitRank", result.getUnitRank());
        if (StringUtils.isNotBlank(result.getRank())) info.put("rank", result.getRank());
        if (StringUtils.isNotBlank(result.getClientName())) info.put("clientName", result.getClientName());
        if (StringUtils.isNotBlank(result.getClientType())) info.put("clientType", result.getClientType());
        if (StringUtils.isNotBlank(result.getRemark())) info.put("remark", result.getRemark());
        info.put("userAgent", getRequest().getHeader("User-Agent"));
        Mode mode = RuntimeMode.current();
        info.put("env", mode.name());
        LogCollector.info("question-error-log", info);
        return MapMessage.successMessage();
    }

    /*
     *  家长作业报告改版2.0（英语一期）
     *  口语句子染红
     */
    @RequestMapping(value = "newhomework/processsyllable.vpage")
    @ResponseBody
    public MapMessage processSyllable() {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_NEW_HOMEWORK_SYLLABLE", "newhomework/processsyllable.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        NewHomeworkSyllable newHomeworkSyllable = getRequestObject(NewHomeworkSyllable.class);
        if (newHomeworkSyllable == null) {
            return MapMessage.errorMessage("提交的未达标音节数据错误");
        }
        if (StringUtils.isBlank(newHomeworkSyllable.getHomeworkId())) {
            return MapMessage.errorMessage("提交的未达标音节数据homeworkId为空");
        }

        String[] objects = newHomeworkSyllable.getHomeworkId().split("_");
        String object;
        if (objects.length == 1) {
            object = objects[0];
        } else {
            object = objects[1];
        }
        String day;
        try {
            ObjectId objectId = new ObjectId(object);
            Date createDate = objectId.getDate();
            //作业过期判断
            if (createDate.before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                return MapMessage.errorMessage("该作业已过期");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            day = sdf.format(createDate);
        } catch (Exception e) {
            return MapMessage.errorMessage("homeworkId格式错误");
        }
        if (StringUtils.isBlank(newHomeworkSyllable.getAudio())) {
            return MapMessage.errorMessage("提交的未达标音节数据audio为空");
        }
        if (CollectionUtils.isEmpty(newHomeworkSyllable.getLines())) {
            return MapMessage.errorMessage("提交的未达标音节数据lines为空");
        }
        newHomeworkSyllable.setUserId(user.getId());
        try {
            return newHomeworkServiceClient.processSyllable(newHomeworkSyllable, day);
        } catch (Exception ex) {
            logger.error("saveSyllable error, student {}, homeworkId {}, error {}", user.getId(), newHomeworkSyllable.getHomeworkId(),
                    ex.getMessage());
            return MapMessage.errorMessage("保存未达标音节结果数据异常");
        }
    }

    /**
     * 上传新绘本配音
     */
    @RequestMapping(value = "picturebookplus/uploaddubbing.vpage")
    @ResponseBody
    public MapMessage uploadPictureBookPlusDubbing() {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PICTURE_BOOK_PLUS_UPLOAD_DUBBING", "picturebookplus/uploaddubbing.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        String homeworkId = getRequestString("homeworkId");
        String pictureBookId = getRequestString("pictureBookId");
        String screenMode = getRequestString("screenMode");
        String contentsJson = getRequestString("contents");
        if (StringUtils.isEmpty(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (StringUtils.isEmpty(pictureBookId)) {
            return MapMessage.errorMessage("绘本id为空");
        }
        List<PictureBookPlusDubbing.Content> contents = JsonUtils.fromJsonToList(contentsJson, PictureBookPlusDubbing.Content.class);
        if (CollectionUtils.isEmpty(contents)) {
            return MapMessage.errorMessage("绘本内容为空");
        }
        return newHomeworkServiceClient.uploadPictureBookPlusDubbing(homeworkId, pictureBookId, user.getId(), contents, screenMode);
    }

    /**
     * 通过轻交互课程IDs查询课程 (将要废弃)
     * @deprecated 推荐使用 {@link ExamFlashController#lightInteractionCourseV2()}
     */
    @RequestMapping(value = "/light/interaction/course.vpage")
    @ResponseBody
    public MapMessage lightInteractionCourse() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "LIGHT_INTERACTION_COURSE", "light/interaction/course.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        ByIdsReq req = getRequestObject(ByIdsReq.class);

        if (req == null || CollectionUtils.isEmpty(req.getIds())) {
            return MapMessage.errorMessage("请求参数异常");
        }
        return MapMessage.successMessage().add("courseInfo", newHomeworkServiceClient.fetchLightInteractionCourse(req.getIds()));
    }

    //通过轻交互课程IDs查询课程
    @RequestMapping(value = "/light/interaction/v2/course.vpage")
    @ResponseBody
    public MapMessage lightInteractionCourseV2() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "LIGHT_INTERACTION_COURSE_V2", "/light/interaction/v2/course.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        ByIdsReq req = getRequestObject(ByIdsReq.class);

        if (req == null || CollectionUtils.isEmpty(req.getIds())) {
            return MapMessage.errorMessage("请求参数异常");
        }
        return MapMessage.successMessage().add("courseInfo", newHomeworkServiceClient.fetchLightInteractionCourseV2(req.getIds()));
    }

    //通过视频课程IDs查询课程
    @RequestMapping(value = "/video/course.vpage")
    @ResponseBody
    public MapMessage videoCourse() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "VIDEO_COURSE", "video/course.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        ByIdsReq req = getRequestObject(ByIdsReq.class);

        if (req == null || CollectionUtils.isEmpty(req.getIds())) {
            return MapMessage.errorMessage("请求参数异常");
        }
        return MapMessage.successMessage().add("courseInfo", newHomeworkServiceClient.fetchVideoCourse(req.getIds()));
    }

    /**
     * 纸质口算上报识别错误的题
     *
     * @return
     */
    @RequestMapping(value = "ocrmistakereport.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage ocrMistakeReport() {
        String hid = getRequestString("hid");
        String processId = getRequestString("pid");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        String imgId = getRequestString("imageId");
        Integer imgWidth = getRequestInt("imgWidth");
        Integer imgHeight = getRequestInt("imgHeight");
        String formStr = getRequestString("form");
        String subject = getRequestString("subject");
        String objectiveType = getRequestString("type");
        if (StringUtils.isEmpty(formStr)) {
            return MapMessage.errorMessage("缺失错题信息");
        }
        OcrMentalImageDetail.Form form = JsonUtils.fromJson(formStr, OcrMentalImageDetail.Form.class);
        String imgUrl = getRequestString("imgUrl");
        //如果是系统替换后的图片，从替换记录里面找原图片
        if (imgUrl.equals(NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_DEFAULT_IMG)) {
            MapMessage mapMessage = newHomeworkServiceClient.getOriginImageUrlByProcessId(processId);
            if (StringUtils.isNotEmpty(mapMessage.getInfo())) {
                imgUrl = mapMessage.getInfo();
            }
        }
        LogCollector.info("backend-general", MapUtils.map(
                "hid", hid,
                "processId", processId,
                "subject", subject,
                "objectiveType", objectiveType,
                "usertoken", sid,
                "imageId", imgId,
                "imgUrl", imgUrl,
                "imgWidth", imgWidth,
                "imgHeight", imgHeight,
                "form", form,
                "ip", getWebRequestContext().getRealRemoteAddress(),
                "op", "aiImageLogs",
                "env", "production"
        ));
        return MapMessage.successMessage();
    }

    /**
     * 趣味配音分享个人配音详情查询接口
     */
    @RequestMapping(value = "studentdubbingwithscoredetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage studentDubbingWithScoreDetail() {
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        try {
            String[] subHomeworkSegments = StringUtils.split(homeworkId, "_");
            String[] vacationHomeworkSegments = StringUtils.split(homeworkId, "-");
            MapMessage result = MapMessage.errorMessage("不存在的作业id");
            if (subHomeworkSegments.length == 3) {
                result = newHomeworkReportServiceClient.studentDubbingWithScoreDetail(homeworkId, studentId, dubbingId);
            } else if (vacationHomeworkSegments.length == 4) {
                result = vacationHomeworkReportLoaderClient.studentDubbingWithScoreDetail(homeworkId, studentId, dubbingId);
            }
            if (!result.isSuccess() || !result.containsKey("content") || result.get("content") == null) {
                return result;
            }
            Map<String, Object> content = (Map<String, Object>) result.get("content");
            if (content.containsKey("imageUrl") && content.get("imageUrl") != null && StringUtils.isNotEmpty(content.get("imageUrl").toString())) {
                content.put("imageUrl", getUserAvatarImgUrl(content.get("imageUrl").toString()));
            }
            return result;
        } catch (Exception ex) {
            logger.error("Failed to load studentDubbingWithScoreDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }

    /**
     * 纸质拍照改判
     */
    @RequestMapping(value = "ocrcorrect.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ocrMentalArithmeticCorrect() {
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String imgUrl = getRequestString("imgUrl");
        String boxJson = getRequestString("boxJson");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(imgUrl)) {
            return MapMessage.errorMessage("图片地址为空");
        }
        if (StringUtils.isBlank(boxJson)) {
            return MapMessage.errorMessage("坐标信息为空");
        }

        try {
            return newHomeworkServiceClient.ocrMentalArithmeticCorrect(studentId, homeworkId, imgUrl, boxJson);
        } catch (Exception ex) {
            logger.error("Failed to load ocrMentalArithmeticCorrect studentId{},homeworkId:{},imgUrl{},boxJson{},", studentId, homeworkId, imgUrl, boxJson, ex);
            return MapMessage.errorMessage("纸质拍照改判异常");
        }
    }
    /**
     * 上传资源
     *
     * @return
     */
    @RequestMapping(value = "uploaderresource.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage uploaderResource() {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "UPLOADER_SOURCE", "uploaderresource.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        String homeworkId = getRequestString("homeworkId");
        String keywork = getRequestString("keywork");
        String fileType = getRequestString("fileType");
        String source = getRequestString("source");
        String courseId = getRequestString("courseId");
        String url = getRequestString("url");

        UploaderResourceLibrary obj = new UploaderResourceLibrary();
        obj.setCourseId(courseId);
        obj.setHomeworkId(homeworkId);
        obj.setFileType(fileType);
        obj.setSource(source);
        obj.setKeywork(keywork);
        obj.setUrl(url);
        obj.setUserId(user.getId());
        return newHomeworkServiceClient.uploaderResourceLibrary(obj);
    }

    /**
     * 字词讲练-图文入韵预览
     * @return
     */
    @RequestMapping(value = "imagetextrhyme/view.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage imageTextRhymeView() {
        String stoneDataId = getRequestString("stoneDataId");
        if (StringUtils.isBlank(stoneDataId)) {
            return MapMessage.errorMessage("题包id为空");
        }
        String wordTeachModuleType = getRequestString("wordTeachModuleType");
        if (StringUtils.isBlank(wordTeachModuleType)) {
            return MapMessage.errorMessage("图文入韵模块类型为空");
        }
        WordTeachModuleType practiceType = WordTeachModuleType.of(wordTeachModuleType);
        if (!practiceType.equals(WordTeachModuleType.IMAGETEXTRHYME)) {
            return MapMessage.errorMessage("图文入韵模块类型错误");
        }

        try {
            return newHomeworkServiceClient.imageTextRhymeView(stoneDataId, practiceType);
        } catch (Exception ex) {
            logger.error("Failed to load studentImageTextRhymeDetail stoneDataId{},practiceType{}", stoneDataId, practiceType, ex);
            return MapMessage.errorMessage("获取图文入韵个人详情异常");
        }
    }

    /**
     * 字词讲练-图文入韵分享页详情
     * @return
     */
    @RequestMapping(value = "student/imagetextrhyme/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage studentImageTextRhymeDetail() {
        String homeworkId = getRequestString("homeworkId");
        Long studentId = getRequestLong("studentId");
        String stoneDataId = getRequestString("stoneDataId");
        String chapterId = getRequestString("chapterId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(stoneDataId)) {
            return MapMessage.errorMessage("题包id为空");
        }
        if (StringUtils.isBlank(chapterId)) {
            return MapMessage.errorMessage("篇章id为空");
        }

        try {
            return newHomeworkReportServiceClient.studentImageTextRhymeDetail(homeworkId, studentId, stoneDataId, chapterId);
        } catch (Exception ex) {
            logger.error("Failed to load studentImageTextRhymeDetail homeworkId:{},studentId{},stoneDataId{},chapterId{}", homeworkId, studentId, stoneDataId, chapterId, ex);
            return MapMessage.errorMessage("获取图文入韵个人详情异常");
        }
    }
}
