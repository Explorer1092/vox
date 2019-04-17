package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_CheckPracticeFinished extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Override
    public void execute(FinishVacationHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                || ObjectiveConfigType.READING.equals(objectiveConfigType)
                || ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)
                || ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)
                || ObjectiveConfigType.NATURAL_SPELLING.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) {
            List<NewHomeworkApp> newHomeworkApps = context.getVacationHomework().findNewHomeworkApps(objectiveConfigType);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = context.getResult().getPractices().get(objectiveConfigType).getAppAnswers();
            if (newHomeworkApps.size() > appAnswers.size()) { //基础应用未完成,退出。
                context.terminateTask();
                return;
            }
        } else {
            List<NewHomeworkQuestion> questionList = context.getVacationHomework().findNewHomeworkQuestions(objectiveConfigType);
            if (CollectionUtils.isEmpty(questionList)) {
                logger.error("VacationHomework {} does not contain practice {}", context.getVacationHomework(), objectiveConfigType);
                context.errorResponse();
                return;
            }
            List<String> arranged = context.getVacationHomework().findQuestionIds(objectiveConfigType, false);
            NewHomeworkResultAnswer answer = context.getResult().getPractices().get(objectiveConfigType);
            if (arranged.size() > answer.getAnswers().size()) { // 如果某个作业类型都没有完成，就滚吧
                context.terminateTask();
                return;
            }
        }
        context.setPracticeFinished(true);
    }
}
