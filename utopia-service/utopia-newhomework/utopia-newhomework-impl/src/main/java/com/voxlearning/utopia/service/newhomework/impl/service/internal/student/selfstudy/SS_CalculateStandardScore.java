package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
public class SS_CalculateStandardScore extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Override
    public void execute(SelfStudyHomeworkContext context) {
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(context.getObjectiveConfigType())) {
            return;
        }
        List<NewHomeworkQuestion> questions = context.getSelfStudyHomework().findNewHomeworkQuestions(context.getObjectiveConfigType());
        if (CollectionUtils.isEmpty(questions)) {
            logger.error("SelfStudyHomework {} does not contain practice {}", context.getHomeworkId(), context.getObjectiveConfigType());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }
        Map<String, Double> standardScore = new HashMap<>();
        List<NewHomeworkQuestion> questionList = questions.stream()
                .filter(q -> StringUtils.equals(q.getQuestionId(), context.getStudentHomeworkAnswer().getQuestionId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(questionList)) {
            logger.error("SelfStudyHomework {} does not contain question {}", context.getHomeworkId(), context.getStudentHomeworkAnswer());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }
        questionList.forEach(o -> standardScore.put(o.getQuestionId(), o.getScore()));

        context.setStandardScore(standardScore);
    }
}
