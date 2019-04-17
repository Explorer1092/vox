package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkProcessResultDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 初始化答题详情表表
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_CreateVacationHomeworkProcessResult extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private VacationHomeworkProcessResultDao vacationHomeworkProcessResultDao;
    @Inject
    private PracticeLoaderClient practiceLoaderClient;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())) {
            if (context.getPracticeId() == null) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getVacationHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST,
                        "op", "student vacation homework result"
                ));
                context.errorResponse("vacation homework practiceId:{} is null userId:{}", JsonUtils.toJson(context.getPracticeId()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                context.setTerminateTask(true);
            }
            PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(context.getPracticeId()));
            if (practiceType == null) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getVacationHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST,
                        "op", "student vacation homework result"
                ));
                context.errorResponse("vacation homework practiceType:{} is null userId:{}", JsonUtils.toJson(context.getPracticeId()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                context.setTerminateTask(true);
            }
            context.setPracticeType(practiceType);
            processErrorQuestion(context);
        }

        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            List<VacationHomeworkProcessResult> results = buildProcessResult(context, context.getStudentHomeworkAnswers(), context.getUserAnswerQuestionMap());
            vacationHomeworkProcessResultDao.inserts(results);
            LinkedHashMap<String, VacationHomeworkProcessResult> tempQuestionMap = new LinkedHashMap<>();
            for (VacationHomeworkProcessResult result : results) {
                tempQuestionMap.put(result.getQuestionId(), result);
            }
            context.setProcessResult(tempQuestionMap);
        }

        if ((ObjectiveConfigType.READING == context.getObjectiveConfigType() || ObjectiveConfigType.LEVEL_READINGS == context.getObjectiveConfigType())
                && CollectionUtils.isNotEmpty(context.getStudentHomeworkOralAnswers())) {
            List<VacationHomeworkProcessResult> oralResults = buildProcessResult(context, context.getStudentHomeworkOralAnswers(), context.getUserAnswerQuestionMap());
            vacationHomeworkProcessResultDao.inserts(oralResults);

            LinkedHashMap<String, VacationHomeworkProcessResult> tempOralQuestionMap = new LinkedHashMap<>();
            for (VacationHomeworkProcessResult result : oralResults) {
                tempOralQuestionMap.put(result.getQuestionId(), result);
            }
            context.setProcessOralResult(tempOralQuestionMap);
        }
    }

    private List<VacationHomeworkProcessResult> buildProcessResult(VacationHomeworkResultContext context, List<StudentHomeworkAnswer> answerList, Map<String, NewQuestion> questionMap) {
        List<VacationHomeworkProcessResult> results = new ArrayList<>();
        for (StudentHomeworkAnswer sha : answerList) {
            String questionId = sha.getQuestionId();
            VacationHomeworkProcessResult result = new VacationHomeworkProcessResult();
            VacationHomeworkProcessResult.ID id = new VacationHomeworkProcessResult.ID(context.getVacationHomework().getCreateAt());
            result.setId(id.toString());
            result.setType(context.getVacationHomework().getType());
            result.setHomeworkTag(context.getVacationHomework().getHomeworkTag());
            result.setClazzGroupId(context.getClazzGroupId());
            result.setUserId(context.getUserId());
            result.setHomeworkId(context.getVacationHomeworkId());
            result.setBookId(context.getBookId());
            result.setUnitGroupId(context.getUnitGroupId());
            result.setUnitId(context.getUnitId());
            result.setLessonId(context.getLessonId());
            result.setSectionId(context.getSectionId());
            result.setQuestionId(questionId);

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
                if (c > 0) {
                    result.setFiles(context.getFiles().get(questionId));
                }
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

    private void processErrorQuestion(VacationHomeworkResultContext context) {
        // 可随时删除
        if (StringUtils.equals(ObjectiveConfigType.BASIC_APP.name(), context.getObjectiveConfigType().name())
                || StringUtils.equals(ObjectiveConfigType.NATURAL_SPELLING.name(), context.getObjectiveConfigType().name())) {
            // 校验基础训练提交的题数和作业数量是否一致
            PracticeType practiceType = context.getPracticeType();
            if (practiceType != null) {
                Integer categoryId = practiceType.getCategoryId() != null ? practiceType.getCategoryId() : 0;
                String lessonId = context.getLessonId();

                // 用户提交的题量
                List<StudentHomeworkAnswer> userAnswers = context.getStudentHomeworkAnswers();
                // 作业中的题量
                List<NewHomeworkQuestion> questionList = context.getVacationHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), lessonId, categoryId);

                if (CollectionUtils.isNotEmpty(questionList) && CollectionUtils.isNotEmpty(userAnswers) && userAnswers.size() != questionList.size()) {
                    Set<String> userQuestions = userAnswers.stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toSet());
                    Set<String> homeworkQuestions = questionList.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toSet());

                    LogCollector.info("backend-general", MiscUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getUserId(),
                            "agent", context.getUserAgent(),
                            "mod1", context.getVacationHomeworkId(),
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
