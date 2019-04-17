package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2016/11/23
 */
@Named
public class NewHomeworkResultUpdate_Exam extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.EXAM;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();
        if (ObjectiveConfigType.MENTAL_ARITHMETIC == context.getObjectiveConfigType()) {
            List<SubHomeworkResultAnswer> answers = new ArrayList<>();
            NewHomework newHomework = context.getHomework();
            for (NewHomeworkProcessResult processResult : processResultMap.values()) {
                String qid = processResult.getQuestionId();
                String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
                String hid = newHomework.getId();
                SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
                aid.setDay(day);
                aid.setHid(hid);
                aid.setType(context.getObjectiveConfigType());
                aid.setUserId(SafeConverter.toString(context.getUserId()));
                aid.setQuestionId(qid);
                SubHomeworkResultAnswer answer = new SubHomeworkResultAnswer();
                answer.setId(aid.toString());
                answer.setProcessId(processResult.getId());

                answers.add(answer);
            }
            if (CollectionUtils.isNotEmpty(answers)) {
                newHomeworkResultService.saveSubHomeworkResultAnswers(answers);
            }
        } else {
            for (NewHomeworkProcessResult npr : processResultMap.values()) {
                newHomeworkResultService.doHomeworkExamAnswer(
                        location,
                        context.getUserId(),
                        npr.getObjectiveConfigType(),
                        npr.getQuestionId(),
                        npr.getId());
            }
        }
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {

    }
}
