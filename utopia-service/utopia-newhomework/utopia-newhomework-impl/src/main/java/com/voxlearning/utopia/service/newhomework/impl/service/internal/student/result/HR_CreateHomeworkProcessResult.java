/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkProcessResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.*;

/**
 * @author Ruib
 * @author guohong.tan
 * @author xuesong.zhang
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class HR_CreateHomeworkProcessResult extends SpringContainerSupport implements HomeworkResultTask {
    @Inject private NewHomeworkProcessResultServiceImpl newHomeworkProcessResultService;

    @Override
    public void execute(HomeworkResultContext context) {
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())) {
            if (context.getPracticeId() == null) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST,
                        "op", "student homework result"
                ));
                context.errorResponse("homework practiceId:{} is null userId:{}", JsonUtils.toJson(context.getPracticeId()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                context.setTerminateTask(true);
            }
            processErrorQuestion(context);
        }

        if (ObjectiveConfigType.ORAL_COMMUNICATION == context.getObjectiveConfigType()
                && CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())
                ) {
            List<NewHomeworkProcessResult> results = buildOralCommunicationProcessResult(context, context.getStudentHomeworkAnswers());
            if (CollectionUtils.isEmpty(results)) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                        "param",JsonUtils.toJson(context.getStudentHomeworkAnswers()),
                        "op", "student homework result"
                ));
                context.getResult().put("NewHomeworkOralCommunication", MapUtils.m("hasError", true));
                context.errorResponse("homework studentHomeworkAnswers:{} is null userId:{}", JsonUtils.toJson(context.getStudentHomeworkAnswers()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
                context.setTerminateTask(true);
                return;
            }
            results = results.stream().filter(Objects::nonNull).filter(p -> StringUtils.isNotEmpty(p.getDialogId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(results) || results.size() != context.getStudentHomeworkAnswers().size()) {
                logger.error("HR_CreateHomeworkProcessResult_oral_communication context: {} ", JsonUtils.toJson(context));
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                        "param",JsonUtils.toJson(results),
                        "op", "student homework result"
                ));
                context.getResult().put("NewHomeworkOralCommunication", MapUtils.m("hasError", true));
                context.errorResponse("homework studentHomeworkAnswers:{} is null userId:{}", JsonUtils.toJson(context.getStudentHomeworkAnswers()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
                context.setTerminateTask(true);
                return;
            }
            newHomeworkProcessResultService.inserts(context.getHomeworkId(), results);
            LinkedHashMap<String, NewHomeworkProcessResult> tempQuestionMap = new LinkedHashMap<>();
            for (NewHomeworkProcessResult result : results) {
                tempQuestionMap.put(result.getDialogId(), result);
            }
            context.setProcessResult(tempQuestionMap);
            return;
        }

        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            List<NewHomeworkProcessResult> results = buildProcessResult(context, context.getStudentHomeworkAnswers(), context.getUserAnswerQuestionMap());
            newHomeworkProcessResultService.inserts(context.getHomeworkId(), results);
            LinkedHashMap<String, NewHomeworkProcessResult> tempQuestionMap = new LinkedHashMap<>();
            for (NewHomeworkProcessResult result : results) {
                tempQuestionMap.put(result.getQuestionId(), result);
            }
            context.setProcessResult(tempQuestionMap);
        }

        if ((ObjectiveConfigType.READING == context.getObjectiveConfigType() || ObjectiveConfigType.LEVEL_READINGS == context.getObjectiveConfigType())
                && CollectionUtils.isNotEmpty(context.getStudentHomeworkOralAnswers())) {
            List<NewHomeworkProcessResult> oralResults = buildProcessResult(context, context.getStudentHomeworkOralAnswers(), context.getUserAnswerQuestionMap());
            newHomeworkProcessResultService.inserts(context.getHomeworkId(), oralResults);

            LinkedHashMap<String, NewHomeworkProcessResult> tempOralQuestionMap = new LinkedHashMap<>();
            for (NewHomeworkProcessResult result : oralResults) {
                tempOralQuestionMap.put(result.getQuestionId(), result);
            }
            context.setProcessOralResult(tempOralQuestionMap);
        }

        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == context.getObjectiveConfigType() && CollectionUtils.isNotEmpty(context.getOcrMentalImageDetails())) {
            List<NewHomeworkProcessResult> ocrMentalResults = buildOcrMentalProcessResult(context, context.getOcrMentalImageDetails());
            newHomeworkProcessResultService.inserts(context.getHomeworkId(), ocrMentalResults);
            context.setOcrMentalProcessResults(ocrMentalResults);
        }

        if (ObjectiveConfigType.OCR_DICTATION == context.getObjectiveConfigType() && CollectionUtils.isNotEmpty(context.getOcrDictationImageDetails())) {
            List<NewHomeworkProcessResult> ocrDictationResults = buildOcrDictationProcessResult(context, context.getOcrDictationImageDetails());
            newHomeworkProcessResultService.inserts(context.getHomeworkId(), ocrDictationResults);
            context.setOcrDictationProcessResults(ocrDictationResults);
        }

        if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE == context.getObjectiveConfigType()
                && WordTeachModuleType.CHINESECHARACTERCULTURE == context.getWordTeachModuleType()
                && context.getCourseId() != null
                && context.getDuration() != null) {
            NewHomeworkProcessResult chineseCourseResult = buildChineseCourseProcessResult(context);
            newHomeworkProcessResultService.insert(chineseCourseResult);
            LinkedHashMap<String, NewHomeworkProcessResult> tempQuestionMap = new LinkedHashMap<>();
            tempQuestionMap.put(chineseCourseResult.getCourseId(), chineseCourseResult);
            context.setProcessResult(tempQuestionMap);
        }
    }

    private NewHomeworkProcessResult buildChineseCourseProcessResult(HomeworkResultContext context) {
        NewHomeworkProcessResult result = new NewHomeworkProcessResult();

        NewHomeworkProcessResult.ID id = new NewHomeworkProcessResult.ID(context.getHomework().getCreateAt());
        result.setId(id.toString());
        result.setType(context.getHomework().getType());
        result.setHomeworkTag(context.getHomework().getHomeworkTag());
        result.setClazzGroupId(context.getClazzGroupId());
        result.setUserId(context.getUserId());
        result.setHomeworkId(context.getHomeworkId());
        result.setBookId(context.getBookId());
        result.setUnitGroupId(context.getUnitGroupId());
        result.setUnitId(context.getUnitId());
        result.setLessonId(context.getLessonId());
        result.setSectionId(context.getSectionId());
        result.setSchoolLevel(context.getHomework().getSchoolLevel());
        result.setGrasp(true);
        result.setCourseId(context.getCourseId());
        result.setStoneId(context.getStoneId());
        result.setDuration(context.getDuration());
        result.setWordTeachModuleType(context.getWordTeachModuleType());
        result.setAdditions(context.getAdditions());
        result.setSubject(context.getSubject());
        result.setObjectiveConfigType(context.getObjectiveConfigType());
        result.setClientType(context.getClientType());
        result.setClientName(context.getClientName());

        return result;
    }

    /**
     * 纸质听写
     * @param context
     * @param ocrMentalImageDetails
     * @return
     */
    private List<NewHomeworkProcessResult> buildOcrDictationProcessResult(HomeworkResultContext context, List<OcrMentalImageDetail> ocrMentalImageDetails) {
        List<NewHomeworkProcessResult> results = new ArrayList<>();
        for (OcrMentalImageDetail ocrMentalImageDetail : ocrMentalImageDetails) {
            NewHomeworkProcessResult result = new NewHomeworkProcessResult();
            NewHomeworkProcessResult.ID id = new NewHomeworkProcessResult.ID(context.getHomework().getCreateAt());
            result.setId(id.toString());
            result.setType(context.getHomework().getType());
            result.setHomeworkTag(context.getHomework().getHomeworkTag());
            result.setClazzGroupId(context.getClazzGroupId());
            result.setUserId(context.getUserId());
            result.setHomeworkId(context.getHomeworkId());
            result.setBookId(context.getBookId());
            result.setUnitGroupId(context.getUnitGroupId());
            result.setUnitId(context.getUnitId());
            result.setLessonId(context.getLessonId());
            result.setSectionId(context.getSectionId());
            result.setSchoolLevel(context.getHomework().getSchoolLevel());
            result.setGrasp(true);
            result.setOcrDictationImageDetail(ocrMentalImageDetail);
            result.setSubject(context.getSubject());
            result.setObjectiveConfigType(context.getObjectiveConfigType());
            result.setClientType(context.getClientType());
            result.setClientName(context.getClientName());
            result.setAdditions(context.getAdditions());
            results.add(result);
        }
        return results;
    }

    private List<NewHomeworkProcessResult> buildOcrMentalProcessResult(HomeworkResultContext context, List<OcrMentalImageDetail> ocrMentalImageDetails) {
        List<NewHomeworkProcessResult> results = new ArrayList<>();

        Map<String, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> omadMap = processOcrMentalArithmeticDiagnosis(context, ocrMentalImageDetails);
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> symptomAnalysisMap = processOcrMentalArithmeticSymptomAnalysis(context, ocrMentalImageDetails);
        for (OcrMentalImageDetail ocrMentalImageDetail : ocrMentalImageDetails) {
            NewHomeworkProcessResult result = new NewHomeworkProcessResult();
            NewHomeworkProcessResult.ID id = new NewHomeworkProcessResult.ID(context.getHomework().getCreateAt());
            result.setId(id.toString());
            result.setType(context.getHomework().getType());
            result.setHomeworkTag(context.getHomework().getHomeworkTag());
            result.setClazzGroupId(context.getClazzGroupId());
            result.setUserId(context.getUserId());
            result.setHomeworkId(context.getHomeworkId());
            result.setBookId(context.getBookId());
            result.setUnitGroupId(context.getUnitGroupId());
            result.setUnitId(context.getUnitId());
            result.setLessonId(context.getLessonId());
            result.setSectionId(context.getSectionId());
            result.setSchoolLevel(context.getHomework().getSchoolLevel());
            result.setGrasp(true);
            OcrMentalImageDetail.OcrMentalArithmeticDiagnosis omad = omadMap.get(ocrMentalImageDetail.getImg_url());
            Map<String, List<String>> textKpIds = new HashMap<>();
            if( omad != null){
                ocrMentalImageDetail.setOmads(omad);
                if (CollectionUtils.isNotEmpty(omad.getItemPoints())) {
                    for (OcrMentalImageDetail.ItemPoint itemPoint : omad.getItemPoints()) {
                        if (CollectionUtils.isNotEmpty(itemPoint.getPoints())) {
                            for (OcrMentalImageDetail.Point point : itemPoint.getPoints()) {
                                textKpIds.computeIfAbsent(itemPoint.getItemContent(), k -> new ArrayList<>()).add(point.getPointId());
                            }
                        }
                    }
                }
            }
            Map<String, List<String>> kpSymptoms = new HashMap<>();
            if (CollectionUtils.isNotEmpty(ocrMentalImageDetail.getForms()) && MapUtils.isNotEmpty(symptomAnalysisMap)) {
                for (OcrMentalImageDetail.Form form : ocrMentalImageDetail.getForms()) {
                    String text = form.getText();
                    if (SafeConverter.toInt(form.getJudge(), -1) == 0 && symptomAnalysisMap.containsKey(text)) {
                        OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis symptomAnalysis = symptomAnalysisMap.get(text);
                        form.setSymptomAnalysis(symptomAnalysis);
                        List<String> kpIds = textKpIds.get(text);
                        if (CollectionUtils.isNotEmpty(kpIds)) {
                            for (String kpId : kpIds) {
                                List<String> symptoms = kpSymptoms.computeIfAbsent(kpId, k -> new ArrayList<>());
                                if (!symptoms.contains(symptomAnalysis.getSymptom())) {
                                    symptoms.add(symptomAnalysis.getSymptom());
                                }
                            }
                        }
                    }
                }
            }
            if (MapUtils.isNotEmpty(kpSymptoms)) {
                ocrMentalImageDetail.setKpSymptoms(kpSymptoms);
            }
            result.setOcrMentalImageDetail(ocrMentalImageDetail);
            result.setSubject(context.getSubject());
            result.setObjectiveConfigType(context.getObjectiveConfigType());
            result.setClientType(context.getClientType());
            result.setClientName(context.getClientName());
            result.setAdditions(context.getAdditions());

            results.add(result);
        }
        return results;
    }

    private Map<String, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> processOcrMentalArithmeticDiagnosis(HomeworkResultContext context, List<OcrMentalImageDetail> ocrMentalImageDetails){
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> omadMap = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        for (OcrMentalImageDetail ocrMentalImageDetail : ocrMentalImageDetails) {
            List<Map<String, Object>> itemSubs = new ArrayList<>();
            for(OcrMentalImageDetail.Form form : ocrMentalImageDetail.getForms()){
                Map<String, Object> item = new HashMap<>();
                if(form.getJudge() < 2){
                    item.put("content", form.getText());
                    item.put("correct", form.getJudge() == 1);
                    itemSubs.add(item);
                }
            }
            items.add(MapUtils.m("imgUrl", ocrMentalImageDetail.getImg_url(), "contents", itemSubs));
        }

        String requestUrl = RuntimeMode.current().le(Mode.TEST) ? OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL_TEST : OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL;
        if(RuntimeMode.current().equals(Mode.STAGING)){
            requestUrl = OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL_STAGING;
        }
        Map<String, Object> httpParams = MapUtils.m("items", items, "userId", context.getUserId());
        try{
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(requestUrl)
                    .json(httpParams)
                    .contentType("application/json").socketTimeout(1 * 1000)
                    .execute();
            if (response == null || response.getStatusCode() != 200) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getHomeworkId(),
                        "mod2", requestUrl,
                        "mod3", httpParams,
                        "op", "hr homework ocr_mental_arithmetic"
                ));
                logger.error("调用:{}失败, httpParams:{}, response: {}",
                        requestUrl,
                        httpParams,
                        response != null ? response.getResponseString() : "");
            }
            if (response != null) {
                Map resp = JsonUtils.fromJson(response.getResponseString(), Map.class);
                if(resp.get("resultCode").equals(200)){
                    Map resultInfo = JsonUtils.fromJson(JsonUtils.toJson(resp.get("resultInfo")));
                    if(resultInfo != null){
                        String itemsJson = JsonUtils.toJson(resultInfo.get("items"));
                        List<OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> omads = JsonUtils.fromJsonToList(itemsJson, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis.class);
                        if(CollectionUtils.isNotEmpty(omads)){
                            for(OcrMentalImageDetail.OcrMentalArithmeticDiagnosis omad : omads){
                                omadMap.put(omad.getImgUrl(), omad);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("获取纸质口算诊断数据失败requestUrl:{}, Exception: {}", requestUrl, e);
        }
        return omadMap;
    }

    private Map<String, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> processOcrMentalArithmeticSymptomAnalysis(HomeworkResultContext context, List<OcrMentalImageDetail> ocrMentalImageDetails) {
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> symptomAnalysisMap = new LinkedHashMap<>();
        Set<String> errorTextSet = new LinkedHashSet<>();
        for (OcrMentalImageDetail ocrMentalImageDetail : ocrMentalImageDetails) {
            if (CollectionUtils.isNotEmpty(ocrMentalImageDetail.getForms())) {
                for (OcrMentalImageDetail.Form form : ocrMentalImageDetail.getForms()) {
                    if (SafeConverter.toInt(form.getJudge(), -1) == 0) {
                        errorTextSet.add(form.getText());
                    }
                }
            }
        }
        String requestUrl = RuntimeMode.current().le(Mode.TEST) ? OCR_FORMULA_SYMPTOM_TEST_URL : OCR_FORMULA_SYMPTOM_PRODUCT_RUL;
        if (RuntimeMode.current().equals(Mode.STAGING)) {
            requestUrl = OCR_FORMULA_SYMPTOM_STAGING_RUL;
        }
        if (CollectionUtils.isEmpty(errorTextSet)) {
            return Collections.emptyMap();
        }
        Map<String, Object> params = MapUtils.m("uid", context.getUserId(), "equations", errorTextSet);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(requestUrl)
                    .json(params)
                    .contentType("application/json")
                    .socketTimeout(1000)
                    .execute();
            if (response != null && response.getStatusCode() == 200) {
                Map resp = JsonUtils.fromJson(response.getResponseString(), Map.class);
                if (MapUtils.isEmpty(resp)) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getUserId(),
                            "mod1", context.getHomeworkId(),
                            "mod2", requestUrl,
                            "mod3", params,
                            "response", response.getResponseString(),
                            "op", "OcrMentalArithmeticSymptomAnalysis response is empty"
                    ));
                } else {
                    String resultJson = JsonUtils.toJson(resp.get("results"));
                    List<OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> symptomAnalyses = JsonUtils.fromJsonToList(resultJson, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis.class);
                    if (CollectionUtils.isNotEmpty(symptomAnalyses)) {
                        int errorTextSize = errorTextSet.size();
                        int symptomAnalysesSize = symptomAnalyses.size();
                        List<String> errorTextList = new ArrayList<>(errorTextSet);
                        if (errorTextSize == symptomAnalysesSize) {
                            for (int index = 0; index < errorTextSize; index++) {
                                String errorText = errorTextList.get(index);
                                OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis symptomAnalysis = symptomAnalyses.get(index);
                                if (symptomAnalysis != null && StringUtils.isNotEmpty(symptomAnalysis.getSymptom())) {
                                    symptomAnalysisMap.put(errorText, symptomAnalysis);
                                }
                            }
                        }
                    }
                }
            } else {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getHomeworkId(),
                        "mod2", requestUrl,
                        "mod3", params,
                        "status", response == null ? "" : response.getStatusCode(),
                        "op", "OcrMentalArithmeticSymptomAnalysis status error"
                ));
                logger.error("调用:{}失败, httpParams:{}, response: {}",
                        requestUrl,
                        params,
                        response != null ? response.getResponseString() : "");
            }
        } catch (Exception e) {
            logger.error("获取纸质口算错因分析数据失败requestUrl:{}, Exception: {}", requestUrl, e);
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", requestUrl,
                    "mod3", params,
                    "op", "OcrMentalArithmeticSymptomAnalysis Exception"
            ));
        }
        return symptomAnalysisMap;
    }

    private List<NewHomeworkProcessResult> buildProcessResult(HomeworkResultContext context, List<StudentHomeworkAnswer> answerList, Map<String, NewQuestion> questionMap) {
        List<NewHomeworkProcessResult> results = new ArrayList<>();
        for (StudentHomeworkAnswer sha : answerList) {
            String questionId = sha.getQuestionId();
            NewHomeworkProcessResult result = new NewHomeworkProcessResult();
            NewHomeworkProcessResult.ID id = new NewHomeworkProcessResult.ID(context.getHomework().getCreateAt());
            result.setId(id.toString());
            result.setType(context.getHomework().getType());
            result.setHomeworkTag(context.getHomework().getHomeworkTag());
            result.setClazzGroupId(context.getClazzGroupId());
            result.setUserId(context.getUserId());
            result.setHomeworkId(context.getHomeworkId());
            result.setBookId(context.getBookId());
            result.setUnitGroupId(context.getUnitGroupId());
            result.setUnitId(context.getUnitId());
            result.setLessonId(context.getLessonId());
            result.setSectionId(context.getSectionId());
            result.setQuestionId(questionId);
            result.setSchoolLevel(context.getHomework().getSchoolLevel());

            NewQuestion question = questionMap.getOrDefault(questionId, null);
            if (question != null) {
                result.setQuestionDocId(question.getDocId());
                result.setQuestionVersion(question.getOlUpdatedAt() != null ? question.getOlUpdatedAt().getTime() : SafeConverter.toLong(question.getVersion()));
            }

            result.setStandardScore(context.getStandardScore().get(questionId));
            result.setScore(context.getScoreResult().get(questionId).getTotalScore());
            result.setActualScore(context.getScoreResult().get(questionId).getActualScore());
            result.setAppOralScoreLevel(context.getScoreResult().get(questionId).getAppOralScoreLevel());
            result.setGrasp(context.getScoreResult().get(questionId).getIsRight());
            result.setVest(context.getScoreResult().get(questionId).getVoiceEngineScoreType());
            result.setSt(sha.getSentenceType());
            result.setSubGrasp(context.getSubGrasp().get(questionId));
            result.setSubScore(context.getSubScore().get(questionId));
            result.setUserAnswers(sha.getAnswer());
            if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType())) {
                result.setDuration(sha.getDurationMilliseconds());
            } else {
                result.setDuration(NewHomeworkUtils.processDuration(sha.getDurationMilliseconds()));
            }
            switch (context.getObjectiveConfigType()) {
                case BASIC_APP:
                case LS_KNOWLEDGE_REVIEW:
                case NATURAL_SPELLING:
                    result.setPracticeId(context.getPracticeType().getId());
                    result.setCategoryId(context.getPracticeType().getCategoryId());
                    result.setVoiceEngineType(sha.getVoiceEngineType());
                    result.setVoiceCoefficient(sha.getVoiceCoefficient());
                    result.setVoiceScoringMode(sha.getVoiceScoringMode());
                    result.setVoiceMode(sha.getVoiceMode());
                    result.setOralDetails(sha.getOralScoreDetails());
                    break;
                case READING:
                case LEVEL_READINGS:
                    result.setPictureBookId(context.getPictureBookId());
                    result.setVoiceEngineType(sha.getVoiceEngineType());
                    result.setVoiceCoefficient(sha.getVoiceCoefficient());
                    result.setVoiceScoringMode(sha.getVoiceScoringMode());
                    result.setVoiceMode(sha.getVoiceMode());
                    result.setOralDetails(sha.getOralScoreDetails());
                    break;
                case ORAL_PRACTICE:
                case ORAL_INTELLIGENT_TEACHING:
                    result.setOralDetails(sha.getOralScoreDetails());
                    break;
                case KEY_POINTS:
                    result.setVideoId(context.getVideoId());
                    break;
                case NEW_READ_RECITE:
                    result.setQuestionBoxId(context.getQuestionBoxId());
                    result.setQuestionBoxType(context.getQuestionBoxType());
                    break;
                case READ_RECITE_WITH_SCORE:
                    result.setVoiceEngineType(sha.getVoiceEngineType());
                    result.setVoiceCoefficient(sha.getVoiceCoefficient());
                    result.setVoiceScoringMode(sha.getVoiceScoringMode());
                    result.setVoiceMode(sha.getVoiceMode());
                    result.setOralDetails(sha.getOralScoreDetails());
                    result.setQuestionBoxId(context.getQuestionBoxId());
                    result.setQuestionBoxType(context.getQuestionBoxType());
                    break;
                case WORD_RECOGNITION_AND_READING:
                    result.setVoiceEngineType(sha.getVoiceEngineType());
                    result.setVoiceCoefficient(sha.getVoiceCoefficient());
                    result.setVoiceScoringMode(sha.getVoiceScoringMode());
                    result.setVoiceMode(sha.getVoiceMode());
                    result.setOralDetails(sha.getOralScoreDetails());
                    result.setQuestionBoxId(context.getQuestionBoxId());
                    break;
                case WORD_TEACH_AND_PRACTICE:
                    result.setVoiceEngineType(sha.getVoiceEngineType());
                    result.setVoiceCoefficient(sha.getVoiceCoefficient());
                    result.setVoiceScoringMode(sha.getVoiceScoringMode());
                    result.setVoiceMode(sha.getVoiceMode());
                    result.setOralDetails(sha.getOralScoreDetails());
                    result.setStoneId(context.getStoneId());
                    result.setWordTeachModuleType(context.getWordTeachModuleType());
                    break;
                case DUBBING:
                    result.setDubbingId(context.getDubbingId());
                    break;
                case DUBBING_WITH_SCORE:
                    result.setDubbingId(context.getDubbingId());
                    result.setVoiceEngineType(sha.getVoiceEngineType());
                    result.setVoiceCoefficient(sha.getVoiceCoefficient());
                    result.setVoiceScoringMode(sha.getVoiceScoringMode());
                    result.setVoiceMode(sha.getVoiceMode());
                    result.setOralDetails(sha.getOralScoreDetails());
                    break;
                default:
                    break;
            }
            result.setSubject(context.getSubject());
            result.setObjectiveConfigType(context.getObjectiveConfigType());
            result.setClientType(context.getClientType());
            result.setClientName(context.getClientName());
            if (CollectionUtils.isNotEmpty(context.getFiles().get(questionId))) {
                long c = context.getFiles().get(questionId).stream().mapToLong(Collection::size).sum();
                if (c > 0) result.setFiles(context.getFiles().get(questionId));
            }
            result.setAdditions(context.getAdditions());
            results.add(result);
            context.getResult().put(questionId,
                    MiscUtils.m(
                            "fullScore", context.getStandardScore().get(questionId),
                            "score", context.getScoreResult().get(questionId).getTotalScore(),
                            "answers", context.getStandardAnswer().get(questionId),
                            "userAnswers", sha.getAnswer(),
                            "subMaster", context.getSubGrasp().get(questionId),
                            "subScore", context.getSubScore().get(questionId),
                            "master", context.getScoreResult().get(questionId).getIsRight()));
        }
        return results;
    }

    private List<NewHomeworkProcessResult> buildOralCommunicationProcessResult(HomeworkResultContext context, List<StudentHomeworkAnswer> answerList){
        List<NewHomeworkProcessResult> results = new ArrayList<>();
        for (StudentHomeworkAnswer sha : answerList) {
            NewHomeworkProcessResult result = new NewHomeworkProcessResult();
            NewHomeworkProcessResult.ID id = new NewHomeworkProcessResult.ID(context.getHomework().getCreateAt());
            result.setId(id.toString());
            result.setType(context.getHomework().getType());
            result.setHomeworkTag(context.getHomework().getHomeworkTag());
            result.setClazzGroupId(context.getClazzGroupId());
            result.setUserId(context.getUserId());
            result.setHomeworkId(context.getHomeworkId());
            result.setBookId(context.getBookId());
            result.setUnitGroupId(context.getUnitGroupId());
            result.setUnitId(context.getUnitId());
            result.setLessonId(context.getLessonId());
            result.setSectionId(context.getSectionId());
            result.setSchoolLevel(context.getHomework().getSchoolLevel());
            result.setGrasp(true);
            result.setQuestionId(sha.getQuestionId());
            result.setSchoolLevel(context.getHomework().getSchoolLevel());
            result.setScore(context.getScoreResult().get(sha.getDialogId()).getTotalScore());
            result.setActualScore(context.getScoreResult().get(sha.getDialogId()).getActualScore());
            result.setAppOralScoreLevel(context.getScoreResult().get(sha.getDialogId()).getAppOralScoreLevel());
            result.setGrasp(context.getScoreResult().get(sha.getDialogId()).getIsRight());
            result.setVest(context.getScoreResult().get(sha.getDialogId()).getVoiceEngineScoreType());
            result.setSt(sha.getSentenceType());
            result.setSubGrasp(context.getSubGrasp().get(sha.getDialogId()));
            result.setSubScore(context.getSubScore().get(sha.getDialogId()));
            result.setDuration(NewHomeworkUtils.processDuration(sha.getDurationMilliseconds()));
            result.setSubject(context.getSubject());
            result.setObjectiveConfigType(context.getObjectiveConfigType());
            result.setClientType(context.getClientType());
            result.setClientName(context.getClientName());
            result.setStoneId(context.getStoneId());
            result.setDialogId(sha.getDialogId());
            result.setAdditions(context.getAdditions());
            result.setStoneType(OralCommunicationContentType.valueOf(context.getStoneType()));
            if (StringUtils.isNotEmpty(context.getTopicRoleId())) {
                result.setRoleTopicId(context.getTopicRoleId());
            }
            result.setVoiceEngineType(sha.getVoiceEngineType());
            result.setOralDetails(sha.getOralScoreDetails());
            result.setSubject(context.getSubject());
            result.setObjectiveConfigType(context.getObjectiveConfigType());
            result.setClientType(context.getClientType());
            result.setClientName(context.getClientName());
            results.add(result);
        }
        return results;
    }

    private void processErrorQuestion(HomeworkResultContext context) {
        // 可随时删除
        if (StringUtils.equals(ObjectiveConfigType.BASIC_APP.name(), context.getObjectiveConfigType().name())
                || StringUtils.equals(ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name(), context.getObjectiveConfigType().name())
                || StringUtils.equals(ObjectiveConfigType.NATURAL_SPELLING.name(), context.getObjectiveConfigType().name())) {
            // 校验基础训练提交的题数和作业数量是否一致
            PracticeType practiceType = context.getPracticeType();
            if (practiceType != null) {
                Integer categoryId = practiceType.getCategoryId() != null ? practiceType.getCategoryId() : 0;
                String lessonId = context.getLessonId();

                // 用户提交的题量
                List<StudentHomeworkAnswer> userAnswers = context.getStudentHomeworkAnswers();
                // 作业中的题量
                List<NewHomeworkQuestion> questionList = context.getHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), lessonId, categoryId);

                if (CollectionUtils.isNotEmpty(questionList) && CollectionUtils.isNotEmpty(userAnswers) && userAnswers.size() != questionList.size()) {
                    Set<String> userQuestions = userAnswers.stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toSet());
                    Set<String> homeworkQuestions = questionList.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toSet());

                    LogCollector.info("backend-general", MiscUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getUserId(),
                            "agent", context.getUserAgent(),
                            "mod1", context.getHomeworkId(),
                            "mod2", CollectionUtils.retainAll(userQuestions, homeworkQuestions),
                            "mod3", context.getObjectiveConfigType(),
                            "mod4", categoryId + "-" + lessonId + "-" + practiceType.getId(),
                            "mod5", context.getClientType() + "-" + context.getClientName(),
                            "op", "question count error"
                    ));
                }
            }
        }
    }
}
