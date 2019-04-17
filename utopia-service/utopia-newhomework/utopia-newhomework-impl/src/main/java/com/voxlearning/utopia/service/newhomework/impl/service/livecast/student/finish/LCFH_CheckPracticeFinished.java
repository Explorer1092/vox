package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCFH_CheckPracticeFinished extends SpringContainerSupport implements FinishLiveCastHomeworkTask {
    @Override
    public void execute(FinishLiveCastHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                || ObjectiveConfigType.READING.equals(objectiveConfigType)
                || ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
            List<NewHomeworkApp> newHomeworkApps = context.getLiveCastHomework().findNewHomeworkApps(objectiveConfigType);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = context.getLiveCastHomeworkResult().getPractices().get(objectiveConfigType).getAppAnswers();
            if (newHomeworkApps.size() > appAnswers.size()) {
                //基础应用未完成
                context.terminateTask();
                return;
            }
        } else {
            List<NewHomeworkQuestion> questionList = context.getLiveCastHomework().findNewHomeworkQuestions(objectiveConfigType);
            if (CollectionUtils.isEmpty(questionList)) {
                logger.error("LiveCastHomework {} does not contain practice {}", context.getHomeworkId(), objectiveConfigType);
                context.errorResponse();
                return;
            }
            List<String> arranged = context.getLiveCastHomework().findQuestionIds(objectiveConfigType, false);
            NewHomeworkResultAnswer answer = context.getLiveCastHomeworkResult().getPractices().get(objectiveConfigType);
            if (arranged.size() > answer.getAnswers().size()) {
                context.terminateTask();
                return;
            }
        }
        context.setPracticeFinished(true);
    }
}
