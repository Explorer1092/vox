package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 新版课文读背
 * @author: Mr_VanGogh
 * @date: 2018/5/30 下午8:55
 */
@Named
public class VacationHomeworkResultUpdate_ReadReciteWithScore extends VacationHomeworkResultUpdateTemplate{
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.READ_RECITE_WITH_SCORE;
    }

    @Override
    public void processVacationHomeworkContext(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        Map<String, VacationHomeworkProcessResult> processResultMap =  context.getProcessResult();

        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        LinkedHashMap<String, String> answerMap = new LinkedHashMap<>();
        for (VacationHomeworkProcessResult result : processResultMap.values()) {
            answerMap.put(result.getQuestionId(), result.getId());
        }

        String key = context.getQuestionBoxId();
        nhraa.setLessonId(context.getLessonId());
        nhraa.setQuestionBoxId(key);
        nhraa.setQuestionBoxType(context.getQuestionBoxType());
        nhraa.setAnswers(answerMap);
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.doHomeworkBasicAppPractice(
                location,
                context.getObjectiveConfigType(),
                key,
                nhraa
        );
        context.setVacationHomeworkResult(vacationHomeworkResult);
    }

    @Override
    public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context) {
        VacationHomework vacationHomework = context.getVacationHomework();
        VacationHomework.Location location = vacationHomework.toLocation();
        VacationHomeworkResult vacationHomeworkResult = context.getVacationHomeworkResult();
        if (vacationHomeworkResult == null) {
            return;
        }

        String questionBoxId = context.getQuestionBoxId();
        List<String> processIds = vacationHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (CollectionUtils.isNotEmpty(processIds)) {
            NewHomeworkResultAppAnswer appAnswer = vacationHomeworkResult
                    .getPractices()
                    .get(ObjectiveConfigType.READ_RECITE_WITH_SCORE)
                    .getAppAnswers()
                    .get(questionBoxId);
            boolean finished = SafeConverter.toBoolean(context.getFinished());
            // 校验是否真的完成
            if (finished || validatePracticeFinished(vacationHomework, questionBoxId, appAnswer)) {
                // 布置的题目&做过的题目一致，将剩下的属性补全
                Map<String, VacationHomeworkProcessResult> processResultMap = vacationHomeworkProcessResultDao.loads(processIds);
                Long duration = 0L;
                Double score = 0D;
                Integer standardNum = 0;
                for (VacationHomeworkProcessResult processResult : processResultMap.values()) {
                    duration += processResult.getDuration();
                    score += processResult.getScore();
                    if (processResult.getGrasp() != null && Boolean.TRUE.equals(processResult.getGrasp())) {
                        ++ standardNum;
                    }
                }

                Integer appQuestionNum = 0;
                List<NewHomeworkQuestion> newHomeworkQuestions = vacationHomework.findNewHomeworkReadReciteQuestions(ObjectiveConfigType.READ_RECITE_WITH_SCORE, questionBoxId);
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    appQuestionNum = newHomeworkQuestions.size();
                }

                score = new BigDecimal(standardNum).divide(new BigDecimal(appQuestionNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;

                vacationHomeworkResultDao.finishHomeworkReadReciteWithScore(
                        location,
                        context.getUserId(),
                        context.getObjectiveConfigType(),
                        questionBoxId,
                        score,
                        duration,
                        standardNum,
                        appQuestionNum
                );
            }
        }
    }

    /**
     * 校验作业中，新课文读背练习中的题目和已做的是否一致
     * @param vacationHomework
     * @param questionBoxId
     * @param appAnswer
     * @return
     */
    private boolean validatePracticeFinished(VacationHomework vacationHomework, String questionBoxId, NewHomeworkResultAppAnswer appAnswer) {
        boolean result = false;
        List<NewHomeworkQuestion> newHomeworkQuestions = vacationHomework.findNewHomeworkReadReciteQuestions(ObjectiveConfigType.READ_RECITE_WITH_SCORE, questionBoxId);
        if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
            Set<String> questionIds = newHomeworkQuestions.stream()
                    .filter(r -> StringUtils.isNotBlank(r.getQuestionId()))
                    .map(NewHomeworkQuestion :: getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQuestionIds = appAnswer.getAnswers().keySet();
            result = CollectionUtils.isEqualCollection(questionIds, resultQuestionIds);
        }
        return result;
    }
}
