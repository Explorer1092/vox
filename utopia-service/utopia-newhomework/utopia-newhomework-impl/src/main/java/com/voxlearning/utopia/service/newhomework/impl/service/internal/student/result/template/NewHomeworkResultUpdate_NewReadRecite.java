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
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbin
 * @since 2017/6/8 13:17
 */
@Named
public class NewHomeworkResultUpdate_NewReadRecite extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.NEW_READ_RECITE;
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

        nhraa.setLessonId(context.getLessonId());
        nhraa.setQuestionBoxId(context.getQuestionBoxId());
        nhraa.setQuestionBoxType(context.getQuestionBoxType());
        nhraa.setAnswers(answers);

        String key = context.getQuestionBoxId();
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
        if (newHomeworkResult == null) {
            return;
        }

        String questionBoxId = context.getQuestionBoxId();
        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteByQuestionBoxId(questionBoxId);
        if (CollectionUtils.isNotEmpty(processIds)) {
            NewHomeworkResultAppAnswer appAnswer = newHomeworkResult
                    .getPractices()
                    .get(ObjectiveConfigType.NEW_READ_RECITE)
                    .getAppAnswers()
                    .get(questionBoxId);
            boolean finished = SafeConverter.toBoolean(context.getFinished());
            // 校验是否真的没完成
            if (finished || validatePracticeFinished(context.getHomework(), appAnswer, questionBoxId)) {
                // 布置的题目和做过的题一致，将剩下的属性补全
                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), processIds);
                Long duration = 0L;
                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                    duration += npr.getDuration();
                }
                //double score = 100;
                newHomeworkResultService.finishHomeworkNewReadRecite(location, context.getUserId(), context.getObjectiveConfigType(),
                        questionBoxId, null, duration);
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
        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkReadReciteQuestions(ObjectiveConfigType.NEW_READ_RECITE, questionBoxId);
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
