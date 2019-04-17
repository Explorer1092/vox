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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guohong.tan
 * @since 2016/11/23
 */
@Named
public class NewHomeworkResultUpdate_KeyPoints extends NewHomeworkResultUpdateTemplate {

    public static final String a = "111";

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.KEY_POINTS;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();

        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (NewHomeworkProcessResult npr : processResultMap.values()) {
            answers.put(npr.getQuestionId(), npr.getId());
        }

        nhraa.setVideoId(context.getVideoId());
        nhraa.setAnswers(answers);
        String key = context.getVideoId();
        NewHomeworkResult newHomeworkResult = newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                nhraa);

        context.setNewHomeworkResult(newHomeworkResult);
        context.setIsOneByOne(true);
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        NewHomeworkResult newHomeworkResult = context.getNewHomeworkResult();
        if (newHomeworkResult == null) return;


        String videoId = context.getVideoId();
        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForKeyPointsByVideoId(context.getVideoId());
        if (CollectionUtils.isNotEmpty(processIds)) {

            NewHomeworkResultAppAnswer appAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.KEY_POINTS).getAppAnswers().get(videoId);
            boolean finished = SafeConverter.toBoolean(context.getFinished());
            // 校验是否真的没完成
            if (finished || validatePracticeFinished(context.getHomework(), appAnswer, videoId)) {
                // 布置的题目和做过的题一致，将剩下的属性补全
                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), processIds);
                Double score = 0d;
                Long duration = 0L;
                boolean allQuestionsRight = true;
                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                    score += npr.getScore();
                    duration += npr.getDuration();
                    if (!SafeConverter.toBoolean(npr.getGrasp())) {
                        allQuestionsRight = false;
                    }
                }
                //当题目全部正确时，但是总分计算结果不是100分就把总分设置为100分
                if (allQuestionsRight && score != null && score < 100D) {
                    score = 100D;
                }

                newHomeworkResultService.finishHomeworkKeyPoint(location, context.getUserId(), context.getObjectiveConfigType(), videoId, score, duration);
            }
        }
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(NewHomework newHomework,
                                             NewHomeworkResultAppAnswer appAnswer,
                                             String videoId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkKeyPointQuestions(ObjectiveConfigType.KEY_POINTS, videoId);
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
