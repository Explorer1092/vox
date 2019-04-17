package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class NewHomeworkResultUpdate_WordRecognitionAndReading extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.WORD_RECOGNITION_AND_READING;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();

        NewHomeworkResultAppAnswer appAnswer = new NewHomeworkResultAppAnswer();
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();

        for (NewHomeworkProcessResult processResult : processResultMap.values()) {
            answers.put(processResult.getQuestionId(), processResult.getId());
        }

        appAnswer.setLessonId(context.getLessonId());
        appAnswer.setQuestionBoxId(context.getQuestionBoxId());
        appAnswer.setAnswers(answers);

        String key = context.getQuestionBoxId();
        NewHomeworkResult newHomeworkResult = newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                appAnswer);

        context.setNewHomeworkResult(newHomeworkResult);
        context.setIsOneByOne(true);
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {
        NewHomework newHomework = context.getHomework();
        NewHomework.Location location = context.getHomework().toLocation();
        NewHomeworkResult newHomeworkResult = context.getNewHomeworkResult();
        if (newHomeworkResult == null) {
            return;
        }
        String questionBoxId = context.getQuestionBoxId();
        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
        if (CollectionUtils.isNotEmpty(processIds)) {
            NewHomeworkResultAppAnswer appAnswer = newHomeworkResult
                    .getPractices()
                    .get(ObjectiveConfigType.WORD_RECOGNITION_AND_READING)
                    .getAppAnswers()
                    .get(questionBoxId);
            boolean finished = SafeConverter.toBoolean(context.getFinished());
            if (finished || validatePracticeFinished(context.getHomework(), appAnswer, questionBoxId)) {
                // 布置的题目和做过的题一致，将剩下的属性补全
                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), processIds);
                Long duration = 0L;
                Double score = 0d;
                Integer standardNum = 0;
                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                    duration += npr.getDuration();
                    score += npr.getScore();
                    if (npr.getGrasp() != null && Boolean.TRUE.equals(npr.getGrasp())) {
                        ++standardNum;
                    }
                }

                Integer appQuestionNum = 0;
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkReadReciteQuestions(ObjectiveConfigType.WORD_RECOGNITION_AND_READING, questionBoxId);
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    appQuestionNum = newHomeworkQuestions.size();
                }

                score = new BigDecimal(standardNum).divide(new BigDecimal(appQuestionNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;

                newHomeworkResultService.finishHomeworkReadReciteWithScore(
                        location,
                        context.getUserId(),
                        context.getObjectiveConfigType(),
                        questionBoxId,
                        score,
                        duration,
                        standardNum,
                        appQuestionNum);
            }
        }
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(NewHomework newHomework,
                                             NewHomeworkResultAppAnswer appAnswer,
                                             String questionBoxId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkReadReciteQuestions(ObjectiveConfigType.WORD_RECOGNITION_AND_READING, questionBoxId);
        if (CollectionUtils.isNotEmpty(questionList)) {
            Set<String> homeworkQids = questionList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQids = appAnswer.getAnswers().keySet();
            result = CollectionUtils.isEqualCollection(homeworkQids, resultQids);
        }
        return result;
    }
}
