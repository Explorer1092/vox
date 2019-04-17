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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/5/24
 */
@Named
public class VacationHomeworkResultUpdate_KeyPoints extends VacationHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.KEY_POINTS;
    }

    @Override
    public void processVacationHomeworkContext(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();

        Map<String, VacationHomeworkProcessResult> processResultMap = context.getProcessResult();
        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (VacationHomeworkProcessResult vpr : processResultMap.values()) {
            answers.put(vpr.getQuestionId(), vpr.getId());
        }

        nhraa.setVideoId(context.getVideoId());
        nhraa.setAnswers(answers);
        String key = context.getVideoId();
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.doHomeworkBasicAppPractice(
                location,
                context.getObjectiveConfigType(),
                key,
                nhraa);
        context.setVacationHomeworkResult(vacationHomeworkResult);
    }

    @Override
    public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        VacationHomeworkResult vacationHomeworkResult = context.getVacationHomeworkResult();
        if (vacationHomeworkResult == null) return;

        String videoId = context.getVideoId();
        List<String> processIds = vacationHomeworkResult.findHomeworkProcessIdsForKeyPointsByVideoId(context.getVideoId());
        if (CollectionUtils.isNotEmpty(processIds)) {
            NewHomeworkResultAppAnswer appAnswer = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.KEY_POINTS).getAppAnswers().get(videoId);
            boolean finished = SafeConverter.toBoolean(context.getFiles());
            // 教验是否真的没完成
            if (finished || validatePracticeFinished(context.getVacationHomework(), appAnswer, videoId)) {
                // 布置的题目和做过的题一致，将剩下的属性补全
                Map<String, VacationHomeworkProcessResult> processResultMap = vacationHomeworkProcessResultDao.loads(processIds);
                Double score = 0d;
                Long duration = 0L;
                for (VacationHomeworkProcessResult vhr : processResultMap.values()) {
                    score += vhr.getScore();
                    duration += vhr.getDuration();
                }
                vacationHomeworkResultDao.finishHomeworkKeyPoint(location, context.getObjectiveConfigType(), videoId, score, duration);
            }
        }
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(VacationHomework vacationHomework,
                                             NewHomeworkResultAppAnswer appAnswer,
                                             String videoId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = vacationHomework.findNewHomeworkKeyPointQuestions(ObjectiveConfigType.KEY_POINTS, videoId);
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
