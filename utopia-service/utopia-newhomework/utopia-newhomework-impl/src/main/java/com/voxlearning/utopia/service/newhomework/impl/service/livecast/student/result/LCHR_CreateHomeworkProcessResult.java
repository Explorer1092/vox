package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_CreateHomeworkProcessResult extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private PracticeServiceClient practiceServiceClient;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())) {
            if (context.getPracticeId() == null) {
                context.errorResponse("LiveCastHomework practiceId:{} is null userId:{}", JsonUtils.toJson(context.getPracticeId()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                context.setTerminateTask(true);
            }
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(context.getPracticeId()));
            if (practiceType == null) {
                context.errorResponse("LiveCastHomework practiceType:{} is null userId:{}", JsonUtils.toJson(context.getPracticeId()), context.getUserId());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                context.setTerminateTask(true);
            }
            context.setPracticeType(practiceType);
        }

        // 用于获取试题的版本和docId，题目中的version和_id中横线后面的那个版本号可能不一致
        List<StudentHomeworkAnswer> answerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            answerList.addAll(context.getStudentHomeworkAnswers());
        }
        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkOralAnswers())) {
            answerList.addAll(context.getStudentHomeworkOralAnswers());
        }

        Set<String> questionSet = answerList
                .stream()
                .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                .map(StudentHomeworkAnswer::getQuestionId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionSet);

        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            List<LiveCastHomeworkProcessResult> results = buildProcessResult(context, context.getStudentHomeworkAnswers(), questionMap);
            liveCastHomeworkProcessResultDao.inserts(results);
            LinkedHashMap<String, LiveCastHomeworkProcessResult> tempQuestionMap = new LinkedHashMap<>();
            for (LiveCastHomeworkProcessResult result : results) {
                tempQuestionMap.put(result.getQuestionId(), result);
            }
            context.setProcessResult(tempQuestionMap);
        }

        if ((ObjectiveConfigType.READING.equals(context.getObjectiveConfigType()) || ObjectiveConfigType.LEVEL_READINGS == context.getObjectiveConfigType())
                && CollectionUtils.isNotEmpty(context.getStudentHomeworkOralAnswers())) {
            List<LiveCastHomeworkProcessResult> oralResults = buildProcessResult(context, context.getStudentHomeworkOralAnswers(), questionMap);
            liveCastHomeworkProcessResultDao.inserts(oralResults);

            LinkedHashMap<String, LiveCastHomeworkProcessResult> tempOralQuestionMap = new LinkedHashMap<>();
            for (LiveCastHomeworkProcessResult result : oralResults) {
                tempOralQuestionMap.put(result.getQuestionId(), result);
            }
            context.setProcessOralResult(tempOralQuestionMap);
        }
    }

    private List<LiveCastHomeworkProcessResult> buildProcessResult(LiveCastHomeworkResultContext context, List<StudentHomeworkAnswer> answerList, Map<String, NewQuestion> questionMap) {
        List<LiveCastHomeworkProcessResult> results = new ArrayList<>();
        for (StudentHomeworkAnswer sha : answerList) {
            String questionId = sha.getQuestionId();
            LiveCastHomeworkProcessResult result = new LiveCastHomeworkProcessResult();
            LiveCastHomeworkProcessResult.ID id = new LiveCastHomeworkProcessResult.ID(context.getLiveCastHomework().getCreateAt());
            result.setId(id.toString());
            result.setType(context.getLiveCastHomework().getType());
            result.setHomeworkTag(context.getLiveCastHomework().getHomeworkTag());
            result.setClazzGroupId(context.getClazzGroupId());
            result.setUserId(context.getUserId());
            result.setHomeworkId(context.getHomeworkId());
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
            result.setDuration(NewHomeworkUtils.processDuration(sha.getDurationMilliseconds()));
            if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())) {
                result.setPracticeId(context.getPracticeType().getId());
                result.setCategoryId(context.getPracticeType().getCategoryId());
                result.setVoiceEngineType(sha.getVoiceEngineType());
                result.setVoiceCoefficient(sha.getVoiceCoefficient());
                result.setVoiceScoringMode(sha.getVoiceScoringMode());
                result.setVoiceMode(sha.getVoiceMode());
                result.setOralDetails(sha.getOralScoreDetails());
            } else if (ObjectiveConfigType.READING.equals(context.getObjectiveConfigType()) || ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())) {
                result.setPictureBookId(context.getPictureBookId());
                result.setVoiceEngineType(sha.getVoiceEngineType());
                result.setVoiceCoefficient(sha.getVoiceCoefficient());
                result.setVoiceScoringMode(sha.getVoiceScoringMode());
                result.setVoiceMode(sha.getVoiceMode());
                result.setOralDetails(sha.getOralScoreDetails());
            } else if (ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())) {
                result.setDubbingId(context.getDubbingId());
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
                    MapUtils.m(
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
}
