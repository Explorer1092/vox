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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Range;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.constant.ServerStatusType;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkQuestionAnswerRequest;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.net.message.exam.SaveNewHomeworkResultRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;

/**
 * Created by tanguohong on 2015/5/11.
 */
@Controller
@RequestMapping(value = "/v1/student")
@Slf4j
public class StudentHomeworkApiController extends AbstractStudentApiController {

    private static Range<Long> pointPracticeRange = Range.between(196L, 250L);              // 知识点应用练习
    private static Range<Long> multiplyPracticeRange = Range.between(347L, 358L);           // 乘法竖式练习
    private static Range<Long> dividePracticeRange = Range.between(359L, 369L);             // 除法竖式练习

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @RequestMapping(value = "/homework/go.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage beginHomework() {
        MapMessage resultMap = new MapMessage();

        try {
            String version = getRequestString(REQ_APP_NATIVE_VERSION);
            if (StringUtils.isBlank(version) || VersionUtil.compareVersion(version, "2.7.0.0") < 0) {
                validateRequired(REQ_HOMEWORK_ID, "作业ID");
                validateRequired(REQ_HOMEWORK_TYPE, "作业类型");
                validateRequest(REQ_HOMEWORK_ID, REQ_HOMEWORK_TYPE);
            } else {
                validateRequired(REQ_HOMEWORK_ID, "作业ID");
                validateRequest(REQ_HOMEWORK_ID);
            }

        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        // 应急预案，走commonConfigValue，判断用户是否在C类区域
        String cityCode = SafeConverter.toString(studentDetail.getCityCode());
        String serverStatus = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "server_status");
        if (StringUtils.isNoneBlank(serverStatus) && ServerStatusType.PART_UNUSABLE.getStatus().equals(serverStatus)) {
            String mainCityList = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "main_cities");
            if (StringUtils.isBlank(cityCode) || !mainCityList.contains(cityCode)) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_SERVICE_TEMPORARILY_UNAVAILIBLE);
                return resultMap;
            }
        }

        if (studentDetail.getClazz() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_CLAZZ_ERROR_MSG);
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        if (StringUtils.isBlank(homeworkId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_HOMEWORK_TYPE);
            return resultMap;
        }
//        Map<String, Object> homework = new HashMap<>();
        List<Map<String, Object>> unFinishPractices = new ArrayList<>();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String version = generateBigVersion(ver, studentDetail);
        resultMap.add(RES_PRACTICE_VERSION, version);
        resultMap.add(RES_NEWHOMEWORK, true);
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "NewIndexUrl")) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework != null && NewHomeworkType.OCR == newHomework.getType()) {
                NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), false);
                if (newHomeworkResult != null && newHomeworkResult.isFinished()) {
                    resultMap.add(RES_HTML5_NEWHOMEWORK_INDEX_URL, "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/foundation/index.vhtml");
                } else {
                    resultMap.add(RES_HTML5_NEWHOMEWORK_INDEX_URL, "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/foundation-growup/index.vhtml");
                }
            } else {
                resultMap.add(RES_HTML5_NEWHOMEWORK_INDEX_URL, "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/foundation/index.vhtml");
            }
        } else {
            resultMap.add(RES_HTML5_NEWHOMEWORK_INDEX_URL, generateVersionUrl("resources/apps/hwh5/homework/" + version + "/foundation/index.html"));
        }
        resultMap.add(RES_NEWHOMEWORK_INDEX_URL, UrlUtils.buildUrlQuery(fetchMainsiteUrlByCurrentSchema() + "/flash/loader/newhomework/index.vpage", MiscUtils.m("homeworkId", homeworkId)));
        resultMap.add(RES_HOMEWORK_SCORE_URL, UrlUtils.buildUrlQuery(fetchMainsiteUrlByCurrentSchema() + "/exam/flash/newhomework/score.vpage", MiscUtils.map("homeworkId", homeworkId)));

        String homeworkResultVersion = "HWResult";
        resultMap.add(RES_HOMEWORK_UNFINISH_PRACTICES, unFinishPractices);
//        resultMap.add(RES_HOMEWORK_ID, homework.get("homeworkId"));
//        resultMap.add(RES_HOMEWORK_FINISH_COUNT, homework.get("finishCount"));
//        resultMap.add(RES_HOMEWORK_COUNT, homework.get("practiceCount"));
        if (StringUtils.isBlank(ver) || VersionUtil.compareVersion(ver, "2.7.0.0") < 0) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentDetail.getId(),
                    "mod1", ver,
                    "mod2", version,
                    "mod3", getRequestString(REQ_SYS),
                    "op", "oldVersion"
            ));
            resultMap.add(RES_HTML5_HOMEWORK_RESULT_URL, generateVersionUrl("resources/apps/hwh5/homework/" + version + "/" + homeworkResultVersion + "/index.vhapp"));
        }
        resultMap.add(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());

        resultMap.add(RES_VOICE_RATIO, getVoiceRatio(studentDetail.getClazzLevel()));
        resultMap.add(RES_TIMEOUT_PARAM, getTimeoutParam());

        if (getGrayFunctionManagerClient().getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "17Student", "VoiceBack", true)) {
            resultMap.add(RES_NEED_BACKGROUND_SCORE, true);
        } else {
            resultMap.add(RES_NEED_BACKGROUND_SCORE, false);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 假期作业
     * @return
     */
    @RequestMapping(value = "/vh/go.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage beginVacationHomework() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_VACATION_HOMEWORK_PACKAGE_ID, "假期作业任务包ID");
            validateRequired(REQ_HOMEWORK_TYPE, "假期作业类型"); //暂无用
            validateRequest(REQ_VACATION_HOMEWORK_PACKAGE_ID, REQ_HOMEWORK_TYPE);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail student = getCurrentStudentDetail();
        if (student == null || student.getClazz() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_CLAZZ_ERROR_MSG);
            return resultMap;
        }
        String packageId = getRequestString(REQ_VACATION_HOMEWORK_PACKAGE_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String version = generateBigVersion(ver, student);
        resultMap.add(RES_PRACTICE_VERSION, version);
        resultMap.add(RES_NEWHOMEWORK, true);
        resultMap.add(RES_VACATION_HOMEWORK_PACKAGE_ID, packageId);
        //十一作业任务临时处理
//        if("task".equals(packageId)){
//            resultMap.add(RES_HTML5_NEWHOMEWORK_INDEX_URL, "/view/mobile/student/wonderland/openapp?url=/resources/apps/hwh5/growingworld/v100/index.html%23NationalDay");
//        }else {
        resultMap.add(RES_HTML5_NEWHOMEWORK_INDEX_URL, generateVersionUrl("resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/vacation-homework/index.vhtml"));
//        }

        resultMap.add(RES_VOICE_RATIO, getVoiceRatio(student.getClazzLevel()));
        resultMap.add(RES_TIMEOUT_PARAM, getTimeoutParam());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/exam/go.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage beginNewExam() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_NEWEXAM_ID, "考试ID");
            validateRequest(REQ_NEWEXAM_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_CLAZZ_ERROR_MSG);
            return resultMap;
        }

        String newExamId = getRequestString(REQ_NEWEXAM_ID);
        NewExam newExam = newExamLoaderClient.load(newExamId);
        String html5_newexam_index_url = "/view/mobile/student/junior/newexamv2/index";
        if (newExam != null && StringUtils.isNotBlank(newExam.getPaperId())) {
            String ver = getRequestString(REQ_APP_NATIVE_VERSION);
            String version = generateBigVersion(ver, studentDetail);
            html5_newexam_index_url = generateVersionUrl("resources/apps/hwh5/examination/" + version + "/entry/index.html");

        }
        resultMap.add(RES_HTML5_NEWEXAM_INDEX_URL, html5_newexam_index_url);
        resultMap.add(RES_NEWEXAM_INDEX_URL, "/flash/loader/newexam/index.vpage");
        resultMap.add(RES_NEWEXAM_ID, newExamId);
        resultMap.add(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        resultMap.add(RES_VOICE_RATIO, getVoiceRatio(studentDetail.getClazzLevel()));
        resultMap.add(RES_TIMEOUT_PARAM, getTimeoutParam());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/exam/view.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage viewExam() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_NEWEXAM_ID, "考试ID");
            validateRequest(REQ_NEWEXAM_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_CLAZZ_ERROR_MSG);
            return resultMap;
        }

        String newExamId = getRequestString(REQ_NEWEXAM_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String version = generateBigVersion(ver, studentDetail);
        resultMap.add(RES_HTML5_NEWEXAM_INDEX_URL, generateVersionUrl("resources/apps/hwh5/examination/" + version + "/exam/index.html"));
        resultMap.add(RES_RENDER_TYPE, "student_history");
        resultMap.add(RES_NEWEXAM_INDEX_URL, "/flash/loader/newexam/index.vpage");
        resultMap.add(RES_NEWEXAM_ID, newExamId);
        resultMap.add(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        resultMap.add(RES_VOICE_RATIO, getVoiceRatio(studentDetail.getClazzLevel()));
        resultMap.add(RES_TIMEOUT_PARAM, getTimeoutParam());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/homework/month/report.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkMonthRank() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            } else {
                resultMap.add(RES_SURPASS_COUNT, 0);
                resultMap.add(RES_HOMEWORK_AVG_SCORE, 0);
                resultMap.add(RES_QUESTION_NUM, 0);
                resultMap.add(RES_HOMEWORK_COUNT, 0);
                resultMap.add(RES_HOMEWORK_FINISH_COUNT, 0);
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            }
            return resultMap;
        }

        //緩存1小时
//        Map<Long, Map<String, MutableInt>> studentInfoMap = newHomeworkCacheClient.cacheSystem.CBS.flushable.wrapCache(newHomeworkResultLoaderClient)
//                .expiration(60 * 60 * 1)
//                .keyPrefix("studentHomeworkCurrentMonthRank")
//                .keys(studentDetail.getClazzId(), studentDetail.getId())
//                .proxy().getCurrentMonthHomeworkRankByGroupId(studentDetail.getClazzId(), studentDetail.getId());

        Map<Long, Map<String, Integer>> studentInfoMap = newHomeworkResultLoaderClient.getCurrentMonthHomeworkRankByGroupId(studentDetail.getId());
        Map<String, Integer> studentInfo = studentInfoMap.get(studentDetail.getId());
        if (studentInfoMap.isEmpty() || studentInfo == null) {
            resultMap.add(RES_SURPASS_COUNT, 0);
            resultMap.add(RES_HOMEWORK_AVG_SCORE, 0);
            resultMap.add(RES_QUESTION_NUM, 0);
            resultMap.add(RES_HOMEWORK_COUNT, 0);
            resultMap.add(RES_HOMEWORK_FINISH_COUNT, 0);
        } else {
            int surpassCount = 0;
            int myAvgScore = 0;
            if (studentInfo.get("finishCount").intValue() > 0) {
                myAvgScore = new BigDecimal(studentInfo.get("finishTotalScore").intValue()).divide(new BigDecimal(studentInfo.get("finishCount").intValue()), 0, BigDecimal.ROUND_HALF_UP).intValue();
            }

            for (Long uid : studentInfoMap.keySet()) {
                Map<String, Integer> si = studentInfoMap.get(uid);
                int avgScore = 0;
                if (si.get("finishCount").intValue() > 0) {
                    avgScore = new BigDecimal(si.get("finishTotalScore").intValue()).divide(new BigDecimal(si.get("finishCount").intValue()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                }
                if (avgScore < myAvgScore) {
                    surpassCount++;
                }
            }
            resultMap.add(RES_SURPASS_COUNT, surpassCount);
            resultMap.add(RES_HOMEWORK_AVG_SCORE, myAvgScore);
            resultMap.add(RES_QUESTION_NUM, studentInfo.get("questionNum"));
            resultMap.add(RES_HOMEWORK_COUNT, studentInfo.get("homeworkCount"));
            resultMap.add(RES_HOMEWORK_FINISH_COUNT, studentInfo.get("finishCount"));
        }
        resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/newhomework/do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage doNewHomework() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_LEARNING_TYPE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);
        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework) {
            return failMessage("不支持的学习类型");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
            List<DubbingSummaryResult> dubbingSummaryResults;
            if (studyType == StudyType.homework) {
                dubbingSummaryResults = dubbingHomeworkServiceClient.getDubbingSummerInfo(homeworkId, studentDetail.getId(), objectiveConfigType);
            } else {
                dubbingSummaryResults = dubbingHomeworkServiceClient.getVacationDubbingSummerInfo(homeworkId, studentDetail.getId(), objectiveConfigType);
            }

            if (CollectionUtils.isEmpty(dubbingSummaryResults)) {
                return failMessage("作业内容为空");
            }
            List<Map<String, Object>> dubbingList = new ArrayList<>();
            for (DubbingSummaryResult summaryResult : dubbingSummaryResults) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_RESULT_DUBBING_ID, summaryResult.getDubbingId());
                map.put(RES_RESULT_DUBBING_NAME, summaryResult.getDubbingName());
                map.put(RES_RESULT_DUBBING_COVER_IMG, summaryResult.getCoverUrl());
                map.put(RES_RESULT_DUBBING_SUMMARY, summaryResult.getVideoSummary());
                map.put(RES_RESULT_DUBBING_VIDEO_URL, summaryResult.getVideoUrl());
                map.put(RES_RESULT_DUBBING_SYNTHETIC, summaryResult.getSynthetic());
                map.put(RES_RESULT_DUBBING_IS_FINISHED, summaryResult.getFinished());
                map.put(RES_CLAZZ_LEVEL, summaryResult.getLevel());
                List<Map<String, Object>> keyWords = summaryResult.getKeyWords();
                if (CollectionUtils.isNotEmpty(keyWords)) {
                    List<Map<String, Object>> keyWordList = new ArrayList<>();
                    for (Map keyWord : keyWords) {
                        keyWordList.add(MapUtils.m(
                                RES_RESULT_DUBBING_KEY_WORD_CHINESE, SafeConverter.toString(keyWord.get("chineseWord")),
                                RES_RESULT_DUBBING_KEY_WORD_ENGLISH, SafeConverter.toString(keyWord.get("englishWord"))
                        ));
                    }
                    map.put(RES_RESULT_DUBBING_KEY_WORD_LIST, keyWordList);
                }
                List<Map<String, Object>> keyGrammars = summaryResult.getKeyGrammars();
                if (CollectionUtils.isNotEmpty(keyGrammars)) {
                    List<Map<String, Object>> keyGrammarList = new ArrayList<>();
                    for (Map keyGrammar : keyGrammars) {
                        keyGrammarList.add(MapUtils.m(
                                RES_RESULT_DUBBING_KEY_GRAMMAR_NAME, SafeConverter.toString(keyGrammar.get("grammarName")),
                                RES_RESULT_DUBBING_KEY_GRAMMAR_EXAMPLE, SafeConverter.toString(keyGrammar.get("exampleSentence"))
                        ));
                    }
                    map.put(RES_RESULT_DUBBING_KEY_GRAMMAR_LIST, keyGrammarList);
                }
                map.put(RES_RESULT_DUBBING_TOPIC_LIST, summaryResult.getTopics());
                dubbingList.add(map);
            }
            return successMessage().add(RES_RESULT_DUBBING_LIST, dubbingList);
        }
        return failMessage("不支持的作业形式");
    }

    @RequestMapping(value = "/newhomework/dubbing/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadNewHomeworkDubbingQuestions() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_DUBBING_ID, "配音id");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_DUBBING_ID, REQ_LEARNING_TYPE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);
        String model = getRequestString(REQ_MODEL);
        String systemVersion = getRequestString(REQ_SYSTEM_VERSION);
        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework) {
            return failMessage("不支持的学习类型");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }

        if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
            HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
            request.setHomeworkId(homeworkId);
            request.setObjectiveConfigType(ObjectiveConfigType.of(objectiveConfigType));
            request.setVideoId(dubbingId);
            Map<String, Object> questionMap;
            if (studyType == StudyType.homework) {
                questionMap = newHomeworkLoaderClient.loadHomeworkQuestions(request);
            } else {
                questionMap = vacationHomeworkLoaderClient.loadHomeworkQuestions(homeworkId, ObjectiveConfigType.of(objectiveConfigType), null, null, dubbingId, null);
            }
            if (MapUtils.isEmpty(questionMap)) {
                return failMessage("获取题目信息失败");
            }
            MapMessage message = successMessage();
            Map<String, Object> dubbingInfoMap = new LinkedHashMap<>();
            dubbingInfoMap.put(RES_RESULT_DUBBING_ID, SafeConverter.toString(questionMap.get("dubbingId")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_NAME, SafeConverter.toString(questionMap.get("dubbingName")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_VIDEO_URL, SafeConverter.toString(questionMap.get("videoUrl")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_BACKGROUND_VIDEO_URL, SafeConverter.toString(questionMap.get("backgroundMusicUrl")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_COVER_IMG, SafeConverter.toString(questionMap.get("coverImgUrl")));
            List<Map<String, Object>> sentences = (List<Map<String, Object>>) questionMap.get("sentenceList");
            if (CollectionUtils.isNotEmpty(sentences)) {
                List<Map<String, Object>> sentenceList = new ArrayList<>();
                for (Map<String, Object> sentence : sentences) {
                    sentenceList.add(MapUtils.m(
                            RES_RESULT_SENTENCE_CHINESE_CONTENT, SafeConverter.toString(sentence.get("sentenceChineseContent")),
                            RES_RESULT_SENTENCE_ENGLISH_CONTENT, SafeConverter.toString(sentence.get("sentenceEnglishContent")),
                            RES_RESULT_SENTENCE_VIDEO_START, SafeConverter.toString(sentence.get("sentenceVideoStart")),
                            RES_RESULT_SENTENCE_VIDEO_END, SafeConverter.toString(sentence.get("sentenceVideoEnd")),
                            RES_RESULT_QUESTION_ID, SafeConverter.toString(sentence.get("questionId"))
                    ));
                }
                dubbingInfoMap.put(RES_RESULT_SENTENCE_LIST, sentenceList);
            }
            message.add(RES_RESULT_DUBBING_INFO, dubbingInfoMap);
            message.add(RES_RESULT_DUBBING_ASYNCHRONOUS_SYNTHETIC, grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(getCurrentStudentDetail(), "DubbingSynthetic", "WhiteList"));
            message.add(RES_RESULT_DUBBING_SOFT_DECODING, dubbingSoftDecoding(model, systemVersion));
            return message;
        }
        return failMessage("不支持的作业形式");
    }

    private boolean dubbingSoftDecoding(String model, String systemVersion) {
        if (StringUtils.isBlank(model) || StringUtils.isBlank(systemVersion)) {
            return false;
        }
        String configContent = getPageBlockContentGenerator().getPageBlockContentHtml("student_homework", "dubbing_soft_decoding");
        if (StringUtils.isBlank(configContent)) {
            return false;
        }
        configContent = configContent.replace("\r", "").replace("\n", "").replace("\t", "");
        List<String> configList = JsonUtils.fromJsonToList(configContent, String.class);
        return !CollectionUtils.isEmpty(configList) && configList.contains(model + ":" + systemVersion);
    }

    @RequestMapping(value = "/newhomework/dubbing/questions/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadNewHomeworkDubbingQuestionsAnswer() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_DUBBING_ID, "配音id");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_DUBBING_ID, REQ_LEARNING_TYPE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);
        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework) {
            return failMessage("不支持的学习类型");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
            Map<String, Object> result;
            HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
            request.setHomeworkId(homeworkId);
            request.setObjectiveConfigType(ObjectiveConfigType.of(objectiveConfigType));
            request.setStudentId(studentDetail.getId());
            request.setVideoId(dubbingId);
            if (studyType == StudyType.homework) {
                result = newHomeworkLoaderClient.loadQuestionAnswer(request);
            } else {
                result = vacationHomeworkLoaderClient.loadQuestionAnswer(ObjectiveConfigType.of(objectiveConfigType), homeworkId, null, null, dubbingId, null);
            }

            if (MapUtils.isEmpty(result)) {
                return failMessage("获取答案失败");
            }
            MapMessage message = successMessage();
            message.add(RES_RESULT_DUBBING_ID, SafeConverter.toString(result.get("dubbingId")));
            message.add(RES_RESULT_DUBBING_NAME, SafeConverter.toString(result.get("dubbingName")));
            message.add(RES_RESULT_DUBBING_COVER_IMG, SafeConverter.toString(result.get("coverImgUrl")));
            message.add(RES_RESULT_DUBBING_VIDEO_URL, SafeConverter.toString(result.get("dubbingVideoUrl")));
            message.add(RES_RESULT_SENTENCE_COUNT, SafeConverter.toString(result.get("sentenceCount")));
            return message;
        }
        return failMessage("不支持的作业形式");
    }

    @RequestMapping(value = "/newhomework/batch/processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage batchProcessNewHomeworkResult() {
        try {
            validateRequired(REQ_HOMEWORK_RESULT_DATA, "答题数据");
            validateRequest(REQ_HOMEWORK_RESULT_DATA);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkResultData = getRequestString(REQ_HOMEWORK_RESULT_DATA);
        Map<String, Object> resultMap = JsonUtils.fromJson(homeworkResultData);
        if (MapUtils.isEmpty(resultMap)) {
            return failMessage("提交结果数据异常");
        }
        Student student = getCurrentStudent();
        SaveNewHomeworkResultRequest request = new SaveNewHomeworkResultRequest();
        request.setHomeworkId(SafeConverter.toString(resultMap.get(REQ_HOMEWORK_ID)));
        request.setObjectiveConfigType(SafeConverter.toString(resultMap.get(REQ_OBJECTIVE_CONFIG_TYPE)));
        request.setLearningType(SafeConverter.toString(resultMap.get(REQ_LEARNING_TYPE)));
        request.setConsumeTime(SafeConverter.toLong(resultMap.get(REQ_CONSUME_TIME)));
        request.setDubbingId(SafeConverter.toString(resultMap.get(REQ_DUBBING_ID)));
        request.setVideoUrl(SafeConverter.toString(resultMap.get(REQ_DUBBING_VIDEO_URL)));
        List<Map<String, Object>> answers = (List<Map<String, Object>>) resultMap.get(REQ_STUDENT_HOMEWORK_ANSWERS);
        if (CollectionUtils.isNotEmpty(answers)) {
            List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
            for (Map<String, Object> answer : answers) {
                String questionId = SafeConverter.toString(answer.get(REQ_QUESTION_ID));
                Long durationMilliseconds = SafeConverter.toLong(answer.get(REQ_DURATION_MILLISECONDS));
                List<List<String>> fileUrls = (List<List<String>>) answer.get(REQ_FILE_URLS);
                StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                studentHomeworkAnswer.setQuestionId(questionId);
                studentHomeworkAnswer.setDurationMilliseconds(durationMilliseconds);
                if (CollectionUtils.isNotEmpty(fileUrls)) {
                    studentHomeworkAnswer.setFileUrls(fileUrls);
                }
                studentHomeworkAnswers.add(studentHomeworkAnswer);
            }
            request.setStudentHomeworkAnswers(studentHomeworkAnswers);
        }
        if (!ObjectiveConfigType.DUBBING.name().equals(request.getObjectiveConfigType()) || !ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(request.getObjectiveConfigType())) {
            return failMessage("不支持的作业形式");
        }
        try {
            MapMessage message = homeworkResultProcessor.processSaveNewHomeworkResultRequest(student, request, getRequest(), getWebRequestContext());
            if (message.isSuccess()) {
                return successMessage();
            } else {
                return failMessage("提交结果失败").setErrorCode(message.getErrorCode());
            }
        } catch (Exception e) {
            return failMessage("提交结果数据异常");
        }
    }
}
