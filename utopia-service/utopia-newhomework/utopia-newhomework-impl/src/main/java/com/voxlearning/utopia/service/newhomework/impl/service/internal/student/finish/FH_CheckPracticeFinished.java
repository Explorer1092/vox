package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class FH_CheckPracticeFinished extends SpringContainerSupport implements FinishHomeworkTask {

    @Override
    public void execute(FinishHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                || ObjectiveConfigType.READING.equals(objectiveConfigType)
                || ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)
                || ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)
                || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(objectiveConfigType)
                || ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.NATURAL_SPELLING.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.ORAL_COMMUNICATION.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType)) {
            List<NewHomeworkApp> newHomeworkApps = context.getHomework().findNewHomeworkApps(objectiveConfigType);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = context.getResult().getPractices().get(objectiveConfigType).getAppAnswers();
            if (newHomeworkApps.size() > appAnswers.size()) { //基础应用未完成,退出。
                context.terminateTask();
                return;
            }
        } else if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
            if (CollectionUtils.isEmpty(context.getOcrMentalAnswerIds())) {
                context.terminateTask();
                return;
            }
        } else if (ObjectiveConfigType.OCR_DICTATION.equals(objectiveConfigType)) {
            if (CollectionUtils.isEmpty(context.getOcrDictationAnswerIds())) {
                context.terminateTask();
                return;
            }
        } else {
            List<NewHomeworkQuestion> questionList = context.getHomework().findNewHomeworkQuestions(objectiveConfigType);
            if (CollectionUtils.isEmpty(questionList)) {
                logger.error("NewHomework {} does not contain practice {}", context.getHomeworkId(), objectiveConfigType);
                context.errorResponse();
                return;
            }
            List<String> arranged = context.getHomework().findQuestionIds(objectiveConfigType, false);
            NewHomeworkResultAnswer answer = context.getResult().getPractices().get(objectiveConfigType);
            if (arranged.size() > answer.getAnswers().size()) { // 如果某个作业类型都没有完成，就滚吧
                context.terminateTask();
                return;
            }
        }
        context.setPracticeFinished(true);
    }
}
