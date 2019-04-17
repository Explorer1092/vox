package com.voxlearning.washington.helpers;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newhomework.api.constant.ImmediateInterventionType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.net.message.exam.SaveHomeworkResultRequest;
import com.voxlearning.washington.net.message.exam.SaveNewHomeworkResultRequest;
import com.voxlearning.washington.support.WashingtonRequestContext;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class HomeworkResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HomeworkResultProcessor.class);

    @Inject
    private NewHomeworkServiceClient newHomeworkServiceClient;
    @Inject
    private VacationHomeworkServiceClient vacationHomeworkServiceClient;

    /**
     * 单题提交作业结果
     */
    public MapMessage processSaveHomeworkResultRequest(User user,
                                                       SaveHomeworkResultRequest result,
                                                       HttpServletRequest httpServletRequest,
                                                       WashingtonRequestContext washingtonRequestContext) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(result.getObjectiveConfigType());
        if (objectiveConfigType == null) return MapMessage.errorMessage("作业形式为空" + JsonUtils.toJson(result));
        StudyType studyType = StudyType.of(result.getLearningType());
        if (studyType == null) {
            return MapMessage.errorMessage("学习类型异常" + JsonUtils.toJson(result));
        }
        switch (studyType) {
            case homework:
                HomeworkResultContext context = initContext(user, result, studyType, objectiveConfigType, httpServletRequest, washingtonRequestContext);
                return processHomework(user, context);
            case vacationHomework:
                VacationHomeworkResultContext vacationContext = initVacationContext(user, result, studyType, objectiveConfigType, httpServletRequest, washingtonRequestContext);
                return processVacationHomework(user, vacationContext);
            default:
                return MapMessage.errorMessage("不支持的学习类型");
        }
    }

    /**
     * 批量提交作业结果
     */
    public MapMessage processSaveNewHomeworkResultRequest(User user,
                                                          SaveNewHomeworkResultRequest result,
                                                          HttpServletRequest httpServletRequest,
                                                          WashingtonRequestContext washingtonRequestContext) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(result.getObjectiveConfigType());
        if (objectiveConfigType == null) return MapMessage.errorMessage("作业形式为空" + JsonUtils.toJson(result));
        StudyType studyType = StudyType.of(result.getLearningType());
        if (studyType == null) {
            return MapMessage.errorMessage("学习类型异常" + JsonUtils.toJson(result));
        }
        switch (studyType) {
            case homework:
                HomeworkResultContext context = initContextByBatchResult(user, result, studyType, objectiveConfigType, httpServletRequest, washingtonRequestContext);
                return processHomework(user, context);
            case vacationHomework:
                VacationHomeworkResultContext vacationContext = initVacationContextByBatchResult(user, result, studyType, objectiveConfigType, httpServletRequest, washingtonRequestContext);
                return processVacationHomework(user, vacationContext);
            default:
                return MapMessage.errorMessage("不支持的学习类型");
        }
    }

    private MapMessage processHomework(User user, HomeworkResultContext context) {
        try {
            MapMessage msg = newHomeworkServiceClient.processorHomeworkResult(context);
            if (msg.isSuccess() || msg.getErrorCode().equals(ErrorCodeConstants.ERROR_CODE_IMMEDIATE_INTERVENTION)) {
                return msg;
            } else {
                String errorCode = msg.getErrorCode();
                if (StringUtils.equals(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_FINISHED, errorCode) || StringUtils.equals(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION, errorCode)) {
                    return msg;
                }
                return MapMessage.errorMessage("提交结果失败").setErrorCode(msg.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error("Failed to save user {} homework result", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    private MapMessage processVacationHomework(User user, VacationHomeworkResultContext context) {
        try {
            MapMessage msg = vacationHomeworkServiceClient.processVacationHomeworkResult(context);
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
            logger.error("Failed to save user {} vacation homework result", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    private HomeworkResultContext initContext(User user,
                                              SaveHomeworkResultRequest result,
                                              StudyType studyType,
                                              ObjectiveConfigType objectiveConfigType,
                                              HttpServletRequest httpServletRequest,
                                              WashingtonRequestContext washingtonRequestContext) {
        HomeworkResultContext context = new HomeworkResultContext();
        context.setUserId(user.getId());
        context.setUser(user);
        context.setHomeworkId(result.getHomeworkId());
        context.setLearningType(studyType);
        context.setObjectiveConfigType(objectiveConfigType);
        context.setBookId(result.getBookId());
        context.setUnitId(result.getUnitId());
        context.setUnitGroupId(result.getUnitGroupId());
        context.setLessonId(result.getLessonId());
        context.setSectionId(result.getSectionId());
        context.setVideoId(result.getVideoId());
        context.setQuestionBoxId(result.getQuestionBoxId());
        context.setQuestionBoxType(result.getQuestionBoxType());
        context.setClientType(result.getClientType());
        context.setClientName(result.getClientName());
        context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : washingtonRequestContext.getRealRemoteAddress());
        context.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        context.setStoneId(result.getStoneDataId());
        context.setWordTeachModuleType(WordTeachModuleType.of(result.getWordTeachModuleType()));
        StudentHomeworkAnswer sha = new StudentHomeworkAnswer();
        sha.setAnswer(result.getAnswer());
        sha.setDurationMilliseconds(NewHomeworkUtils.processDuration(result.getDuration()));
        sha.setFileUrls(result.getFileUrls());
        sha.setQuestionId(result.getQuestionId());
        sha.setOralScoreDetails(result.getOralScoreDetails());
        context.setStudentHomeworkAnswers(Collections.singletonList(sha));
        if (result.getHwTrajectory() != null) {
            context.putIfAbsent("hwTrajectory", JsonUtils.toJson(result.getHwTrajectory()));
        }
        if (result.getHintId() != null) {
            context.putIfAbsent(NewHomeworkConstants.HINT_ID, SafeConverter.toString(result.getHintId()));
        }
        if (result.getInterventionAnswer() != null) {
            if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType)) {
                context.putIfAbsent(NewHomeworkConstants.HINT_ID, SafeConverter.toString(ImmediateInterventionType.WORDTEACH.getHintId()));
                context.putIfAbsent(NewHomeworkConstants.INTERVENTION_ANSWER, JsonUtils.toJson(result.getInterventionAnswer()));
            } else {
                context.putIfAbsent(NewHomeworkConstants.INTERVENTION_ANSWER, JsonUtils.toJson(result.getInterventionAnswer()));
            }
        }
        context.setHintId(result.getHintId());
        context.setInterventionAnswer(result.getInterventionAnswer());
        context.setTimestamp(result.getTimestamp());
        context.setDuration(result.getDuration());
        context.setInterventionReSubmit(result.isInterventionReSubmit());
        return context;
    }

    private HomeworkResultContext initContextByBatchResult(User user,
                                                           SaveNewHomeworkResultRequest result,
                                                           StudyType studyType,
                                                           ObjectiveConfigType objectiveConfigType,
                                                           HttpServletRequest httpServletRequest,
                                                           WashingtonRequestContext washingtonRequestContext) {
        HomeworkResultContext context = new HomeworkResultContext();
        context.setUserId(user.getId());
        context.setUser(user);
        context.setHomeworkId(result.getHomeworkId());
        context.setLearningType(studyType);
        context.setSkipUploadVideo(result.getSkipUploadVideo());
        context.setObjectiveConfigType(objectiveConfigType);
        context.setBookId(result.getBookId());
        context.setUnitId(result.getUnitId());
        context.setUnitGroupId(result.getUnitGroupId());
        context.setLessonId(result.getLessonId());
        context.setSectionId(result.getSectionId());
        context.setPracticeId(result.getPracticeId());
        context.setPictureBookId(result.getPictureBookId());
        context.setVideoId(result.getVideoId());
        context.setDubbingId(result.getDubbingId());
        context.setVideoUrl(result.getVideoUrl());
        context.setStoneType(result.getStoneType());
        context.setTopicRoleId(result.getTopicRoleId());
        context.setQuestionBoxId(result.getQuestionBoxId());
        context.setQuestionBoxType(result.getQuestionBoxType());
        context.setDurations(result.getDurations());
        context.setClientType(result.getClientType());
        context.setClientName(result.getClientName());
        context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : washingtonRequestContext.getRealRemoteAddress());
        context.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        context.setStoneId(result.getStoneDataId());
        context.setWordTeachModuleType(WordTeachModuleType.of(result.getWordTeachModuleType()));
        if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType) && WordTeachModuleType.CHINESECHARACTERCULTURE.equals(WordTeachModuleType.of(result.getWordTeachModuleType()))) {
            context.setDuration(result.getDuration());
            context.setCourseId(result.getCourseId());
        }
        // 新口算没有答题详情
        if (ObjectiveConfigType.MENTAL_ARITHMETIC != objectiveConfigType && CollectionUtils.isNotEmpty(result.getStudentHomeworkAnswers())) {
            for (StudentHomeworkAnswer answer : result.getStudentHomeworkAnswers()) {
                if (answer != null) {
                    answer.setDurationMilliseconds(NewHomeworkUtils.processDuration(answer.getDurationMilliseconds()));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(result.getStudentHomeworkOralAnswers())) {
            for (StudentHomeworkAnswer answer : result.getStudentHomeworkOralAnswers()) {
                if (answer != null) {
                    answer.setDurationMilliseconds(NewHomeworkUtils.processDuration(answer.getDurationMilliseconds()));
                }
            }
        }
        context.setStudentHomeworkAnswers(result.getStudentHomeworkAnswers());
        if (CollectionUtils.isNotEmpty(result.getOcrMentalImageDetails()) && objectiveConfigType.equals(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
            List<OcrMentalImageDetail> ocrMentalImageDetails = new ArrayList<>();
            for (OcrMentalImageDetail ocrMentalImageDetail : result.getOcrMentalImageDetails()) {
                if (ocrMentalImageDetail != null) {
                    ocrMentalImageDetails.add(ocrMentalImageDetail);
                }
            }
            context.setOcrMentalImageDetails(ocrMentalImageDetails);
        }
        if (CollectionUtils.isNotEmpty(result.getOcrMentalImageDetails()) && objectiveConfigType.equals(ObjectiveConfigType.OCR_DICTATION)) {
            context.setOcrDictationImageDetails(result.getOcrMentalImageDetails().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        if (Objects.equals(objectiveConfigType, ObjectiveConfigType.READING)) {
            // 绘本特有
            context.setStudentHomeworkOralAnswers(result.getStudentHomeworkOralAnswers());
            // 绘本的总耗时，超过1个小时，按1个小时处理
            if (result.getConsumeTime() != null) {
                Long doQuestionTime = result.getStudentHomeworkAnswers()
                        .stream()
                        .filter(o -> o.getDurationMilliseconds() != null && o.getDurationMilliseconds() > 0)
                        .mapToLong(StudentHomeworkAnswer::getDurationMilliseconds)
                        .sum();

                Long doOralQuestionTime = 0L;
                if (CollectionUtils.isNotEmpty(result.getStudentHomeworkOralAnswers())) {
                    doOralQuestionTime = result.getStudentHomeworkOralAnswers()
                            .stream()
                            .filter(o -> o.getDurationMilliseconds() != null && o.getDurationMilliseconds() > 0)
                            .mapToLong(StudentHomeworkAnswer::getDurationMilliseconds)
                            .sum();
                }
                Long totalDoQuestionTime = doQuestionTime + doOralQuestionTime;
                Long maxDuration = 3600000L;
                if (result.getConsumeTime() > maxDuration) {
                    context.setConsumeTime(maxDuration);
                } else if (result.getConsumeTime() < totalDoQuestionTime && totalDoQuestionTime < maxDuration) {
                    context.setConsumeTime(totalDoQuestionTime);
                } else {
                    context.setConsumeTime(result.getConsumeTime());
                }
            }
        } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.DUBBING)
                || Objects.equals(objectiveConfigType, ObjectiveConfigType.DUBBING_WITH_SCORE)
                || Objects.equals(objectiveConfigType, ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)
                || Objects.equals(objectiveConfigType, ObjectiveConfigType.OCR_DICTATION)
                ) {
            context.setConsumeTime(result.getConsumeTime());
        } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.ORAL_COMMUNICATION)) {
            context.setConsumeTime(result.getConsumeTime());
            context.setStoneId(result.getStoneId());
        } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.LEVEL_READINGS)) {
            context.setStudentHomeworkOralAnswers(result.getStudentHomeworkOralAnswers());
        }
        return context;
    }

    private VacationHomeworkResultContext initVacationContext(User user,
                                                              SaveHomeworkResultRequest result,
                                                              StudyType studyType,
                                                              ObjectiveConfigType objectiveConfigType,
                                                              HttpServletRequest httpServletRequest,
                                                              WashingtonRequestContext washingtonRequestContext) {
        VacationHomeworkResultContext context = new VacationHomeworkResultContext();
        context.setUserId(user.getId());
        context.setUser(user);
        context.setVacationHomeworkId(result.getHomeworkId());
        context.setLearningType(studyType);
        context.setObjectiveConfigType(objectiveConfigType);
        context.setBookId(result.getBookId());
        context.setUnitId(result.getUnitId());
        context.setUnitGroupId(result.getUnitGroupId());
        context.setLessonId(result.getLessonId());
        context.setSectionId(result.getSectionId());
        context.setVideoId(result.getVideoId());
        context.setQuestionBoxId(result.getQuestionBoxId());
        context.setQuestionBoxType(result.getQuestionBoxType());
        context.setClientType(result.getClientType());
        context.setClientName(result.getClientName());
        context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : washingtonRequestContext.getRealRemoteAddress());
        context.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        StudentHomeworkAnswer sha = new StudentHomeworkAnswer();
        sha.setAnswer(result.getAnswer());
        sha.setDurationMilliseconds(NewHomeworkUtils.processDuration(result.getDuration()));
        sha.setFileUrls(result.getFileUrls());
        sha.setQuestionId(result.getQuestionId());
        sha.setOralScoreDetails(result.getOralScoreDetails());
        context.setStudentHomeworkAnswers(Collections.singletonList(sha));
        if (result.getHwTrajectory() != null) {
            context.putIfAbsent("hwTrajectory", JsonUtils.toJson(result.getHwTrajectory()));
        }
        return context;
    }

    private VacationHomeworkResultContext initVacationContextByBatchResult(User user,
                                                                           SaveNewHomeworkResultRequest result,
                                                                           StudyType studyType,
                                                                           ObjectiveConfigType objectiveConfigType,
                                                                           HttpServletRequest httpServletRequest,
                                                                           WashingtonRequestContext washingtonRequestContext) {
        VacationHomeworkResultContext context = new VacationHomeworkResultContext();
        context.setUserId(user.getId());
        context.setUser(user);
        context.setVacationHomeworkId(result.getHomeworkId());
        context.setLearningType(studyType);
        context.setSkipUploadVideo(result.getSkipUploadVideo());
        context.setObjectiveConfigType(objectiveConfigType);
        context.setBookId(result.getBookId());
        context.setUnitId(result.getUnitId());
        context.setUnitGroupId(result.getUnitGroupId());
        context.setLessonId(result.getLessonId());
        context.setSectionId(result.getSectionId());
        context.setPracticeId(result.getPracticeId());
        context.setPictureBookId(result.getPictureBookId());
        context.setVideoId(result.getVideoId());
        context.setDubbingId(result.getDubbingId());
        context.setVideoUrl(result.getVideoUrl());
        context.setQuestionBoxId(result.getQuestionBoxId());
        context.setQuestionBoxType(result.getQuestionBoxType());
        context.setDurations(result.getDurations());
        context.setClientType(result.getClientType());
        context.setClientName(result.getClientName());
        context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : washingtonRequestContext.getRealRemoteAddress());
        context.setUserAgent(httpServletRequest.getHeader("User-Agent"));

        // 新口算作答时长不做处理
        if (ObjectiveConfigType.MENTAL_ARITHMETIC != objectiveConfigType && CollectionUtils.isNotEmpty(result.getStudentHomeworkAnswers())) {
            for (StudentHomeworkAnswer answer : result.getStudentHomeworkAnswers()) {
                if (answer != null) {
                    answer.setDurationMilliseconds(NewHomeworkUtils.processDuration(answer.getDurationMilliseconds()));
                }
            }
        }
        context.setStudentHomeworkAnswers(result.getStudentHomeworkAnswers());

        if (Objects.equals(objectiveConfigType, ObjectiveConfigType.READING)) {
            // 绘本特有
            context.setStudentHomeworkOralAnswers(result.getStudentHomeworkOralAnswers());
            // 绘本的总耗时，超过1个小时，按1个小时处理
            if (result.getConsumeTime() != null) {
                Long doQuestionTime = result.getStudentHomeworkAnswers()
                        .stream()
                        .filter(o -> o.getDurationMilliseconds() != null && o.getDurationMilliseconds() > 0)
                        .mapToLong(StudentHomeworkAnswer::getDurationMilliseconds)
                        .sum();

                Long doOralQuestionTime = 0L;
                if (CollectionUtils.isNotEmpty(result.getStudentHomeworkOralAnswers())) {
                    doOralQuestionTime = result.getStudentHomeworkOralAnswers()
                            .stream()
                            .filter(o -> o.getDurationMilliseconds() != null && o.getDurationMilliseconds() > 0)
                            .mapToLong(StudentHomeworkAnswer::getDurationMilliseconds)
                            .sum();
                }
                Long totalDoQuestionTime = doQuestionTime + doOralQuestionTime;
                Long maxDuration = 3600000L;
                if (result.getConsumeTime() > maxDuration) {
                    context.setConsumeTime(maxDuration);
                } else if (result.getConsumeTime() < totalDoQuestionTime && totalDoQuestionTime < maxDuration) {
                    context.setConsumeTime(totalDoQuestionTime);
                } else {
                    context.setConsumeTime(result.getConsumeTime());
                }
            }
        } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.DUBBING)) {
            context.setConsumeTime(result.getConsumeTime());
        } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.LEVEL_READINGS)) {
            context.setStudentHomeworkOralAnswers(result.getStudentHomeworkOralAnswers());
        }
        return context;
    }
}
